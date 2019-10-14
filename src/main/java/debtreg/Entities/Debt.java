package debtreg.Entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Debt {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private long moneysum;
    private User getter;
    private User giver;

    public Debt(long id, long moneysum, User getter, User giver) {
        this.id = id;
        this.moneysum = moneysum;
        this.getter = getter;
        this.giver = giver;
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