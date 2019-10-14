package debtreg.Entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String text;
    private Date date;
    private User getter;
    private User giver;

    public Message(long id, String text, Date date, User getter, User giver) {
        this.id = id;
        this.text = text;
        this.date = date;
        this.getter = getter;
        this.giver = giver;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public User getGetter() {
        return getter;
    }

    public void setGetter(User getter) {
        this.getter = getter;
    }

    public User getGiver() {
        return giver;
    }

    public void setGiver(User giver) {
        this.giver = giver;
    }
    
}