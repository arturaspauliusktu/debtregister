package debtreg.Controllers;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import debtreg.Entities.Deposite;
import debtreg.Exceptions.ResourceNotFoundException;
import debtreg.Repositories.DebtRepository;
import debtreg.Repositories.DepositeRepository;

@RestController
public class DepositeController {
    @Autowired
    DepositeRepository depositerepo;

    @Autowired
    DebtRepository debtrepo;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/deposites")
    public List<Deposite> getDeposites(){
        return (List<Deposite>) depositerepo.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/deposite/{id}")
    public Deposite getDeposite(@PathVariable Long id) throws Exception{
        Optional<Deposite> optionalDeposite = depositerepo.findById(id);
        if (!optionalDeposite.isPresent()) throw new Exception("Deposite Not Found with id : " + id);
        return optionalDeposite.get();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/debt/{id}/deposite")
    public Deposite getDepositeByDebtId(@PathVariable Long id) throws Exception{
        Optional<Deposite> optionalDeposite = depositerepo.findById(id);
        if (!optionalDeposite.isPresent()) throw new Exception("Deposite Not Found with id : " + id);
        return optionalDeposite.get();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/debt/{id}/deposite")
    public Deposite createDeposite(@PathVariable Long id, @RequestBody Deposite deposite){
           return debtrepo.findById(id).map( debt -> {
               deposite.setDebt(debt);
               return depositerepo.save(deposite);
           }).orElseThrow(() -> new ResourceNotFoundException("debt id - " + id + "Not Found!"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/deposite")
    public ResponseEntity<Object> createDeposite(@RequestBody Deposite deposite){
        Deposite savedDeposite = depositerepo.save(deposite);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}").buildAndExpand(savedDeposite.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deposite/{id}")
    public ResponseEntity<Object> deleteDeposite(@PathVariable Long id) throws Exception{
        Optional<Deposite> optionalDeposite = depositerepo.findById(id);
        if (!optionalDeposite.isPresent()) throw new Exception("Deposite not found with id : "+ id);
        depositerepo.deleteById(id);
        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/dept/{id}/deposite")
    public ResponseEntity<?> deleteDebtDeposite(@PathVariable Long id){
        return debtrepo.findById(id).map( debt -> {
            debtrepo.delete(debt);
            return ResponseEntity.ok().build();
        }).orElseThrow(() -> new ResourceNotFoundException("Debt Not Found Whit userId Of " + id ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/deposite/{id}")
    public ResponseEntity<Object> updateDeposite(@PathVariable Long id, @RequestBody Deposite deposite){
        Optional<Deposite> optDeposite = depositerepo.findById(id);
        if(!optDeposite.isPresent()) {
            depositerepo.save(deposite);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}").buildAndExpand(deposite.getId()).toUri();
            return ResponseEntity.created(location).build();
        }
        optDeposite.get().setId(deposite.getId());
        optDeposite.get().setDebt(deposite.getDebt());
        optDeposite.get().setDescription(deposite.getDescription());
        optDeposite.get().setName(deposite.getName());
        depositerepo.save(optDeposite.get());
        return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/deposite/{id}")
    public ResponseEntity<Object> putDeposite(@PathVariable Long id, @RequestBody Deposite deposite){
        Optional<Deposite> optDeposite = depositerepo.findById(id);
        if(!optDeposite.isPresent()) {
            depositerepo.save(deposite);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}").buildAndExpand(deposite.getId()).toUri();
            return ResponseEntity.created(location).build();
        }
        optDeposite.get().setId(deposite.getId());
        optDeposite.get().setDebt(deposite.getDebt());
        optDeposite.get().setDescription(deposite.getDescription());
        optDeposite.get().setName(deposite.getName());
        depositerepo.save(optDeposite.get());
        return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
    }
}