import java.util.Optional;

/**
 * Business-logic layer.
 * All operations validate input and record a Transaction before persisting.
 */
public class BankService {

    private static final double MAX_WITHDRAWAL  = 50_000.00;
    private static final double MIN_DEPOSIT     =      1.00;
    private static final double MIN_BALANCE     =     50.00;  // minimum balance rule
    private static final int    MAX_PIN_ATTEMPTS =        3;

    private final BankDatabase db;

    public BankService(BankDatabase db) {
        this.db = db;
    }

    // ── Authentication ────────────────────────────────────────────────────────

    /**
     * Returns the account if accountNumber + PIN match; throws otherwise.
     * Caller must track failed attempts externally (see Main).
     */
    public Account authenticate(String accountNumber, String pin) throws BankException {
        Account account = db.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BankException("Account not found: " + accountNumber));

        if (!account.verifyPin(pin)) {
            throw new BankException("Incorrect PIN.");
        }
        return account;
    }

    // ── Registration ──────────────────────────────────────────────────────────

    public Account registerAccount(String name, String address, String pin,
                                   double initialDeposit) throws BankException {
        validateName(name);
        validatePin(pin);
        if (initialDeposit < MIN_BALANCE) {
            throw new BankException(
                String.format("Initial deposit must be at least RM %.2f.", MIN_BALANCE));
        }

        String accNo = db.generateAccountNumber();
        Account account = new Account(accNo, name, address, pin, initialDeposit);
        account.addTransaction(new Transaction(Transaction.Type.DEPOSIT, initialDeposit, initialDeposit));
        db.addAccount(account);
        return account;
    }

    // ── Core transactions ──────────────────────────────────────────────────────

    public double checkBalance(Account account) {
        double bal = account.getBalance();
        account.addTransaction(new Transaction(Transaction.Type.BALANCE_CHECK, 0, bal));
        db.update(account);
        return bal;
    }

    public double deposit(Account account, double amount) throws BankException {
        if (amount < MIN_DEPOSIT) {
            throw new BankException(
                String.format("Deposit amount must be at least RM %.2f.", MIN_DEPOSIT));
        }
        account.deposit(amount);
        account.addTransaction(new Transaction(Transaction.Type.DEPOSIT, amount, account.getBalance()));
        db.update(account);
        return account.getBalance();
    }

    public double withdraw(Account account, double amount) throws BankException {
        if (amount <= 0) {
            throw new BankException("Withdrawal amount must be greater than zero.");
        }
        if (amount > MAX_WITHDRAWAL) {
            throw new BankException(
                String.format("Single withdrawal cannot exceed RM %,.2f.", MAX_WITHDRAWAL));
        }
        double projected = account.getBalance() - amount;
        if (projected < MIN_BALANCE) {
            throw new BankException(
                String.format("Insufficient funds. Minimum balance of RM %.2f must be maintained.%n" +
                              "Your balance: RM %,.2f  |  Requested: RM %,.2f",
                              MIN_BALANCE, account.getBalance(), amount));
        }
        account.withdraw(amount);
        account.addTransaction(new Transaction(Transaction.Type.WITHDRAWAL, amount, account.getBalance()));
        db.update(account);
        return account.getBalance();
    }

    // ── Profile update ────────────────────────────────────────────────────────

    public void changePin(Account account, String oldPin, String newPin) throws BankException {
        if (!account.verifyPin(oldPin)) {
            throw new BankException("Current PIN is incorrect.");
        }
        validatePin(newPin);
        account.setHashedPin(Account.hashPin(newPin));
        db.update(account);
    }

    public void updateProfile(Account account, String newName, String newAddress) throws BankException {
        validateName(newName);
        account.setName(newName);
        account.setAddress(newAddress);
        db.update(account);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void validatePin(String pin) throws BankException {
        if (pin == null || !pin.matches("\\d{4,6}")) {
            throw new BankException("PIN must be 4–6 digits.");
        }
    }

    private void validateName(String name) throws BankException {
        if (name == null || name.trim().isEmpty()) {
            throw new BankException("Name cannot be empty.");
        }
    }

    public static int maxPinAttempts() { return MAX_PIN_ATTEMPTS; }
}
