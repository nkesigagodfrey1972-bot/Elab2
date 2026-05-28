package library_management_system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Manages physical copies of a specific book.
 * Opened as a modal dialog from BooksPanel.
 */
public class BookCopiesPanel extends JDialog {

    private static final String[] STATUSES   = {"Available", "Issued", "Reserved", "Lost", "Damaged", "Under Repair"};
    private static final String[] CONDITIONS = {"Good", "Fair", "Poor", "Damaged"};

    private final String bookId;
    private final String bookTitle;

    private final DefaultTableModel tableModel = new DefaultTableModel(
        new String[]{"Copy ID", "Barcode", "Status", "Condition", "Shelf Location", "Created"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    // Form
    private final JTextField fldCopyId    = UiTheme.makeFormField("Auto-generated if blank");
    private final JTextField fldBarcode   = UiTheme.makeFormField("Barcode / scan code");
    private final JComboBox<String> fldStatus    = new JComboBox<>(STATUSES);
    private final JComboBox<String> fldCondition = new JComboBox<>(CONDITIONS);
    private final JTextField fldShelf     = UiTheme.makeFormField("e.g. A-12-3");

    private final JLabel statusBar = new JLabel("Ready");

    public BookCopiesPanel(java.awt.Frame parent, String bookId, String bookTitle) {
        super(parent, "Book Copies — " + bookTitle, true);
        this.bookId    = bookId;
        this.bookTitle = bookTitle;
        setSize(900, 580);
        setLocationRelativeTo(parent);
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(UiTheme.BACKGROUND);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Header
        JLabel title = UiTheme.makeSectionTitle("\uD83D\uDCDA  Copies of: " + bookTitle);
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        // Table
        UiTheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(2).setCellRenderer(statusRenderer());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromTable();
        });
        JScrollPane tableScroll = UiTheme.makeTableScrollPane(table);

        // Form
        JPanel formCard = UiTheme.makeCard();
        formCard.setLayout(new GridBagLayout());
        formCard.setPreferredSize(new Dimension(260, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridx = 0; gbc.weightx = 1;

        JLabel formTitle = UiTheme.makeSectionTitle("Copy Details");
        gbc.gridy = 0; formCard.add(formTitle, gbc);

        gbc.gridy = 1; formCard.add(UiTheme.makeFormLabel("Copy ID"), gbc);
        gbc.gridy = 2; formCard.add(fldCopyId, gbc);
        gbc.gridy = 3; formCard.add(UiTheme.makeFormLabel("Barcode"), gbc);
        gbc.gridy = 4; formCard.add(fldBarcode, gbc);
        gbc.gridy = 5; formCard.add(UiTheme.makeFormLabel("Status"), gbc);
        gbc.gridy = 6; formCard.add(fldStatus, gbc);
        gbc.gridy = 7; formCard.add(UiTheme.makeFormLabel("Condition"), gbc);
        gbc.gridy = 8; formCard.add(fldCondition, gbc);
        gbc.gridy = 9; formCard.add(UiTheme.makeFormLabel("Shelf Location"), gbc);
        gbc.gridy = 10; formCard.add(fldShelf, gbc);

        // Barcode preview label
        JLabel barcodePreview = new JLabel("Barcode Preview");
        barcodePreview.setFont(UiTheme.SMALL_FONT);
        barcodePreview.setForeground(UiTheme.MUTED);
        gbc.gridy = 11; formCard.add(barcodePreview, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new GridBagLayout());
        btnPanel.setOpaque(false);
        GridBagConstraints bg = new GridBagConstraints();
        bg.fill = GridBagConstraints.HORIZONTAL; bg.weightx = 1; bg.insets = new Insets(3, 0, 3, 0); bg.gridx = 0;

        JButton addBtn    = UiTheme.makeSuccessButton("Add Copy");
        JButton updateBtn = UiTheme.makePrimaryButton("Update");
        JButton deleteBtn = UiTheme.makeDangerButton("Delete");
        JButton printBtn  = UiTheme.makeStyledButton("\uD83D\uDDC4 Print Label", new Color(80, 80, 80), Color.WHITE);
        JButton clearBtn  = UiTheme.makeSecondaryButton("Clear");

        addBtn.addActionListener(e    -> addCopy());
        updateBtn.addActionListener(e -> updateCopy());
        deleteBtn.addActionListener(e -> deleteCopy());
        printBtn.addActionListener(e  -> printLabel());
        clearBtn.addActionListener(e  -> clearForm());

        if (!UserSession.canManageCopies()) {
            addBtn.setEnabled(false);
            updateBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
        }

        bg.gridy = 0; btnPanel.add(addBtn,    bg);
        bg.gridy = 1; btnPanel.add(updateBtn, bg);
        bg.gridy = 2; btnPanel.add(deleteBtn, bg);
        bg.gridy = 3; btnPanel.add(printBtn,  bg);
        bg.gridy = 4; btnPanel.add(clearBtn,  bg);

        gbc.gridy = 12; gbc.insets = new Insets(10, 4, 4, 4);
        formCard.add(btnPanel, gbc);

        statusBar.setFont(UiTheme.SMALL_FONT);
        statusBar.setForeground(UiTheme.MUTED);

        JPanel center = new JPanel(new BorderLayout(12, 0));
        center.setOpaque(false);
        center.add(tableScroll, BorderLayout.CENTER);
        center.add(formCard,    BorderLayout.EAST);

        root.add(title,     BorderLayout.NORTH);
        root.add(center,    BorderLayout.CENTER);
        root.add(statusBar, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private DefaultTableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String v = val == null ? "" : val.toString();
                if (!sel) {
                    setBackground(switch (v) {
                        case "Available"    -> new Color(220, 255, 220);
                        case "Issued"       -> new Color(255, 235, 210);
                        case "Reserved"     -> new Color(210, 230, 255);
                        case "Lost"         -> new Color(255, 210, 210);
                        case "Damaged"      -> new Color(255, 220, 180);
                        case "Under Repair" -> new Color(240, 240, 200);
                        default             -> UiTheme.CARD;
                    });
                    setForeground(switch (v) {
                        case "Available" -> new Color(20, 100, 20);
                        case "Issued"    -> new Color(160, 80, 0);
                        case "Lost",
                             "Damaged"   -> new Color(140, 20, 20);
                        default          -> UiTheme.TEXT;
                    });
                }
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        };
    }

    private void loadData() {
        statusBar.setText("Loading copies…");
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override protected List<Map<String, String>> doInBackground() throws Exception {
                return FirebaseBootstrap.getCopiesForBook(bookId);
            }
            @Override protected void done() {
                try {
                    List<Map<String, String>> copies = get();
                    tableModel.setRowCount(0);
                    for (Map<String, String> c : copies) {
                        tableModel.addRow(new Object[]{
                            c.getOrDefault("copyId",       c.getOrDefault("_id", "")),
                            c.getOrDefault("barcode",      ""),
                            c.getOrDefault("status",       "Available"),
                            c.getOrDefault("condition",    "Good"),
                            c.getOrDefault("shelfLocation",""),
                            c.getOrDefault("createdAt",    "")
                        });
                    }
                    statusBar.setText(copies.size() + " copies. Available: " +
                        copies.stream().filter(c -> "Available".equals(c.getOrDefault("status",""))).count());
                } catch (Exception ex) {
                    statusBar.setText("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void populateFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        fldCopyId.setText(tableModel.getValueAt(row, 0).toString());
        fldBarcode.setText(tableModel.getValueAt(row, 1).toString());
        fldStatus.setSelectedItem(tableModel.getValueAt(row, 2).toString());
        fldCondition.setSelectedItem(tableModel.getValueAt(row, 3).toString());
        fldShelf.setText(tableModel.getValueAt(row, 4).toString());
    }

    private void addCopy() {
        if (!UserSession.canManageCopies()) { UiTheme.showWarning(this, "Access denied."); return; }
        String copyId  = fldCopyId.getText().trim();
        String barcode = fldBarcode.getText().trim();
        if (copyId.isEmpty()) copyId = bookId + "_COPY_" + System.currentTimeMillis();
        if (barcode.isEmpty()) barcode = "BC-" + bookId + "-" + System.currentTimeMillis();
        final String finalCopyId = copyId, finalBarcode = barcode;
        statusBar.setText("Adding copy…");
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                Map<String, String> fields = new HashMap<>();
                fields.put("copyId",        finalCopyId);
                fields.put("bookId",        bookId);
                fields.put("barcode",       finalBarcode);
                fields.put("status",        (String) fldStatus.getSelectedItem());
                fields.put("condition",     (String) fldCondition.getSelectedItem());
                fields.put("shelfLocation", fldShelf.getText().trim());
                fields.put("createdAt",     Instant.now().toString());
                fields.put("updatedAt",     Instant.now().toString());
                FirebaseBootstrap.saveBookCopy(fields);
                AuditService.log(AuditService.ACTION_ADD_COPY, AuditService.MODULE_COPIES,
                    "Added copy " + finalCopyId + " for book " + bookId + " (barcode: " + finalBarcode + ")");
                return null;
            }
            @Override protected void done() {
                try { get(); UiTheme.showSuccess(BookCopiesPanel.this, "Copy added."); clearForm(); loadData(); }
                catch (Exception ex) { UiTheme.showError(BookCopiesPanel.this, ex.getMessage()); statusBar.setText("Error."); }
            }
        }.execute();
    }

    private void updateCopy() {
        if (!UserSession.canManageCopies()) { UiTheme.showWarning(this, "Access denied."); return; }
        String copyId = fldCopyId.getText().trim();
        if (copyId.isEmpty()) { UiTheme.showError(this, "Select a copy to update."); return; }
        statusBar.setText("Updating…");
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                Map<String, String> existing = FirebaseBootstrap.getBookCopy(copyId);
                if (existing == null) throw new IllegalStateException("Copy not found.");
                existing.put("barcode",       fldBarcode.getText().trim());
                existing.put("status",        (String) fldStatus.getSelectedItem());
                existing.put("condition",     (String) fldCondition.getSelectedItem());
                existing.put("shelfLocation", fldShelf.getText().trim());
                existing.put("updatedAt",     Instant.now().toString());
                FirebaseBootstrap.saveBookCopy(existing);
                AuditService.log(AuditService.ACTION_EDIT_COPY, AuditService.MODULE_COPIES,
                    "Updated copy " + copyId + " status=" + fldStatus.getSelectedItem());
                return null;
            }
            @Override protected void done() {
                try { get(); UiTheme.showSuccess(BookCopiesPanel.this, "Copy updated."); clearForm(); loadData(); }
                catch (Exception ex) { UiTheme.showError(BookCopiesPanel.this, ex.getMessage()); statusBar.setText("Error."); }
            }
        }.execute();
    }

    private void deleteCopy() {
        if (!UserSession.canManageCopies()) { UiTheme.showWarning(this, "Access denied."); return; }
        String copyId = fldCopyId.getText().trim();
        if (copyId.isEmpty()) { UiTheme.showError(this, "Select a copy to delete."); return; }
        if (!UiTheme.confirm(this, "Delete copy " + copyId + "? This cannot be undone.")) return;
        statusBar.setText("Deleting…");
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                FirebaseBootstrap.deleteBookCopy(copyId);
                AuditService.log(AuditService.ACTION_DELETE_COPY, AuditService.MODULE_COPIES,
                    "Deleted copy " + copyId + " from book " + bookId);
                return null;
            }
            @Override protected void done() {
                try { get(); UiTheme.showSuccess(BookCopiesPanel.this, "Copy deleted."); clearForm(); loadData(); }
                catch (Exception ex) { UiTheme.showError(BookCopiesPanel.this, ex.getMessage()); statusBar.setText("Error."); }
            }
        }.execute();
    }

    private void printLabel() {
        String copyId  = fldCopyId.getText().trim();
        String barcode = fldBarcode.getText().trim();
        if (copyId.isEmpty()) { UiTheme.showError(this, "Select or enter a copy first."); return; }
        if (barcode.isEmpty()) barcode = "BC-" + copyId;

        // Barcode label preview dialog
        JDialog labelDlg = new JDialog(this, "Barcode Label Preview", true);
        labelDlg.setSize(400, 280);
        labelDlg.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(24, 32, 24, 32));

        JLabel libName = new JLabel(AppSettings.libraryName(), javax.swing.SwingConstants.CENTER);
        libName.setFont(UiTheme.HEADING_FONT);
        libName.setForeground(UiTheme.ACCENT_DARK);

        JLabel bookLbl = new JLabel("Book: " + bookTitle, javax.swing.SwingConstants.CENTER);
        bookLbl.setFont(UiTheme.BODY_FONT);

        JLabel copyLbl = new JLabel("Copy ID: " + copyId, javax.swing.SwingConstants.CENTER);
        copyLbl.setFont(UiTheme.BODY_FONT);

        // Barcode text displayed in a monospace font to simulate barcode
        JLabel barcodeLbl = new JLabel(barcode, javax.swing.SwingConstants.CENTER);
        barcodeLbl.setFont(new java.awt.Font("Courier New", java.awt.Font.BOLD, 18));
        barcodeLbl.setForeground(Color.BLACK);
        barcodeLbl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            new EmptyBorder(8, 16, 8, 16)
        ));

        JLabel shelfLbl = new JLabel("Shelf: " + fldShelf.getText().trim(), javax.swing.SwingConstants.CENTER);
        shelfLbl.setFont(UiTheme.SMALL_FONT);
        shelfLbl.setForeground(UiTheme.MUTED);

        JPanel content = new JPanel();
        content.setBackground(Color.WHITE);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(libName);
        content.add(Box.createVerticalStrut(8));
        content.add(bookLbl);
        content.add(Box.createVerticalStrut(4));
        content.add(copyLbl);
        content.add(Box.createVerticalStrut(12));
        content.add(barcodeLbl);
        content.add(Box.createVerticalStrut(8));
        content.add(shelfLbl);

        JButton closeBtn = UiTheme.makeSecondaryButton("Close");
        closeBtn.addActionListener(e -> labelDlg.dispose());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setBackground(Color.WHITE);
        btnRow.add(closeBtn);

        panel.add(content, BorderLayout.CENTER);
        panel.add(btnRow,  BorderLayout.SOUTH);
        labelDlg.setContentPane(panel);
        labelDlg.setVisible(true);
    }

    private void clearForm() {
        fldCopyId.setText(""); fldBarcode.setText(""); fldShelf.setText("");
        fldStatus.setSelectedIndex(0); fldCondition.setSelectedIndex(0);
        table.clearSelection();
    }
}
