package debtreg.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import debtreg.Entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {}