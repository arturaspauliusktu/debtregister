package debtreg.Controllers;

import java.util.List;
import java.util.Optional;
//import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import debtreg.Security.CurrentUser;
import debtreg.Security.UserPrincipal;

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
      @PreAuthorize("hasRole('ADMIN')")
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
     @PreAuthorize("hasRole('ADMIN')")          
     @GetMapping("/debt/{id}")
     public Debt getDebt(@PathVariable Long id) throws Exception {
          Optional<Debt> debt = debtrepo.findById(id);
          if(!debt.isPresent()) throw new Exception("Debt not found! id-"+id);
          return debt.get();
     }

     @PreAuthorize("hasRole('ADMIN')")
     @GetMapping("/user/{userId}/debts")
     public Page<Debt> getAllDebtsByUserId(@PathVariable(name = "userId") Long userId,
      Pageable pageable){
          return debtrepo.findByDebtGetterId(userId, pageable);
     }

     @GetMapping("/user/me/debts")
     public Page<Debt> getAllDebtsByUserId(@CurrentUser UserPrincipal userprincipal,
      Pageable pageable){
          long userId = userprincipal.getId();
          return debtrepo.findByDebtGetterId(userId, pageable);
     }

     @PreAuthorize("hasRole('ADMIN')")
     @GetMapping("/user/{userId}/assets")
     public Page<Debt> getAllAssetsByUserId(@PathVariable(name = "userId") Long userId,
      Pageable pageable){
          return debtrepo.findByDebtGiverId(userId, pageable);
     }

     @GetMapping("/user/me/assets")
     public Page<Debt> getAllAssetsByUserId(@CurrentUser UserPrincipal userprincipal,
      Pageable pageable){
           Long userId = userprincipal.getId();
          return debtrepo.findByDebtGiverId(userId, pageable);
     }

     @PreAuthorize("hasRole('ADMIN')")
     @PostMapping("/giver/{userId}/debt")
     public Debt addDebtToGiver(@PathVariable Long userId, @RequestBody Debt requestdebt) {
           return userrepo.findById(userId).map( user -> {
               requestdebt.setGiver(user);
               return debtrepo.save(requestdebt);
           }).orElseThrow(() -> new ResourceNotFoundException("User id - " + userId + "Not Found!"));
     }

     @PostMapping("/giver/me/debt")
     public Debt addDebtToGiver(@CurrentUser UserPrincipal userprincipal, @RequestBody Debt requestdebt) {
          Long userId = userprincipal.getId();
           return userrepo.findById(userId).map( user -> {
               requestdebt.setGiver(user);
               return debtrepo.save(requestdebt);
           }).orElseThrow(() -> new ResourceNotFoundException("User id - " + userId + "Not Found!"));
     }

     @PreAuthorize("hasRole('ADMIN')")
     @PostMapping("/getter/{userId}/debt")
     public Debt addDebtToGetter(@PathVariable Long userId, @RequestBody Debt requestdebt) {
          if(!userrepo.findById(requestdebt.getGiver().getId()).isPresent()) {
               throw new ResourceNotFoundException("There is no user with id - " + requestdebt.getGiver().getId());
          }
           return userrepo.findById(userId).map( user -> {
               requestdebt.setGetter(user);
               requestdebt.setGiver(userrepo.findById(requestdebt.getGiver().getId()).get());
               return debtrepo.save(requestdebt);
           }).orElseThrow(() -> new ResourceNotFoundException("User id - " + userId + "Not Found!"));
     }    

     /**
      * Adds new debt to user. request has to have at least giver with set id.
      * @param userprincipal
      * @param requestdebt
      * @return Debt saved to repository if succeeded.
      */
     @PostMapping("/getter/me/debt")
     public Debt addDebtToGetter(@CurrentUser UserPrincipal userprincipal, @RequestBody Debt requestdebt) {
          Long userId = userprincipal.getId();
          if(!userrepo.findById(requestdebt.getGiver().getId()).isPresent()) {
               throw new ResourceNotFoundException("There is no user with id - " + requestdebt.getGiver().getId());
          }
           return userrepo.findById(userId).map( user -> {
               requestdebt.setGetter(user);
               requestdebt.setGiver(userrepo.findById(requestdebt.getGiver().getId()).get());
               return debtrepo.save(requestdebt);
           }).orElseThrow(() -> new ResourceNotFoundException("User id - " + userId + "Not Found!"));
     }

     @PreAuthorize("hasRole('ADMIN')")
     @DeleteMapping("/user/{userId}/debt/{debtId}")
     public ResponseEntity<?> deleteUserDebt(@PathVariable Long userId, @PathVariable Long debtId){
          return debtrepo.findByIdAndDebtUserId(debtId, userId).map( debt -> {
               debtrepo.delete(debt);
               return ResponseEntity.ok().build();
          }).orElseThrow(() -> new ResourceNotFoundException("Debt Not Found Whit userId Of " + userId + "and debtId " + debtId ));
     }

     @DeleteMapping("/user/me/debt/{debtId}")
     public ResponseEntity<?> deleteUserDebt(@CurrentUser UserPrincipal userprincipal, @PathVariable Long debtId){
          Long userId = userprincipal.getId();
          return debtrepo.findByIdAndDebtUserId(debtId, userId).map( debt -> {
               debtrepo.delete(debt);
               return ResponseEntity.ok().build();
          }).orElseThrow(() -> new ResourceNotFoundException("Debt Not Found Whit userId Of " + userId + "and debtId " + debtId ));
     }

     @PreAuthorize("hasRole('ADMIN')")
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

     @PreAuthorize("hasRole('ADMIN')")
     @PutMapping("/user/me/debt/{debtId}")
     public Debt updateDebt(@CurrentUser UserPrincipal userprincipal,
     @PathVariable Long debtId,
     @RequestBody Debt debtrequest){
          Long userId = userprincipal.getId();
          if(!userrepo.existsById(userId)){
               throw new ResourceNotFoundException("userId " + userId + "Not Found!");
          }

          if(!debtrepo.findByIdAndDebtGetterId(userId, debtId).isPresent() ||
          !debtrepo.findByIdAndDebtGiverId(userId, debtId).isPresent()){
               throw new ResourceNotFoundException("Message not found " + debtId);
          }

          return debtrepo.findById(userId).map( debt -> {
               if(debtrequest.getId() != 0) debt.setId(debtrequest.getId());
               if(debtrequest.getMoneysum() != 0 ) debt.setMoneysum(debtrequest.getMoneysum());
               if(debtrequest.getGetter() != null) debt.setGetter(debtrequest.getGetter());
               if(debtrequest.getGiver() != null) debt.setGiver(debtrequest.getGiver());
               return debtrepo.save(debt);
          }).orElseThrow(() -> new ResourceNotFoundException("debtId " + debtId + "Not Found"));
     }

     @PreAuthorize("hasRole('ADMIN')")
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