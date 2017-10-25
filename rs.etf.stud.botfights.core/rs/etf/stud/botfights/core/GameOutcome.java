package rs.etf.stud.botfights.core;

public class GameOutcome {
    public enum OutcomeType {ERROR, RULE_BREACH, SUCCESS}
    private String description;

    public GameOutcome(String description, OutcomeType outcomeType){
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
