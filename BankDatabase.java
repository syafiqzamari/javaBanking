import java.io.*;
import java.util.*;

/**
 * Persistence layer — stores accounts in a serialized file (bank_data.ser).
 * Behaves as an in-memory HashMap backed by disk so data survives restarts.
 */
public class BankDatabase {

    private static final String DB_FILE = "bank_data.ser";
    private Map<String, Account> accounts;   // key = account number
    private int nextAccountSuffix;

    // ── Construction ─────────────────────────────────────────────────────────

    public BankDatabase() {
        accounts = new HashMap<>();
        nextAccountSuffix = 1001;
        loadFromDisk();

        if (accounts.isEmpty()) {
            seedDemoAccounts();
        }
    }

    // ── Account number generation ─────────────────────────────────────────────

    public synchronized String generateAccountNumber() {
        return "ACC" + (nextAccountSuffix++);
    }

    // ── CRUD operations ───────────────────────────────────────────────────────

    public void addAccount(Account account) {
        accounts.put(account.getAccountNumber(), account);
        saveToDisk();
    }

    public Optional<Account> findByAccountNumber(String accountNumber) {
        return Optional.ofNullable(accounts.get(accountNumber));
    }

    public boolean accountExists(String accountNumber) {
        return accounts.containsKey(accountNumber);
    }

    /** Persist any in-place change (balance, name, address) back to disk. */
    public void update(Account account) {
        accounts.put(account.getAccountNumber(), account);
        saveToDisk();
    }

    public Collection<Account> getAllAccounts() {
        return Collections.unmodifiableCollection(accounts.values());
    }

    // ── Persistence ────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void loadFromDisk() {
        File file = new File(DB_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            accounts = (Map<String, Account>) ois.readObject();
            nextAccountSuffix = (int) ois.readObject();
            System.out.println("  [DB] Loaded " + accounts.size() + " account(s) from " + DB_FILE);
        } catch (Exception e) {
            System.out.println("  [DB] Could not load database (" + e.getMessage() + "). Starting fresh.");
            accounts = new HashMap<>();
        }
    }

    public void saveToDisk() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DB_FILE))) {
            oos.writeObject(accounts);
            oos.writeObject(nextAccountSuffix);
        } catch (IOException e) {
            System.out.println("  [DB] Warning: could not save database — " + e.getMessage());
        }
    }

    // ── Demo data ──────────────────────────────────────────────────────────────

    private void seedDemoAccounts() {
        Account a1 = new Account(generateAccountNumber(), "Ahmad Faris", "12 Jalan Merdeka, KL",     "1234", 5000.00);
        Account a2 = new Account(generateAccountNumber(), "Siti Rahayu", "88 Lorong Damai, Penang",  "5678", 12500.50);
        Account a3 = new Account(generateAccountNumber(), "Tan Wei Liang","3 Taman Harmoni, JB",     "0000", 850.00);

        accounts.put(a1.getAccountNumber(), a1);
        accounts.put(a2.getAccountNumber(), a2);
        accounts.put(a3.getAccountNumber(), a3);
        saveToDisk();

        System.out.println("  [DB] Demo accounts created:");
        for (Account a : accounts.values()) {
            System.out.printf("       %-8s  %-20s  PIN (plain): %s%n",
                    a.getAccountNumber(), a.getName(),
                    a == a1 ? "1234" : a == a2 ? "5678" : "0000");
        }
        System.out.println();
    }
}
