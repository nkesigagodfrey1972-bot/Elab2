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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

public class SIGNUP_FORM extends JFrame {

    private final JTextField fullNameField = UiTheme.makeFormField("Enter full name");
    private final JTextField usernameField = UiTheme.makeFormField("Choose a username");
    private final JTextField emailField = UiTheme.makeFormField("Enter email address");
    private final JTextField phoneField = UiTheme.makeFormField("Enter phone number");
    private final JComboBox<String> departmentBox = KiuCatalog.createDepartmentCombo();
    private final JComboBox<String> roleBox = new JComboBox<>(new String[]{
        UserSession.ROLE_LIBRARIAN,
        UserSession.ROLE_ASSISTANT,
        UserSession.ROLE_VIEWER
    });
    private final JPasswordField passwordField = new JPasswordField();
    private final JPasswordField confirmField = new JPasswordField();
    private final JButton showHideBtn1 = new JButton("\uD83D\uDC41");
    private final JButton showHideBtn2 = new JButton("\uD83D\uDC41");
    private final JButton submitBtn = UiTheme.makePrimaryButton("Create Account");
    private final JButton backBtn = makeTextButton("Back to Login");
    private final JLabel errorLabel = new JLabel(" ");
    private final JProgressBar progressBar = new JProgressBar();
    private boolean pass1Visible = false;
    private boolean pass2Visible = false;

    public SIGNUP_FORM() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Elab Library System - Create Account");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        setSize(940, 720);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createLineBorder(UiTheme.ACCENT_DARK, 1));

        root.add(buildLeftPanel(), BorderLayout.CENTER);
        root.add(buildRightPanel(), BorderLayout.EAST);

        setContentPane(root);
        getRootPane().setDefaultButton(submitBtn);
        fullNameField.requestFocusInWindow();
    }

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UiTheme.ACCENT_DARK, getWidth(), getHeight(), new Color(34, 139, 34));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
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

        JLabel tagline = new JLabel("Create your staff account");
        tagline.setFont(UiTheme.BODY_FONT);
        tagline.setForeground(new Color(200, 240, 210));
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(logo);
        panel.add(Box.createVerticalStrut(20));
        panel.add(appName);
        panel.add(Box.createVerticalStrut(8));
        panel.add(tagline);
        panel.add(Box.createVerticalStrut(28));
        panel.add(featureLine("\u2705 Full staff profile capture"));
        panel.add(featureLine("\u2705 Secure password hashing"));
        panel.add(featureLine("\u2705 Role-based access (Admin reserved)"));
        panel.add(featureLine("\u2705 Firebase-backed authentication"));
        panel.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("Kampala International University · Elab Library System v2.0");
        footer.setFont(UiTheme.SMALL_FONT);
        footer.setForeground(new Color(160, 210, 175));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(footer);

        return panel;
    }

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 252, 250));
        panel.setPreferredSize(new Dimension(420, 0));
        panel.setBorder(new EmptyBorder(34, 40, 34, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.insets = new Insets(5, 0, 5, 0);

        JLabel heading = new JLabel("Create Account");
        heading.setFont(UiTheme.TITLE_FONT);
        heading.setForeground(UiTheme.TEXT);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 16, 0);
        panel.add(heading, gbc);
        gbc.insets = new Insets(5, 0, 5, 0);

        addField(panel, gbc, 1, "Full Name", fullNameField);
        addField(panel, gbc, 3, "Username", usernameField);
        addField(panel, gbc, 5, "Email Address", emailField);
        addField(panel, gbc, 7, "Phone Number", phoneField);
        styleDepartmentBox();
        addField(panel, gbc, 9, "Department / Section", departmentBox);

        styleRoleBox();
        addField(panel, gbc, 11, "Role", roleBox);

        gbc.gridy = 13;
        panel.add(UiTheme.makeFormLabel("Password"), gbc);
        gbc.gridy = 14;
        panel.add(buildPasswordRow(passwordField, showHideBtn1, 1), gbc);

        gbc.gridy = 15;
        panel.add(UiTheme.makeFormLabel("Confirm Password"), gbc);
        gbc.gridy = 16;
        panel.add(buildPasswordRow(confirmField, showHideBtn2, 2), gbc);

        errorLabel.setFont(UiTheme.SMALL_FONT);
        errorLabel.setForeground(UiTheme.DANGER);
        gbc.gridy = 17;
        gbc.insets = new Insets(2, 0, 2, 0);
        panel.add(errorLabel, gbc);
        gbc.insets = new Insets(5, 0, 5, 0);

        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(0, 4));
        progressBar.setForeground(UiTheme.SUCCESS);
        gbc.gridy = 18;
        panel.add(progressBar, gbc);

        submitBtn.setPreferredSize(new Dimension(0, 42));
        submitBtn.addActionListener(e -> doSignup());
        gbc.gridy = 19;
        gbc.insets = new Insets(14, 0, 6, 0);
        panel.add(submitBtn, gbc);
        gbc.insets = new Insets(5, 0, 5, 0);

        gbc.gridy = 20;
        JPanel linkRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 4, 0));
        linkRow.setOpaque(false);
        linkRow.add(new JLabel("Already have an account?"));
        backBtn.addActionListener(e -> goToLogin());
        linkRow.add(backBtn);
        panel.add(linkRow, gbc);

        gbc.gridy = 21;
        gbc.weighty = 1;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int labelRow, String label, Component input) {
        gbc.gridy = labelRow;
        panel.add(UiTheme.makeFormLabel(label), gbc);
        gbc.gridy = labelRow + 1;
        panel.add(input, gbc);
    }

    private JPanel buildPasswordRow(JPasswordField field, JButton toggleBtn, int which) {
        JPanel row = new JPanel(new BorderLayout(4, 0));
        row.setOpaque(false);

        field.setFont(UiTheme.BODY_FONT);
        field.setBackground(UiTheme.CARD);
        field.setForeground(UiTheme.TEXT);
        field.setCaretColor(UiTheme.TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UiTheme.CARD_BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));

        toggleBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        toggleBtn.setFocusPainted(false);
        toggleBtn.setBorderPainted(false);
        toggleBtn.setContentAreaFilled(false);
        toggleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleBtn.addActionListener(e -> toggleVisibility(field, toggleBtn, which));

        row.add(field, BorderLayout.CENTER);
        row.add(toggleBtn, BorderLayout.EAST);
        return row;
    }

    private void doSignup() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String department = selectedComboValue(departmentBox);
        String role = (String) roleBox.getSelectedItem();
        String password = new String(passwordField.getPassword()).trim();
        String confirm = new String(confirmField.getPassword()).trim();

        if (fullName.isEmpty()) {
            showError("Full name is required.");
            fullNameField.requestFocus();
            return;
        }
        if (username.isEmpty()) {
            showError("Username is required.");
            usernameField.requestFocus();
            return;
        }
        if (!username.matches("[A-Za-z0-9._-]{3,30}")) {
            showError("Username must be 3-30 characters using letters, numbers, dot, dash, or underscore.");
            usernameField.requestFocus();
            return;
        }
        if (!email.isEmpty() && !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            showError("Enter a valid email address.");
            emailField.requestFocus();
            return;
        }
        if (!phone.isEmpty() && !phone.matches("^[0-9+()\\-\\s]{7,20}$")) {
            showError("Enter a valid phone number.");
            phoneField.requestFocus();
            return;
        }
        if (role == null || role.isBlank()) {
            showError("Please choose a role.");
            roleBox.requestFocus();
            return;
        }
        if (UserSession.ROLE_ADMIN.equalsIgnoreCase(role)) {
            showError("Admin role can only be assigned directly in Firebase.");
            return;
        }
        if (password.isEmpty()) {
            showError("Password is required.");
            passwordField.requestFocus();
            return;
        }
        if (confirm.isEmpty()) {
            showError("Please confirm your password.");
            confirmField.requestFocus();
            return;
        }
        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            confirmField.requestFocus();
            return;
        }
        if (password.length() < 4) {
            showError("Password must be at least 4 characters.");
            passwordField.requestFocus();
            return;
        }

        setLoading(true);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                if (FirebaseBootstrap.employeeExists(username)) {
                    throw new IllegalStateException("That username already exists. Choose another one.");
                }
                FirebaseBootstrap.createOrUpdateEmployee(username, password, role, fullName, email, phone, department);
                return null;
            }

            @Override protected void done() {
                setLoading(false);
                try {
                    get();
                    UiTheme.showSuccess(SIGNUP_FORM.this,
                        "Account created successfully!\n\nRole: " + role + "\nAdmin remains restricted to Firebase only.");
                    goToLogin();
                } catch (Exception ex) {
                    showError("Registration failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void goToLogin() {
        new LOGIN_FORM().setVisible(true);
        dispose();
    }

    private void toggleVisibility(JPasswordField field, JButton btn, int which) {
        if (which == 1) {
            pass1Visible = !pass1Visible;
            field.setEchoChar(pass1Visible ? (char) 0 : '\u2022');
            btn.setText(pass1Visible ? "\uD83D\uDEAB" : "\uD83D\uDC41");
        } else {
            pass2Visible = !pass2Visible;
            field.setEchoChar(pass2Visible ? (char) 0 : '\u2022');
            btn.setText(pass2Visible ? "\uD83D\uDEAB" : "\uD83D\uDC41");
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisible(loading);
        progressBar.setIndeterminate(loading);
        submitBtn.setEnabled(!loading);
        fullNameField.setEnabled(!loading);
        usernameField.setEnabled(!loading);
        emailField.setEnabled(!loading);
        phoneField.setEnabled(!loading);
        departmentBox.setEnabled(!loading);
        roleBox.setEnabled(!loading);
        passwordField.setEnabled(!loading);
        confirmField.setEnabled(!loading);
        if (loading) {
            errorLabel.setText(" ");
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }

    private void styleRoleBox() {
        roleBox.setFont(UiTheme.BODY_FONT);
        roleBox.setBackground(UiTheme.CARD);
        roleBox.setForeground(UiTheme.TEXT);
        roleBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UiTheme.CARD_BORDER, 1, true),
            new EmptyBorder(4, 6, 4, 6)
        ));
        roleBox.setSelectedItem(UserSession.ROLE_LIBRARIAN);
        roleBox.setToolTipText("Admin role is intentionally restricted to Firebase updates only.");
    }

    private void styleDepartmentBox() {
        departmentBox.setFont(UiTheme.BODY_FONT);
        departmentBox.setBackground(UiTheme.CARD);
        departmentBox.setForeground(UiTheme.TEXT);
        departmentBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UiTheme.CARD_BORDER, 1, true),
            new EmptyBorder(4, 6, 4, 6)
        ));
        departmentBox.setSelectedIndex(0);
        departmentBox.setToolTipText("Choose the KIU faculty or school the staff member belongs to.");
    }

    private String selectedComboValue(JComboBox<String> comboBox) {
        Object selected = comboBox.getSelectedItem();
        return selected == null ? "" : selected.toString().trim();
    }

    private JLabel featureLine(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(200, 240, 210));
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
        java.awt.EventQueue.invokeLater(() -> new SIGNUP_FORM().setVisible(true));
    }
}
