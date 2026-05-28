package library_management_system;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class AuditLogsPanel extends JPanel {

    private final DefaultTableModel tableModel = new DefaultTableModel(
        new String[]{"Timestamp", "User", "Role", "Action", "Module", "Description", "Old Value", "New Value"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

    private final JTextField searchField = UiTheme.makeFormField("Search user, action, module…");
    private final JComboBox<String> moduleFilter = new JComboBox<>(new String[]{
        "All", "Authentication", "Books", "Members", "Issue", "Return",
        "Fines", "Reservations", "Reports", "Settings", "BookCopies"
    });
    private final JLabel statusBar = new JLabel("Ready");

    public AuditLogsPanel() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BACKGROUND);
        buildUI();
        if (UserSession.canViewAuditLogs()) loadData();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(UiTheme.BACKGROUND);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = UiTheme.makeSectionTitle("\uD83D\uDDD2  Audit Logs");
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchBar.setOpaque(false);
        searchField.setPreferredSize(new Dimension(280, 34));
        moduleFilter.setPreferredSize(new Dimension(160, 34));
        JButton refreshBtn = UiTheme.makePrimaryButton("\u21BB Refresh");
        JButton exportBtn  = UiTheme.makeSuccessButton("\uD83D\uDCBE Export CSV");
        refreshBtn.addActionListener(e -> loadData());
        exportBtn.addActionListener(e  -> exportCsv());
        searchBar.add(new JLabel("Search:"));
        searchBar.add(searchField);
        searchBar.add(new JLabel("Module:"));
        searchBar.add(moduleFilter);
        searchBar.add(refreshBtn);
        searchBar.add(exportBtn);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        moduleFilter.addActionListener(e -> applyFilter());

        // Table
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UiTheme.styleTable(table);
        // Widen description column
        table.getColumnModel().getColumn(5).setPreferredWidth(260);
        JScrollPane tableScroll = UiTheme.makeTableScrollPane(table);

        statusBar.setFont(UiTheme.SMALL_FONT);
        statusBar.setForeground(UiTheme.MUTED);

        JPanel topSection = new JPanel(new BorderLayout(0, 8));
        topSection.setOpaque(false);
        topSection.add(title,     BorderLayout.NORTH);
        topSection.add(searchBar, BorderLayout.CENTER);

        root.add(topSection,  BorderLayout.NORTH);
        root.add(tableScroll, BorderLayout.CENTER);
        root.add(statusBar,   BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);
    }

    private void loadData() {
        if (!UserSession.canViewAuditLogs()) {
            statusBar.setText("Access denied.");
            return;
        }
        statusBar.setText("Loading audit logs…");
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override protected List<Map<String, String>> doInBackground() throws Exception {
                return FirebaseBootstrap.listCollectionFull("auditLogs");
            }
            @Override protected void done() {
                try {
                    List<Map<String, String>> logs = get();
                    tableModel.setRowCount(0);
                    // Sort by timestamp descending (newest first)
                    logs.sort((a, b) -> b.getOrDefault("timestamp", "").compareTo(a.getOrDefault("timestamp", "")));
                    for (Map<String, String> log : logs) {
                        tableModel.addRow(new Object[]{
                            log.getOrDefault("timestamp",   ""),
                            log.getOrDefault("userName",    log.getOrDefault("userId", "")),
                            log.getOrDefault("userRole",    ""),
                            log.getOrDefault("action",      ""),
                            log.getOrDefault("module",      ""),
                            log.getOrDefault("description", ""),
                            log.getOrDefault("oldValue",    ""),
                            log.getOrDefault("newValue",    "")
                        });
                    }
                    statusBar.setText(logs.size() + " audit log entries loaded.");
                } catch (Exception ex) {
                    statusBar.setText("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void applyFilter() {
        String text   = searchField.getText().trim();
        String module = (String) moduleFilter.getSelectedItem();
        RowFilter<DefaultTableModel, Integer> textFilter = text.isEmpty()
            ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text));
        RowFilter<DefaultTableModel, Integer> modFilter = new RowFilter<>() {
            @Override public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> e) {
                if ("All".equals(module)) return true;
                return module != null && module.equalsIgnoreCase(e.getStringValue(4));
            }
        };
        if (textFilter == null) {
            sorter.setRowFilter(modFilter);
        } else {
            java.util.List<RowFilter<DefaultTableModel, Integer>> filters = new java.util.ArrayList<>();
            filters.add(textFilter);
            filters.add(modFilter);
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private void exportCsv() {
        if (tableModel.getRowCount() == 0) { UiTheme.showWarning(this, "No data to export."); return; }
        String fname = "audit_logs_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setSelectedFile(new File(AppSettings.exportFolder(), fname));
        if (chooser.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) return;
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                StringBuilder csv = new StringBuilder("Timestamp,User,Role,Action,Module,Description,Old Value,New Value\n");
                for (int r = 0; r < tableModel.getRowCount(); r++) {
                    for (int c = 0; c < tableModel.getColumnCount(); c++) {
                        if (c > 0) csv.append(',');
                        csv.append(csvEscape(tableModel.getValueAt(r, c)));
                    }
                    csv.append('\n');
                }
                Files.writeString(chooser.getSelectedFile().toPath(), csv.toString(), StandardCharsets.UTF_8);
                return null;
            }
            @Override protected void done() {
                try { get(); UiTheme.showSuccess(AuditLogsPanel.this, "Exported to: " + chooser.getSelectedFile()); }
                catch (Exception ex) { UiTheme.showError(AuditLogsPanel.this, ex.getMessage()); }
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
