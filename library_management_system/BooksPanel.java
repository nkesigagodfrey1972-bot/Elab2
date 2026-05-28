package library_management_system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class BooksPanel extends JPanel {

    private final DefaultTableModel tableModel = new DefaultTableModel(
        new String[]{"Book ID", "Title", "Author", "Category", "Price", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

    // Form fields
    private final JTextField fldId       = UiTheme.makeFormField("Auto-generated when saving");
    private final JTextField fldTitle    = UiTheme.makeFormField("Book title *");
    private final JTextField fldAuthor   = UiTheme.makeFormField("Author name *");
    private final JComboBox<String> fldCategory = KiuCatalog.createBookCategoryCombo();
    private final JTextField fldPrice    = UiTheme.makeFormField("Price (numeric)");
    private final JComboBox<String> fldStatus = new JComboBox<>(new String[]{"Available", "Issued"});

    // Search / filter
    private final JTextField searchField    = UiTheme.makeFormField("Search title, author, ID…");
    private final JComboBox<String> catFilter    = new JComboBox<>(new String[]{"All Categories"});
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "Available", "Issued"});

    private final JLabel statusBar = new JLabel("Ready");

    public BooksPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UiTheme.BACKGROUND);
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(UiTheme.BACKGROUND);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        // ── Title ─────────────────────────────────────────────────────────────
        JLabel title = UiTheme.makeSectionTitle("\uD83D\uDCDA  Book Management");
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        // ── Search bar ────────────────────────────────────────────────────────
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchBar.setOpaque(false);
        searchField.setPreferredSize(new Dimension(260, 34));
        catFilter.setPreferredSize(new Dimension(160, 34));
        statusFilter.setPreferredSize(new Dimension(130, 34));
        JButton refreshBtn = UiTheme.makePrimaryButton("\u21BB Refresh");
        refreshBtn.addActionListener(e -> loadData());
        searchBar.add(new JLabel("Search:"));
        searchBar.add(searchField);
        searchBar.add(new JLabel("Category:"));
        searchBar.add(catFilter);
        searchBar.add(new JLabel("Status:"));
        searchBar.add(statusFilter);
        searchBar.add(refreshBtn);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        catFilter.addActionListener(e -> applyFilter());
        statusFilter.addActionListener(e -> applyFilter());

        // ── Table ─────────────────────────────────────────────────────────────
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UiTheme.styleTable(table);
        // Status column colored badge renderer
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String v = val == null ? "" : val.toString();
                if (!sel) {
                    setBackground("Available".equalsIgnoreCase(v)
                        ? new Color(220, 255, 220) : new Color(255, 220, 220));
                    setForeground("Available".equalsIgnoreCase(v)
                        ? new Color(20, 100, 20) : new Color(140, 20, 20));
                }
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromTable();
        });

        JScrollPane tableScroll = UiTheme.makeTableScrollPane(table);

        // ── Form panel ────────────────────────────────────────────────────────
        JPanel formCard = UiTheme.makeCard();
        formCard.setLayout(new BorderLayout(0, 12));
        formCard.setPreferredSize(new Dimension(280, 0));

        JLabel formTitle = UiTheme.makeSectionTitle("Book Details");
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.gridx = 0; gbc.weightx = 1;

        fldId.setEditable(false);
        fldId.setFocusable(false);

        JPanel idRow = new JPanel(new BorderLayout(6, 0));
        idRow.setOpaque(false);
        idRow.add(fldId, BorderLayout.CENTER);

        addFormRow(fields, gbc, "Book ID", idRow);
        addFormRow(fields, gbc, "Title *",   fldTitle);
        addFormRow(fields, gbc, "Author *",  fldAuthor);
        styleCategoryBox();
        addFormRow(fields, gbc, "Category",  fldCategory);
        addFormRow(fields, gbc, "Price",     fldPrice);
        addFormRow(fields, gbc, "Status",    fldStatus);

        formCard.add(fields, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new GridBagLayout());
        btnPanel.setOpaque(false);
        GridBagConstraints bg = new GridBagConstraints();
        bg.fill = GridBagConstraints.HORIZONTAL; bg.weightx = 1; bg.insets = new Insets(3, 0, 3, 0); bg.gridx = 0;

        JButton addBtn    = UiTheme.makeSuccessButton("Add Book");
        JButton updateBtn = UiTheme.makePrimaryButton("Update");
        JButton deleteBtn = UiTheme.makeDangerButton("Delete");
        JButton clearBtn  = UiTheme.makeSecondaryButton("Clear");
        JButton copiesBtn = UiTheme.makeStyledButton("\uD83D\uDCDA Copies", new Color(80, 60, 140), Color.WHITE);

        addBtn.addActionListener(e    -> addBook());
        updateBtn.addActionListener(e -> updateBook());
        deleteBtn.addActionListener(e -> deleteBook());
        clearBtn.addActionListener(e  -> clearForm());
        copiesBtn.addActionListener(e -> manageCopies());

        if (!UserSession.canManageBooks()) {
            formTitle.setText("Book Details (Read Only)");
            addBtn.setEnabled(false);
            updateBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
            copiesBtn.setEnabled(false);
        }

        bg.gridy = 0; btnPanel.add(addBtn,    bg);
        bg.gridy = 1; btnPanel.add(updateBtn, bg);
        bg.gridy = 2; btnPanel.add(deleteBtn, bg);
        bg.gridy = 3; btnPanel.add(copiesBtn, bg);
        bg.gridy = 4; btnPanel.add(clearBtn,  bg);

        formCard.add(btnPanel, BorderLayout.SOUTH);

        // ── Status bar ────────────────────────────────────────────────────────
        statusBar.setFont(UiTheme.SMALL_FONT);
        statusBar.setForeground(UiTheme.MUTED);
        statusBar.setBorder(new EmptyBorder(4, 0, 0, 0));

        // ── Layout ────────────────────────────────────────────────────────────
        JPanel topRow = new JPanel(new BorderLayout(0, 8));
        topRow.setOpaque(false);
        topRow.add(title,     BorderLayout.NORTH);
        topRow.add(searchBar, BorderLayout.CENTER);

        JScrollPane formScroll = new JScrollPane(formCard);
        formScroll.setBorder(null);
        formScroll.getViewport().setOpaque(false);
        formScroll.getViewport().setBackground(UiTheme.BACKGROUND);
        formScroll.setOpaque(false);
        formScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        formScroll.setPreferredSize(new Dimension(300, 0));

        JPanel center = new JPanel(new BorderLayout(12, 0));
        center.setOpaque(false);
        center.add(tableScroll, BorderLayout.CENTER);
        center.add(formScroll,  BorderLayout.EAST);

        root.add(topRow,      BorderLayout.NORTH);
        root.add(center,      BorderLayout.CENTER);
        root.add(statusBar,   BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, String label, Component field) {
        gbc.gridy++;
        JLabel lbl = UiTheme.makeFormLabel(label);
        panel.add(lbl, gbc);
        gbc.gridy++;
        panel.add(field, gbc);
    }

    // ── Data operations ───────────────────────────────────────────────────────

    private void loadData() {
        statusBar.setText("Loading…");
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override protected List<Map<String, String>> doInBackground() throws Exception {
                return FirebaseBootstrap.listBooks();
            }
            @Override protected void done() {
                try {
                    List<Map<String, String>> books = get();
                    tableModel.setRowCount(0);
                    catFilter.removeAllItems();
                    catFilter.addItem("All Categories");
                    java.util.Set<String> cats = new java.util.LinkedHashSet<>();
                    for (Map<String, String> b : books) {
                        String cat = b.getOrDefault("category", "");
                        if (!cat.isBlank()) cats.add(cat);
                        tableModel.addRow(new Object[]{
                            b.getOrDefault("bookId",   b.getOrDefault("_id", "")),
                            b.getOrDefault("bookName", ""),
                            b.getOrDefault("author",   ""),
                            b.getOrDefault("category", ""),
                            b.getOrDefault("price",    ""),
                            "yes".equalsIgnoreCase(b.getOrDefault("issued", "no")) ? "Issued" : "Available"
                        });
                    }
                    cats.forEach(catFilter::addItem);
                    statusBar.setText(books.size() + " books loaded.");
                } catch (Exception ex) {
                    statusBar.setText("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void applyFilter() {
        String text   = searchField.getText().trim().toLowerCase();
        String cat    = (String) catFilter.getSelectedItem();
        String status = (String) statusFilter.getSelectedItem();
        sorter.setRowFilter(RowFilter.andFilter(java.util.Arrays.asList(
            RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)),
            new RowFilter<DefaultTableModel, Integer>() {
                @Override public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    if ("All Categories".equals(cat)) return true;
                    return cat != null && cat.equalsIgnoreCase(entry.getStringValue(3));
                }
            },
            new RowFilter<DefaultTableModel, Integer>() {
                @Override public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    if ("All".equals(status)) return true;
                    return status != null && status.equalsIgnoreCase(entry.getStringValue(5));
                }
            }
        )));
    }

    private void populateFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int modelRow = table.convertRowIndexToModel(row);
        fldId.setText(tableModel.getValueAt(modelRow, 0).toString());
        fldTitle.setText(tableModel.getValueAt(modelRow, 1).toString());
        fldAuthor.setText(tableModel.getValueAt(modelRow, 2).toString());
        fldCategory.setSelectedItem(tableModel.getValueAt(modelRow, 3).toString());
        fldPrice.setText(tableModel.getValueAt(modelRow, 4).toString());
        String st = tableModel.getValueAt(modelRow, 5).toString();
        fldStatus.setSelectedItem(st);
    }

    private boolean validateForm() {
        if (fldTitle.getText().trim().isEmpty()) {
            UiTheme.showError(this, "Title is required.");
            fldTitle.requestFocus();
            return false;
        }
        if (fldAuthor.getText().trim().isEmpty()) {
            UiTheme.showError(this, "Author is required.");
            fldAuthor.requestFocus();
            return false;
        }
        String price = fldPrice.getText().trim();
        if (!price.isEmpty()) {
            try { Double.parseDouble(price); }
            catch (NumberFormatException e) {
                UiTheme.showError(this, "Price must be a numeric value.");
                fldPrice.requestFocus();
                return false;
            }
        }
        return true;
    }

    private void addBook() {
        if (!UserSession.canManageBooks()) { UiTheme.showWarning(this, "Access denied."); return; }
        if (!validateForm()) return;
        statusBar.setText("Saving…");
        new SwingWorker<Void, Void>() {
            private String generatedId;

            @Override protected Void doInBackground() throws Exception {
                generatedId = FirebaseBootstrap.generateBookId();
                FirebaseBootstrap.saveBook(generatedId, fldTitle.getText().trim(),
                    fldAuthor.getText().trim(), selectedCategory(), fldPrice.getText().trim());
                AuditService.log(AuditService.ACTION_ADD_BOOK, AuditService.MODULE_BOOKS,
                    "Added book: " + fldTitle.getText().trim() + " (ID: " + generatedId + ")");
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    UiTheme.showSuccess(BooksPanel.this,
                        "Book added successfully.\nAssigned Book ID: " + generatedId);
                    clearForm();
                    loadData();
                }
                catch (Exception ex) { UiTheme.showError(BooksPanel.this, ex.getMessage()); statusBar.setText("Error."); }
            }
        }.execute();
    }

    private void updateBook() {
        if (!UserSession.canManageBooks()) { UiTheme.showWarning(this, "Access denied."); return; }
        if (!validateForm()) return;
        String id = fldId.getText().trim();
        if (id.isEmpty()) { UiTheme.showError(this, "Select a book to update."); return; }
        statusBar.setText("Updating…");
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                FirebaseBootstrap.updateBook(id, fldTitle.getText().trim(),
                    fldAuthor.getText().trim(), selectedCategory(), fldPrice.getText().trim());
                AuditService.log(AuditService.ACTION_EDIT_BOOK, AuditService.MODULE_BOOKS,
                    "Updated book ID: " + id + " title: " + fldTitle.getText().trim());
                return null;
            }
            @Override protected void done() {
                try { get(); UiTheme.showSuccess(BooksPanel.this, "Book updated."); clearForm(); loadData(); }
                catch (Exception ex) { UiTheme.showError(BooksPanel.this, ex.getMessage()); statusBar.setText("Error."); }
            }
        }.execute();
    }

    private void deleteBook() {
        if (!UserSession.canManageBooks()) { UiTheme.showWarning(this, "Access denied."); return; }
        String id = fldId.getText().trim();
        if (id.isEmpty()) { UiTheme.showError(this, "Select a book to delete."); return; }
        if (!UiTheme.confirm(this, "Delete book ID: " + id + "? This cannot be undone.")) return;
        statusBar.setText("Deleting…");
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                FirebaseBootstrap.deleteBook(id);
                AuditService.log(AuditService.ACTION_DELETE_BOOK, AuditService.MODULE_BOOKS,
                    "Deleted book ID: " + id);
                return null;
            }
            @Override protected void done() {
                try { get(); UiTheme.showSuccess(BooksPanel.this, "Book deleted."); clearForm(); loadData(); }
                catch (Exception ex) { UiTheme.showError(BooksPanel.this, ex.getMessage()); statusBar.setText("Error."); }
            }
        }.execute();
    }

    private void manageCopies() {
        String id    = fldId.getText().trim();
        String title = fldTitle.getText().trim();
        if (id.isEmpty()) { UiTheme.showError(this, "Select a book first."); return; }
        java.awt.Frame frame = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this);
        new BookCopiesPanel(frame, id, title.isEmpty() ? id : title).setVisible(true);
    }

    private void clearForm() {
        fldId.setText(""); fldTitle.setText(""); fldAuthor.setText("");
        fldCategory.setSelectedIndex(0); fldPrice.setText(""); fldStatus.setSelectedIndex(0);
        table.clearSelection();
    }

    private void styleCategoryBox() {
        fldCategory.setFont(UiTheme.BODY_FONT);
        fldCategory.setBackground(UiTheme.CARD);
        fldCategory.setForeground(UiTheme.TEXT);
        fldCategory.setToolTipText("Choose a KIU faculty/school category or type a custom one.");
    }

    private String selectedCategory() {
        Object selected = fldCategory.getSelectedItem();
        return selected == null ? "" : selected.toString().trim();
    }
}
