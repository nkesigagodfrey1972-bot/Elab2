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

public class SEARCH_BOOKID extends JFrame {

    private final JTextField bookId = new JTextField();
    private final JTextField bookName = new JTextField();
    private final JTextField author = new JTextField();
    private final JTextField category = new JTextField();
    private final JTextField price = new JTextField();

    public SEARCH_BOOKID() {
        initComponents();
        UiTheme.installFrameChrome(this, "Book Lookup", "Find a book by its ID");
    }

    private void initComponents() {
        setTitle("Search Book By ID");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
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

        JPanel buttons = new JPanel(new GridLayout(1, 4, 8, 8));
        JButton searchButton = new JButton("SEARCH");
        JButton saveButton = new JButton("SAVE");
        JButton deleteButton = new JButton("DELETE");
        JButton homeButton = new JButton("HOME");

        searchButton.addActionListener(evt -> searchBook());
        saveButton.addActionListener(evt -> saveBook());
        deleteButton.addActionListener(evt -> deleteBook());
        homeButton.addActionListener(evt -> goHome());

        buttons.add(searchButton);
        buttons.add(saveButton);
        buttons.add(deleteButton);
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
            bookName.setText(book.getOrDefault("bookName", ""));
            author.setText(book.getOrDefault("author", ""));
            category.setText(book.getOrDefault("category", ""));
            price.setText(book.getOrDefault("price", ""));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Search failed: " + ex.getMessage());
        }
    }

    private void saveBook() {
        try {
            FirebaseBootstrap.saveBook(bookId.getText().trim(), bookName.getText().trim(), author.getText().trim(), category.getText().trim(), price.getText().trim());
            JOptionPane.showMessageDialog(this, "Saved successfully");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
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
        author.setText("");
        category.setText("");
        price.setText("");
    }

    private void goHome() {
        new HOME().setVisible(true);
        dispose();
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new SEARCH_BOOKID().setVisible(true));
    }
}
