package debtreg.Repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import debtreg.Entities.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query(value = "select m from Message m where m.debt_giver.id = :user_id")
    Page<Message> findAllByMessageGiverId(@Param(value = "user_id")Long userId, Pageable pageable);
    @Query(value = "select m from Message m where m.debt_getter.id = :user_id")
    Page<Message> findAllByMessageGetterId(@Param(value = "user_id") Long userId, Pageable pageable);
    @Query(value = "select m from Message m where m.debt_giver.id = :user_id and m.id = :message_id")
    Optional<Message> findByIdAndMessageGiverId(@Param(value = "user_id") Long userId, @Param(value = "message_id") Long messageId);
    @Query(value = "select m from Message m where m.debt_getter.id = :user_id and m.id = :message_id")
    Optional<Message> findByIdAndMessageGetterId(@Param(value = "user_id") Long userId, @Param(value = "message_id") Long messageId);
}