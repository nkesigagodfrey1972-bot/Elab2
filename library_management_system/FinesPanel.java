package library_management_system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class FinesPanel extends JPanel {

    private final DefaultTableModel tableModel = new DefaultTableModel(
        new String[]{"Fine ID", "Member", "Book", "Amount", "Reason", "Status", "Created", "Handled By"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

    private final JTextField searchField = UiTheme.makeFormField("Search member, book…");
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "Pending", "Paid", "Waived"});
    private final JLabel statusBar = new JLabel("Ready");
    private final JLabel totalLabel = new JLabel("Total pending: –");

    public FinesPanel() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BACKGROUND);
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(UiTheme.BACKGROUND);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title + summary
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JLabel title = UiTheme.makeSectionTitle("\uD83D\uDCB0  Fine Management");
        titleRow.add(title, BorderLayout.WEST);
        totalLabel.setFont(UiTheme.BODY_FONT);
        totalLabel.setForeground(UiTheme.DANGER);
        titleRow.add(totalLabel, BorderLayout.EAST);

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchBar.setOpaque(false);
        searchField.setPreferredSize(new Dimension(260, 34));
        statusFilter.setPreferredSize(new Dimension(130, 34));
        JButton refreshBtn = UiTheme.makePrimaryButton("\u21BB Refresh");
        refreshBtn.addActionListener(e -> loadData());
        searchBar.add(new JLabel("Search:"));
        searchBar.add(searchField);
        searchBar.add(new JLabel("Status:"));
        searchBar.add(statusFilter);
        searchBar.add(refreshBtn);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        statusFilter.addActionListener(e -> applyFilter());

        // Table
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UiTheme.styleTable(table);
        // Status column colored
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String v = val == null ? "" : val.toString();
                if (!sel) {
                    setBackground(switch (v) {
                        case "Pending" -> new Color(255, 235, 210);
                        case "Paid"    -> new Color(220, 255, 220);
                        case "Waived"  -> new Color(220, 230, 255);
                        default        -> UiTheme.CARD;
                    });
                    setForeground(switch (v) {
                        case "Pending" -> new Color(160, 60, 0);
                        case "Paid"    -> new Color(20, 100, 20);
                        case "Waived"  -> new Color(30, 60, 160);
                        default        -> UiTheme.TEXT;
                    });
                }
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        });
        JScrollPane tableScroll = UiTheme.makeTableScrollPane(table);

        // Action buttons
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionBar.setOpaque(false);
        JButton markPaidBtn   = UiTheme.makeSuccessButton("\u2714 Mark Paid");
        JButton markWaivedBtn = UiTheme.makeStyledButton("\u2716 Waive Fine", new Color(142, 68, 173), Color.WHITE);
        JButton exportBtn     = UiTheme.makePrimaryButton("\uD83D\uDCBE Export CSV");

        markPaidBtn.addActionListener(e   -> updateFineStatus("Paid"));
        markWaivedBtn.addActionListener(e -> updateFineStatus("Waived"));
        exportBtn.addActionListener(e     -> exportCsv());

        if (!UserSession.canManageFines()) {
            markPaidBtn.setEnabled(false);
            markWaivedBtn.setEnabled(false);
        }

        actionBar.add(markPaidBtn);
        actionBar.add(markWaivedBtn);
        actionBar.add(exportBtn);

        statusBar.setFont(UiTheme.SMALL_FONT);
        statusBar.setForeground(UiTheme.MUTED);

        JPanel topSection = new JPanel(new BorderLayout(0, 8));
        topSection.setOpaque(false);
        topSection.add(titleRow,   BorderLayout.NORTH);
        topSection.add(searchBar,  BorderLayout.CENTER);
        topSection.add(actionBar,  BorderLayout.SOUTH);

        root.add(topSection,   BorderLayout.NORTH);
        root.add(tableScroll,  BorderLayout.CENTER);
        root.add(statusBar,    BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);
    }

    private void loadData() {
        statusBar.setText("Loading…");
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override protected List<Map<String, String>> doInBackground() throws Exception {
                return FirebaseBootstrap.listCollectionFull("fines");
            }
            @Override protected void done() {
                try {
                    List<Map<String, String>> fines = get();
                    tableModel.setRowCount(0);
                    double pendingTotal = 0;
                    for (Map<String, String> f : fines) {
                        String status = f.getOrDefault("status", "Pending");
                        if ("Pending".equals(status)) {
                            try { pendingTotal += Double.parseDouble(f.getOrDefault("amount", "0")); }
                            catch (NumberFormatException ignored) {}
                        }
                        tableModel.addRow(new Object[]{
                            f.getOrDefault("fineId",     f.getOrDefault("_id", "")),
                            f.getOrDefault("memberName", f.getOrDefault("memberId", "")),
                            f.getOrDefault("bookTitle",  f.getOrDefault("bookId", "")),
                            f.getOrDefault("amount",     "0"),
                            f.getOrDefault("reason",     ""),
                            status,
                            f.getOrDefault("createdAt",  ""),
                            f.getOrDefault("handledBy",  "")
                        });
                    }
                    totalLabel.setText(String.format("Total pending: %.2f", pendingTotal));
                    statusBar.setText(fines.size() + " fine records loaded.");
                } catch (Exception ex) {
                    statusBar.setText("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void applyFilter() {
        String text   = searchField.getText().trim();
        String status = (String) statusFilter.getSelectedItem();
        RowFilter<DefaultTableModel, Integer> textFilter = text.isEmpty()
            ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text));
        RowFilter<DefaultTableModel, Integer> statusF = new RowFilter<>() {
            @Override public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> e) {
                if ("All".equals(status)) return true;
                return status != null && status.equalsIgnoreCase(e.getStringValue(5));
            }
        };
        if (textFilter == null) {
            sorter.setRowFilter(statusF);
        } else {
            java.util.List<RowFilter<DefaultTableModel, Integer>> filters = new java.util.ArrayList<>();
            filters.add(textFilter);
            filters.add(statusF);
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private void updateFineStatus(String newStatus) {
        if (!UserSession.canManageFines()) {
            UiTheme.showWarning(this, "Access denied. Only Librarians and Admins can manage fines.");
            return;
        }
        int row = table.getSelectedRow();
        if (row < 0) { UiTheme.showError(this, "Select a fine record first."); return; }
        int mr = table.convertRowIndexToModel(row);
        String fineId = tableModel.getValueAt(mr, 0).toString();
        String current = tableModel.getValueAt(mr, 5).toString();
        if (!current.equals("Pending")) {
            UiTheme.showWarning(this, "Only Pending fines can be updated."); return;
        }
        if (!UiTheme.confirm(this, "Mark fine " + fineId + " as " + newStatus + "?")) return;

        statusBar.setText("Updating…");
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                Map<String, String> fine = FirebaseBootstrap.listCollectionFull("fines").stream()
                    .filter(f -> fineId.equals(f.getOrDefault("fineId", f.getOrDefault("_id", ""))))
                    .findFirst().orElse(null);
                if (fine == null) throw new IllegalStateException("Fine not found.");
                fine.put("status",    newStatus);
                fine.put("handledBy", UserSession.getUsername());
                fine.put("fineId",    fineId);
                if ("Paid".equals(newStatus))   fine.put("paidAt",   java.time.Instant.now().toString());
                if ("Waived".equals(newStatus)) fine.put("waivedAt", java.time.Instant.now().toString());
                FirebaseBootstrap.saveFine(fine);
                AuditService.log(
                    "Paid".equals(newStatus) ? AuditService.ACTION_PAY_FINE : AuditService.ACTION_WAIVE_FINE,
                    AuditService.MODULE_FINES,
                    "Fine " + fineId + " marked as " + newStatus,
                    "Pending", newStatus);
                return null;
            }
            @Override protected void done() {
                try { get(); UiTheme.showSuccess(FinesPanel.this, "Fine marked as " + newStatus + "."); loadData(); }
                catch (Exception ex) { UiTheme.showError(FinesPanel.this, ex.getMessage()); statusBar.setText("Error."); }
            }
        }.execute();
    }

    private void exportCsv() {
        if (tableModel.getRowCount() == 0) { UiTheme.showWarning(this, "No data to export."); return; }
        String fname = "fines_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setSelectedFile(new File(AppSettings.exportFolder(), fname));
        if (chooser.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) return;
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                StringBuilder csv = new StringBuilder("Fine ID,Member,Book,Amount,Reason,Status,Created,Handled By\n");
                for (int r = 0; r < tableModel.getRowCount(); r++) {
                    for (int c = 0; c < tableModel.getColumnCount(); c++) {
                        if (c > 0) csv.append(',');
                        csv.append(csvEscape(tableModel.getValueAt(r, c)));
                    }
                    csv.append('\n');
                }
                Files.writeString(chooser.getSelectedFile().toPath(), csv.toString(), StandardCharsets.UTF_8);
                AuditService.log(AuditService.ACTION_EXPORT_REPORT, AuditService.MODULE_FINES, "Exported fines CSV");
                return null;
            }
            @Override protected void done() {
                try { get(); UiTheme.showSuccess(FinesPanel.this, "Exported to: " + chooser.getSelectedFile()); }
                catch (Exception ex) { UiTheme.showError(FinesPanel.this, ex.getMessage()); }
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
