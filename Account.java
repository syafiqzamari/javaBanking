import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bank account with user biodata and balance.
 * PIN is stored as a SHA-256 hash for security.
 */
public class Account implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String accountNumber;
    private String name;
    private String address;
    private String hashedPin;
    private double balance;
    private final List<Transaction> transactions;

    public Account(String accountNumber, String name, String address, String pin, double initialBalance) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.address = address;
        this.hashedPin = hashPin(pin);
        this.balance = initialBalance;
        this.transactions = new ArrayList<>();
    }

    // ── PIN ──────────────────────────────────────────────────────────────────

    public static String hashPin(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(pin.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public boolean verifyPin(String pin) {
        return this.hashedPin.equals(hashPin(pin));
    }

    // ── Balance operations ───────────────────────────────────────────────────

    public void deposit(double amount) {
        this.balance += amount;
    }

    public void withdraw(double amount) {
        this.balance -= amount;
    }

    // ── Transaction history ──────────────────────────────────────────────────

    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public String getAccountNumber() { return accountNumber; }
    public String getName()          { return name; }
    public String getAddress()       { return address; }
    public double getBalance()       { return balance; }

    public void setName(String name)       { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setHashedPin(String hp)    { this.hashedPin = hp; }
}
