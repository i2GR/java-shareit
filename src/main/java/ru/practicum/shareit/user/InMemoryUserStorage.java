package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.util.inmemory.InMemoryStorage;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Repository
public class InMemoryUserStorage extends InMemoryStorage<User> implements UserRepository {

    public InMemoryUserStorage(){
        super("User", new HashSet<>(List.of("email")));
    }

    /**
     * реализация создания нового объекта пользователя для отвязки от хранящегося in-memory в структуре данных
     * @implNote применен модификатор final,т.к. наследование не планируется
     * @param user копируемый экз. пользователя (из HashMap как структуры данных хранилища)
     * @return новый экз. пользователя, для корректного обновления (update) в Service-слое.
     */
    @Override
    protected final User copy(User user) {
        if (user == null) {
            log.warn("User Not found reading repository");
            throw new NotFoundException("User Not found reading repository");
        }
        return User.builder().id(user.getId()).email(user.getEmail()).name(user.getName()).build();
    }
}
