package library_management_system;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class YES_NO_RECORD extends JFrame {

    private final JTextField status = new JTextField("yes");
    private final JTextArea results = new JTextArea(14, 32);

    public YES_NO_RECORD() {
        initComponents();
        UiTheme.installFrameChrome(this, "Return Status", "Track issued and returned items");
    }

    private void initComponents() {
        setTitle("Issued / Not Issued Records");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        results.setEditable(false);

        JPanel top = new JPanel(new GridLayout(1, 2, 8, 8));
        top.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        top.add(new JLabel("ISSUED STATUS"));
        top.add(status);

        JPanel buttons = new JPanel(new GridLayout(1, 3, 8, 8));
        JButton searchButton = new JButton("SEARCH");
        JButton clearButton = new JButton("CLEAR");
        JButton homeButton = new JButton("HOME");

        searchButton.addActionListener(evt -> searchRecords());
        clearButton.addActionListener(evt -> results.setText(""));
        homeButton.addActionListener(evt -> goHome());

        buttons.add(searchButton);
        buttons.add(clearButton);
        buttons.add(homeButton);

        setLayout(new BorderLayout(10, 10));
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(results), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    private void searchRecords() {
        try {
            List<Map<String, String>> records = FirebaseBootstrap.searchIssueRecordsByStatus(status.getText().trim());
            results.setText(formatRecords(records));
            if (records.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No matching records found");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Search failed: " + ex.getMessage());
        }
    }

    private String formatRecords(List<Map<String, String>> records) {
        StringBuilder builder = new StringBuilder();
        for (Map<String, String> record : records) {
            builder.append("BOOK_ID: ").append(record.getOrDefault("bookId", "")).append('\n');
            builder.append("BOOK_NAME: ").append(record.getOrDefault("bookName", "")).append('\n');
            builder.append("REGISTRATION_NO: ").append(record.getOrDefault("registrationNo", "")).append('\n');
            builder.append("STUDENT_NAME: ").append(record.getOrDefault("studentName", "")).append('\n');
            builder.append("ISSUE_DATE: ").append(record.getOrDefault("issueDate", "")).append('\n');
            builder.append("RETURN_DATE: ").append(record.getOrDefault("returnDate", "")).append('\n');
            builder.append("ISSUED: ").append(record.getOrDefault("issued", "")).append('\n');
            builder.append("------------------------------\n");
        }
        return builder.length() == 0 ? "No data" : builder.toString();
    }

    private void goHome() {
        new HOME().setVisible(true);
        dispose();
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new YES_NO_RECORD().setVisible(true));
    }
}
