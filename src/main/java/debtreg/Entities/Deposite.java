package debtreg.Entities;

public class Deposite {
    private String name;
    private String description;
    private Debt debt;

    public Deposite(){
        name = "";
        description = "";
        debt = null;
    }

    public Deposite(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Deposite(String name, String description, Debt debt) {
        this.name = name;
        this.description = description;
        this.debt = debt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Debt getDebt() {
        return debt;
    }

    public void setDebt(Debt debt) {
        this.debt = debt;
    }
}