package debtreg.Controllers;

import java.util.List;
import java.util.Optional;
//import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import debtreg.Entities.Debt;
import debtreg.Exceptions.ResourceNotFoundException;
import debtreg.Repositories.DebtRepository;
import debtreg.Repositories.UserRepository;

@RestController
public class DebtController {
     //private final AtomicLong counter = new AtomicLong();

     @Autowired
     private DebtRepository debtrepo;
     @Autowired
     private UserRepository userrepo;


     /**
      * Not optimal method to return all debts from repository.
      * @return all debts in the database
      */
     @GetMapping("/debts")
     public List<Debt> getDebts(){
          return (List<Debt>) debtrepo.findAll();
     }

     /**
      * Returns one debt by id.
      * @param id
      * @return Debt
      * @throws Exception
      */
     @GetMapping("/debt/{id}")
     public Debt getDebt(@PathVariable Long id) throws Exception {
          Optional<Debt> debt = debtrepo.findById(id);
          if(!debt.isPresent()) throw new Exception("Debt not found! id-"+id);
          return debt.get();
     }

     @GetMapping("/user/{userId}/debts")
     public Page<Debt> getAllDebtsByUserId(@PathVariable(name = "userId") Long userId,
      Pageable pageable){
          Page<Debt> giverd = debtrepo.findByDebtGiverId(userId, pageable);
          Page<Debt> getterd = debtrepo.findByDebtGetterId(userId, pageable);
          if (giverd.getSize() != 0) return giverd;
          if (getterd.getSize() != 0) return getterd;
          return giverd;
     }

     @PostMapping("/user/{userId}/debt")
     public Debt addDebtToUser(@PathVariable Long userId, @RequestBody Debt requestdebt) {
           return userrepo.findById(userId).map( user -> {
               requestdebt.setGiver(user);
               return debtrepo.save(requestdebt);
           }).orElseThrow(() -> new ResourceNotFoundException("User id - " + userId + "Not Found!"));
     }

     @DeleteMapping("/user/{userId}/debt/{debtId}")
     public ResponseEntity<?> deleteUserDebt(@PathVariable Long userId, @PathVariable Long debtId){
          return debtrepo.findByIdAndDebtGiverId(debtId, userId).map( debt -> {
               debtrepo.delete(debt);
               return ResponseEntity.ok().build();
          }).orElseThrow(() -> new ResourceNotFoundException("Debt Not Found Whit userId Of " + userId + "and debtId " + debtId ));
     }

     @PutMapping("/user/{userId}/debt/{debtId}")
     public Debt updateDebt(@PathVariable Long userId,
     @PathVariable Long debtId,
     @RequestBody Debt debtrequest){
          if(!userrepo.existsById(userId)){
               throw new ResourceNotFoundException("userId " + userId + "Not Found!");
          }

          return debtrepo.findById(userId).map( debt -> {
               if(debtrequest.getId() != 0) debt.setId(debtrequest.getId());
               if(debtrequest.getMoneysum() != 0 ) debt.setMoneysum(debtrequest.getMoneysum());
               if(debtrequest.getGetter() != null) debt.setGetter(debtrequest.getGetter());
               if(debtrequest.getGiver() != null) debt.setGiver(debtrequest.getGiver());
               return debtrepo.save(debt);
          }).orElseThrow(() -> new ResourceNotFoundException("debtId " + debtId + "Not Found"));
     }

     @PatchMapping("/debt/{id}")
     public ResponseEntity<Object> updateDebt(@RequestBody Debt requestDebt, @PathVariable Long id) throws Exception{
          Optional<Debt> optionalDebt = debtrepo.findById(id);
          if(!optionalDebt.isPresent()) throw new Exception("Debt Not Found with id = "+ id);
          Debt debt = optionalDebt.get();
          if(requestDebt.getMoneysum() != 0 ) debt.setMoneysum(requestDebt.getMoneysum());
          if(requestDebt.getGetter() != null) debt.setGetter(requestDebt.getGetter());
          if(requestDebt.getGiver() != null) debt.setGiver(requestDebt.getGiver());
          return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
     }
}