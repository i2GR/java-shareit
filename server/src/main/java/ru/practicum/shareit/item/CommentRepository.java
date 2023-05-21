package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Comment;
import java.util.List;

/**
 * интерфейс для Jpa-репозитория комментариев к вещам <p>
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByItem_OwnerIdEquals(Long ownerId);

    List<Comment> findByItem_Id(Long itemId);
}