package debtreg.Controllers;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
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

import debtreg.Entities.Debt;
import debtreg.Entities.Message;
import debtreg.Exceptions.ResourceNotFoundException;
import debtreg.Repositories.MessageRepository;
import debtreg.Repositories.UserRepository;

@RestController
public class MessageController {
     @Autowired
     private MessageRepository messagerepo;

     @Autowired
     private UserRepository userrepo;

     @GetMapping("/messages")
     public List<Message> getDebts(){
          return (List<Message>) messagerepo.findAll();
     }

     @GetMapping("/message/{id}")
     public Message getDebt(@PathVariable Long id) throws Exception {
          Optional<Message> message = messagerepo.findById(id);
          if(!message.isPresent()) throw new Exception("Message not found! id-"+id);
          return message.get();
     }
     
     @DeleteMapping("/message/{id}")
     public ResponseEntity<Object> removeDebt(@PathVariable Long id){
          messagerepo.deleteById(id);
          Optional<Message> message = messagerepo.findById(id);
          if(message.isPresent()){
               URI location = ServletUriComponentsBuilder.fromCurrentRequest()
               .path("/{id}").buildAndExpand(message.get().getId()).toUri();
               HttpHeaders responHeaders = new HttpHeaders();
               responHeaders.setLocation(location);
               return new ResponseEntity<Object>("Could Not delete message", responHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
          }
          return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
     }

     @PatchMapping("/message/{id}")
     public ResponseEntity<Object> updateDebt(@RequestBody Message requestMessage, @PathVariable Long id) throws Exception{
          Optional<Message> optionalDebt = messagerepo.findById(id);
          if(!optionalDebt.isPresent()) throw new Exception("Message Not Found with id = "+ id);
          Message message = optionalDebt.get();
          if(requestMessage.getId() != 0) message.setId(requestMessage.getId());
          if(requestMessage.getDate() != null ) message.setDate(requestMessage.getDate());
          if(requestMessage.getGetter() != null) message.setGetter(requestMessage.getGetter());
          if(requestMessage.getGiver() != null) message.setGiver(requestMessage.getGiver());
          if(requestMessage.getText() != null) message.setText(requestMessage.getText());
          return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
     }

     // @GetMapping("/user/{userId}/messages")
     // public Page<Debt> getAllDebtsByUserId(@PathVariable(name = "userId") Long userId,
     //  Pageable pageable){
     //      return messagerepo.findByDebtGiverId(userId, pageable);
     // }

     // @PostMapping("/user/{userId}/debt")
     // public Debt addDebtToUser(@PathVariable Long userId, @RequestBody Debt requestdebt) {
     //       return userrepo.findById(userId).map( user -> {
     //           requestdebt.setGiver(user);
     //           return debtrepo.save(requestdebt);
     //       }).orElseThrow(() -> new ResourceNotFoundException("User id - " + userId + "Not Found!"));
     // }

     // @DeleteMapping("/user/{userId}/debt/{debtId}")
     // public ResponseEntity<?> deleteUserDebt(@PathVariable Long userId, @PathVariable Long debtId){
     //      return debtrepo.findByIdAndDebtGiverId(debtId, userId).map( debt -> {
     //           debtrepo.delete(debt);
     //           return ResponseEntity.ok().build();
     //      }).orElseThrow(() -> new ResourceNotFoundException("Debt Not Found Whit userId Of " + userId + "and debtId " + debtId ));
     // }

     // @PutMapping("/user/{userId}/debt/{debtId}")
     // public Debt updateDebt(@PathVariable Long userId,
     // @PathVariable Long debtId,
     // @RequestBody Debt debtrequest){
     //      if(!userrepo.existsById(userId)){
     //           throw new ResourceNotFoundException("userId " + userId + "Not Found!");
     //      }

     //      return debtrepo.findById(userId).map( debt -> {
     //           if(debtrequest.getId() != 0) debt.setId(debtrequest.getId());
     //           if(debtrequest.getMoneysum() != 0 ) debt.setMoneysum(debtrequest.getMoneysum());
     //           if(debtrequest.getGetter() != null) debt.setGetter(debtrequest.getGetter());
     //           if(debtrequest.getGiver() != null) debt.setGiver(debtrequest.getGiver());
     //           return debtrepo.save(debt);
     //      }).orElseThrow(() -> new ResourceNotFoundException("debtId " + debtId + "Not Found"));
     // }

     // @PatchMapping("/debt/{id}")
     // public ResponseEntity<Object> updateDebt(@RequestBody Debt requestDebt, @PathVariable Long id) throws Exception{
     //      Optional<Debt> optionalDebt = debtrepo.findById(id);
     //      if(!optionalDebt.isPresent()) throw new Exception("Debt Not Found with id = "+ id);
     //      Debt debt = optionalDebt.get();
     //      if(requestDebt.getMoneysum() != 0 ) debt.setMoneysum(requestDebt.getMoneysum());
     //      if(requestDebt.getGetter() != null) debt.setGetter(requestDebt.getGetter());
     //      if(requestDebt.getGiver() != null) debt.setGiver(requestDebt.getGiver());
     //      return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
     // }

}