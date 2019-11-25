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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import debtreg.Entities.Debt;
import debtreg.Entities.Deposite;
import debtreg.Exceptions.ResourceNotFoundException;
import debtreg.Repositories.DebtRepository;
import debtreg.Repositories.DepositeRepository;
import debtreg.Security.CurrentUser;
import debtreg.Security.UserPrincipal;

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
        Optional<Debt> optionalDept = debtrepo.findById(id);
        if (!optionalDept.isPresent()) throw new Exception("Deposite Not Found with id : " + id);
        return optionalDept.get().getDeposite();
    }

    @GetMapping("/user/me/debt/{id}/deposite")
    public Deposite getDepositeByDebtId(@CurrentUser UserPrincipal userprincipal, @PathVariable Long id) throws Exception{
        Optional<Debt> optionalDept = debtrepo.findByIdAndDebtGetterId(id, userprincipal.getId());
        if (!optionalDept.isPresent()) throw new Exception("Deposite Not Found with id : " + id);
        return optionalDept.get().getDeposite();
    }

    @GetMapping("/user/me/asset/{id}/deposite")
    public Deposite getDepositeByAssetId(@CurrentUser UserPrincipal userprincipal, @PathVariable Long id) throws Exception{
        Optional<Debt> optionalDept = debtrepo.findByIdAndDebtGiverId(id, userprincipal.getId());
        if (!optionalDept.isPresent()) throw new Exception("Deposite Not Found with id : " + id);
        return optionalDept.get().getDeposite();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/debt/{id}/deposite")
    public ResponseEntity<Deposite> createDeposite(@PathVariable Long id, @RequestBody Deposite deposite){
           return debtrepo.findById(id).map( debt -> {
                Deposite returndep = depositerepo.save(deposite);
               debt.setDeposite(deposite);
               debtrepo.save(debt);
               ResponseEntity<Deposite> dEntity = new ResponseEntity<>(returndep, HttpStatus.CREATED);
               return dEntity;
           }).orElseThrow(() -> new ResourceNotFoundException("debt id - " + id + "Not Found!"));
    }

    @PostMapping("/user/me/debt/{id}/deposite")
    public ResponseEntity<Deposite> createDeposite(@CurrentUser UserPrincipal userprincipal, 
    @PathVariable Long id, @RequestBody Deposite deposite){
        if(!debtrepo.findByIdAndDebtGetterId(id, userprincipal.getId()).isPresent() 
        && !debtrepo.findByIdAndDebtGiverId(id, userprincipal.getId()).isPresent()){
            throw new ResourceNotFoundException("debt not found whit id - " + id);
        }
            Debt founddebt = debtrepo.findById(id).get();
            Deposite returndepo = depositerepo.save(deposite);
            founddebt.setDeposite(deposite);
            debtrepo.save(founddebt);
            ResponseEntity<Deposite> dEntity = new ResponseEntity<>(returndepo, HttpStatus.CREATED);
           return dEntity;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/deposite")
    public ResponseEntity<Deposite> createDeposite(@RequestBody Deposite deposite){
        Deposite savedDeposite = depositerepo.save(deposite);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}").buildAndExpand(savedDeposite.getId()).toUri();
        return ResponseEntity.created(location).body(savedDeposite);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deposite/{id}")
    public ResponseEntity<Object> deleteDeposite(@PathVariable Long id) throws Exception{
        Optional<Deposite> optionalDeposite = depositerepo.findById(id);
        if (!optionalDeposite.isPresent()) 
        throw new Exception("Deposite not found with id : "+ id);
        Optional<Debt> founddebpt = debtrepo.findByDepositeId(id);
        if(founddebpt.isPresent()){
            founddebpt.get().setDeposite(null);
            debtrepo.save(founddebpt.get());
        }

        depositerepo.deleteById(id);
        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/debt/{id}/deposite")
    public ResponseEntity<?> 
    deleteDebtDeposite(@PathVariable Long id)
    throws Exception {
        Optional<Debt> optionalDebt = debtrepo.findById(id);
        if(!optionalDebt.isPresent()) 
        throw new Exception("Debt not found with id : " + id);
        if(optionalDebt.get().getDeposite() == null)
        throw new Exception("Debt don't have deposite");
        Long depoid = optionalDebt.get().getDeposite().getId();
        optionalDebt.get().setDeposite(null);
        debtrepo.save(optionalDebt.get());
        depositerepo.deleteById(depoid);
        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    @DeleteMapping("/user/me/debt/{id}/deposite")
    public ResponseEntity<?> 
    deleteDebtDeposite(@CurrentUser UserPrincipal userprincipal,
    @PathVariable Long id) throws Exception{
        if(!debtrepo.findByIdAndDebtGetterId(id, userprincipal.getId()).isPresent() 
        && !debtrepo.findByIdAndDebtGiverId(id, userprincipal.getId()).isPresent()){
            throw new ResourceNotFoundException("debt not found whit id - " + id);
        }
        Optional<Debt> optionalDebt = debtrepo.findById(id);
        if(optionalDebt.get().getDeposite() == null)
        throw new ResourceNotFoundException("Debt don't have deposite");
        Long depoid = optionalDebt.get().getDeposite().getId();
        optionalDebt.get().setDeposite(null);
        debtrepo.save(optionalDebt.get());
        depositerepo.deleteById(depoid);
        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/deposite/{id}")
    public ResponseEntity<Object> putDeposite(@PathVariable Long id, @RequestBody Deposite deposite){
        Optional<Deposite> optDeposite = depositerepo.findById(id);
        if(!optDeposite.isPresent()) {
            throw new ResourceNotFoundException("Deposite Not Found with id :" + id);
        }
        optDeposite.get().setId(deposite.getId());
        optDeposite.get().setDescription(deposite.getDescription());
        optDeposite.get().setName(deposite.getName());
        depositerepo.save(optDeposite.get());
        return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/user/me/debt/{id}/deposite/")
    public ResponseEntity<Object> putDeposite(@CurrentUser UserPrincipal userprincipal, 
    @PathVariable Long id, @RequestBody Deposite deposite){
        if(!debtrepo.findByIdAndDebtGetterId(id, userprincipal.getId()).isPresent() 
        && !debtrepo.findByIdAndDebtGiverId(id, userprincipal.getId()).isPresent()){
            throw new ResourceNotFoundException("debt not found whit id - " + id);
        }
        if(debtrepo.findById(id).get().getDeposite() == null) {
            throw new ResourceNotFoundException("Debt doesn't have deposite");
        }
        Deposite debtdeposite = debtrepo.findById(id).get().getDeposite();

        debtdeposite.setId(deposite.getId());
        debtdeposite.setDescription(deposite.getDescription());
        debtdeposite.setName(deposite.getName());
        depositerepo.save(debtdeposite);
        return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
    }
}