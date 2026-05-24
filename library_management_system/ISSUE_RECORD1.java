package library_management_system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ISSUE_RECORD1 extends JFrame {

    private final JTextField bookId = new JTextField();
    private final JTextField bookName = new JTextField();
    private final JTextField author = new JTextField();
    private final JTextField category = new JTextField();
    private final JTextField price = new JTextField();
    private final JTextField registrationNo = new JTextField();
    private final JTextField studentName = new JTextField();
    private final JTextField issueDate = new JTextField();
    private final JTextField issued = new JTextField("yes");
    private final JTextField returnDate = new JTextField();

    public ISSUE_RECORD1() {
        initComponents();
        UiTheme.installFrameChrome(this, "Issue Desk", "Issue books and maintain return status");
    }

    private void initComponents() {
        setTitle("Issue Record");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(18, 18, 12, 18));
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        form.add(new JLabel("BOOK ID"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        bookId.setPreferredSize(new Dimension(220, 28));
        form.add(bookId, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        form.add(new JLabel("BOOK NAME"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        bookName.setPreferredSize(new Dimension(360, 28));
        form.add(bookName, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        form.add(new JLabel("AUTHOR"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        form.add(author, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        form.add(new JLabel("CATEGORY"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        form.add(category, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        form.add(new JLabel("PRICE"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        form.add(price, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        form.add(new JLabel("REGISTRATION NO"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 1.0;
        form.add(registrationNo, gbc);

        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.0;
        form.add(new JLabel("STUDENT NAME"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; gbc.weightx = 1.0;
        form.add(studentName, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0.0;
        form.add(new JLabel("ISSUE DATE"), gbc);
        gbc.gridx = 1; gbc.gridy = 7; gbc.weightx = 1.0;
        form.add(issueDate, gbc);

        gbc.gridx = 0; gbc.gridy = 8; gbc.weightx = 0.0;
        form.add(new JLabel("RETURN DATE"), gbc);
        gbc.gridx = 1; gbc.gridy = 8; gbc.weightx = 1.0;
        form.add(returnDate, gbc);

        gbc.gridx = 0; gbc.gridy = 9; gbc.weightx = 0.0;
        form.add(new JLabel("ISSUED"), gbc);
        gbc.gridx = 1; gbc.gridy = 9; gbc.weightx = 1.0;
        form.add(issued, gbc);

        JPanel buttons = new JPanel();
        buttons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 12, 6));
        JButton searchButton = new JButton("Search");
        JButton issueButton = new JButton("Issue");
        JButton returnButton = new JButton("Return");
        JButton clearButton = new JButton("Clear");
        JButton homeButton = new JButton("Home");
        UiTheme.decorateIconButton(searchButton, "book", UiTheme.ACCENT_BLUE);
        UiTheme.decorateIconButton(issueButton, "issue", UiTheme.ACCENT);
        UiTheme.decorateIconButton(returnButton, "refresh", UiTheme.ACCENT_BLUE);
        UiTheme.decorateIconButton(clearButton, "export", UiTheme.ACCENT_DARK);
        UiTheme.decorateIconButton(homeButton, "home", UiTheme.ACCENT_BLUE);

        searchButton.addActionListener(evt -> searchIssueRecord());
        issueButton.addActionListener(evt -> saveIssueRecord());
        returnButton.addActionListener(evt -> updateReturn());
        clearButton.addActionListener(evt -> clearFields());
        homeButton.addActionListener(evt -> goHome());

        buttons.add(searchButton);
        buttons.add(issueButton);
        buttons.add(returnButton);
        buttons.add(clearButton);
        buttons.add(homeButton);

        setLayout(new BorderLayout(10, 10));
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    private void searchIssueRecord() {
        try {
            Map<String, String> record = FirebaseBootstrap.getIssueRecord(bookId.getText().trim(), registrationNo.getText().trim());
            if (record == null) {
                JOptionPane.showMessageDialog(this, "No record found");
                return;
            }
            bookName.setText(record.getOrDefault("bookName", ""));
            author.setText(record.getOrDefault("author", ""));
            category.setText(record.getOrDefault("category", ""));
            price.setText(record.getOrDefault("price", ""));
            studentName.setText(record.getOrDefault("studentName", ""));
            issueDate.setText(record.getOrDefault("issueDate", ""));
            returnDate.setText(record.getOrDefault("returnDate", ""));
            issued.setText(record.getOrDefault("issued", ""));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Search failed: " + ex.getMessage());
        }
    }

    private void saveIssueRecord() {
        try {
            String bId = bookId.getText().trim();
            String reg = registrationNo.getText().trim();
            if (bId.isBlank()) {
                JOptionPane.showMessageDialog(this, "Book ID is required", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (reg.isBlank()) {
                JOptionPane.showMessageDialog(this, "Registration number is required", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // prevent issuing a book that's already issued
            try {
                Map<String, String> book = FirebaseBootstrap.getBook(bId);
                if (book != null && "yes".equalsIgnoreCase(book.getOrDefault("issued", "no"))) {
                    JOptionPane.showMessageDialog(this, "This book appears to be already issued.", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (Exception ignore) {
                // if we cannot check, proceed and let server-side errors surface
            }

            FirebaseBootstrap.saveIssueRecord(bId, bookName.getText().trim(), author.getText().trim(), category.getText().trim(), price.getText().trim(), reg, studentName.getText().trim(), issueDate.getText().trim(), issued.getText().trim());
            JOptionPane.showMessageDialog(this, "Issue record saved");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
        }
    }

    private void updateReturn() {
        try {
            String bId = bookId.getText().trim();
            String reg = registrationNo.getText().trim();
            if (bId.isBlank() || reg.isBlank()) {
                JOptionPane.showMessageDialog(this, "Book ID and registration number are required to update return", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            FirebaseBootstrap.updateIssueRecordReturn(bId, reg, returnDate.getText().trim(), issued.getText().trim());
            JOptionPane.showMessageDialog(this, "Return updated");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage());
        }
    }

    private void clearFields() {
        bookId.setText("");
        bookName.setText("");
        author.setText("");
        category.setText("");
        price.setText("");
        registrationNo.setText("");
        studentName.setText("");
        issueDate.setText("");
        returnDate.setText("");
        issued.setText("yes");
    }

    private void goHome() {
        new HOME().setVisible(true);
        dispose();
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new ISSUE_RECORD1().setVisible(true));
    }
}
