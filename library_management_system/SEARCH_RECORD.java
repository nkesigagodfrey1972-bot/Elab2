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

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(18, 18, 12, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        form.add(new JLabel("BOOK ID"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        bookId.setPreferredSize(new Dimension(220, 28));
        form.add(bookId, gbc);

        java.awt.GridBagConstraints btnGbc = (java.awt.GridBagConstraints) gbc.clone();
        btnGbc.gridx = 2; btnGbc.gridy = 0; btnGbc.weightx = 0.0; btnGbc.fill = GridBagConstraints.NONE;
        JButton genIdBtn = new JButton("Generate");
        genIdBtn.setPreferredSize(new Dimension(110, 28));
        genIdBtn.addActionListener(evt -> {
            try {
                String id = FirebaseBootstrap.generateBookId();
                bookId.setText(id);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not generate Book ID: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        form.add(genIdBtn, btnGbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        form.add(new JLabel("BOOK NAME"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        bookName.setPreferredSize(new Dimension(360, 28));
        form.add(bookName, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        form.add(new JLabel("AUTHOR"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        form.add(authorName, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        form.add(new JLabel("CATEGORY"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        form.add(publisherName, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        form.add(new JLabel("PRICE"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        form.add(quantity, gbc);
        JPanel buttons = new JPanel();
        buttons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 12, 6));
        JButton searchButton = new JButton("Search");
        JButton addButton = new JButton("Add record");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear");
        JButton homeButton = new JButton("Home");

        UiTheme.decorateIconButton(searchButton, "book", UiTheme.ACCENT_BLUE);
        UiTheme.decorateIconButton(addButton, "book", UiTheme.ACCENT);
        UiTheme.decorateIconButton(updateButton, "refresh", UiTheme.ACCENT_BLUE);
        UiTheme.decorateIconButton(deleteButton, "issue", new Color(206, 100, 20));
        UiTheme.decorateIconButton(clearButton, "export", UiTheme.ACCENT_DARK);
        UiTheme.decorateIconButton(homeButton, "home", UiTheme.ACCENT_BLUE);

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
            authorName.setText(book.getOrDefault("author", ""));
            publisherName.setText(book.getOrDefault("category", ""));
            quantity.setText(book.getOrDefault("price", ""));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Search failed: " + ex.getMessage());
        }
    }

    private void addBook() {
        try {
            String id = bookId.getText().trim();
            String name = bookName.getText().trim();
            if (name.isBlank()) {
                JOptionPane.showMessageDialog(this, "Book name is required", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (id.isBlank()) {
                id = FirebaseBootstrap.generateBookId();
                bookId.setText(id);
            }
            FirebaseBootstrap.saveBook(id, name, authorName.getText().trim(), publisherName.getText().trim(), quantity.getText().trim());
            JOptionPane.showMessageDialog(this, "Record added successfully");
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Add failed: " + ex.getMessage());
        }
    }

    private void updateBook() {
        try {
            String id = bookId.getText().trim();
            if (id.isBlank()) {
                JOptionPane.showMessageDialog(this, "Book ID is required to update", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            FirebaseBootstrap.updateBook(id, bookName.getText().trim(), authorName.getText().trim(), publisherName.getText().trim(), quantity.getText().trim());
            JOptionPane.showMessageDialog(this, "Updated successfully");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage());
        }
    }

    private void deleteBook() {
        try {
            String id = bookId.getText().trim();
            if (id.isBlank()) {
                JOptionPane.showMessageDialog(this, "Book ID is required to delete", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            FirebaseBootstrap.deleteBook(id);
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
