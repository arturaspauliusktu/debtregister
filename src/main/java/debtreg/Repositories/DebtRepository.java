package debtreg.Repositories;

import org.springframework.data.repository.CrudRepository;

import debtreg.Entities.Debt;

public interface DebtRepository extends CrudRepository<Debt, Integer>{

}