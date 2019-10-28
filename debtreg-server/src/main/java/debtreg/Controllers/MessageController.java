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
     public List<Message> getMessages(){
          return (List<Message>) messagerepo.findAll();
     }

     @GetMapping("/message/{id}")
     public Message getMessage(@PathVariable Long id) throws Exception {
          Optional<Message> message = messagerepo.findById(id);
          if(!message.isPresent()) throw new Exception("Message not found! id-"+id);
          return message.get();
     }
     
     @DeleteMapping("/message/{id}")
     public ResponseEntity<Object> removeMessage(@PathVariable Long id){
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

     @GetMapping("/giver/{userId}/messages")
     public Page<Message> getAllMessagessByGiverId(@PathVariable(name = "userId") Long userId,
      Pageable pageable){
          return messagerepo.findAllByMessageGiverId(userId, pageable);
     }

     @GetMapping("/getter/{userId}/messages")
     public Page<Message> getAllMessagessByUserId(@PathVariable(name = "userId") Long userId,
      Pageable pageable){
          return messagerepo.findAllByMessageGetterId(userId, pageable);
     }

     @PostMapping("/giver/{userId}/message")
     public Message addMessageToGiver(@PathVariable Long userId, @RequestBody Message requestmessage) {
           return userrepo.findById(userId).map( user -> {
               requestmessage.setGiver(user);
               return messagerepo.save(requestmessage);
           }).orElseThrow(() -> new ResourceNotFoundException("User id - " + userId + "Not Found!"));
     }

     @PostMapping("/getter/{userId}/message")
     public Message addMessageToGetter(@PathVariable Long userId, @RequestBody Message requestmessage) {
           return userrepo.findById(userId).map( user -> {
               requestmessage.setGetter(user);
               return messagerepo.save(requestmessage);
           }).orElseThrow(() -> new ResourceNotFoundException("User id - " + userId + "Not Found!"));
     }

     @DeleteMapping("/user/{userId}/message/{messageId}")
     public ResponseEntity<?> deleteUserMessage(@PathVariable Long userId, @PathVariable Long messageId){
          return messagerepo.findByIdAndMessageGiverId(messageId, userId).map( message -> {
               messagerepo.delete(message);
               return ResponseEntity.ok().build();
          }).orElseThrow(() -> new ResourceNotFoundException("Message Not Found Whit userId Of " + userId + "and debtId " + messageId ));
     }

     @PutMapping("/user/{userId}/message/{messageId}")
     public Message updateM(@PathVariable Long userId,
     @PathVariable Long messageId,
     @RequestBody Message messagerequest){
          if(!userrepo.existsById(userId)){
               throw new ResourceNotFoundException("userId " + userId + "Not Found!");
          }

          return messagerepo.findById(userId).map( message -> {
               if(messagerequest.getId() != 0) message.setId(messagerequest.getId());
               if(messagerequest.getText() != null ) message.setText(messagerequest.getText());
               if(messagerequest.getGetter() != null) message.setGetter(messagerequest.getGetter());
               if(messagerequest.getGiver() != null) message.setGiver(messagerequest.getGiver());
               if(messagerequest.getDate() != null) message.setDate(messagerequest.getDate());
               return messagerepo.save(message);
          }).orElseThrow(() -> new ResourceNotFoundException("messageId " + messageId + "Not Found"));
     }

     @PatchMapping("/message/{id}")
     public ResponseEntity<Object> updateMessage(@RequestBody Message messagerequest, @PathVariable Long id) throws Exception{
          Optional<Message> optionalMessage = messagerepo.findById(id);
          if(!optionalMessage.isPresent()) throw new Exception("Message Not Found with id = "+ id);
          Message message = optionalMessage.get();
          if(messagerequest.getText() != null ) message.setText(messagerequest.getText());
          if(messagerequest.getDate() != null ) message.setDate(messagerequest.getDate());
          if(messagerequest.getGetter() != null) message.setGetter(messagerequest.getGetter());
          if(messagerequest.getGiver() != null) message.setGiver(messagerequest.getGiver());
          return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
     }

}