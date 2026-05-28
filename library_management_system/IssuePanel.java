package library_management_system;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

public class IssuePanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final JComboBox<String> cmbBookSelect = new JComboBox<>();
    private final JTextField fldBookId = UiTheme.makeFormField("Enter Book ID or barcode");
    private final JLabel lblBookName = infoLabel("-");
    private final JLabel lblBookAuthor = infoLabel("-");
    private final JLabel lblBookStatus = infoLabel("-");

    private final JTextField fldRegNo = UiTheme.makeFormField("Enter Registration No");
    private final JLabel lblMemberName = infoLabel("-");
    private final JLabel lblMemberBranch = infoLabel("-");
    private final JLabel lblPendingFines = infoLabel("-");

    private final JTextField fldIssueDate = UiTheme.makeFormField("YYYY-MM-DD");
    private final JTextField fldDueDate = UiTheme.makeFormField("YYYY-MM-DD");
    private final JLabel statusLabel = new JLabel(" ");

    private Map<String, String> currentBook;
    private Map<String, String> currentMember;

    public IssuePanel() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BACKGROUND);
        buildUI();
        resetDates();
        loadBookOptions();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiTheme.BACKGROUND);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel title = UiTheme.makeSectionTitle("\uD83D\uDCE4  Issue Book");
        title.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel formCard = UiTheme.makeCard();
        formCard.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.weightx = 1;

        addSectionHeader(formCard, gbc, 0, "Step 1 - Find Book");
        styleBookCombo();
        addComboRow(formCard, gbc, 1, "Select Existing Book", cmbBookSelect);
        addRow(formCard, gbc, 2, "Book ID / Barcode *", fldBookId);

        JButton lookupBookBtn = UiTheme.makePrimaryButton("Look Up");
        lookupBookBtn.addActionListener(e -> lookupBook());
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.weightx = 0;
        formCard.add(lookupBookBtn, gbc);
        gbc.weightx = 1;

        addInfoRow(formCard, gbc, 3, "Book Name:", lblBookName);
        addInfoRow(formCard, gbc, 4, "Author:", lblBookAuthor);
        addInfoRow(formCard, gbc, 5, "Status:", lblBookStatus);

        addSectionHeader(formCard, gbc, 6, "Step 2 - Find Member");
        addRow(formCard, gbc, 7, "Registration No *", fldRegNo);

        JButton lookupMemberBtn = UiTheme.makePrimaryButton("Look Up");
        lookupMemberBtn.addActionListener(e -> lookupMember());
        gbc.gridx = 2;
        gbc.gridy = 7;
        gbc.weightx = 0;
        formCard.add(lookupMemberBtn, gbc);
        gbc.weightx = 1;

        addInfoRow(formCard, gbc, 8, "Member Name:", lblMemberName);
        addInfoRow(formCard, gbc, 9, "Branch:", lblMemberBranch);
        addInfoRow(formCard, gbc, 10, "Pending Fines:", lblPendingFines);

        addSectionHeader(formCard, gbc, 11, "Step 3 - Issue Details");
        addRow(formCard, gbc, 12, "Issue Date", fldIssueDate);
        addRow(formCard, gbc, 13, "Due Date", fldDueDate);

        JButton issueBtn = UiTheme.makeSuccessButton("\uD83D\uDCE4  Issue Book");
        JButton clearBtn = UiTheme.makeSecondaryButton("Clear");
        issueBtn.addActionListener(e -> issueBook());
        clearBtn.addActionListener(e -> clearForm());
        if (!UserSession.canIssueReturn()) {
            issueBtn.setEnabled(false);
        }

        gbc.gridx = 0;
        gbc.gridy = 14;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(16, 8, 6, 8);
        JPanel btnRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(issueBtn);
        btnRow.add(clearBtn);
        formCard.add(btnRow, gbc);
        gbc.gridwidth = 1;

        gbc.gridy = 15;
        gbc.insets = new Insets(4, 8, 4, 8);
        statusLabel.setFont(UiTheme.BODY_FONT);
        formCard.add(statusLabel, gbc);

        gbc.gridy = 16;
        gbc.weighty = 1;
        formCard.add(Box.createVerticalGlue(), gbc);

        root.add(title, BorderLayout.NORTH);
        root.add(formCard, BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);
    }

    private void addSectionHeader(JPanel panel, GridBagConstraints gbc, int row, String text) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(14, 8, 4, 8);
        JLabel lbl = new JLabel(text);
        lbl.setFont(UiTheme.BODY_FONT.deriveFont(Font.BOLD));
        lbl.setForeground(UiTheme.ACCENT_BLUE);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.CARD_BORDER));
        panel.add(lbl, gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 8, 6, 8);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(UiTheme.makeFormLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private void addComboRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComboBox<String> comboBox) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(UiTheme.makeFormLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.gridwidth = 2;
        panel.add(comboBox, gbc);
        gbc.gridwidth = 1;
    }

    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int row, String label, JLabel value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(UiTheme.makeFormLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.gridwidth = 2;
        panel.add(value, gbc);
        gbc.gridwidth = 1;
    }

    private static JLabel infoLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UiTheme.BODY_FONT);
        lbl.setForeground(UiTheme.MUTED);
        return lbl;
    }

    private void styleBookCombo() {
        cmbBookSelect.setFont(UiTheme.BODY_FONT);
        cmbBookSelect.setBackground(UiTheme.CARD);
        cmbBookSelect.setForeground(UiTheme.TEXT);
        cmbBookSelect.addActionListener(e -> {
            Object selected = cmbBookSelect.getSelectedItem();
            if (selected == null) {
                return;
            }
            String text = selected.toString();
            if (text.startsWith("Select ")) {
                return;
            }
            int sep = text.indexOf(" - ");
            fldBookId.setText(sep > 0 ? text.substring(0, sep).trim() : text.trim());
        });
    }

    private void loadBookOptions() {
        cmbBookSelect.removeAllItems();
        cmbBookSelect.addItem("Select from existing books...");
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override protected List<Map<String, String>> doInBackground() throws Exception {
                return FirebaseBootstrap.listBooks();
            }

            @Override protected void done() {
                try {
                    for (Map<String, String> book : get()) {
                        String id = book.getOrDefault("bookId", book.getOrDefault("_id", ""));
                        String title = book.getOrDefault("bookName", "");
                        cmbBookSelect.addItem(id + " - " + title);
                    }
                } catch (Exception ignored) {
                    // Manual entry is still available.
                }
            }
        }.execute();
    }

    private void lookupBook() {
        String id = fldBookId.getText().trim();
        if (id.isEmpty()) {
            UiTheme.showError(this, "Enter a Book ID or choose an existing book.");
            return;
        }
        setStatus("Looking up book...", UiTheme.MUTED);
        new SwingWorker<Map<String, String>, Void>() {
            @Override protected Map<String, String> doInBackground() throws Exception {
                Map<String, String> book = FirebaseBootstrap.getBook(id);
                if (book == null) {
                    Map<String, String> copy = FirebaseBootstrap.getBookCopyByBarcode(id);
                    if (copy != null) {
                        book = FirebaseBootstrap.getBook(copy.getOrDefault("bookId", ""));
                        if (book != null) {
                            book.put("_copyId", copy.getOrDefault("copyId", ""));
                        }
                    }
                }
                return book;
            }

            @Override protected void done() {
                try {
                    currentBook = get();
                    if (currentBook == null) {
                        lblBookName.setText("Not found");
                        lblBookAuthor.setText("-");
                        lblBookStatus.setText("-");
                        lblBookStatus.setForeground(UiTheme.DANGER);
                        setStatus("Book not found.", UiTheme.DANGER);
                    } else {
                        lblBookName.setText(currentBook.getOrDefault("bookName", "-"));
                        lblBookAuthor.setText(currentBook.getOrDefault("author", "-"));
                        boolean isIssued = "yes".equalsIgnoreCase(currentBook.getOrDefault("issued", "no"));
                        lblBookStatus.setText(isIssued ? "Issued (not available)" : "Available");
                        lblBookStatus.setForeground(isIssued ? UiTheme.DANGER : UiTheme.SUCCESS);
                        setStatus(isIssued ? "This book is already issued." : "Book found and available.",
                            isIssued ? UiTheme.DANGER : UiTheme.SUCCESS);
                    }
                } catch (Exception ex) {
                    setStatus("Error: " + ex.getMessage(), UiTheme.DANGER);
                }
            }
        }.execute();
    }

    private void lookupMember() {
        String regNo = fldRegNo.getText().trim();
        if (regNo.isEmpty()) {
            UiTheme.showError(this, "Enter a Registration No.");
            return;
        }
        setStatus("Looking up member...", UiTheme.MUTED);
        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() throws Exception {
                Map<String, String> member = FirebaseBootstrap.getStudent(regNo);
                double pendingFines = member != null ? FirebaseBootstrap.getPendingFineTotal(regNo) : 0;
                return new Object[]{member, pendingFines};
            }

            @Override protected void done() {
                try {
                    Object[] result = get();
                    currentMember = (Map<String, String>) result[0];
                    double fines = (double) result[1];
                    if (currentMember == null) {
                        lblMemberName.setText("Not found");
                        lblMemberBranch.setText("-");
                        lblPendingFines.setText("-");
                        setStatus("Member not found.", UiTheme.DANGER);
                    } else {
                        lblMemberName.setText(currentMember.getOrDefault("studentName", "-"));
                        lblMemberBranch.setText(currentMember.getOrDefault("branch", "-"));
                        if (fines > 0) {
                            lblPendingFines.setText(String.format("%.2f (unpaid)", fines));
                            lblPendingFines.setForeground(UiTheme.DANGER);
                        } else {
                            lblPendingFines.setText("None");
                            lblPendingFines.setForeground(UiTheme.SUCCESS);
                        }
                        setStatus("Member found.", UiTheme.SUCCESS);
                    }
                } catch (Exception ex) {
                    setStatus("Error: " + ex.getMessage(), UiTheme.DANGER);
                }
            }
        }.execute();
    }

    private void issueBook() {
        if (!UserSession.canIssueReturn()) {
            UiTheme.showWarning(this, "Access denied. You do not have permission to issue books.");
            return;
        }
        String bookId = fldBookId.getText().trim();
        String regNo = fldRegNo.getText().trim();
        if (bookId.isEmpty()) {
            UiTheme.showError(this, "Book ID is required.");
            return;
        }
        if (regNo.isEmpty()) {
            UiTheme.showError(this, "Registration No is required.");
            return;
        }
        if (currentBook == null || "Not found".equals(lblBookName.getText())) {
            UiTheme.showError(this, "Look up the book first.");
            return;
        }
        if ("yes".equalsIgnoreCase(currentBook.getOrDefault("issued", "no"))) {
            UiTheme.showError(this, "This book is already issued.");
            return;
        }
        if (currentMember == null || "Not found".equals(lblMemberName.getText())) {
            UiTheme.showError(this, "Look up the member first.");
            return;
        }
        if (AppSettings.blockOnPendingFines()) {
            String fineText = lblPendingFines.getText();
            if (!"None".equals(fineText) && !"-".equals(fineText) && !fineText.isEmpty()) {
                UiTheme.showError(this, "Member has unpaid fines (" + fineText + ").\nPlease clear fines before issuing books.");
                return;
            }
        }

        String issueDate = fldIssueDate.getText().trim();
        String dueDate = fldDueDate.getText().trim();
        String bookName = lblBookName.getText();
        String memberName = lblMemberName.getText();
        String author = lblBookAuthor.getText();
        String actualBookId = currentBook.getOrDefault("bookId", currentBook.getOrDefault("_id", bookId));

        setStatus("Issuing book...", UiTheme.MUTED);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                Map<String, String> fields = new HashMap<>();
                fields.put("bookId", actualBookId);
                fields.put("bookName", bookName);
                fields.put("author", author);
                fields.put("category", currentBook.getOrDefault("category", ""));
                fields.put("price", currentBook.getOrDefault("price", ""));
                fields.put("registrationNo", regNo);
                fields.put("studentName", memberName);
                fields.put("issueDate", issueDate);
                fields.put("dueDate", dueDate);
                fields.put("returnDate", "");
                fields.put("issued", "yes");
                fields.put("renewalCount", "0");
                fields.put("renewedAt", "");
                fields.put("renewedBy", "");
                FirebaseBootstrap.saveIssueRecordFull(fields);
                AuditService.log(AuditService.ACTION_ISSUE_BOOK, AuditService.MODULE_ISSUE,
                    "Issued book '" + bookName + "' (ID: " + actualBookId + ") to member " + regNo +
                    " (" + memberName + "). Due: " + dueDate);
                return null;
            }

            @Override protected void done() {
                try {
                    get();
                    UiTheme.showSuccess(IssuePanel.this,
                        "Book issued successfully!\n\n" +
                        "Book: " + bookName + "\n" +
                        "Member: " + memberName + "\n" +
                        "Issue Date: " + issueDate + "\n" +
                        "Due Date: " + dueDate);
                    clearForm();
                } catch (Exception ex) {
                    setStatus("Error: " + ex.getMessage(), UiTheme.DANGER);
                }
            }
        }.execute();
    }

    private void resetDates() {
        LocalDate today = LocalDate.now();
        int borrowDays = AppSettings.borrowingDays();
        fldIssueDate.setText(today.format(DATE_FMT));
        fldDueDate.setText(today.plusDays(borrowDays).format(DATE_FMT));
    }

    private void clearForm() {
        fldBookId.setText("");
        fldRegNo.setText("");
        if (cmbBookSelect.getItemCount() > 0) {
            cmbBookSelect.setSelectedIndex(0);
        }
        lblBookName.setText("-");
        lblBookAuthor.setText("-");
        lblBookStatus.setText("-");
        lblMemberName.setText("-");
        lblMemberBranch.setText("-");
        lblPendingFines.setText("-");
        lblBookStatus.setForeground(UiTheme.MUTED);
        lblPendingFines.setForeground(UiTheme.MUTED);
        currentBook = null;
        currentMember = null;
        resetDates();
        setStatus(" ", UiTheme.MUTED);
    }

    private void setStatus(String msg, java.awt.Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }
}
