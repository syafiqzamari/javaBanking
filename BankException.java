/**
 * Domain-specific exception for all bank business-rule violations.
 */
public class BankException extends Exception {
    public BankException(String message) {
        super(message);
    }
}
