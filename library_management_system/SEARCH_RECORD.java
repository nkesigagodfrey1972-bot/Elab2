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

public class SEARCH_RECORD extends JFrame {

    private final JTextField bookId = new JTextField();
    private final JTextField bookName = new JTextField();
    private final JTextField authorName = new JTextField();
    private final JTextField publisherName = new JTextField();
    private final JTextField quantity = new JTextField();
    public SEARCH_RECORD() {
        initComponents();
        UiTheme.installFrameChrome(this, "Book Catalog", "Search, add, update, and remove titles");
    }

    private void initComponents() {
        setTitle("Book Record");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        form.add(new JLabel("BOOK_ID"));
        form.add(bookId);
        form.add(new JLabel("BOOK_NAME"));
        form.add(bookName);
        form.add(new JLabel("AUTHOR_NAME"));
        form.add(authorName);
        form.add(new JLabel("PUBLISHER_NAME"));
        form.add(publisherName);
        form.add(new JLabel("QUANTITY"));
        form.add(quantity);
        JPanel buttons = new JPanel(new GridLayout(2, 3, 8, 8));
        JButton searchButton = new JButton("SEARCH");
        JButton addButton = new JButton("ADD RECORD");
        JButton updateButton = new JButton("UPDATE");
        JButton deleteButton = new JButton("DELETE");
        JButton clearButton = new JButton("CLEAR");
        JButton homeButton = new JButton("HOME");

        searchButton.addActionListener(evt -> searchBook());
        addButton.addActionListener(evt -> addBook());
        updateButton.addActionListener(evt -> updateBook());
        deleteButton.addActionListener(evt -> deleteBook());
        clearButton.addActionListener(evt -> clearFields());
        homeButton.addActionListener(evt -> goHome());

        buttons.add(searchButton);
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);
        buttons.add(homeButton);

        setLayout(new BorderLayout(10, 10));
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void searchBook() {
        try {
            Map<String, String> book = FirebaseBootstrap.getBook(bookId.getText().trim());
            if (book == null) {
                JOptionPane.showMessageDialog(this, "No record found");
                return;
            }

            bookId.setText(book.getOrDefault("bookId", ""));
            bookName.setText(book.getOrDefault("bookName", ""));
            authorName.setText(book.getOrDefault("authorName", ""));
            publisherName.setText(book.getOrDefault("publisherName", ""));
            quantity.setText(book.getOrDefault("quantity", ""));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Search failed: " + ex.getMessage());
        }
    }

    private void addBook() {
        try {
            FirebaseBootstrap.saveBook(bookId.getText().trim(), bookName.getText().trim(), authorName.getText().trim(), publisherName.getText().trim(), quantity.getText().trim());
            JOptionPane.showMessageDialog(this, "Record added successfully");
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Add failed: " + ex.getMessage());
        }
    }

    private void updateBook() {
        try {
            FirebaseBootstrap.saveBook(bookId.getText().trim(), bookName.getText().trim(), authorName.getText().trim(), publisherName.getText().trim(), quantity.getText().trim());
            JOptionPane.showMessageDialog(this, "Updated successfully");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage());
        }
    }

    private void deleteBook() {
        try {
            FirebaseBootstrap.deleteBook(bookId.getText().trim());
            JOptionPane.showMessageDialog(this, "Deleted successfully");
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage());
        }
    }

    private void clearFields() {
        bookId.setText("");
        bookName.setText("");
        authorName.setText("");
        publisherName.setText("");
        quantity.setText("");
    }

    private void goHome() {
        new HOME().setVisible(true);
        dispose();
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new SEARCH_RECORD().setVisible(true));
    }
}
