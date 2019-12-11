package debtreg.Controllers;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import debtreg.Entities.User;
import debtreg.Exceptions.ResourceNotFoundException;
import debtreg.Repositories.UserRepository;
import debtreg.Security.CurrentUser;
import debtreg.Security.UserPrincipal;

@RestController
public class UserController {
    @Autowired
     private UserRepository repo;

     @GetMapping("/users")
     public List<User> getUsers(){
          return (List<User>) repo.findAll();
     }

     @GetMapping("/user/{id}")
     public User getUser(@PathVariable Long id) throws Exception {
          Optional<User> user = repo.findById(id);
          if(!user.isPresent()) throw new Exception("User not found! id-"+id);
          return user.get();
     }

     @GetMapping("/user/me")
     //@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
     public User getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
          return repo.findById(userPrincipal.getId()).get();
                    //TODO: uncomment .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
     }

     @PostMapping("/user")
     @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")         
     public ResponseEntity<Object> addUser(@RequestBody User requestuser){
          //requestuser.setId(counter.incrementAndGet());
          User savedUser = repo.save(requestuser);
          URI location = ServletUriComponentsBuilder.fromCurrentRequest()
          .path("/{id}").buildAndExpand(savedUser.getId()).toUri();
          return ResponseEntity.created(location).build();
     }
     
     @DeleteMapping("/user/{id}")
     @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")     
     public ResponseEntity<Object> removeUser(@PathVariable Long id){
          repo.deleteById(id);
          Optional<User> user = repo.findById(id);
          if(user.isPresent()){
               URI location = ServletUriComponentsBuilder.fromCurrentRequest()
               .path("/{id}").buildAndExpand(user.get().getId()).toUri();
               HttpHeaders responHeaders = new HttpHeaders();
               responHeaders.setLocation(location);
               return new ResponseEntity<Object>("Could Not delete user", responHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
          }
          return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
     }

     @PatchMapping("/user/{id}")
     @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")     
     public ResponseEntity<Object> updateUser(@RequestBody User requestUser, @PathVariable Long id) throws Exception{
          Optional<User> optionalUser = repo.findById(id);
          if(!optionalUser.isPresent()) 
          throw new Exception("User Not Found with id = "+ id);
          User user = optionalUser.get();
          if(requestUser.getId() != 0) 
          user.setId(requestUser.getId());
          if(requestUser.getUsername() != "" ) 
          user.setUsername(requestUser.getUsername());
          if(requestUser.getPassword() != "" ) 
          user.setPassword(requestUser.getPassword());
          if(requestUser.getRegistration() != null) 
          user.setRegistration(requestUser.getRegistration());
          return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
     }
}