package debtreg.Entities;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "debt_givers")
public class Debt_giver extends User {
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "debt_giver")
    private List<Debt> debts;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "debt_giver")
    private List<Message> messages;

    public Debt_giver(long id, String username, String password, Date registration, List<Debt> debts,
            List<Message> messages) {
        super(id, username, password, registration);
        this.debts = debts;
        this.messages = messages;
    }

    public Debt_giver(List<Debt> debts, List<Message> messages) {
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