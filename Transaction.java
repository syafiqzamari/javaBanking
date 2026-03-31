import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable record of a single bank transaction.
 */
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        DEPOSIT("DEPOSIT   "),
        WITHDRAWAL("WITHDRAWAL"),
        BALANCE_CHECK("BAL CHECK ");

        private final String label;
        Type(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Type type;
    private final double amount;
    private final double balanceAfter;
    private final String timestamp;

    public Transaction(Type type, double amount, double balanceAfter) {
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = LocalDateTime.now().format(FMT);
    }

    public Type   getType()         { return type; }
    public double getAmount()       { return amount; }
    public double getBalanceAfter() { return balanceAfter; }
    public String getTimestamp()    { return timestamp; }

    @Override
    public String toString() {
        if (type == Type.BALANCE_CHECK) {
            return String.format("  [%s]  %s  Balance: RM %,.2f",
                    timestamp, type.getLabel(), balanceAfter);
        }
        return String.format("  [%s]  %s  Amount: RM %,9.2f  |  Balance: RM %,.2f",
                timestamp, type.getLabel(), amount, balanceAfter);
    }
}
