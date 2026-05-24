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

public class SEARCH_BOOKNAME extends JFrame {

    private final JTextField searchText = new JTextField();
    private final JTextArea results = new JTextArea(14, 32);

    public SEARCH_BOOKNAME() {
        initComponents();
        UiTheme.installFrameChrome(this, "Book Search", "Quick search by book name prefix");
    }

    private void initComponents() {
        setTitle("Search Book By Name");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        results.setEditable(false);

        JPanel top = new JPanel(new GridLayout(1, 2, 8, 8));
        top.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        top.add(new JLabel("BOOK NAME PREFIX"));
        top.add(searchText);

        JPanel buttons = new JPanel(new GridLayout(1, 3, 8, 8));
        JButton searchButton = new JButton("SEARCH");
        JButton clearButton = new JButton("CLEAR");
        JButton homeButton = new JButton("HOME");

        searchButton.addActionListener(evt -> searchBooks());
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

    private void searchBooks() {
        try {
            List<Map<String, String>> books = FirebaseBootstrap.searchBooksByNamePrefix(searchText.getText().trim());
            results.setText(formatBooks(books));
            if (books.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No matching books found");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Search failed: " + ex.getMessage());
        }
    }

    private String formatBooks(List<Map<String, String>> books) {
        StringBuilder builder = new StringBuilder();
        for (Map<String, String> book : books) {
            builder.append("BOOK_ID: ").append(book.getOrDefault("bookId", "")).append('\n');
            builder.append("BOOK_NAME: ").append(book.getOrDefault("bookName", "")).append('\n');
            builder.append("AUTHOR_NAME: ").append(book.getOrDefault("author", "")).append('\n');
            builder.append("CATEGORY: ").append(book.getOrDefault("category", "")).append('\n');
            builder.append("PRICE: ").append(book.getOrDefault("price", "")).append('\n');
            builder.append("------------------------------\n");
        }
        return builder.length() == 0 ? "No data" : builder.toString();
    }

    private void goHome() {
        new HOME().setVisible(true);
        dispose();
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new SEARCH_BOOKNAME().setVisible(true));
    }
}
