package debtreg.Entities;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "debt_getters")
public class Debt_getter extends User {
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "debt_getter")
    private List<Debt> debts;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "debt_getter")
    private List<Message> messages;

    public Debt_getter(long id, String username, String password, Date registration, List<Debt> debts,
            List<Message> messages) {
        super(id, username, password, registration);
        this.debts = debts;
        this.messages = messages;
    }

    public Debt_getter(List<Debt> debts, List<Message> messages) {
        this.debts = debts;
        this.messages = messages;
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

}