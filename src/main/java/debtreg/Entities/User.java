package debtreg.Entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @NotNull
    @Size( max = 100 )
    @Column(unique = true)
    private String username;
    @NotNull
    @Size( max = 100 )
    @Column(unique = true)
    private String password;
    private Date registration;

    public User(){
        id = 0;
        username = "";
        password = "";
        registration = null;
    }

    public User(long id, String username, String password, Date registration) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.registration = registration;
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
}