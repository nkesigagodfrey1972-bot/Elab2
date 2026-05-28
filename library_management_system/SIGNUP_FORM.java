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
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.WindowEvent;
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
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class SIGNUP_FORM extends JFrame {

    private final JTextField fullNameField = UiTheme.makeFormField("Enter full name");
    private final JTextField usernameField = UiTheme.makeFormField("Choose a username");
    private final JTextField emailField = UiTheme.makeFormField("Enter email address");
    private final JTextField phoneField = UiTheme.makeFormField("Enter phone number");
    private final JComboBox<String> departmentBox = KiuCatalog.createDepartmentCombo();
    private final JComboBox<String> roleBox = new JComboBox<>(new String[] {
        UserSession.ROLE_LIBRARIAN,
        UserSession.ROLE_ASSISTANT,
        UserSession.ROLE_VIEWER
    });
    private final JPasswordField passwordField = new JPasswordField();
    private final JPasswordField confirmField = new JPasswordField();
    private final JButton showHideBtn1 = new JButton("Show");
    private final JButton showHideBtn2 = new JButton("Show");
    private final JButton submitBtn = UiTheme.makePrimaryButton("Create Account");
    private final JButton backBtn = makeTextButton("Back to Login");
    private final JButton exitBtn = makeTextButton("Exit App");
    private final JLabel errorLabel = new JLabel(" ");
    private final JLabel helperLabel = new JLabel("Create a staff account with the correct department and role.");
    private final JLabel strengthLabel = new JLabel("Password strength: Waiting");
    private final JProgressBar progressBar = new JProgressBar();
    private final JProgressBar strengthBar = new JProgressBar(0, 100);
    private boolean pass1Visible = false;
    private boolean pass2Visible = false;

    public SIGNUP_FORM() {
        initComponents();
        bindInteractions();
    }

    private void initComponents() {
        setTitle("Elab Library System - Create Account");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        setSize(1080, 760);
        setLocationRelativeTo(null);
        UiTheme.applyWindowIcon(this);

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBorder(BorderFactory.createLineBorder(new Color(34, 62, 96), 1));
        root.add(buildLeftPanel());
        root.add(buildRightPanel());
        setContentPane(root);
        getRootPane().setDefaultButton(submitBtn);
    }

    private JPanel buildLeftPanel() {
        JPanel panel = new GradientPanel(new Color(16, 84, 116), new Color(47, 137, 101));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(38, 42, 38, 42));

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

        JLabel heading = new JLabel("<html>Create a staff account in a few quick steps.</html>");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 28));
        heading.setForeground(Color.WHITE);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subheading = new JLabel("<html>Enter the staff details, choose a department and role, then save.</html>");
        subheading.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subheading.setForeground(new Color(219, 243, 233));
        subheading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel checklist = new JPanel();
        checklist.setOpaque(false);
        checklist.setLayout(new BoxLayout(checklist, BoxLayout.Y_AXIS));
        checklist.setAlignmentX(Component.LEFT_ALIGNMENT);
        checklist.add(featureLine("Staff details"));
        checklist.add(Box.createVerticalStrut(8));
        checklist.add(featureLine("Department and role"));
        checklist.add(Box.createVerticalStrut(8));
        checklist.add(featureLine("Password setup"));

        JLabel footer = new JLabel("Simple onboarding for the ELAB library team");
        footer.setFont(UiTheme.SMALL_FONT);
        footer.setForeground(new Color(203, 234, 222));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(topBar);
        panel.add(Box.createVerticalStrut(42));
        panel.add(logo);
        panel.add(Box.createVerticalStrut(20));
        panel.add(heading);
        panel.add(Box.createVerticalStrut(14));
        panel.add(subheading);
        panel.add(Box.createVerticalStrut(24));
        panel.add(checklist);
        panel.add(Box.createVerticalGlue());
        panel.add(footer);
        return panel;
    }

    private JPanel buildRightPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(245, 248, 251));
        wrapper.setBorder(new EmptyBorder(28, 26, 28, 26));

        JPanel card = createFormSurface();
        card.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(4, 0, 4, 0);

        JLabel eyebrow = new JLabel("STAFF ACCOUNT SETUP");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 11));
        eyebrow.setForeground(new Color(28, 136, 92));
        gbc.gridy = 0;
        form.add(eyebrow, gbc);

        JLabel heading = new JLabel("Create an account");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 28));
        heading.setForeground(UiTheme.TEXT);
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 0, 8, 0);
        form.add(heading, gbc);

        helperLabel.setFont(UiTheme.BODY_FONT);
        helperLabel.setForeground(UiTheme.MUTED);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 16, 0);
        form.add(helperLabel, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 0, 5, 8);
        addField(form, gbc, 3, 0, "Full Name", fullNameField);
        addField(form, gbc, 3, 1, "Username", usernameField);
        addField(form, gbc, 5, 0, "Email Address", emailField);
        addField(form, gbc, 5, 1, "Phone Number", phoneField);

        styleComboBox(departmentBox, "Choose the KIU faculty or school the staff member belongs to.");
        styleComboBox(roleBox, "Admin is intentionally excluded here and must be assigned in Firebase.");
        addWideField(form, gbc, 7, "Department / Section", departmentBox);
        addWideField(form, gbc, 9, "Role", roleBox);

        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(8, 0, 4, 0);
        form.add(UiTheme.makeFormLabel("Password"), gbc);

        gbc.gridy = 12;
        gbc.insets = new Insets(4, 0, 4, 0);
        form.add(buildPasswordRow(passwordField, showHideBtn1, 1), gbc);

        gbc.gridy = 13;
        strengthLabel.setFont(UiTheme.SMALL_FONT);
        strengthLabel.setForeground(UiTheme.MUTED);
        form.add(strengthLabel, gbc);

        gbc.gridy = 14;
        strengthBar.setValue(0);
        strengthBar.setStringPainted(false);
        strengthBar.setPreferredSize(new Dimension(0, 6));
        strengthBar.setForeground(UiTheme.WARNING);
        form.add(strengthBar, gbc);

        gbc.gridy = 15;
        gbc.insets = new Insets(10, 0, 4, 0);
        form.add(UiTheme.makeFormLabel("Confirm Password"), gbc);

        gbc.gridy = 16;
        gbc.insets = new Insets(4, 0, 4, 0);
        form.add(buildPasswordRow(confirmField, showHideBtn2, 2), gbc);

        gbc.gridy = 17;
        gbc.insets = new Insets(12, 0, 8, 0);
        form.add(buildInfoStrip("Permission note", "New accounts can be Librarian, Assistant Librarian, or Viewer. Promote to Admin later from Firebase if needed."), gbc);

        errorLabel.setFont(UiTheme.SMALL_FONT);
        errorLabel.setForeground(UiTheme.DANGER);
        gbc.gridy = 18;
        gbc.insets = new Insets(2, 0, 2, 0);
        form.add(errorLabel, gbc);

        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(0, 5));
        progressBar.setForeground(UiTheme.SUCCESS);
        gbc.gridy = 19;
        gbc.insets = new Insets(4, 0, 8, 0);
        form.add(progressBar, gbc);

        submitBtn.setPreferredSize(new Dimension(0, 46));
        submitBtn.addActionListener(e -> doSignup());
        gbc.gridy = 20;
        gbc.insets = new Insets(10, 0, 8, 0);
        form.add(submitBtn, gbc);

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        JLabel prompt = new JLabel("Already onboarded?");
        prompt.setFont(UiTheme.BODY_FONT);
        prompt.setForeground(UiTheme.MUTED);
        backBtn.addActionListener(e -> goToLogin());
        exitBtn.addActionListener(e -> exitApplication());
        bottomRow.add(prompt, BorderLayout.WEST);
        JPanel actionButtons = new JPanel(new BorderLayout(6, 0));
        actionButtons.setOpaque(false);
        actionButtons.add(exitBtn, BorderLayout.WEST);
        actionButtons.add(backBtn, BorderLayout.EAST);
        bottomRow.add(actionButtons, BorderLayout.EAST);
        gbc.gridy = 21;
        gbc.insets = new Insets(8, 0, 0, 0);
        form.add(bottomRow, gbc);

        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);

        card.add(scrollPane, BorderLayout.CENTER);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, int column, String label, Component input) {
        GridBagConstraints labelConstraints = (GridBagConstraints) gbc.clone();
        labelConstraints.gridx = column;
        labelConstraints.gridy = row;
        labelConstraints.gridwidth = 1;
        labelConstraints.insets = new Insets(5, column == 0 ? 0 : 8, 5, column == 0 ? 8 : 0);
        panel.add(UiTheme.makeFormLabel(label), labelConstraints);

        GridBagConstraints fieldConstraints = (GridBagConstraints) gbc.clone();
        fieldConstraints.gridx = column;
        fieldConstraints.gridy = row + 1;
        fieldConstraints.gridwidth = 1;
        fieldConstraints.insets = new Insets(4, column == 0 ? 0 : 8, 4, column == 0 ? 8 : 0);
        if (input instanceof JTextField field) {
            field.setPreferredSize(new Dimension(0, 42));
        }
        panel.add(input, fieldConstraints);
    }

    private void addWideField(JPanel panel, GridBagConstraints gbc, int row, String label, Component input) {
        GridBagConstraints labelConstraints = (GridBagConstraints) gbc.clone();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.gridwidth = 2;
        labelConstraints.insets = new Insets(8, 0, 4, 0);
        panel.add(UiTheme.makeFormLabel(label), labelConstraints);

        GridBagConstraints fieldConstraints = (GridBagConstraints) gbc.clone();
        fieldConstraints.gridx = 0;
        fieldConstraints.gridy = row + 1;
        fieldConstraints.gridwidth = 2;
        fieldConstraints.insets = new Insets(4, 0, 4, 0);
        panel.add(input, fieldConstraints);
    }

    private JPanel buildPasswordRow(JPasswordField field, JButton toggleBtn, int which) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        stylePasswordField(field);
        field.setPreferredSize(new Dimension(0, 42));

        toggleBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        toggleBtn.setForeground(UiTheme.ACCENT_BLUE);
        toggleBtn.setFocusPainted(false);
        toggleBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(215, 223, 236), 1, true),
            new EmptyBorder(0, 14, 0, 14)
        ));
        toggleBtn.setBackground(Color.WHITE);
        toggleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleBtn.addActionListener(e -> toggleVisibility(field, toggleBtn, which));

        row.add(field, BorderLayout.CENTER);
        row.add(toggleBtn, BorderLayout.EAST);
        return row;
    }

    private JPanel buildFeatureCard(String title, String text) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setOpaque(true);
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.setBackground(new Color(255, 255, 255, 18));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(Color.WHITE);

        JLabel textLabel = new JLabel("<html>" + text + "</html>");
        textLabel.setFont(UiTheme.BODY_FONT);
        textLabel.setForeground(new Color(224, 242, 234));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(textLabel, BorderLayout.CENTER);
        return card;
    }

    private JLabel featureLine(String text) {
        JLabel label = new JLabel("\u2022 " + text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(224, 242, 234));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JPanel buildInfoStrip(String title, String text) {
        JPanel info = new JPanel(new BorderLayout(0, 4));
        info.setOpaque(true);
        info.setBackground(new Color(239, 249, 243));
        info.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 231, 212), 1, true),
            new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(new Color(28, 136, 92));

        JLabel textLabel = new JLabel("<html>" + text + "</html>");
        textLabel.setFont(UiTheme.BODY_FONT);
        textLabel.setForeground(UiTheme.TEXT);

        info.add(titleLabel, BorderLayout.NORTH);
        info.add(textLabel, BorderLayout.CENTER);
        return info;
    }

    private void bindInteractions() {
        passwordField.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void update(DocumentEvent event) {
                refreshPasswordStrength();
                errorLabel.setText(" ");
            }
        });

        confirmField.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void update(DocumentEvent event) {
                errorLabel.setText(" ");
            }
        });

        usernameField.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void update(DocumentEvent event) {
                String username = usernameField.getText().trim();
                helperLabel.setText(username.isEmpty()
                    ? "Enter the staff details below."
                    : "Preparing account for " + username + ".");
            }
        });
    }

    private void doSignup() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String department = selectedComboValue(departmentBox);
        String role = selectedComboValue(roleBox);
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
        if (department.isBlank()) {
            showError("Please choose a department or school.");
            departmentBox.requestFocus();
            return;
        }
        if (role.isBlank()) {
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
        if (password.length() < 4) {
            showError("Password must be at least 4 characters.");
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

        setLoading(true);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (FirebaseBootstrap.employeeExists(username)) {
                    throw new IllegalStateException("That username already exists. Choose another one.");
                }
                FirebaseBootstrap.createOrUpdateEmployee(username, password, role, fullName, email, phone, department);
                return null;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    get();
                    UiTheme.showSuccess(
                        SIGNUP_FORM.this,
                        "Account created successfully.\n\nRole: " + role + "\nDepartment: " + department
                    );
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

    private void exitApplication() {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        System.exit(0);
    }

    private void toggleVisibility(JPasswordField field, JButton button, int which) {
        if (which == 1) {
            pass1Visible = !pass1Visible;
            field.setEchoChar(pass1Visible ? (char) 0 : '\u2022');
            button.setText(pass1Visible ? "Hide" : "Show");
        } else {
            pass2Visible = !pass2Visible;
            field.setEchoChar(pass2Visible ? (char) 0 : '\u2022');
            button.setText(pass2Visible ? "Hide" : "Show");
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisible(loading);
        progressBar.setIndeterminate(loading);
        submitBtn.setEnabled(!loading);
        backBtn.setEnabled(!loading);
        fullNameField.setEnabled(!loading);
        usernameField.setEnabled(!loading);
        emailField.setEnabled(!loading);
        phoneField.setEnabled(!loading);
        departmentBox.setEnabled(!loading);
        roleBox.setEnabled(!loading);
        passwordField.setEnabled(!loading);
        confirmField.setEnabled(!loading);
        showHideBtn1.setEnabled(!loading);
        showHideBtn2.setEnabled(!loading);
        if (loading) {
            helperLabel.setText("Creating account...");
            errorLabel.setText(" ");
        }
    }

    private void refreshPasswordStrength() {
        String password = new String(passwordField.getPassword());
        int score = 0;
        if (password.length() >= 4) {
            score += 25;
        }
        if (password.length() >= 8) {
            score += 25;
        }
        if (password.matches(".*[A-Z].*")) {
            score += 15;
        }
        if (password.matches(".*[a-z].*")) {
            score += 15;
        }
        if (password.matches(".*\\d.*")) {
            score += 10;
        }
        if (password.matches(".*[^A-Za-z0-9].*")) {
            score += 10;
        }

        strengthBar.setValue(score);
        if (score <= 25) {
            strengthBar.setForeground(UiTheme.DANGER);
            strengthLabel.setText("Password strength: Basic");
        } else if (score <= 55) {
            strengthBar.setForeground(UiTheme.WARNING);
            strengthLabel.setText("Password strength: Fair");
        } else if (score <= 80) {
            strengthBar.setForeground(new Color(54, 136, 219));
            strengthLabel.setText("Password strength: Good");
        } else {
            strengthBar.setForeground(UiTheme.SUCCESS);
            strengthLabel.setText("Password strength: Strong");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private void styleComboBox(JComboBox<String> comboBox, String tooltip) {
        comboBox.setFont(UiTheme.BODY_FONT);
        comboBox.setBackground(UiTheme.CARD);
        comboBox.setForeground(UiTheme.TEXT);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UiTheme.CARD_BORDER, 1, true),
            new EmptyBorder(5, 8, 5, 8)
        ));
        comboBox.setPreferredSize(new Dimension(0, 42));
        comboBox.setToolTipText(tooltip);
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

    private String selectedComboValue(JComboBox<String> comboBox) {
        Object selected = comboBox.getSelectedItem();
        return selected == null ? "" : selected.toString().trim();
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
            new EmptyBorder(24, 24, 24, 24)
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
            g2.fillOval(-60, -40, 220, 220);
            g2.fillOval(getWidth() - 180, 50, 230, 230);
            g2.fillOval(getWidth() - 140, getHeight() - 180, 240, 240);
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
        java.awt.EventQueue.invokeLater(() -> new SIGNUP_FORM().setVisible(true));
    }
}
