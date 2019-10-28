package debtreg.Entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.security.access.annotation.Secured;


@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @NotNull
    @Size( max = 100 )
    @Column(unique = true)
    private String username;
    @Email
    @Column(nullable = false)
    private String email;
    @NotNull
    @Size( max = 100 )
    @Column(unique = true)
    private String password;
    private Date registration;
    private String imageUrl;
    private String providerId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private UserRole role;

    

    public User(){
        id = 0;
        username = "";
        password = "";
        registration = null;
        imageUrl = "";
    }

    public User(UserRole role){
        id = 0;
        username = "";
        password = "";
        registration = null;
        imageUrl = "";
        this.role = role;
    }

    public User(long id,@NotNull @Size(max = 100) String username,
    @NotNull @Size(max = 100) String password, Date registration) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.registration = registration;
    }

    public User(long id, @NotNull @Size(max = 100) String username, @Email String email,
    @NotNull @Size(max = 100) String password, Date registration, String imageUrl, String providerId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.registration = registration;
        this.imageUrl = imageUrl;
        this.providerId = providerId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getRegistration() {
        return registration;
    }

    public void setRegistration(Date registration) {
        this.registration = registration;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public AuthProvider getProvider() {
        return this.provider;
    }

    public void setProvider(AuthProvider provider){
        this.provider = provider;
    }

    public UserRole getRole(){
        return this.role;
    }

    @Secured("ROLE_ADMIN")
    public void setRole(UserRole role){
        this.role = role;
    }
}