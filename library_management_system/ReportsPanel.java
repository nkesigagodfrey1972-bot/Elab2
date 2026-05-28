package library_management_system;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class ReportsPanel extends JPanel {

    private static final String[] REPORT_TYPES = {
        "All Books",
        "Available Books",
        "All Members",
        "All Issue Records",
        "Overdue Books",
        "Pending Fines",
        "Paid / Waived Fines",
        "Pending Reservations",
        "All Reservations",
        "Audit Logs"
    };

    private final JRadioButton[] radioButtons = new JRadioButton[REPORT_TYPES.length];
    private final DefaultTableModel tableModel = new DefaultTableModel() {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable previewTable = new JTable(tableModel);
    private final JLabel summaryLabel = new JLabel("Select a report type and click Generate.");
    private final JLabel statusBar    = new JLabel("Ready");

    public ReportsPanel() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BACKGROUND);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(UiTheme.BACKGROUND);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = UiTheme.makeSectionTitle("\uD83D\uDCCA  Reports");
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        // Report type selector — 2 rows of 5
        JPanel typeCard = UiTheme.makeCard();
        typeCard.setLayout(new BorderLayout(0, 10));
        typeCard.add(UiTheme.makeFormLabel("Select Report Type:"), BorderLayout.NORTH);

        JPanel radioPanel = new JPanel(new GridLayout(2, 5, 10, 6));
        radioPanel.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < REPORT_TYPES.length; i++) {
            radioButtons[i] = new JRadioButton(REPORT_TYPES[i]);
            radioButtons[i].setFont(UiTheme.BODY_FONT);
            radioButtons[i].setOpaque(false);
            group.add(radioButtons[i]);
            radioPanel.add(radioButtons[i]);
        }
        radioButtons[0].setSelected(true);
        typeCard.add(radioPanel, BorderLayout.CENTER);

        // Action buttons
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionBar.setOpaque(false);
        JButton generateBtn = UiTheme.makePrimaryButton("\uD83D\uDD04 Generate Preview");
        JButton exportBtn   = UiTheme.makeSuccessButton("\uD83D\uDCBE Export to CSV");
        generateBtn.addActionListener(e -> generateReport());
        exportBtn.addActionListener(e   -> exportReport());
        actionBar.add(generateBtn);
        actionBar.add(exportBtn);

        summaryLabel.setFont(UiTheme.BODY_FONT);
        summaryLabel.setForeground(UiTheme.MUTED);
        summaryLabel.setBorder(new EmptyBorder(4, 0, 4, 0));

        UiTheme.styleTable(previewTable);
        JScrollPane tableScroll = UiTheme.makeTableScrollPane(previewTable);

        statusBar.setFont(UiTheme.SMALL_FONT);
        statusBar.setForeground(UiTheme.MUTED);

        JPanel topSection = new JPanel(new BorderLayout(0, 10));
        topSection.setOpaque(false);
        topSection.add(title,     BorderLayout.NORTH);
        topSection.add(typeCard,  BorderLayout.CENTER);
        topSection.add(actionBar, BorderLayout.SOUTH);

        JPanel centerSection = new JPanel(new BorderLayout(0, 8));
        centerSection.setOpaque(false);
        centerSection.add(summaryLabel, BorderLayout.NORTH);
        centerSection.add(tableScroll,  BorderLayout.CENTER);
        centerSection.add(statusBar,    BorderLayout.SOUTH);

        root.add(topSection,    BorderLayout.NORTH);
        root.add(centerSection, BorderLayout.CENTER);

        add(root, BorderLayout.CENTER);
    }

    private String getSelectedReportType() {
        for (int i = 0; i < radioButtons.length; i++) {
            if (radioButtons[i].isSelected()) return REPORT_TYPES[i];
        }
        return REPORT_TYPES[0];
    }

    private void generateReport() {
        String type = getSelectedReportType();
        statusBar.setText("Loading " + type + "…");
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override protected List<Map<String, String>> doInBackground() throws Exception {
                return fetchData(type);
            }
            @Override protected void done() {
                try {
                    List<Map<String, String>> data = get();
                    populateTable(type, data);
                    summaryLabel.setText(type + ": " + data.size() + " records");
                    statusBar.setText("Preview loaded — " + data.size() + " rows.");
                } catch (Exception ex) {
                    statusBar.setText("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private List<Map<String, String>> fetchData(String type) throws Exception {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        int borrowDays = AppSettings.borrowingDays();

        return switch (type) {
            case "All Books"        -> FirebaseBootstrap.listBooks();
            case "Available Books"  -> {
                List<Map<String, String>> avail = new ArrayList<>();
                for (Map<String, String> b : FirebaseBootstrap.listBooks())
                    if (!"yes".equalsIgnoreCase(b.getOrDefault("issued", ""))) avail.add(b);
                yield avail;
            }
            case "All Members"      -> FirebaseBootstrap.listStudents();
            case "All Issue Records"-> FirebaseBootstrap.listIssueRecords();
            case "Overdue Books"    -> {
                List<Map<String, String>> overdue = new ArrayList<>();
                for (Map<String, String> r : FirebaseBootstrap.listIssueRecords()) {
                    if (!"yes".equalsIgnoreCase(r.getOrDefault("issued", ""))) continue;
                    String ds = r.getOrDefault("issueDate", "");
                    if (ds.isBlank()) continue;
                    try { if (LocalDate.parse(ds, fmt).plusDays(borrowDays).isBefore(today)) overdue.add(r); }
                    catch (Exception ignored) {}
                }
                yield overdue;
            }
            case "Pending Fines"    -> {
                List<Map<String, String>> pending = new ArrayList<>();
                for (Map<String, String> f : FirebaseBootstrap.listCollectionFull("fines"))
                    if ("Pending".equalsIgnoreCase(f.getOrDefault("status", ""))) pending.add(f);
                yield pending;
            }
            case "Paid / Waived Fines" -> {
                List<Map<String, String>> done2 = new ArrayList<>();
                for (Map<String, String> f : FirebaseBootstrap.listCollectionFull("fines")) {
                    String s = f.getOrDefault("status", "");
                    if ("Paid".equalsIgnoreCase(s) || "Waived".equalsIgnoreCase(s)) done2.add(f);
                }
                yield done2;
            }
            case "Pending Reservations" -> {
                List<Map<String, String>> pres = new ArrayList<>();
                for (Map<String, String> r : FirebaseBootstrap.listCollectionFull("reservations"))
                    if ("Pending".equalsIgnoreCase(r.getOrDefault("status", ""))) pres.add(r);
                yield pres;
            }
            case "All Reservations" -> FirebaseBootstrap.listCollectionFull("reservations");
            case "Audit Logs"       -> {
                if (!UserSession.canViewAuditLogs()) yield new ArrayList<>();
                List<Map<String, String>> logs = FirebaseBootstrap.listCollectionFull("auditLogs");
                logs.sort((a, b) -> b.getOrDefault("timestamp", "").compareTo(a.getOrDefault("timestamp", "")));
                yield logs;
            }
            default -> new ArrayList<>();
        };
    }

    private void populateTable(String type, List<Map<String, String>> data) {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        String[] cols = switch (type) {
            case "All Books", "Available Books" ->
                new String[]{"Book ID", "Title", "Author", "Category", "Price", "Status"};
            case "All Members" ->
                new String[]{"Reg No", "Full Name", "Mobile", "Branch"};
            case "All Issue Records", "Overdue Books" ->
                new String[]{"Book ID", "Book Name", "Reg No", "Student", "Issue Date", "Due Date", "Return Date", "Status", "Renewals"};
            case "Pending Fines", "Paid / Waived Fines" ->
                new String[]{"Fine ID", "Member", "Book", "Amount", "Reason", "Status", "Created", "Handled By"};
            case "Pending Reservations", "All Reservations" ->
                new String[]{"Reservation ID", "Book ID", "Book Title", "Member ID", "Member Name", "Reserved On", "Expires", "Status", "Queue"};
            case "Audit Logs" ->
                new String[]{"Timestamp", "User", "Role", "Action", "Module", "Description"};
            default -> new String[]{};
        };

        for (String col : cols) tableModel.addColumn(col);

        for (Map<String, String> row : data) {
            Object[] rowData = switch (type) {
                case "All Books", "Available Books" -> new Object[]{
                    row.getOrDefault("bookId",   row.getOrDefault("_id", "")),
                    row.getOrDefault("bookName", ""),
                    row.getOrDefault("author",   ""),
                    row.getOrDefault("category", ""),
                    row.getOrDefault("price",    ""),
                    "yes".equalsIgnoreCase(row.getOrDefault("issued", "")) ? "Issued" : "Available"
                };
                case "All Members" -> new Object[]{
                    row.getOrDefault("registrationNo", row.getOrDefault("_id", "")),
                    row.getOrDefault("studentName", ""),
                    row.getOrDefault("mobileNo",    ""),
                    row.getOrDefault("branch",      "")
                };
                case "All Issue Records", "Overdue Books" -> new Object[]{
                    row.getOrDefault("bookId",         row.getOrDefault("_id", "")),
                    row.getOrDefault("bookName",       ""),
                    row.getOrDefault("registrationNo", ""),
                    row.getOrDefault("studentName",    ""),
                    row.getOrDefault("issueDate",      ""),
                    row.getOrDefault("dueDate",        ""),
                    row.getOrDefault("returnDate",     ""),
                    "yes".equalsIgnoreCase(row.getOrDefault("issued", "")) ? "Issued" : "Returned",
                    row.getOrDefault("renewalCount",   "0")
                };
                case "Pending Fines", "Paid / Waived Fines" -> new Object[]{
                    row.getOrDefault("fineId",     row.getOrDefault("_id", "")),
                    row.getOrDefault("memberName", row.getOrDefault("memberId", "")),
                    row.getOrDefault("bookTitle",  row.getOrDefault("bookId", "")),
                    row.getOrDefault("amount",     ""),
                    row.getOrDefault("reason",     ""),
                    row.getOrDefault("status",     ""),
                    row.getOrDefault("createdAt",  ""),
                    row.getOrDefault("handledBy",  "")
                };
                case "Pending Reservations", "All Reservations" -> new Object[]{
                    row.getOrDefault("reservationId",  row.getOrDefault("_id", "")),
                    row.getOrDefault("bookId",         ""),
                    row.getOrDefault("bookTitle",      ""),
                    row.getOrDefault("memberId",       ""),
                    row.getOrDefault("memberName",     ""),
                    row.getOrDefault("reservationDate",""),
                    row.getOrDefault("expiryDate",     ""),
                    row.getOrDefault("status",         ""),
                    row.getOrDefault("queuePosition",  "")
                };
                case "Audit Logs" -> new Object[]{
                    row.getOrDefault("timestamp",   ""),
                    row.getOrDefault("userName",    ""),
                    row.getOrDefault("userRole",    ""),
                    row.getOrDefault("action",      ""),
                    row.getOrDefault("module",      ""),
                    row.getOrDefault("description", "")
                };
                default -> new Object[]{};
            };
            tableModel.addRow(rowData);
        }
        UiTheme.styleTable(previewTable);
    }

    private void exportReport() {
        String type = getSelectedReportType();
        if (tableModel.getRowCount() == 0) {
            UiTheme.showWarning(this, "Generate a report preview first."); return;
        }

        String slug = type.toLowerCase()
            .replace(" / ", "_").replace(" ", "_").replace("/", "_");
        String defaultName = slug + "_" +
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(AppSettings.exportFolder(), defaultName));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                StringBuilder csv = new StringBuilder();
                // Header
                for (int c = 0; c < tableModel.getColumnCount(); c++) {
                    if (c > 0) csv.append(',');
                    csv.append(csvEscape(tableModel.getColumnName(c)));
                }
                csv.append('\n');
                // Rows from current preview
                for (int r = 0; r < tableModel.getRowCount(); r++) {
                    for (int c = 0; c < tableModel.getColumnCount(); c++) {
                        if (c > 0) csv.append(',');
                        csv.append(csvEscape(tableModel.getValueAt(r, c)));
                    }
                    csv.append('\n');
                }
                Files.writeString(chooser.getSelectedFile().toPath(), csv.toString(), StandardCharsets.UTF_8);
                AuditService.log(AuditService.ACTION_EXPORT_REPORT, AuditService.MODULE_REPORTS,
                    "Exported report: " + type + " → " + chooser.getSelectedFile().getName());
                return null;
            }
            @Override protected void done() {
                try { get(); UiTheme.showSuccess(ReportsPanel.this, "Exported to:\n" + chooser.getSelectedFile()); }
                catch (Exception ex) { UiTheme.showError(ReportsPanel.this, ex.getMessage()); }
            }
        }.execute();
    }

    private String csvEscape(Object v) {
        String s = v == null ? "" : v.toString();
        if (s.contains(",") || s.contains("\"") || s.contains("\n"))
            return '"' + s.replace("\"", "\"\"") + '"';
        return s;
    }
}
