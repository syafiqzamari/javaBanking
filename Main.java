import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * GUI entry point — Swing-based UI for the Simple Bank Transaction System.
 *
 *  Compile :  javac *.java
 *  Run     :  java Main
 */
public class Main {

    private static BankDatabase db;
    private static BankService  service;

    private static JFrame     frame;
    private static CardLayout cardLayout;
    private static JPanel     mainPanel;

    private static JPanel welcomePanel;
    private static JPanel loginPanel;
    private static JPanel registerPanel;
    private static JPanel accountPanel;

    private static Account currentAccount;

    // ── Colour palette ─────────────────────────────────────────────────────────
    private static final Color PRIMARY   = new Color(0,  105,  92);
    private static final Color ACCENT    = new Color(0,  150, 136);
    private static final Color BG        = new Color(245, 245, 245);
    private static final Color WHITE     = Color.WHITE;
    private static final Color TEXT      = new Color(33,  33,  33);
    private static final Color MUTED     = new Color(80,  80,  80);
    private static final Color ERR       = new Color(183,  28,  28);
    private static final Color BORDER    = new Color(200, 200, 200);
    private static final Color CARD_HOV  = new Color(232, 245, 233);

    // ── Bootstrap ──────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        db      = new BankDatabase();
        service = new BankService(db);

        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Simple Bank Transaction System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(480, 600);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);

            cardLayout = new CardLayout();
            mainPanel  = new JPanel(cardLayout);

            buildWelcomePanel();
            buildLoginPanel();
            buildRegisterPanel();
            buildAccountPanel();

            mainPanel.add(welcomePanel,  "welcome");
            mainPanel.add(loginPanel,    "login");
            mainPanel.add(registerPanel, "register");
            mainPanel.add(accountPanel,  "account");

            frame.add(mainPanel);
            frame.setVisible(true);
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  WELCOME PANEL
    // ══════════════════════════════════════════════════════════════════════════

    private static void buildWelcomePanel() {
        welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(PRIMARY);

        // ── Branding ──
        JPanel brand = new JPanel(new GridBagLayout());
        brand.setBackground(PRIMARY);
        brand.setBorder(BorderFactory.createEmptyBorder(70, 30, 50, 30));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.anchor = GridBagConstraints.CENTER;

        g.gridy = 0; g.insets = new Insets(0, 0, 10, 0);
        JLabel circle = new JLabel("SB");
        circle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        circle.setForeground(PRIMARY);
        circle.setOpaque(true);
        circle.setBackground(WHITE);
        circle.setPreferredSize(new Dimension(72, 72));
        circle.setHorizontalAlignment(SwingConstants.CENTER);
        circle.setBorder(BorderFactory.createLineBorder(WHITE, 2, true));
        brand.add(circle, g);

        g.gridy = 1; g.insets = new Insets(6, 0, 4, 0);
        JLabel title = new JLabel("Simple Bank");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(WHITE);
        brand.add(title, g);

        g.gridy = 2; g.insets = new Insets(0, 0, 0, 0);
        JLabel sub = new JLabel("Transaction System");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        sub.setForeground(new Color(200, 230, 201));
        brand.add(sub, g);

        welcomePanel.add(brand, BorderLayout.CENTER);

        // ── Buttons ──
        JPanel btns = new JPanel(new GridLayout(3, 1, 0, 10));
        btns.setBackground(PRIMARY);
        btns.setBorder(BorderFactory.createEmptyBorder(10, 60, 60, 60));

        JButton loginBtn    = createPrimaryButton("Login to Account");
        JButton registerBtn = createOutlineButton("Open New Account");
        JButton exitBtn     = createTextButton("Exit");

        loginBtn.addActionListener(e    -> cardLayout.show(mainPanel, "login"));
        registerBtn.addActionListener(e -> cardLayout.show(mainPanel, "register"));
        exitBtn.addActionListener(e     -> System.exit(0));

        btns.add(loginBtn);
        btns.add(registerBtn);
        btns.add(exitBtn);
        welcomePanel.add(btns, BorderLayout.SOUTH);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  LOGIN PANEL
    // ══════════════════════════════════════════════════════════════════════════

    private static void buildLoginPanel() {
        loginPanel = new JPanel(new BorderLayout());
        loginPanel.setBackground(BG);
        loginPanel.add(buildTopBar("Login", () -> cardLayout.show(mainPanel, "welcome")),
                       BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG);
        form.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.weightx = 1.0;

        g.gridy = 0; g.insets = new Insets(6, 0, 4, 0);
        form.add(makeLabel("Account Number"), g);

        g.gridy = 1; g.insets = new Insets(0, 0, 12, 0);
        JTextField accField = makeTextField();
        form.add(accField, g);

        g.gridy = 2; g.insets = new Insets(6, 0, 4, 0);
        form.add(makeLabel("PIN"), g);

        g.gridy = 3; g.insets = new Insets(0, 0, 0, 0);
        JPasswordField pinField = makePasswordField();
        form.add(pinField, g);

        g.gridy = 4; g.insets = new Insets(22, 0, 8, 0);
        JButton loginBtn = createPrimaryButton("Login");
        form.add(loginBtn, g);

        g.gridy = 5; g.insets = new Insets(4, 0, 0, 0);
        JLabel statusLbl = makeStatusLabel();
        form.add(statusLbl, g);

        loginPanel.add(form, BorderLayout.CENTER);

        int[] attempts = {0};

        Runnable doLogin = () -> {
            String accNo = accField.getText().trim().toUpperCase();
            String pin   = new String(pinField.getPassword());

            if (accNo.isEmpty() || pin.isEmpty()) {
                setError(statusLbl, "Please fill in all fields.");
                return;
            }
            try {
                currentAccount = service.authenticate(accNo, pin);
                attempts[0] = 0;
                accField.setText(""); pinField.setText(""); statusLbl.setText(" ");
                refreshAccountPanel();
                cardLayout.show(mainPanel, "account");
            } catch (BankException ex) {
                int remaining = BankService.maxPinAttempts() - (++attempts[0]);
                if (remaining <= 0) {
                    setError(statusLbl, "Too many failed attempts. Please wait...");
                    loginBtn.setEnabled(false);
                    Timer t = new Timer(3000, ev -> {
                        loginBtn.setEnabled(true);
                        attempts[0] = 0;
                        statusLbl.setText(" ");
                    });
                    t.setRepeats(false); t.start();
                } else {
                    setError(statusLbl, ex.getMessage() + " (" + remaining + " attempt(s) left)");
                }
            }
        };

        loginBtn.addActionListener(e -> doLogin.run());
        pinField.addActionListener(e -> doLogin.run());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  REGISTER PANEL
    // ══════════════════════════════════════════════════════════════════════════

    private static void buildRegisterPanel() {
        registerPanel = new JPanel(new BorderLayout());
        registerPanel.setBackground(BG);
        registerPanel.add(buildTopBar("Open New Account", () -> cardLayout.show(mainPanel, "welcome")),
                          BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG);
        form.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.weightx = 1.0;

        g.gridy = 0; g.insets = new Insets(6, 0, 4, 0);  form.add(makeLabel("Full Name"), g);
        g.gridy = 1; g.insets = new Insets(0, 0, 10, 0); JTextField nameField = makeTextField();    form.add(nameField, g);
        g.gridy = 2; g.insets = new Insets(6, 0, 4, 0);  form.add(makeLabel("Address"), g);
        g.gridy = 3; g.insets = new Insets(0, 0, 10, 0); JTextField addrField = makeTextField();    form.add(addrField, g);
        g.gridy = 4; g.insets = new Insets(6, 0, 4, 0);  form.add(makeLabel("Create PIN (4\u20136 digits)"), g);
        g.gridy = 5; g.insets = new Insets(0, 0, 10, 0); JPasswordField pinField  = makePasswordField(); form.add(pinField, g);
        g.gridy = 6; g.insets = new Insets(6, 0, 4, 0);  form.add(makeLabel("Confirm PIN"), g);
        g.gridy = 7; g.insets = new Insets(0, 0, 10, 0); JPasswordField pinConf   = makePasswordField(); form.add(pinConf, g);
        g.gridy = 8; g.insets = new Insets(6, 0, 4, 0);  form.add(makeLabel("Initial Deposit (RM)"), g);
        g.gridy = 9; g.insets = new Insets(0, 0, 0, 0);  JTextField depField  = makeTextField();    form.add(depField, g);

        g.gridy = 10; g.insets = new Insets(22, 0, 8, 0);
        JButton btn = createPrimaryButton("Create Account");
        form.add(btn, g);

        g.gridy = 11; g.insets = new Insets(4, 0, 0, 0);
        JLabel statusLbl = makeStatusLabel();
        form.add(statusLbl, g);

        btn.addActionListener(e -> {
            String name    = nameField.getText().trim();
            String address = addrField.getText().trim();
            String pin     = new String(pinField.getPassword());
            String conf    = new String(pinConf.getPassword());
            String depStr  = depField.getText().trim();

            if (name.isEmpty() || address.isEmpty() || pin.isEmpty() || depStr.isEmpty()) {
                setError(statusLbl, "Please fill in all fields."); return;
            }
            if (!pin.equals(conf)) {
                setError(statusLbl, "PINs do not match."); return;
            }
            double deposit;
            try { deposit = Double.parseDouble(depStr); }
            catch (NumberFormatException ex) { setError(statusLbl, "Invalid deposit amount."); return; }

            try {
                Account acc = service.registerAccount(name, address, pin, deposit);
                nameField.setText(""); addrField.setText("");
                pinField.setText(""); pinConf.setText(""); depField.setText("");
                statusLbl.setText(" ");
                JOptionPane.showMessageDialog(frame,
                    String.format("Account created successfully!%n%nAccount Number : %s%nAccount Holder : %s%nOpening Balance: RM %,.2f",
                        acc.getAccountNumber(), acc.getName(), acc.getBalance()),
                    "Account Created", JOptionPane.INFORMATION_MESSAGE);
                cardLayout.show(mainPanel, "welcome");
            } catch (BankException ex) {
                setError(statusLbl, ex.getMessage());
            }
        });

        JScrollPane scroll = new JScrollPane(form,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        registerPanel.add(scroll, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ACCOUNT PANEL
    // ══════════════════════════════════════════════════════════════════════════

    private static JLabel acctHeaderLabel;
    private static JLabel balanceLabel;

    private static void buildAccountPanel() {
        accountPanel = new JPanel(new BorderLayout());
        accountPanel.setBackground(BG);

        // ── Balance header ──
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        acctHeaderLabel = new JLabel(" ");
        acctHeaderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        acctHeaderLabel.setForeground(new Color(200, 230, 201));

        JLabel balTitle = new JLabel("Current Balance");
        balTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        balTitle.setForeground(new Color(200, 230, 201));

        balanceLabel = new JLabel("RM 0.00");
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        balanceLabel.setForeground(WHITE);

        JPanel info = new JPanel(new GridLayout(3, 1, 0, 2));
        info.setBackground(PRIMARY);
        info.add(acctHeaderLabel);
        info.add(balTitle);
        info.add(balanceLabel);
        header.add(info, BorderLayout.CENTER);

        JButton logoutBtn = createOutlineButton("Logout");
        logoutBtn.setPreferredSize(new Dimension(90, 36));
        logoutBtn.addActionListener(e -> {
            currentAccount = null;
            cardLayout.show(mainPanel, "welcome");
        });
        header.add(logoutBtn, BorderLayout.EAST);

        accountPanel.add(header, BorderLayout.NORTH);

        // ── Menu grid ──
        JPanel menu = new JPanel(new GridLayout(3, 2, 12, 12));
        menu.setBackground(BG);
        menu.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        menu.add(makeMenuCard("Check Balance",   e -> doCheckBalance()));
        menu.add(makeMenuCard("Deposit",         e -> doDeposit()));
        menu.add(makeMenuCard("Withdraw",        e -> doWithdraw()));
        menu.add(makeMenuCard("History",         e -> doShowHistory()));
        menu.add(makeMenuCard("Account Details", e -> doShowDetails()));
        menu.add(makeMenuCard("Change PIN",      e -> doChangePin()));

        accountPanel.add(menu, BorderLayout.CENTER);
    }

    private static void refreshAccountPanel() {
        if (currentAccount == null) return;
        acctHeaderLabel.setText(currentAccount.getAccountNumber() + "  |  " + currentAccount.getName());
        balanceLabel.setText(String.format("RM %,.2f", currentAccount.getBalance()));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ACCOUNT OPERATIONS
    // ══════════════════════════════════════════════════════════════════════════

    private static void doCheckBalance() {
        double bal = service.checkBalance(currentAccount);
        balanceLabel.setText(String.format("RM %,.2f", bal));
        JOptionPane.showMessageDialog(frame,
            String.format("Current Balance: RM %,.2f", bal),
            "Balance", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void doDeposit() {
        String input = JOptionPane.showInputDialog(frame,
            "Enter deposit amount (RM):", "Deposit", JOptionPane.PLAIN_MESSAGE);
        if (input == null) return;
        try {
            double amount = Double.parseDouble(input.trim());
            double newBal = service.deposit(currentAccount, amount);
            balanceLabel.setText(String.format("RM %,.2f", newBal));
            JOptionPane.showMessageDialog(frame,
                String.format("Deposited RM %,.2f successfully.%nNew Balance: RM %,.2f", amount, newBal),
                "Deposit Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (BankException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void doWithdraw() {
        String input = JOptionPane.showInputDialog(frame,
            String.format("Current Balance: RM %,.2f%n%nEnter withdrawal amount (RM):",
                currentAccount.getBalance()),
            "Withdraw", JOptionPane.PLAIN_MESSAGE);
        if (input == null) return;
        try {
            double amount = Double.parseDouble(input.trim());
            double newBal = service.withdraw(currentAccount, amount);
            balanceLabel.setText(String.format("RM %,.2f", newBal));
            JOptionPane.showMessageDialog(frame,
                String.format("Withdrawn RM %,.2f successfully.%nRemaining Balance: RM %,.2f", amount, newBal),
                "Withdrawal Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (BankException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void doShowHistory() {
        List<Transaction> history = currentAccount.getTransactions();
        if (history.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No transactions yet.",
                "Transaction History", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] cols = {"Date & Time", "Type", "Amount (RM)", "Balance (RM)"};
        Object[][] data = history.stream().map(t -> new Object[]{
            t.getTimestamp(),
            t.getType().getLabel().trim(),
            t.getType() == Transaction.Type.BALANCE_CHECK ? "—" : String.format("%,.2f", t.getAmount()),
            String.format("%,.2f", t.getBalanceAfter())
        }).toArray(Object[][]::new);

        DefaultTableModel model = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setGridColor(new Color(230, 230, 230));
        table.setShowGrid(true);

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(520, 280));

        JOptionPane.showMessageDialog(frame, sp,
            "Transaction History — " + currentAccount.getAccountNumber(),
            JOptionPane.PLAIN_MESSAGE);
    }

    private static void doShowDetails() {
        JOptionPane.showMessageDialog(frame,
            String.format(
                "Account Number : %s%n" +
                "Account Holder : %s%n" +
                "Address        : %s%n" +
                "Current Balance: RM %,.2f",
                currentAccount.getAccountNumber(),
                currentAccount.getName(),
                currentAccount.getAddress(),
                currentAccount.getBalance()),
            "Account Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void doChangePin() {
        JPasswordField oldPin = makePasswordField();
        JPasswordField newPin = makePasswordField();
        JPasswordField conf   = makePasswordField();

        JPanel panel = new JPanel(new GridLayout(6, 1, 0, 6));
        panel.add(makeLabel("Current PIN:"));       panel.add(oldPin);
        panel.add(makeLabel("New PIN (4\u20136 digits):")); panel.add(newPin);
        panel.add(makeLabel("Confirm New PIN:"));   panel.add(conf);

        int result = JOptionPane.showConfirmDialog(frame, panel,
            "Change PIN", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String np = new String(newPin.getPassword());
        String cp = new String(conf.getPassword());
        if (!np.equals(cp)) {
            JOptionPane.showMessageDialog(frame, "PINs do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            service.changePin(currentAccount, new String(oldPin.getPassword()), np);
            JOptionPane.showMessageDialog(frame, "PIN changed successfully.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (BankException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UI COMPONENT HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private static JPanel buildTopBar(String title, Runnable onBack) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PRIMARY);
        bar.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JButton back = new JButton("<- Back");
        back.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        back.setForeground(WHITE);
        back.setBackground(PRIMARY);
        back.setBorderPainted(false);
        back.setFocusPainted(false);
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.addActionListener(e -> onBack.run());

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lbl.setForeground(WHITE);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);

        bar.add(back, BorderLayout.WEST);
        bar.add(lbl,  BorderLayout.CENTER);
        return bar;
    }

    private static JButton makeMenuCard(String text, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(WHITE);
        btn.setForeground(TEXT);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(20, 8, 20, 8)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(CARD_HOV); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(WHITE); }
        });
        return btn;
    }

    private static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBackground(ACCENT);
        btn.setForeground(WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(PRIMARY); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(ACCENT); }
        });
        return btn;
    }

    private static JButton createOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(WHITE);
        btn.setBorder(BorderFactory.createLineBorder(WHITE, 2, true));
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setPreferredSize(new Dimension(0, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static JButton createTextButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(200, 230, 201));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(MUTED);
        return lbl;
    }

    private static JLabel makeStatusLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(ERR);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }

    private static JTextField makeTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setPreferredSize(new Dimension(0, 38));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        return tf;
    }

    private static JPasswordField makePasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf.setPreferredSize(new Dimension(0, 38));
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        return pf;
    }

    private static void setError(JLabel lbl, String msg) {
        lbl.setForeground(ERR);
        lbl.setText(msg);
    }
}
