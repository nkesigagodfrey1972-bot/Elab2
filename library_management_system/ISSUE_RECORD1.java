package library_management_system;

import java.awt.BorderLayout;
import java.awt.GridLayout;
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

        JPanel form = new JPanel(new GridLayout(10, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        form.add(new JLabel("BOOK_ID"));
        form.add(bookId);
        form.add(new JLabel("BOOK_NAME"));
        form.add(bookName);
        form.add(new JLabel("AUTHOR_NAME"));
        form.add(author);
        form.add(new JLabel("CATEGORY"));
        form.add(category);
        form.add(new JLabel("PRICE"));
        form.add(price);
        form.add(new JLabel("REGISTRATION_NO"));
        form.add(registrationNo);
        form.add(new JLabel("STUDENT_NAME"));
        form.add(studentName);
        form.add(new JLabel("ISSUE_DATE"));
        form.add(issueDate);
        form.add(new JLabel("RETURN_DATE"));
        form.add(returnDate);
        form.add(new JLabel("ISSUED"));
        form.add(issued);

        JPanel buttons = new JPanel(new GridLayout(1, 5, 8, 8));
        JButton searchButton = new JButton("SEARCH");
        JButton issueButton = new JButton("ISSUE");
        JButton returnButton = new JButton("RETURN");
        JButton clearButton = new JButton("CLEAR");
        JButton homeButton = new JButton("HOME");

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
            FirebaseBootstrap.saveIssueRecord(bookId.getText().trim(), bookName.getText().trim(), author.getText().trim(), category.getText().trim(), price.getText().trim(), registrationNo.getText().trim(), studentName.getText().trim(), issueDate.getText().trim(), issued.getText().trim());
            JOptionPane.showMessageDialog(this, "Issue record saved");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
        }
    }

    private void updateReturn() {
        try {
            FirebaseBootstrap.updateIssueRecordReturn(bookId.getText().trim(), registrationNo.getText().trim(), returnDate.getText().trim(), issued.getText().trim());
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
