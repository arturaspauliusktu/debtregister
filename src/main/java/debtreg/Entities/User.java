package debtreg.Entities;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String username;
    private String password;
    private Date registration;
    private List<Debt> debts;
    private List<Message> messages;

    public User(){
        id = 0;
        username = "";
        password = "";
        registration = null;
        debts = null;
        messages = null;
    }

    public User(long id, String username, String password, Date registration, List<Debt> debts,
            List<Message> messages) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.registration = registration;
        this.debts = debts;
        this.messages = messages;
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

    public List<Debt> getDebts() {
        return debts;
    }

    public void setDebts(List<Debt> debts) {
        this.debts = debts;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
    
    // public void addMessage(Message message){
    //     try {
    //         this.messages.add(message);
    //     } catch (Exception e) {
    //         System.out.println(e.getMessage());
    //     }
    // }

    // public void removeMessage(Message message){
    //     try {
    //         this.messages.remove(message);
    //     } catch (Exception e) {
    //         System.out.println(e.getMessage());
    //     }
    // }


}