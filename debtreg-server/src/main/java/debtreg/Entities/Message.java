package debtreg.Entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @NotNull
    @Size( max = 300)
    private String text;
    private Date date;
    @ManyToOne
    @JoinColumn( name = "debt_getter_id", unique = false, nullable = false)
    @JsonIgnore
    @NotNull
    private User debt_getter;
    @ManyToOne
    @JoinColumn( name = "debt_giver_id", unique = false, nullable = false)
    @JsonIgnore
    @NotNull
    private User debt_giver;
    @NotNull 
    private Long owner;

    public Message(){
        this.id = 0;
        this.text = "text";
        this.date = null;
        this.debt_getter = null;
        this.debt_giver = null;
        this.owner = null;
    }

    public Message(long id, String text, Date date, User debt_getter, User debt_giver, Long owner) {
        this.id = id;
        this.text = text;
        this.date = date;
        this.debt_getter = debt_getter;
        this.debt_giver = debt_giver;
        this.owner = owner;
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
        return debt_getter;
    }

    public void setGetter(User debt_getter) {
        this.debt_getter = debt_getter;
    }

    public User getGiver() {
        return debt_giver;
    }

    public void setGiver(User debt_giver) {
        this.debt_giver = debt_giver;
    }

    public User getDebt_getter() {
        return debt_getter;
    }

    public void setDebt_getter(User debt_getter) {
        this.debt_getter = debt_getter;
    }

    public User getDebt_giver() {
        return debt_giver;
    }

    public void setDebt_giver(User debt_giver) {
        this.debt_giver = debt_giver;
    }

    public Long getOwner() {
        return owner;
    }

    public void setOwner(Long owner) {
        this.owner = owner;
    }
    
}