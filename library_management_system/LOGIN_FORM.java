package library_management_system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

public class LOGIN_FORM extends JFrame {

    private final JTextField     usernameField = UiTheme.makeFormField("Enter username");
    private final JPasswordField passwordField = new JPasswordField();
    private final JButton        showHideBtn   = new JButton("\uD83D\uDC41");
    private final JButton        loginBtn      = UiTheme.makePrimaryButton("Sign In");
    private final JButton        signupBtn     = makeTextButton("Create Account");
    private final JLabel         errorLabel    = new JLabel(" ");
    private final JProgressBar   progressBar   = new JProgressBar();
    private boolean passwordVisible = false;

    public LOGIN_FORM() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Elab Library System – Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        setSize(900, 560);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createLineBorder(UiTheme.ACCENT_DARK, 1));

        root.add(buildLeftPanel(),  BorderLayout.CENTER);
        root.add(buildRightPanel(), BorderLayout.EAST);

        setContentPane(root);
        getRootPane().setDefaultButton(loginBtn);
        usernameField.requestFocusInWindow();
    }

    // ── Left branding panel ───────────────────────────────────────────────────

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UiTheme.ACCENT_DARK, getWidth(), getHeight(), UiTheme.ACCENT_BLUE);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Decorative circles
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillOval(-40, -40, 200, 200);
                g2.fillOval(getWidth() - 80, getHeight() - 80, 180, 180);
                g2.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(48, 48, 48, 48));

        JLabel logo = new JLabel(UiTheme.createLogoIcon(80));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel appName = new JLabel("ELAB LIBRARY");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 30));
        appName.setForeground(Color.WHITE);
        appName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("Smart Digital Library Management");
        tagline.setFont(UiTheme.BODY_FONT);
        tagline.setForeground(new Color(200, 220, 255));
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(logo);
        panel.add(Box.createVerticalStrut(20));
        panel.add(appName);
        panel.add(Box.createVerticalStrut(8));
        panel.add(tagline);
        panel.add(Box.createVerticalStrut(28));
        panel.add(featureLine("\uD83D\uDCDA Books, members, and issue records"));
        panel.add(featureLine("\uD83D\uDD25 Firebase Firestore backend"));
        panel.add(featureLine("\uD83D\uDCCA Reports and CSV exports"));
        panel.add(featureLine("\u26A1 Fast and easy to use"));
        panel.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("Kampala International University · Elab Library System v2.0");
        footer.setFont(UiTheme.SMALL_FONT);
        footer.setForeground(new Color(160, 185, 230));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(footer);

        return panel;
    }

    // ── Right login form ──────────────────────────────────────────────────────

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 250, 255));
        panel.setPreferredSize(new Dimension(380, 0));
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.weightx = 1;
        gbc.insets = new Insets(6, 0, 6, 0);

        // Heading
        JLabel heading = new JLabel("Sign In");
        heading.setFont(UiTheme.TITLE_FONT);
        heading.setForeground(UiTheme.TEXT);
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(heading, gbc);
        gbc.insets = new Insets(6, 0, 6, 0);

        // Username
        gbc.gridy = 1;
        panel.add(UiTheme.makeFormLabel("Username"), gbc);
        gbc.gridy = 2;
        panel.add(usernameField, gbc);

        // Password
        gbc.gridy = 3;
        panel.add(UiTheme.makeFormLabel("Password"), gbc);
        gbc.gridy = 4;
        panel.add(buildPasswordRow(), gbc);

        // Error label
        errorLabel.setFont(UiTheme.SMALL_FONT);
        errorLabel.setForeground(UiTheme.DANGER);
        gbc.gridy = 5; gbc.insets = new Insets(2, 0, 2, 0);
        panel.add(errorLabel, gbc);
        gbc.insets = new Insets(6, 0, 6, 0);

        // Progress bar
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(0, 4));
        progressBar.setForeground(UiTheme.ACCENT_BLUE);
        gbc.gridy = 6;
        panel.add(progressBar, gbc);

        // Login button
        loginBtn.setPreferredSize(new Dimension(0, 42));
        loginBtn.addActionListener(e -> doLogin());
        gbc.gridy = 7; gbc.insets = new Insets(14, 0, 6, 0);
        panel.add(loginBtn, gbc);
        gbc.insets = new Insets(6, 0, 6, 0);

        // Signup link
        gbc.gridy = 8;
        JPanel linkRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 4, 0));
        linkRow.setOpaque(false);
        linkRow.add(new JLabel("Don't have an account?"));
        signupBtn.addActionListener(e -> openSignup());
        linkRow.add(signupBtn);
        panel.add(linkRow, gbc);

        // Filler
        gbc.gridy = 9; gbc.weighty = 1;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    private JPanel buildPasswordRow() {
        JPanel row = new JPanel(new BorderLayout(4, 0));
        row.setOpaque(false);

        passwordField.setFont(UiTheme.BODY_FONT);
        passwordField.setBackground(UiTheme.CARD);
        passwordField.setForeground(UiTheme.TEXT);
        passwordField.setCaretColor(UiTheme.TEXT);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UiTheme.CARD_BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));

        showHideBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        showHideBtn.setFocusPainted(false);
        showHideBtn.setBorderPainted(false);
        showHideBtn.setContentAreaFilled(false);
        showHideBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        showHideBtn.setToolTipText("Show/hide password");
        showHideBtn.addActionListener(e -> togglePasswordVisibility());

        row.add(passwordField, BorderLayout.CENTER);
        row.add(showHideBtn,   BorderLayout.EAST);
        return row;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty()) {
            showError("Username is required."); usernameField.requestFocus(); return;
        }
        if (password.isEmpty()) {
            showError("Password is required."); passwordField.requestFocus(); return;
        }

        setLoading(true);
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                boolean ok = FirebaseBootstrap.validateEmployee(username, password);
                if (!ok) return null;
                return FirebaseBootstrap.getEmployeeRole(username);
            }
            @Override protected void done() {
                setLoading(false);
                try {
                    String role = get();
                    if (role != null) {
                        UserSession.set(username, role);
                        AuditService.log(AuditService.ACTION_LOGIN, AuditService.MODULE_AUTH,
                            "User logged in: " + username + " [" + role + "]");
                        new MainWindow(username).setVisible(true);
                        dispose();
                    } else {
                        showError("Invalid username or password.");
                        passwordField.setText("");
                        passwordField.requestFocus();
                    }
                } catch (Exception ex) {
                    showError("Login failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void openSignup() {
        new SIGNUP_FORM().setVisible(true);
        setVisible(false);
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        passwordField.setEchoChar(passwordVisible ? (char) 0 : '\u2022');
        showHideBtn.setText(passwordVisible ? "\uD83D\uDEAB" : "\uD83D\uDC41");
    }

    private void setLoading(boolean loading) {
        progressBar.setVisible(loading);
        progressBar.setIndeterminate(loading);
        loginBtn.setEnabled(!loading);
        usernameField.setEnabled(!loading);
        passwordField.setEnabled(!loading);
        if (loading) errorLabel.setText(" ");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JLabel featureLine(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(200, 220, 255));
        lbl.setFont(UiTheme.BODY_FONT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(3, 0, 3, 0));
        return lbl;
    }

    private static JButton makeTextButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UiTheme.BODY_FONT.deriveFont(Font.BOLD));
        btn.setForeground(UiTheme.ACCENT_BLUE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(0, 4, 0, 4));
        return btn;
    }

    public static void main(String[] args) {
        AppLauncher.main(args);
    }
}
