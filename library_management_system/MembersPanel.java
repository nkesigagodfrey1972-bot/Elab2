package library_management_system;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class MembersPanel extends JPanel {

    private final DefaultTableModel tableModel = new DefaultTableModel(
        new String[]{"Reg No", "Full Name", "Mobile", "Branch", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

    // Form fields
    private final JTextField fldRegNo  = UiTheme.makeFormField("Registration number *");
    private final JTextField fldName   = UiTheme.makeFormField("Full name *");
    private final JTextField fldMobile = UiTheme.makeFormField("Mobile number");
    private final JComboBox<String> fldBranch = KiuCatalog.createDepartmentCombo();

    // Search
    private final JTextField searchField = UiTheme.makeFormField("Search by name or reg no…");

    private final JLabel statusBar = new JLabel("Ready");

    public MembersPanel() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BACKGROUND);
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(UiTheme.BACKGROUND);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = UiTheme.makeSectionTitle("\uD83D\uDC65  Member Management");
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchBar.setOpaque(false);
        searchField.setPreferredSize(new Dimension(300, 34));
        JButton refreshBtn = UiTheme.makePrimaryButton("\u21BB Refresh");
        refreshBtn.addActionListener(e -> loadData());
        searchBar.add(new JLabel("Search:"));
        searchBar.add(searchField);
        searchBar.add(refreshBtn);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        // Table
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UiTheme.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromTable();
        });
        JScrollPane tableScroll = UiTheme.makeTableScrollPane(table);

        // Form panel
        JPanel formCard = UiTheme.makeCard();
        formCard.setLayout(new BorderLayout(0, 12));
        formCard.setPreferredSize(new Dimension(320, 0));
        JLabel formTitle = UiTheme.makeSectionTitle("Member Details");
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.gridx = 0; gbc.weightx = 1;

        addFormRow(fields, gbc, "Reg No *",  fldRegNo);
        addFormRow(fields, gbc, "Full Name *", fldName);
        addFormRow(fields, gbc, "Mobile",    fldMobile);
        styleDepartmentBox();
        addFormRow(fields, gbc, "Department / School", fldBranch);

        JPanel formBody = new JPanel(new BorderLayout(0, 10));
        formBody.setOpaque(false);
        JLabel helperText = new JLabel("Capture the student's KIU department or school before saving.");
        helperText.setFont(UiTheme.SMALL_FONT);
        helperText.setForeground(UiTheme.MUTED);
        formBody.add(helperText, BorderLayout.NORTH);
        formBody.add(fields, BorderLayout.CENTER);
        formCard.add(formBody, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridBagLayout());
        btnPanel.setOpaque(false);
        GridBagConstraints bg = new GridBagConstraints();
        bg.fill = GridBagConstraints.HORIZONTAL; bg.weightx = 1; bg.insets = new Insets(3, 0, 3, 0); bg.gridx = 0;

        JButton addBtn     = UiTheme.makeSuccessButton("Add Member");
        JButton updateBtn  = UiTheme.makePrimaryButton("Update");
        JButton deleteBtn  = UiTheme.makeDangerButton("Delete");
        JButton clearBtn   = UiTheme.makeSecondaryButton("Clear");
        JButton historyBtn = UiTheme.makeStyledButton("View History", new java.awt.Color(142, 68, 173), java.awt.Color.WHITE);

        addBtn.addActionListener(e    -> addMember());
        updateBtn.addActionListener(e -> updateMember());
        deleteBtn.addActionListener(e -> deleteMember());
        clearBtn.addActionListener(e  -> clearForm());
        historyBtn.addActionListener(e -> viewHistory());

        if (!UserSession.canManageMembers()) {
            formTitle.setText("Member Details (Read Only)");
            addBtn.setEnabled(false);
            updateBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
        }

        bg.gridy = 0; btnPanel.add(addBtn,     bg);
        bg.gridy = 1; btnPanel.add(updateBtn,  bg);
        bg.gridy = 2; btnPanel.add(deleteBtn,  bg);
        bg.gridy = 3; btnPanel.add(historyBtn, bg);
        bg.gridy = 4; btnPanel.add(clearBtn,   bg);

        formCard.add(btnPanel, BorderLayout.SOUTH);

        statusBar.setFont(UiTheme.SMALL_FONT);
        statusBar.setForeground(UiTheme.MUTED);
        statusBar.setBorder(new EmptyBorder(4, 0, 0, 0));

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
        formScroll.setPreferredSize(new Dimension(340, 0));

        JPanel center = new JPanel(new BorderLayout(12, 0));
        center.setOpaque(false);
        center.add(tableScroll, BorderLayout.CENTER);
        center.add(formScroll,  BorderLayout.EAST);

        root.add(topRow,    BorderLayout.NORTH);
        root.add(center,    BorderLayout.CENTER);
        root.add(statusBar, BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, String label, Component field) {
        gbc.gridy++;
        panel.add(UiTheme.makeFormLabel(label), gbc);
        gbc.gridy++;
        panel.add(field, gbc);
    }

    // ── Data operations ───────────────────────────────────────────────────────

    private void loadData() {
        statusBar.setText("Loading…");
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override protected List<Map<String, String>> doInBackground() throws Exception {
                return FirebaseBootstrap.listStudents();
            }
            @Override protected void done() {
                try {
                    List<Map<String, String>> members = get();
                    tableModel.setRowCount(0);
                    for (Map<String, String> m : members) {
                        tableModel.addRow(new Object[]{
                            m.getOrDefault("registrationNo", m.getOrDefault("_id", "")),
                            m.getOrDefault("studentName", ""),
                            m.getOrDefault("mobileNo",    ""),
                            m.getOrDefault("branch",      ""),
                            "Active"
                        });
                    }
                    statusBar.setText(members.size() + " members loaded.");
                } catch (Exception ex) {
                    statusBar.setText("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void applyFilter() {
        String text = searchField.getText().trim();
        sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
    }

    private void populateFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int mr = table.convertRowIndexToModel(row);
        fldRegNo.setText(tableModel.getValueAt(mr, 0).toString());
        fldName.setText(tableModel.getValueAt(mr, 1).toString());
        fldMobile.setText(tableModel.getValueAt(mr, 2).toString());
        fldBranch.setSelectedItem(tableModel.getValueAt(mr, 3).toString());
    }

    private boolean validateForm() {
        if (fldRegNo.getText().trim().isEmpty()) {
            UiTheme.showError(this, "Registration No is required."); fldRegNo.requestFocus(); return false;
        }
        if (fldName.getText().trim().isEmpty()) {
            UiTheme.showError(this, "Full Name is required."); fldName.requestFocus(); return false;
        }
        return true;
    }

    private void addMember() {
        if (!UserSession.canManageMembers()) { UiTheme.showWarning(this, "Access denied."); return; }
        if (!validateForm()) return;
        statusBar.setText("Saving…");
        String regNo = fldRegNo.getText().trim();
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                FirebaseBootstrap.saveStudent(regNo, fldName.getText().trim(),
                    fldMobile.getText().trim(), selectedDepartment());
                AuditService.log(AuditService.ACTION_ADD_MEMBER, AuditService.MODULE_MEMBERS,
                    "Added member: " + fldName.getText().trim() + " (Reg: " + regNo + ")");
                return null;
            }
            @Override protected void done() {
                try { get(); UiTheme.showSuccess(MembersPanel.this, "Member added."); clearForm(); loadData(); }
                catch (Exception ex) { UiTheme.showError(MembersPanel.this, ex.getMessage()); statusBar.setText("Error."); }
            }
        }.execute();
    }

    private void updateMember() {
        if (!UserSession.canManageMembers()) { UiTheme.showWarning(this, "Access denied."); return; }
        if (!validateForm()) return;
        String regNo = fldRegNo.getText().trim();
        statusBar.setText("Updating…");
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                FirebaseBootstrap.updateStudent(regNo, fldName.getText().trim(),
                    fldMobile.getText().trim(), selectedDepartment());
                AuditService.log(AuditService.ACTION_EDIT_MEMBER, AuditService.MODULE_MEMBERS,
                    "Updated member Reg: " + regNo);
                return null;
            }
            @Override protected void done() {
                try { get(); UiTheme.showSuccess(MembersPanel.this, "Member updated."); clearForm(); loadData(); }
                catch (Exception ex) { UiTheme.showError(MembersPanel.this, ex.getMessage()); statusBar.setText("Error."); }
            }
        }.execute();
    }

    private void deleteMember() {
        if (!UserSession.canManageMembers()) { UiTheme.showWarning(this, "Access denied."); return; }
        String regNo = fldRegNo.getText().trim();
        if (regNo.isEmpty()) { UiTheme.showError(this, "Select a member to delete."); return; }
        if (!UiTheme.confirm(this, "Delete member " + regNo + "? This cannot be undone.")) return;
        statusBar.setText("Deleting…");
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                FirebaseBootstrap.deleteStudent(regNo);
                AuditService.log(AuditService.ACTION_DELETE_MEMBER, AuditService.MODULE_MEMBERS,
                    "Deleted member Reg: " + regNo);
                return null;
            }
            @Override protected void done() {
                try { get(); UiTheme.showSuccess(MembersPanel.this, "Member deleted."); clearForm(); loadData(); }
                catch (Exception ex) { UiTheme.showError(MembersPanel.this, ex.getMessage()); statusBar.setText("Error."); }
            }
        }.execute();
    }

    private void viewHistory() {
        String regNo = fldRegNo.getText().trim();
        if (regNo.isEmpty()) { UiTheme.showError(this, "Select a member first."); return; }
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override protected List<Map<String, String>> doInBackground() throws Exception {
                return FirebaseBootstrap.searchIssueRecordsByRegistrationNo(regNo);
            }
            @Override protected void done() {
                try {
                    List<Map<String, String>> records = get();
                    showHistoryDialog(regNo, records);
                } catch (Exception ex) {
                    UiTheme.showError(MembersPanel.this, ex.getMessage());
                }
            }
        }.execute();
    }

    private void showHistoryDialog(String regNo, List<Map<String, String>> records) {
        JDialog dlg = new JDialog();
        dlg.setTitle("Issue History – " + regNo);
        dlg.setSize(700, 400);
        dlg.setLocationRelativeTo(this);
        dlg.setModal(true);

        DefaultTableModel hModel = new DefaultTableModel(
            new String[]{"Book ID", "Book Name", "Issue Date", "Return Date", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Map<String, String> r : records) {
            hModel.addRow(new Object[]{
                r.getOrDefault("bookId",     ""),
                r.getOrDefault("bookName",   ""),
                r.getOrDefault("issueDate",  ""),
                r.getOrDefault("returnDate", ""),
                "yes".equalsIgnoreCase(r.getOrDefault("issued", "")) ? "Issued" : "Returned"
            });
        }
        JTable hTable = new JTable(hModel);
        UiTheme.styleTable(hTable);
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        panel.add(new JLabel("Issue history for member: " + regNo + " (" + records.size() + " records)"), BorderLayout.NORTH);
        panel.add(UiTheme.makeTableScrollPane(hTable), BorderLayout.CENTER);
        JButton closeBtn = UiTheme.makeSecondaryButton("Close");
        closeBtn.addActionListener(e -> dlg.dispose());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.add(closeBtn);
        panel.add(btnRow, BorderLayout.SOUTH);
        dlg.setContentPane(panel);
        dlg.setVisible(true);
    }

    private void clearForm() {
        fldRegNo.setText(""); fldName.setText(""); fldMobile.setText(""); fldBranch.setSelectedIndex(0);
        table.clearSelection();
    }

    private void styleDepartmentBox() {
        fldBranch.setFont(UiTheme.BODY_FONT);
        fldBranch.setBackground(UiTheme.CARD);
        fldBranch.setForeground(UiTheme.TEXT);
    }

    private String selectedDepartment() {
        Object selected = fldBranch.getSelectedItem();
        return selected == null ? "" : selected.toString().trim();
    }
}
