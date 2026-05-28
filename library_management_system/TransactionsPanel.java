package library_management_system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
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

public class TransactionsPanel extends JPanel {

    private final DefaultTableModel tableModel = new DefaultTableModel(
        new String[]{"Book ID", "Book Name", "Reg No", "Student Name", "Issue Date", "Return Date", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

    private final JTextField searchField    = UiTheme.makeFormField("Search book, member, ID…");
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "Issued", "Returned"});
    private final JLabel statusBar = new JLabel("Ready");

    public TransactionsPanel() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BACKGROUND);
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(UiTheme.BACKGROUND);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = UiTheme.makeSectionTitle("\uD83D\uDCCB  Transactions");
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);
        searchField.setPreferredSize(new Dimension(280, 34));
        statusFilter.setPreferredSize(new Dimension(140, 34));
        JButton refreshBtn = UiTheme.makePrimaryButton("\u21BB Refresh");
        JButton exportBtn  = UiTheme.makeSecondaryButton("\uD83D\uDCBE Export CSV");
        refreshBtn.addActionListener(e -> loadData());
        exportBtn.addActionListener(e  -> exportCsv());
        filterBar.add(new JLabel("Search:"));
        filterBar.add(searchField);
        filterBar.add(new JLabel("Status:"));
        filterBar.add(statusFilter);
        filterBar.add(refreshBtn);
        filterBar.add(exportBtn);

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

        // Status column badge renderer
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String v = val == null ? "" : val.toString();
                if (!sel) {
                    setBackground("Issued".equalsIgnoreCase(v)
                        ? new Color(255, 235, 200) : new Color(220, 255, 220));
                    setForeground("Issued".equalsIgnoreCase(v)
                        ? new Color(150, 60, 0) : new Color(20, 100, 20));
                }
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        });

        JScrollPane tableScroll = UiTheme.makeTableScrollPane(table);

        statusBar.setFont(UiTheme.SMALL_FONT);
        statusBar.setForeground(UiTheme.MUTED);
        statusBar.setBorder(new EmptyBorder(4, 0, 0, 0));

        JPanel topRow = new JPanel(new BorderLayout(0, 8));
        topRow.setOpaque(false);
        topRow.add(title,     BorderLayout.NORTH);
        topRow.add(filterBar, BorderLayout.CENTER);

        root.add(topRow,      BorderLayout.NORTH);
        root.add(tableScroll, BorderLayout.CENTER);
        root.add(statusBar,   BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);
    }

    private void loadData() {
        statusBar.setText("Loading…");
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override protected List<Map<String, String>> doInBackground() throws Exception {
                return FirebaseBootstrap.listIssueRecords();
            }
            @Override protected void done() {
                try {
                    List<Map<String, String>> records = get();
                    tableModel.setRowCount(0);
                    for (Map<String, String> r : records) {
                        tableModel.addRow(new Object[]{
                            r.getOrDefault("bookId",       r.getOrDefault("_id", "")),
                            r.getOrDefault("bookName",     ""),
                            r.getOrDefault("registrationNo", ""),
                            r.getOrDefault("studentName",  ""),
                            r.getOrDefault("issueDate",    ""),
                            r.getOrDefault("returnDate",   ""),
                            "yes".equalsIgnoreCase(r.getOrDefault("issued", "")) ? "Issued" : "Returned"
                        });
                    }
                    statusBar.setText(records.size() + " records loaded.");
                } catch (Exception ex) {
                    statusBar.setText("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void applyFilter() {
        String text   = searchField.getText().trim();
        String status = (String) statusFilter.getSelectedItem();
        sorter.setRowFilter(RowFilter.andFilter(java.util.Arrays.asList(
            text.isEmpty() ? RowFilter.regexFilter("") : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)),
            new RowFilter<DefaultTableModel, Integer>() {
                @Override public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    if ("All".equals(status)) return true;
                    return status != null && status.equalsIgnoreCase(entry.getStringValue(6));
                }
            }
        )));
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("transactions_" +
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                List<Map<String, String>> records = FirebaseBootstrap.listIssueRecords();
                StringBuilder csv = new StringBuilder("Book ID,Book Name,Reg No,Student Name,Issue Date,Return Date,Status\n");
                for (Map<String, String> r : records) {
                    csv.append(csvEscape(r.getOrDefault("bookId", ""))).append(',')
                       .append(csvEscape(r.getOrDefault("bookName", ""))).append(',')
                       .append(csvEscape(r.getOrDefault("registrationNo", ""))).append(',')
                       .append(csvEscape(r.getOrDefault("studentName", ""))).append(',')
                       .append(csvEscape(r.getOrDefault("issueDate", ""))).append(',')
                       .append(csvEscape(r.getOrDefault("returnDate", ""))).append(',')
                       .append("yes".equalsIgnoreCase(r.getOrDefault("issued", "")) ? "Issued" : "Returned")
                       .append('\n');
                }
                Files.writeString(chooser.getSelectedFile().toPath(), csv.toString(), StandardCharsets.UTF_8);
                return null;
            }
            @Override protected void done() {
                try { get(); UiTheme.showSuccess(TransactionsPanel.this, "Exported to: " + chooser.getSelectedFile()); }
                catch (Exception ex) { UiTheme.showError(TransactionsPanel.this, ex.getMessage()); }
            }
        }.execute();
    }

    private String csvEscape(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n"))
            return '"' + v.replace("\"", "\"\"") + '"';
        return v;
    }

}
