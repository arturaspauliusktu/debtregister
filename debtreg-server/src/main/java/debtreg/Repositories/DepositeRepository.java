package debtreg.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import debtreg.Entities.Deposite;

@Repository
public interface DepositeRepository extends JpaRepository<Deposite, Long> {}