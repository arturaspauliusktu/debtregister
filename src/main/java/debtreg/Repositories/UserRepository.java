package debtreg.Repositories;

import org.springframework.data.repository.CrudRepository;

import debtreg.Entities.User;

public interface UserRepository extends CrudRepository<User, Integer> {}