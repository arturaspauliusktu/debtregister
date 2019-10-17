package debtreg.Controllers;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import debtreg.Entities.Deposite;
import debtreg.Repositories.DepositeRepository;

@RestController
public class DepositeController {
    @Autowired
    DepositeRepository repo;

    @GetMapping("/deposites")
    public List<Deposite> getDeposites(){
        return (List<Deposite>) repo.findAll();
    }

    @GetMapping("/deposite/{id}")
    public Deposite getDeposite(@PathVariable Integer id) throws Exception{
        Optional<Deposite> optionalDeposite = repo.findById(id);
        if (!optionalDeposite.isPresent()) throw new Exception("Deposite Not Found with id : " + id);
        return optionalDeposite.get();
    }

    @PostMapping("/deposite")
    public ResponseEntity<Object> createDeposite(@RequestBody Deposite deposite){
        Deposite savedDeposite = repo.save(deposite);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}").buildAndExpand(savedDeposite.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/deposite/{id}")
    public ResponseEntity<Object> deleteDeposite(@PathVariable Integer id) throws Exception{
        Optional<Deposite> optionalDeposite = repo.findById(id);
        if (!optionalDeposite.isPresent()) throw new Exception("Deposite not found with id : "+ id);
        repo.deleteById(id);
        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    @PatchMapping("/deposite/{id}")
    public ResponseEntity<Object> updateDeposite(@PathVariable Integer id, @RequestBody Deposite deposite){
        Optional<Deposite> optDeposite = repo.findById(id);
        if(!optDeposite.isPresent()) {
            repo.save(deposite);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}").buildAndExpand(deposite.getId()).toUri();
            return ResponseEntity.created(location).build();
        }
        optDeposite.get().setId(deposite.getId());
        optDeposite.get().setDebt(deposite.getDebt());
        optDeposite.get().setDescription(deposite.getDescription());
        optDeposite.get().setName(deposite.getName());
        repo.save(optDeposite.get());
        return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/deposite/{id}")
    public ResponseEntity<Object> putDeposite(@PathVariable Integer id, @RequestBody Deposite deposite){
        Optional<Deposite> optDeposite = repo.findById(id);
        if(!optDeposite.isPresent()) {
            repo.save(deposite);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}").buildAndExpand(deposite.getId()).toUri();
            return ResponseEntity.created(location).build();
        }
        optDeposite.get().setId(deposite.getId());
        optDeposite.get().setDebt(deposite.getDebt());
        optDeposite.get().setDescription(deposite.getDescription());
        optDeposite.get().setName(deposite.getName());
        repo.save(optDeposite.get());
        return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
    }
}