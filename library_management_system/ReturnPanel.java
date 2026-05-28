package library_management_system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

public class ReturnPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Search
    private final JTextField fldBookId = UiTheme.makeFormField("Enter Book ID or barcode");
    private final JTextField fldRegNo  = UiTheme.makeFormField("Enter Registration No");

    // Info display
    private final JLabel lblBookTitle   = infoLabel("–");
    private final JLabel lblMemberName  = infoLabel("–");
    private final JLabel lblIssueDate   = infoLabel("–");
    private final JLabel lblDueDate     = infoLabel("–");
    private final JLabel lblDaysOverdue = infoLabel("–");
    private final JLabel lblFine        = infoLabel("–");
    private final JLabel lblRenewals    = infoLabel("–");

    // Return date
    private final JTextField fldReturnDate = UiTheme.makeFormField("YYYY-MM-DD");

    private final JLabel statusLabel = new JLabel(" ");

    // Loaded record
    private Map<String, String> currentRecord = null;
    private long overdueDays = 0;
    private double fineAmount = 0;

    public ReturnPanel() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BACKGROUND);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UiTheme.BACKGROUND);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel title = UiTheme.makeSectionTitle("\uD83D\uDCE5  Return Book");
        title.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel formCard = UiTheme.makeCard();
        formCard.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.weightx = 1;

        // ── Search ────────────────────────────────────────────────────────────
        addSectionHeader(formCard, gbc, 0, "Find Issue Record");

        addRow(formCard, gbc, 1, "Book ID / Barcode *", fldBookId);
        addRow(formCard, gbc, 2, "Registration No *",   fldRegNo);

        JButton searchBtn = UiTheme.makePrimaryButton("\uD83D\uDD0D  Find Record");
        searchBtn.addActionListener(e -> findRecord());
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3; gbc.insets = new Insets(8, 8, 8, 8);
        formCard.add(searchBtn, gbc);
        gbc.gridwidth = 1; gbc.insets = new Insets(6, 8, 6, 8);

        // ── Issue details ─────────────────────────────────────────────────────
        addSectionHeader(formCard, gbc, 4, "Issue Details");

        addInfoRow(formCard, gbc, 5,  "Book Title:",    lblBookTitle);
        addInfoRow(formCard, gbc, 6,  "Member Name:",   lblMemberName);
        addInfoRow(formCard, gbc, 7,  "Issue Date:",    lblIssueDate);
        addInfoRow(formCard, gbc, 8,  "Due Date:",      lblDueDate);
        addInfoRow(formCard, gbc, 9,  "Days Overdue:",  lblDaysOverdue);
        addInfoRow(formCard, gbc, 10, "Fine Amount:",   lblFine);
        addInfoRow(formCard, gbc, 11, "Renewals Used:", lblRenewals);

        // ── Return ────────────────────────────────────────────────────────────
        addSectionHeader(formCard, gbc, 12, "Process Return");

        addRow(formCard, gbc, 13, "Return Date", fldReturnDate);
        fldReturnDate.setText(LocalDate.now().format(DATE_FMT));

        JButton returnBtn = UiTheme.makeSuccessButton("\uD83D\uDCE5  Process Return");
        JButton renewBtn  = UiTheme.makeStyledButton("\uD83D\uDD04  Renew", new Color(34, 91, 184), Color.WHITE);
        JButton clearBtn  = UiTheme.makeSecondaryButton("Clear");
        returnBtn.addActionListener(e -> processReturn());
        renewBtn.addActionListener(e  -> renewBook());
        clearBtn.addActionListener(e  -> clearForm());

        if (!UserSession.canIssueReturn()) {
            returnBtn.setEnabled(false);
            renewBtn.setEnabled(false);
        }

        gbc.gridx = 0; gbc.gridy = 14; gbc.gridwidth = 3; gbc.insets = new Insets(16, 8, 6, 8);
        JPanel btnRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(returnBtn);
        btnRow.add(renewBtn);
        btnRow.add(clearBtn);
        formCard.add(btnRow, gbc);
        gbc.gridwidth = 1;

        gbc.gridy = 15; gbc.insets = new Insets(4, 8, 4, 8);
        statusLabel.setFont(UiTheme.BODY_FONT);
        formCard.add(statusLabel, gbc);

        gbc.gridy = 16; gbc.weighty = 1;
        formCard.add(Box.createVerticalGlue(), gbc);

        root.add(title,    BorderLayout.NORTH);
        root.add(formCard, BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);
    }

    private void addSectionHeader(JPanel panel, GridBagConstraints gbc, int row, String text) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        gbc.insets = new Insets(14, 8, 4, 8);
        JLabel lbl = new JLabel(text);
        lbl.setFont(UiTheme.BODY_FONT.deriveFont(Font.BOLD));
        lbl.setForeground(UiTheme.ACCENT_BLUE);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.CARD_BORDER));
        panel.add(lbl, gbc);
        gbc.gridwidth = 1; gbc.insets = new Insets(6, 8, 6, 8);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(UiTheme.makeFormLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 2;
        panel.add(field, gbc);
        gbc.gridwidth = 1;
    }

    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int row, String label, JLabel value) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(UiTheme.makeFormLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 2;
        panel.add(value, gbc);
        gbc.gridwidth = 1;
    }

    private static JLabel infoLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UiTheme.BODY_FONT);
        lbl.setForeground(UiTheme.MUTED);
        return lbl;
    }

    // ── Find record ───────────────────────────────────────────────────────────

    private void findRecord() {
        String bookIdInput = fldBookId.getText().trim();
        String regNo       = fldRegNo.getText().trim();
        if (bookIdInput.isEmpty() || regNo.isEmpty()) {
            UiTheme.showError(this, "Both Book ID and Registration No are required."); return;
        }
        setStatus("Searching…", UiTheme.MUTED);
        new SwingWorker<Map<String, String>, Void>() {
            @Override protected Map<String, String> doInBackground() throws Exception {
                // Try direct lookup; if not found, resolve barcode → bookId
                Map<String, String> rec = FirebaseBootstrap.getIssueRecord(bookIdInput, regNo);
                if (rec == null) {
                    Map<String, String> copy = FirebaseBootstrap.getBookCopyByBarcode(bookIdInput);
                    if (copy != null) {
                        String resolvedId = copy.getOrDefault("bookId", "");
                        rec = FirebaseBootstrap.getIssueRecord(resolvedId, regNo);
                        if (rec != null) fldBookId.setText(resolvedId);
                    }
                }
                return rec;
            }
            @Override protected void done() {
                try {
                    currentRecord = get();
                    if (currentRecord == null) {
                        setStatus("No active issue record found.", UiTheme.DANGER);
                        clearInfoLabels();
                    } else {
                        // Validate not already returned
                        if (!"yes".equalsIgnoreCase(currentRecord.getOrDefault("issued", ""))) {
                            setStatus("This book has already been returned.", UiTheme.WARNING);
                            populateInfo(currentRecord);
                            currentRecord = null; // prevent re-return
                        } else {
                            populateInfo(currentRecord);
                            setStatus("Record found.", UiTheme.SUCCESS);
                        }
                    }
                } catch (Exception ex) { setStatus("Error: " + ex.getMessage(), UiTheme.DANGER); }
            }
        }.execute();
    }

    private void populateInfo(Map<String, String> record) {
        lblBookTitle.setText(record.getOrDefault("bookName", "–"));
        lblMemberName.setText(record.getOrDefault("studentName", "–"));
        String issueDateStr = record.getOrDefault("issueDate", "");
        lblIssueDate.setText(issueDateStr.isEmpty() ? "–" : issueDateStr);

        // Prefer stored dueDate, fall back to issueDate + borrowingDays
        String dueDateStr = record.getOrDefault("dueDate", "");
        if (dueDateStr.isEmpty() && !issueDateStr.isEmpty()) {
            try {
                dueDateStr = LocalDate.parse(issueDateStr, DATE_FMT)
                    .plusDays(AppSettings.borrowingDays()).format(DATE_FMT);
            } catch (Exception ignored) {}
        }
        lblDueDate.setText(dueDateStr.isEmpty() ? "–" : dueDateStr);

        // Overdue calculation
        overdueDays = 0;
        fineAmount  = 0;
        if (!dueDateStr.isEmpty()) {
            try {
                LocalDate dueDate = LocalDate.parse(dueDateStr, DATE_FMT);
                LocalDate today   = LocalDate.now();
                if (today.isAfter(dueDate)) {
                    overdueDays = ChronoUnit.DAYS.between(dueDate, today);
                    fineAmount  = overdueDays * AppSettings.finePerDay();
                }
            } catch (Exception ignored) {}
        }

        if (overdueDays > 0) {
            lblDaysOverdue.setText(overdueDays + " days");
            lblDaysOverdue.setForeground(UiTheme.DANGER);
            lblFine.setText(String.format("%.2f (@ %.2f/day)", fineAmount, AppSettings.finePerDay()));
            lblFine.setForeground(UiTheme.DANGER);
        } else {
            lblDaysOverdue.setText("None");
            lblDaysOverdue.setForeground(UiTheme.SUCCESS);
            lblFine.setText("No fine");
            lblFine.setForeground(UiTheme.SUCCESS);
        }

        // Renewals
        String renewalCount = record.getOrDefault("renewalCount", "0");
        int limit = AppSettings.renewalLimit();
        lblRenewals.setText(renewalCount + " / " + limit);
        try {
            int used = Integer.parseInt(renewalCount);
            lblRenewals.setForeground(used >= limit ? UiTheme.DANGER : UiTheme.MUTED);
        } catch (NumberFormatException ignored) {}
    }

    // ── Process return ────────────────────────────────────────────────────────

    private void processReturn() {
        if (!UserSession.canIssueReturn()) {
            UiTheme.showWarning(this, "Access denied."); return;
        }
        if (currentRecord == null) {
            UiTheme.showError(this, "Find an active issue record first."); return;
        }
        String bookId     = fldBookId.getText().trim();
        String regNo      = fldRegNo.getText().trim();
        String returnDate = fldReturnDate.getText().trim();
        if (returnDate.isEmpty()) { UiTheme.showError(this, "Return date is required."); return; }

        String bookTitle  = lblBookTitle.getText();
        String memberName = lblMemberName.getText();
        final long od     = overdueDays;
        final double fine = fineAmount;

        setStatus("Processing return…", UiTheme.MUTED);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                FirebaseBootstrap.updateIssueRecordReturn(bookId, regNo, returnDate, "no");

                // Create fine record if overdue
                if (od > 0 && fine > 0) {
                    String fineId = "FINE_" + bookId + "_" + regNo + "_" + System.currentTimeMillis();
                    Map<String, String> fineFields = new HashMap<>();
                    fineFields.put("fineId",     fineId);
                    fineFields.put("issueId",    bookId + "_" + regNo);
                    fineFields.put("memberId",   regNo);
                    fineFields.put("memberName", memberName);
                    fineFields.put("bookId",     bookId);
                    fineFields.put("bookTitle",  bookTitle);
                    fineFields.put("amount",     String.format("%.2f", fine));
                    fineFields.put("reason",     od + " days overdue");
                    fineFields.put("status",     "Pending");
                    fineFields.put("createdAt",  java.time.Instant.now().toString());
                    fineFields.put("paidAt",     "");
                    fineFields.put("waivedAt",   "");
                    fineFields.put("handledBy",  "");
                    FirebaseBootstrap.saveFine(fineFields);
                    AuditService.log(AuditService.ACTION_ADD_FINE, AuditService.MODULE_FINES,
                        "Fine created for member " + regNo + ": " + String.format("%.2f", fine) +
                        " (" + od + " days overdue)");
                }

                // Check for pending reservations on this book
                List<Map<String, String>> pending = FirebaseBootstrap.getPendingReservationsForBook(bookId);
                String reservationNote = pending.isEmpty() ? "" :
                    "\n\n\u26A0\uFE0F " + pending.size() + " pending reservation(s) for this book.";

                AuditService.log(AuditService.ACTION_RETURN_BOOK, AuditService.MODULE_RETURN,
                    "Returned book '" + bookTitle + "' (ID: " + bookId + ") by member " + regNo +
                    ". Return date: " + returnDate + (od > 0 ? ". Overdue: " + od + " days." : "."));

                // Store reservation note for UI
                currentRecord.put("_reservationNote", reservationNote);
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    String reservationNote = currentRecord != null ?
                        currentRecord.getOrDefault("_reservationNote", "") : "";
                    String receipt = "Return processed successfully!\n\n" +
                        "Book: " + bookTitle + "\n" +
                        "Member: " + memberName + "\n" +
                        "Issue Date: " + lblIssueDate.getText() + "\n" +
                        "Return Date: " + returnDate + "\n" +
                        "Fine: " + lblFine.getText() +
                        reservationNote;
                    UiTheme.showSuccess(ReturnPanel.this, receipt);
                    clearForm();
                } catch (Exception ex) { setStatus("Error: " + ex.getMessage(), UiTheme.DANGER); }
            }
        }.execute();
    }

    // ── Renew ─────────────────────────────────────────────────────────────────

    private void renewBook() {
        if (!UserSession.canIssueReturn()) {
            UiTheme.showWarning(this, "Access denied."); return;
        }
        if (currentRecord == null) {
            UiTheme.showError(this, "Find an active issue record first."); return;
        }
        String bookId = fldBookId.getText().trim();
        String regNo  = fldRegNo.getText().trim();

        // Check for pending reservations — block renewal if any
        setStatus("Checking reservations…", UiTheme.MUTED);
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override protected List<Map<String, String>> doInBackground() throws Exception {
                return FirebaseBootstrap.getPendingReservationsForBook(bookId);
            }
            @Override protected void done() {
                try {
                    List<Map<String, String>> pending = get();
                    if (!pending.isEmpty()) {
                        UiTheme.showWarning(ReturnPanel.this,
                            "Cannot renew: " + pending.size() + " member(s) have reserved this book.");
                        setStatus("Renewal blocked — pending reservations.", UiTheme.DANGER);
                        return;
                    }
                    doRenew(bookId, regNo);
                } catch (Exception ex) { setStatus("Error: " + ex.getMessage(), UiTheme.DANGER); }
            }
        }.execute();
    }

    private void doRenew(String bookId, String regNo) {
        int renewalDays = AppSettings.renewalDays();
        String currentDue = lblDueDate.getText();
        String newDueDate;
        try {
            LocalDate base = "–".equals(currentDue) ? LocalDate.now() : LocalDate.parse(currentDue, DATE_FMT);
            newDueDate = base.plusDays(renewalDays).format(DATE_FMT);
        } catch (Exception e) {
            newDueDate = LocalDate.now().plusDays(renewalDays).format(DATE_FMT);
        }
        final String finalNewDue = newDueDate;
        setStatus("Renewing…", UiTheme.MUTED);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                FirebaseBootstrap.renewIssueRecord(bookId, regNo, finalNewDue, UserSession.getUsername());
                AuditService.log(AuditService.ACTION_RENEW_BOOK, AuditService.MODULE_RETURN,
                    "Renewed book ID: " + bookId + " for member " + regNo +
                    ". New due date: " + finalNewDue);
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    UiTheme.showSuccess(ReturnPanel.this,
                        "Book renewed successfully!\nNew due date: " + finalNewDue);
                    // Refresh the record display
                    findRecord();
                } catch (Exception ex) { setStatus("Error: " + ex.getMessage(), UiTheme.DANGER); }
            }
        }.execute();
    }

    private void clearInfoLabels() {
        lblBookTitle.setText("–"); lblMemberName.setText("–");
        lblIssueDate.setText("–"); lblDueDate.setText("–");
        lblDaysOverdue.setText("–"); lblFine.setText("–"); lblRenewals.setText("–");
        lblDaysOverdue.setForeground(UiTheme.MUTED);
        lblFine.setForeground(UiTheme.MUTED);
    }

    private void clearForm() {
        fldBookId.setText(""); fldRegNo.setText("");
        fldReturnDate.setText(LocalDate.now().format(DATE_FMT));
        clearInfoLabels();
        currentRecord = null; overdueDays = 0; fineAmount = 0;
        setStatus(" ", UiTheme.MUTED);
    }

    private void setStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }
}
