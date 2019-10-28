package debtreg.Entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.lang.Nullable;

@Entity
@Table(name = "debts")
public class Debt {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private long moneysum;
    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinColumn(name = "debt_giver_id", nullable = true)
    private User debt_giver;
    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinColumn(name = "debt_getter_id", nullable = true)
    private User debt_getter;
    @OneToOne
    @Nullable
    private Deposite deposite;

    public Debt(){
        this.id = 0;
        this.moneysum = 0;
        this.debt_getter = null;
        this.debt_giver = null;
        this.deposite = null;
    }

    public Debt(long id, long moneysum, User getter, User giver, Deposite deposite) {
        this.id = id;
        this.moneysum = moneysum;
        this.debt_getter = getter;
        this.debt_giver = giver;
        this.deposite =deposite;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getMoneysum() {
        return moneysum;
    }

    public void setMoneysum(long moneysum) {
        this.moneysum = moneysum;
    }

    public User getGetter() {
        return debt_getter;
    }

    public void setGetter(User getter) {
        this.debt_getter = getter;
    }

    public User getGiver() {
        return debt_giver;
    }

    public void setGiver(User giver) {
        this.debt_giver = giver;
    }

    public Deposite getDeposite(){
        return this.deposite;
    }

    public void setDeposite(Deposite deposite){
        this.deposite = deposite;
    }
}