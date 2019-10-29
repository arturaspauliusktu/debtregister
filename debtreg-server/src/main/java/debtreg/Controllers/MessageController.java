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

import debtreg.Entities.Message;
import debtreg.Exceptions.ResourceNotFoundException;
import debtreg.Repositories.MessageRepository;
import debtreg.Repositories.UserRepository;
import debtreg.Security.CurrentUser;
import debtreg.Security.UserPrincipal;

@RestController
public class MessageController {
     @Autowired
     private MessageRepository messagerepo;

     @Autowired
     private UserRepository userrepo;

     @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
     @GetMapping("/messages")
     public List<Message> getMessages(){
          return (List<Message>) messagerepo.findAll();
     }

     @PreAuthorize("hasRole('ADMIN')")
     @GetMapping("/message/{id}")
     public Message getMessage(@PathVariable Long id) throws Exception {
          Optional<Message> message = messagerepo.findById(id);
          if(!message.isPresent()) throw new Exception("Message not found! id-"+id);
          return message.get();
     }
     
     @PreAuthorize("hasRole('ADMIN')")
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

     @PreAuthorize("hasRole('ADMIN')")
     @GetMapping("/giver/{userId}/messages")
     public Page<Message> getAllMessagessByGiverId(@PathVariable(name = "userId") Long userId,
      Pageable pageable){
          return messagerepo.findAllByMessageGiverId(userId, pageable);
     }

     @GetMapping("/giver/me/messages")
     public Page<Message> getAllMessagessByGiverId(@CurrentUser UserPrincipal userprincipal,
      Pageable pageable){
          Long userId = userprincipal.getId();
          return messagerepo.findAllByMessageGiverId(userId, pageable);
     }

     @GetMapping("/giver/me/messages/{id}")
     public Message getAllMessagessByGiverId(@CurrentUser UserPrincipal userprincipal,
     @PathVariable Long id){
          Long userId = userprincipal.getId();
          return messagerepo.findByIdAndMessageGiverId(userId, id).get();
     }

     @PreAuthorize("hasRole('ADMIN')")
     @GetMapping("/getter/{userId}/messages")
     public Page<Message> getAllMessagessByUserId(@PathVariable(name = "userId") Long userId,
      Pageable pageable){
          return messagerepo.findAllByMessageGetterId(userId, pageable);
     }

     @GetMapping("/getter/me/messages")
     public Page<Message> getAllMessagessByUserId(@CurrentUser UserPrincipal userprincipal,
      Pageable pageable){
           Long userId = userprincipal.getId();
          return messagerepo.findAllByMessageGetterId(userId, pageable);
     }

     @GetMapping("/getter/me/messages/{id}")
     public Message getAllMessagessByGetterId(@CurrentUser UserPrincipal userprincipal,
      @PathVariable Long id){
          Long userId = userprincipal.getId();
          return messagerepo.findByIdAndMessageGetterId(userId, id).get();
     }

     @PreAuthorize("hasRole('ADMIN')")
     @PostMapping("/giver/{userId}/message")
     public Message addMessageToGiver(@PathVariable Long userId, @RequestBody Message requestmessage) {
           return userrepo.findById(userId).map( message -> {
               requestmessage.setGiver(message);
               requestmessage.setOwner(userId);
               return messagerepo.save(requestmessage);
           }).orElseThrow(() -> new ResourceNotFoundException("user id - " + userId + "Not Found!"));
     }

     @PostMapping("/giver/me/message")
     public Message addMessageToGiver(@CurrentUser UserPrincipal userprincipal, @RequestBody Message requestmessage) {
          Long userId = userprincipal.getId();
          if(!userrepo.findById(requestmessage.getGiver().getId()).isPresent()) {
               throw new ResourceNotFoundException("There is no user with id - " + requestmessage.getGiver().getId());
          }
           return userrepo.findById(userId).map( message -> {
               requestmessage.setGiver(message);
               requestmessage.setGetter(userrepo.findById(requestmessage.getGetter().getId()).get());
               requestmessage.setOwner(userprincipal.getId());
               return messagerepo.save(requestmessage);
           }).orElseThrow(() -> new ResourceNotFoundException("user id - " + userId + "Not Found!"));
     }


     @PreAuthorize("hasRole('ADMIN')")
     @PostMapping("/getter/{userId}/message")
     public Message addMessageToGetter(@PathVariable Long userId, @RequestBody Message requestmessage) {
           return userrepo.findById(userId).map( message -> {
               requestmessage.setOwner(userId);
               requestmessage.setGetter(message);
               return messagerepo.save(requestmessage);
           }).orElseThrow(() -> new ResourceNotFoundException("user id - " + userId + "Not Found!"));
     }

     @PostMapping("/getter/me/message")
     public Message addMessageToGetter(@CurrentUser UserPrincipal userprincipal, @RequestBody Message requestmessage) {
          Long userId = userprincipal.getId();
           return userrepo.findById(userId).map( message -> {
               requestmessage.setGetter(message);
               requestmessage.setGiver(userrepo.findById(requestmessage.getGiver().getId()).get());
               requestmessage.setOwner(userprincipal.getId());
               return messagerepo.save(requestmessage);
           }).orElseThrow(() -> new ResourceNotFoundException("user id - " + userId + "Not Found!"));
     }

     @PreAuthorize("hasRole('ADMIN')")
     @DeleteMapping("/user/{userId}/message/{messageId}")
     public ResponseEntity<?> deleteUserMessage(@PathVariable Long userId, @PathVariable Long messageId){
          return messagerepo.findByIdAndMessageGiverId(userId, messageId).map( message -> {
               messagerepo.delete(message);
               return ResponseEntity.ok().build();
          }).orElseThrow(() -> new ResourceNotFoundException("Message Not Found Whit userId Of " + userId + "and debtId " + messageId ));
     }

     @DeleteMapping("/user/me/message/{messageId}")
     public ResponseEntity<?> deleteUserMessage(@CurrentUser UserPrincipal userprincipal, @PathVariable Long messageId){
          Long userId = userprincipal.getId();
          return messagerepo.findByIdAndMessageUserId(messageId, userId).map( message -> {
               messagerepo.delete(message);
               return ResponseEntity.ok().build();
          }).orElseThrow(() -> new ResourceNotFoundException("Message Not Found Whit userId Of " + userId + "and debtId " + messageId ));
     }

     @PreAuthorize("hasRole('ADMIN')")
     @PutMapping("/user/{userId}/message/{messageId}")
     public Message updateM(@PathVariable Long userId,
     @PathVariable Long messageId,
     @RequestBody Message messagerequest){
          if(!userrepo.existsById(userId)){
               throw new ResourceNotFoundException("userId " + userId + "Not Found!");
          }

          return messagerepo.findById(messageId).map( message -> {
               if(messagerequest.getId() != 0) message.setId(messagerequest.getId());
               if(messagerequest.getText() != null ) message.setText(messagerequest.getText());
               if(messagerequest.getGetter() != null) message.setGetter(messagerequest.getGetter());
               if(messagerequest.getGiver() != null) message.setGiver(messagerequest.getGiver());
               if(messagerequest.getDate() != null) message.setDate(messagerequest.getDate());
               return messagerepo.save(message);
          }).orElseThrow(() -> new ResourceNotFoundException("messageId " + messageId + "Not Found"));
     }

     @PutMapping("/user/me/message/{messageId}")
     public Message updateM(@CurrentUser UserPrincipal userprincipal,
     @PathVariable Long messageId,
     @RequestBody Message messagerequest){
          Long userId = userprincipal.getId();
          if(!userrepo.existsById(userId)){
               throw new ResourceNotFoundException("userId " + userId + "Not Found!");
          }

          if(!messagerepo.findByIdAndMessageGetterId(userId, messageId).isPresent() ||
          !messagerepo.findByIdAndMessageGetterId(userId, messageId).isPresent()){
               throw new ResourceNotFoundException("Message not found " + messageId);
          }

          return messagerepo.findById(messageId).map( message -> {
               if(messagerequest.getId() != 0) message.setId(messagerequest.getId());
               if(messagerequest.getText() != null ) message.setText(messagerequest.getText());
               if(messagerequest.getGetter() != null) message.setGetter(messagerequest.getGetter());
               if(messagerequest.getGiver() != null) message.setGiver(messagerequest.getGiver());
               if(messagerequest.getDate() != null) message.setDate(messagerequest.getDate());
               return messagerepo.save(message);
          }).orElseThrow(() -> new ResourceNotFoundException("messageId " + messageId + "Not Found"));
     }

     @PreAuthorize("hasRole('ADMIN')")
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