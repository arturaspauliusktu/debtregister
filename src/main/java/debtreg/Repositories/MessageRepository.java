package debtreg.Repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import debtreg.Entities.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query(value = "select m from Message m where m.debt_giver.id = :user_id")
    Page<Message> findAllByDebtGiverId(@Param(value = "user_id")Long userId, Pageable pageable);
    @Query(value = "select m from Message m where m.debt_getter.id = :user_id")
    Page<Message> findAllByDebtGetterId(@Param(value = "user_id") Long userId, Pageable pageable);
    @Query(value = "select m from Message m where m.debt_giver.id = :user_id and m.id = :message_id")
    Optional<Message> findByIdAndDebtGiverId(@Param(value = "user_id") Long userId, @Param(value = "message_id") Long messageId);
    @Query(value = "select m from Message m where m.debt_getter.id = :user_id and m.id = :message_id")
    Optional<Message> findByIdAndDebtGetterId(@Param(value = "user_id") Long userId, @Param(value = "message_id") Long messageId);
}