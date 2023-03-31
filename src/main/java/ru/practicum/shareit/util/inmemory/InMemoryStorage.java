package ru.practicum.shareit.util.inmemory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import ru.practicum.shareit.util.Identifiable;
import ru.practicum.shareit.util.Repository;
import ru.practicum.shareit.exception.StorageConflictException;
import ru.practicum.shareit.exception.StorageErrorException;

import javax.validation.Valid;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public abstract class InMemoryStorage<T extends Identifiable> implements Repository<T> {

    @NonNull
    private final String loggingEntityName;
    @NonNull
    private final Set<String> uniqueFieldsNames;
    private final IdService idService = new IdService();
    protected final Map<Long, T> idMapEntity = new HashMap<>();

    @Override
    public Optional<T> create(@Valid T entity) {
        log.info("create {} in storage", loggingEntityName);
        Long newId = idService.getNewId(entity);
        try {
            if (!idMapEntity.containsValue(entity)) {
                log.info("{} created with id {}", loggingEntityName, newId);
                T storedEntity = (T) idService.updateEntityWithId(entity, newId);
                idMapEntity.put(newId, storedEntity);
                return Optional.of(storedEntity);
            }
        } catch (Throwable e) {
            log.info("Storage Error creating {}", loggingEntityName);
            throw new StorageErrorException("Storage Error creating " + loggingEntityName);
        }
        log.info("Conflict when creating {}", loggingEntityName);
        throw new StorageConflictException("Conflict when creating " + loggingEntityName);
    }

    @Override
    public Optional<T> readById(Long id) {
        log.info("reading {} by id {}", loggingEntityName, id);
        return Optional.ofNullable(copy(idMapEntity.get(id)));
    }

    @Override
    public Optional<T> update(T entity) {
        Long id = entity.getId();
        log.info("updating {} by id {}", loggingEntityName, id);
        //return updateWithUniqueValueConstraintCheck(id, entity);
        uniqueValueConstraintCheck(entity);
        return idMapEntity.replace(id, entity) == null ? Optional.empty() : Optional.of(entity);
    }

    @Override
    public List<T> readAll() {
        log.info("reading all {}s", loggingEntityName);
        return idMapEntity.values().stream()
                                  .sorted(Comparator.comparing(Identifiable::getId))
                                  .collect(Collectors.toList());
    }

    @Override
    public Optional<T> delete(Long id) {
        log.info("deleting {} with id {}", loggingEntityName, id);
        return Optional.ofNullable(idMapEntity.remove(id));
    }

    /**
     * Метод проверки наличия дубликата значения поля, которое должно быть уникальным в репозитории <p>
     * немножко усложения (овер-инжиниринга) для тренировки работы с Reflection API <p>
     * а) по заранее установленным в конструкторе именам полей обрабатываемого domain-объекта получаем набор полей,
     * значения которых не должны повторяться для объектов в хранилище (фильруем в stream по совпадению названия)
     * {@code Set<Field>} <p>
     * б) в списке полей каждого из них из HashMap получаем хранящиеся в HashMap значения <p>
     * в) для этого используем Геттер по имени, исходя из соглашения, что имя Геттера Lombok создает по имени поля <p>
     * {@code Domain#filed ==> Domain#getField())} <p>
     * и делаем cast значения поля к типу поля <p>
     * д) в стриме фильтруем значения полей если они равны значению поля для объекта,
     * который должен быть обновлен в хранилище, а также оставляем (фильруем) значения в случае, только если они
     * принадлежат не объекту в хранилище (идентификатор объекта из хранилиша не равен идентификатору объекта
     * который должен быть обновлен) <p>
     * е) если после фильтрации есть повторы {@code >0} выбрасываем исключение для HTTP-кода согласно тестам PM <p>
     * @param entity объект, который должен быть обновлен
     */
    private void uniqueValueConstraintCheck(T entity) {
        if (!uniqueFieldsNames.isEmpty()) {
            Set<Field> uniqueValueConstraintFields = Stream
                    .of(entity.getClass().getDeclaredFields())
                    .filter(f -> uniqueFieldsNames.contains(f.getName()))
                    .collect(Collectors.toSet());
            Stream<T> streamedStoredEntities = idMapEntity.values().stream();
            for (Field field : uniqueValueConstraintFields) {
                if (streamedStoredEntities
                        .filter(e -> invokeGetFieldMethod(e, field, field.getType())
                                .equals(invokeGetFieldMethod(entity, field, field.getType())))
                        .filter(e -> !e.getId().equals(entity.getId()))
                        .collect(Collectors.toSet()).size() > 0) {
                    throw new StorageConflictException("Conflict when updating field: " + field.getName());
                }
            }
        }
    }

    /**
     * вызов геттера проверяемого поля и cast к типу поля
     * {@code Domain#filed ==> Domain#getField())}
     * @param entity объект, который должен быть обновлен
     * @param field поле передаваемого объекта
     * @param to класс,к которму нужно привести тип поля
     * @return значение поля, приведенное к типу
     * @param <C> класс поля
     */
    private <C> C invokeGetFieldMethod(T entity, Field field, Class<C> to) {
        try {
            Object value = entity.getClass().getMethod("get"
                                                        + field.getName().substring(0, 1).toUpperCase()
                                                        + field.getName().substring(1))
                                            .invoke(entity);
            if (value != null) {
                Class<?> c = value.getClass();
                if (to.isAssignableFrom(c)) {
                    return to.cast(value);
                }
            }
        } catch (InvocationTargetException ex) {
            throw new StorageErrorException("InvocationTargetException error");
        } catch (NoSuchMethodException ex) {
            throw new StorageErrorException("NoSuchMethodException error");
        } catch (IllegalAccessException ex) {
            throw new StorageErrorException("IllegalAccessException error");
        }
        throw new StorageErrorException("internal error");
    }

    /**
     * контракт создания нового экз. Identifiable для отвязки от объекта, хранящегося в in-memory-реализации
     * @implNote
     * паттерн реализации PATCH-методов на Service-уровне подсмотрен на просторах... <p>
     * а) получение domain-объекта из хранилища: <p>
     * б) модификация маппером на основе полей DTO-класса <p>
     * в) Update-обновленного domain-объекта в репозиторий приложения <p>
     * {@code domain = repo.read(id);}<p>
     * {@code mapper.update(dto, domain);}<p>
     * {@code repo.update(domain);}<p>
     * при хранении в БД должно работать т.к. использование RowMapper<[Domain.class]> создает новый объект <p>
     * <p>
     * но при in-memory-реализации (хранение в HashMap) объекты передаются по ссылкам <p>
     * поэтому уже на этапе б) произойдет обновление хранящегося экземпляра. Он будет сразу перезапишется и в HashMap <p>
     * чтобы не менять реализацию паттерна в сервис-слое костыль временно подставлен в реализации хранилищ
     */

     protected abstract T copy(T entity);
}