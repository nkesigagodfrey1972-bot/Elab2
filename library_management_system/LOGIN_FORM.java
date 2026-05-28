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
import java.awt.event.WindowEvent;
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
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class LOGIN_FORM extends JFrame {

    private final JTextField usernameField = UiTheme.makeFormField("Enter username");
    private final JPasswordField passwordField = new JPasswordField();
    private final JButton showHideBtn = new JButton("Show");
    private final JButton loginBtn = UiTheme.makePrimaryButton("Sign In");
    private final JButton createAccountBtn = UiTheme.makeSecondaryButton("Open Signup Form");
    private final JButton signupBtn = makeTextButton("Create Account");
    private final JButton exitBtn = makeTextButton("Exit App");
    private final JLabel errorLabel = new JLabel(" ");
    private final JLabel helperLabel = new JLabel("Use your staff account to continue.");
    private final JProgressBar progressBar = new JProgressBar();
    private boolean passwordVisible = false;

    public LOGIN_FORM() {
        initComponents();
        bindInteractions();
    }

    private void initComponents() {
        setTitle("Elab Library System - Sign In");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        setSize(1040, 640);
        setLocationRelativeTo(null);
        UiTheme.applyWindowIcon(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createLineBorder(new Color(28, 48, 90), 1));
        root.add(buildLeftPanel(), BorderLayout.CENTER);
        root.add(buildRightPanel(), BorderLayout.EAST);

        setContentPane(root);
        getRootPane().setDefaultButton(loginBtn);
    }

    private JPanel buildLeftPanel() {
        JPanel panel = new GradientPanel(UiTheme.ACCENT_DARK, new Color(16, 112, 172));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(40, 42, 40, 42));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel brand = new JLabel("ELAB LIBRARY");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brand.setForeground(Color.WHITE);
        JButton closeBtn = createWindowButton("Exit App");
        closeBtn.addActionListener(e -> exitApplication());
        topBar.add(brand, BorderLayout.WEST);
        topBar.add(closeBtn, BorderLayout.EAST);

        JLabel logo = new JLabel(UiTheme.createLogoIcon(88));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel heading = new JLabel("Library operations, one secure workspace.");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 30));
        heading.setForeground(Color.WHITE);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subheading = new JLabel("<html>Manage circulation, staff roles, copies, fines, and reservations with a cleaner Firebase-backed desktop experience.</html>");
        subheading.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subheading.setForeground(new Color(221, 234, 255));
        subheading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel metrics = new JPanel(new GridBagLayout());
        metrics.setOpaque(false);
        metrics.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 14, 14);
        gbc.gridx = 0;
        gbc.gridy = 0;
        metrics.add(buildMetricCard("Realtime Access", "Role-based screens and secure sign-in."), gbc);
        gbc.gridx = 1;
        metrics.add(buildMetricCard("Daily Control", "Track issues, returns, fines, and logs."), gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        metrics.add(buildMetricCard("KIU Ready", "Departments and categories tuned for campus use."), gbc);
        gbc.gridx = 1;
        metrics.add(buildMetricCard("Portable Build", "Launch the packaged app on another machine."), gbc);

        JLabel footer = new JLabel("Kampala International University");
        footer.setFont(UiTheme.SMALL_FONT);
        footer.setForeground(new Color(184, 211, 245));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(topBar);
        panel.add(Box.createVerticalGlue());
        panel.add(logo);
        panel.add(Box.createVerticalStrut(22));
        panel.add(heading);
        panel.add(Box.createVerticalStrut(14));
        panel.add(subheading);
        panel.add(Box.createVerticalStrut(28));
        panel.add(metrics);
        panel.add(Box.createVerticalGlue());
        panel.add(footer);
        return panel;
    }

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(244, 247, 252));
        panel.setPreferredSize(new Dimension(420, 0));
        panel.setBorder(new EmptyBorder(34, 28, 34, 28));

        JPanel card = createFormSurface();
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(5, 0, 5, 0);

        JLabel eyebrow = new JLabel("SECURE STAFF ACCESS");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 11));
        eyebrow.setForeground(UiTheme.ACCENT_BLUE);
        gbc.gridy = 0;
        card.add(eyebrow, gbc);

        JLabel heading = new JLabel("Welcome back");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 28));
        heading.setForeground(UiTheme.TEXT);
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 0, 10, 0);
        card.add(heading, gbc);

        helperLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        helperLabel.setForeground(UiTheme.MUTED);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 18, 0);
        card.add(helperLabel, gbc);

        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridy = 3;
        card.add(UiTheme.makeFormLabel("Username"), gbc);
        gbc.gridy = 4;
        usernameField.setPreferredSize(new Dimension(0, 42));
        card.add(usernameField, gbc);

        gbc.gridy = 5;
        card.add(UiTheme.makeFormLabel("Password"), gbc);
        gbc.gridy = 6;
        passwordField.setPreferredSize(new Dimension(0, 42));
        card.add(buildPasswordRow(), gbc);

        JPanel tip = buildInfoStrip("Quick tip", "Your role is loaded after sign-in, so menu access adjusts automatically.");
        gbc.gridy = 7;
        gbc.insets = new Insets(12, 0, 8, 0);
        card.add(tip, gbc);

        errorLabel.setFont(UiTheme.SMALL_FONT);
        errorLabel.setForeground(UiTheme.DANGER);
        gbc.gridy = 8;
        gbc.insets = new Insets(2, 0, 2, 0);
        card.add(errorLabel, gbc);

        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(0, 5));
        progressBar.setForeground(UiTheme.ACCENT_BLUE);
        gbc.gridy = 9;
        gbc.insets = new Insets(4, 0, 8, 0);
        card.add(progressBar, gbc);

        loginBtn.setPreferredSize(new Dimension(0, 46));
        loginBtn.addActionListener(e -> doLogin());
        gbc.gridy = 10;
        gbc.insets = new Insets(10, 0, 8, 0);
        card.add(loginBtn, gbc);

        createAccountBtn.setPreferredSize(new Dimension(0, 42));
        createAccountBtn.addActionListener(e -> openSignup());
        gbc.gridy = 11;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(createAccountBtn, gbc);

        JPanel linkRow = new JPanel(new BorderLayout(8, 0));
        linkRow.setOpaque(false);
        JLabel linkPrompt = new JLabel("New staff member?");
        linkPrompt.setFont(UiTheme.BODY_FONT);
        linkPrompt.setForeground(UiTheme.MUTED);
        signupBtn.addActionListener(e -> openSignup());
        exitBtn.addActionListener(e -> exitApplication());
        linkRow.add(linkPrompt, BorderLayout.WEST);
        JPanel actions = new JPanel(new BorderLayout(6, 0));
        actions.setOpaque(false);
        actions.add(exitBtn, BorderLayout.WEST);
        actions.add(signupBtn, BorderLayout.EAST);
        linkRow.add(actions, BorderLayout.EAST);
        gbc.gridy = 12;
        gbc.insets = new Insets(8, 0, 0, 0);
        card.add(linkRow, gbc);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildPasswordRow() {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        stylePasswordField(passwordField);

        showHideBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        showHideBtn.setForeground(UiTheme.ACCENT_BLUE);
        showHideBtn.setFocusPainted(false);
        showHideBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(215, 223, 236), 1, true),
            new EmptyBorder(0, 14, 0, 14)
        ));
        showHideBtn.setBackground(Color.WHITE);
        showHideBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        showHideBtn.addActionListener(e -> togglePasswordVisibility());

        row.add(passwordField, BorderLayout.CENTER);
        row.add(showHideBtn, BorderLayout.EAST);
        return row;
    }

    private JPanel buildMetricCard(String title, String text) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setOpaque(true);
        card.setLayout(new BorderLayout(0, 8));
        card.setPreferredSize(new Dimension(210, 104));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(Color.WHITE);

        JLabel bodyLabel = new JLabel("<html><div style='width:160px;'>" + text + "</div></html>");
        bodyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bodyLabel.setForeground(new Color(223, 235, 255));

        card.setBackground(new Color(255, 255, 255, 18));
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(bodyLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildInfoStrip(String title, String text) {
        JPanel info = new JPanel(new BorderLayout(0, 4));
        info.setOpaque(true);
        info.setBackground(new Color(240, 246, 255));
        info.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(211, 225, 247), 1, true),
            new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(UiTheme.ACCENT_BLUE);

        JLabel bodyLabel = new JLabel("<html>" + text + "</html>");
        bodyLabel.setFont(UiTheme.BODY_FONT);
        bodyLabel.setForeground(UiTheme.TEXT);

        info.add(titleLabel, BorderLayout.NORTH);
        info.add(bodyLabel, BorderLayout.CENTER);
        return info;
    }

    private void bindInteractions() {
        usernameField.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void update(DocumentEvent event) {
                String username = usernameField.getText().trim();
                if (username.isEmpty()) {
                    helperLabel.setText("Use your staff account to continue.");
                } else {
                    helperLabel.setText("Signing in as " + username + ".");
                }
                errorLabel.setText(" ");
            }
        });

        passwordField.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void update(DocumentEvent event) {
                errorLabel.setText(" ");
            }
        });
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty()) {
            showError("Username is required.");
            usernameField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Password is required.");
            passwordField.requestFocus();
            return;
        }

        setLoading(true);
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                boolean ok = FirebaseBootstrap.validateEmployee(username, password);
                if (!ok) {
                    return null;
                }
                return FirebaseBootstrap.getEmployeeRole(username);
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    String role = get();
                    if (role != null) {
                        UserSession.set(username, role);
                        AuditService.log(
                            AuditService.ACTION_LOGIN,
                            AuditService.MODULE_AUTH,
                            "User logged in: " + username + " [" + role + "]"
                        );
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
        SIGNUP_FORM signupForm = new SIGNUP_FORM();
        signupForm.setVisible(true);
        signupForm.toFront();
        signupForm.requestFocus();
        dispose();
    }

    private void exitApplication() {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        System.exit(0);
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        passwordField.setEchoChar(passwordVisible ? (char) 0 : '\u2022');
        showHideBtn.setText(passwordVisible ? "Hide" : "Show");
    }

    private void setLoading(boolean loading) {
        progressBar.setVisible(loading);
        progressBar.setIndeterminate(loading);
        loginBtn.setEnabled(!loading);
        createAccountBtn.setEnabled(!loading);
        signupBtn.setEnabled(!loading);
        usernameField.setEnabled(!loading);
        passwordField.setEnabled(!loading);
        showHideBtn.setEnabled(!loading);
        if (loading) {
            helperLabel.setText("Validating credentials and loading your role...");
            errorLabel.setText(" ");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private void stylePasswordField(JPasswordField field) {
        field.setFont(UiTheme.BODY_FONT);
        field.setBackground(UiTheme.CARD);
        field.setForeground(UiTheme.TEXT);
        field.setCaretColor(UiTheme.TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UiTheme.CARD_BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
    }

    private JButton createWindowButton(String text) {
        JButton button = new JButton(text);
        button.setFocusable(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(255, 255, 255, 26));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 45), 1, true),
            new EmptyBorder(7, 14, 7, 14)
        ));
        return button;
    }

    private JPanel createFormSurface() {
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 242), 1, true),
            new EmptyBorder(28, 28, 28, 28)
        ));
        return panel;
    }

    private static JButton makeTextButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UiTheme.BODY_FONT.deriveFont(Font.BOLD));
        btn.setForeground(UiTheme.ACCENT_BLUE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.RIGHT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(0, 8, 0, 0));
        return btn;
    }

    private static class GradientPanel extends JPanel {
        private final Color start;
        private final Color end;

        private GradientPanel(Color start, Color end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, start, getWidth(), getHeight(), end));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(255, 255, 255, 18));
            g2.fillOval(-70, -50, 230, 230);
            g2.fillOval(getWidth() - 180, 40, 240, 240);
            g2.fillOval(getWidth() - 120, getHeight() - 160, 220, 220);
            g2.dispose();
        }
    }

    private abstract static class SimpleDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update(e);
        }

        public abstract void update(DocumentEvent event);
    }

    public static void main(String[] args) {
        AppLauncher.main(args);
    }
}
