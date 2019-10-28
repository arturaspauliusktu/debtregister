package debtreg.Repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import debtreg.Entities.Debt;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long>{
    @Query(value = "select d from Debt d where d.debt_giver.id = :user_id")
    Page<Debt> findByDebtGiverId(@Param("user_id") Long userId, Pageable pageable);
    @Query(value = "select d from Debt d where d.debt_giver.id = :user_id and d.id = :debt_id ")
    Optional<Debt> findByIdAndDebtGiverId(@Param("debt_id") Long id,@Param("user_id") Long userId);
    @Query(value = "select d from Debt d where d.debt_getter.id = :user_id")
    Page<Debt> findByDebtGetterId(@Param("user_id") Long userId, Pageable pageable);
    @Query(value = "select d from Debt d where d.debt_getter.id = :user_id and d.id = :debt_id ")
    Optional<Debt> findByIdAndDebtGetterId(@Param("debt_id") Long id,@Param("user_id") Long userId);
    @Query(value = "select d from Debt d where d.debt_getter.id = :user_id and d.id = :debt_id or d.debt_giver.id = :user_id and d.id = :debt_id")
    Optional<Debt> findByIdAndDebtUserId(@Param("debt_id") Long id,@Param("user_id") Long userId);
}