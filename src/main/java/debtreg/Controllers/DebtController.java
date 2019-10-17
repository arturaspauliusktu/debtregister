package debtreg.Controllers;

import java.net.URI;
import java.util.List;
import java.util.Optional;
//import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import debtreg.Entities.Debt;
import debtreg.Repositories.DebtRepository;

@RestController
public class DebtController {
     //private final AtomicLong counter = new AtomicLong();

     @Autowired
     private DebtRepository repo;

     @GetMapping("/debts")
     public List<Debt> getDebts(){
          return (List<Debt>) repo.findAll();
     }

     @GetMapping("/debt/{id}")
     public Debt getDebt(@PathVariable Integer id) throws Exception {
          Optional<Debt> debt = repo.findById(id);
          if(!debt.isPresent()) throw new Exception("Debt not found! id-"+id);
          return debt.get();
     }

     @PostMapping("/debt")
     public ResponseEntity<Object> addDebt(@RequestBody Debt requestdebt){
          //requestdebt.setId(counter.incrementAndGet());
          Debt savedDebt = repo.save(requestdebt);
          URI location = ServletUriComponentsBuilder.fromCurrentRequest()
          .path("/{id}").buildAndExpand(savedDebt.getId()).toUri();
          return ResponseEntity.created(location).build();
     }

     @DeleteMapping("/debt/{id}")
     public ResponseEntity<Object> removeDebt(@PathVariable Integer id){
          repo.deleteById(id);
          Optional<Debt> debt = repo.findById(id);
          if(debt.isPresent()){
               URI location = ServletUriComponentsBuilder.fromCurrentRequest()
               .path("/{id}").buildAndExpand(debt.get().getId()).toUri();
               HttpHeaders responHeaders = new HttpHeaders();
               responHeaders.setLocation(location);
               return new ResponseEntity<Object>("Could Not delete debt", responHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
          }
          return new ResponseEntity<Object>(HttpStatus.OK);
     }

     @PatchMapping("/debt/{id}")
     public ResponseEntity<Object> updateDebt(@RequestBody Debt requestDebt, @PathVariable Integer id) throws Exception{
          Optional<Debt> optionalDebt = repo.findById(id);
          if(!optionalDebt.isPresent()) throw new Exception("Debt Not Found with id = "+ id);
          Debt debt = optionalDebt.get();
          if(requestDebt.getId() != 0) debt.setId(requestDebt.getId());
          if(requestDebt.getMoneysum() != 0 ) debt.setMoneysum(requestDebt.getMoneysum());
          if(requestDebt.getGetter() != null) debt.setGetter(requestDebt.getGetter());
          if(requestDebt.getGiver() != null) debt.setGiver(requestDebt.getGiver());
          return new ResponseEntity<Object>(HttpStatus.OK);
     }
}