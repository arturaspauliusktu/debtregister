package debtreg.Repositories;

import org.springframework.data.repository.CrudRepository;

import debtreg.Entities.Message;

public interface MessageRepository extends CrudRepository<Message, Integer> {}