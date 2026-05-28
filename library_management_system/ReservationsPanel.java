package library_management_system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.Box;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class ReservationsPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DefaultTableModel tableModel = new DefaultTableModel(
        new String[]{"Reservation ID", "Book ID", "Book Title", "Member ID", "Member Name",
                     "Reserved On", "Expires", "Status", "Queue"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

    // New reservation form
    private final JTextField fldBookId    = UiTheme.makeFormField("Book ID");
    private final JTextField fldMemberId  = UiTheme.makeFormField("Member Registration No");
    private final JLabel     lblBookTitle = infoLabel("–");
    private final JLabel     lblMemberName= infoLabel("–");

    private final JTextField searchField    = UiTheme.makeFormField("Search book, member…");
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "Pending", "Fulfilled", "Cancelled", "Expired"});
    private final JLabel statusBar = new JLabel("Ready");

    public ReservationsPanel() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BACKGROUND);
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(UiTheme.BACKGROUND);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = UiTheme.makeSectionTitle("\uD83D\uDCC5  Reservations");
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchBar.setOpaque(false);
        searchField.setPreferredSize(new Dimension(240, 34));
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
        table.getColumnModel().getColumn(7).setCellRenderer(statusRenderer());
        JScrollPane tableScroll = UiTheme.makeTableScrollPane(table);

        // Action buttons
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionBar.setOpaque(false);
        JButton fulfillBtn = UiTheme.makeSuccessButton("\u2714 Fulfill");
        JButton cancelBtn  = UiTheme.makeDangerButton("\u2716 Cancel");
        fulfillBtn.addActionListener(e -> updateStatus("Fulfilled"));
        cancelBtn.addActionListener(e  -> updateStatus("Cancelled"));
        if (!UserSession.canManageReservations()) {
            fulfillBtn.setEnabled(false);
            cancelBtn.setEnabled(false);
        }
        actionBar.add(fulfillBtn);
        actionBar.add(cancelBtn);

        // New reservation form
        JPanel formCard = UiTheme.makeCard();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.gridx = 0; gbc.weightx = 1;

        JLabel formTitle = UiTheme.makeSectionTitle("New Reservation");
        gbc.gridy = 0; gbc.gridwidth = 2;
        formCard.add(formTitle, gbc);
        gbc.gridwidth = 1;

        gbc.gridy = 1; gbc.weightx = 0;
        formCard.add(UiTheme.makeFormLabel("Book ID *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formCard.add(fldBookId, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formCard.add(UiTheme.makeFormLabel("Book Title:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formCard.add(lblBookTitle, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        formCard.add(UiTheme.makeFormLabel("Member Reg No *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formCard.add(fldMemberId, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        formCard.add(UiTheme.makeFormLabel("Member Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formCard.add(lblMemberName, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.insets = new Insets(10, 6, 4, 6);
        JPanel formBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        formBtns.setOpaque(false);
        JButton lookupBtn  = UiTheme.makePrimaryButton("Look Up");
        JButton reserveBtn = UiTheme.makeSuccessButton("Reserve");
        JButton clearBtn   = UiTheme.makeSecondaryButton("Clear");
        lookupBtn.addActionListener(e  -> lookupBookAndMember());
        reserveBtn.addActionListener(e -> createReservation());
        clearBtn.addActionListener(e   -> clearForm());
        if (!UserSession.canManageReservations()) reserveBtn.setEnabled(false);
        formBtns.add(lookupBtn);
        formBtns.add(reserveBtn);
        formBtns.add(clearBtn);
        formCard.add(formBtns, gbc);

        statusBar.setFont(UiTheme.SMALL_FONT);
        statusBar.setForeground(UiTheme.MUTED);

        JPanel topSection = new JPanel(new BorderLayout(0, 8));
        topSection.setOpaque(false);
        topSection.add(title,     BorderLayout.NORTH);
        topSection.add(searchBar, BorderLayout.CENTER);
        topSection.add(actionBar, BorderLayout.SOUTH);

        JScrollPane formScroll = new JScrollPane(formCard);
        formScroll.setBorder(null);
        formScroll.getViewport().setOpaque(false);
        formScroll.getViewport().setBackground(UiTheme.BACKGROUND);
        formScroll.setOpaque(false);
        formScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        formScroll.setPreferredSize(new Dimension(340, 0));

        JPanel centerSection = new JPanel(new BorderLayout(12, 0));
        centerSection.setOpaque(false);
        centerSection.add(tableScroll, BorderLayout.CENTER);
        centerSection.add(formScroll,  BorderLayout.EAST);

        root.add(topSection,    BorderLayout.NORTH);
        root.add(centerSection, BorderLayout.CENTER);
        root.add(statusBar,     BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);
    }

    private DefaultTableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String v = val == null ? "" : val.toString();
                if (!sel) {
                    setBackground(switch (v) {
                        case "Pending"   -> new Color(255, 243, 205);
                        case "Fulfilled" -> new Color(220, 255, 220);
                        case "Cancelled" -> new Color(255, 220, 220);
                        case "Expired"   -> new Color(230, 230, 230);
                        default          -> UiTheme.CARD;
                    });
                    setForeground(switch (v) {
                        case "Pending"   -> new Color(140, 80, 0);
                        case "Fulfilled" -> new Color(20, 100, 20);
                        case "Cancelled" -> new Color(140, 20, 20);
                        default          -> UiTheme.MUTED;
                    });
                }
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        };
    }

    private void loadData() {
        statusBar.setText("Loading…");
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override protected List<Map<String, String>> doInBackground() throws Exception {
                return FirebaseBootstrap.listCollectionFull("reservations");
            }
            @Override protected void done() {
                try {
                    List<Map<String, String>> recs = get();
                    tableModel.setRowCount(0);
                    for (Map<String, String> r : recs) {
                        tableModel.addRow(new Object[]{
                            r.getOrDefault("reservationId", r.getOrDefault("_id", "")),
                            r.getOrDefault("bookId",        ""),
                            r.getOrDefault("bookTitle",     ""),
                            r.getOrDefault("memberId",      ""),
                            r.getOrDefault("memberName",    ""),
                            r.getOrDefault("reservationDate",""),
                            r.getOrDefault("expiryDate",    ""),
                            r.getOrDefault("status",        "Pending"),
                            r.getOrDefault("queuePosition", "1")
                        });
                    }
                    statusBar.setText(recs.size() + " reservations loaded.");
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
                return status != null && status.equalsIgnoreCase(e.getStringValue(7));
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

    private void lookupBookAndMember() {
        String bookId  = fldBookId.getText().trim();
        String memberId= fldMemberId.getText().trim();
        if (bookId.isEmpty() && memberId.isEmpty()) {
            UiTheme.showError(this, "Enter at least a Book ID or Member Reg No."); return;
        }
        statusBar.setText("Looking up…");
        new SwingWorker<String[], Void>() {
            @Override protected String[] doInBackground() throws Exception {
                String bookTitle = "–", memberName = "–";
                if (!bookId.isEmpty()) {
                    Map<String, String> book = FirebaseBootstrap.getBook(bookId);
                    bookTitle = book != null ? book.getOrDefault("bookName", "Not found") : "Not found";
                }
                if (!memberId.isEmpty()) {
                    Map<String, String> member = FirebaseBootstrap.getStudent(memberId);
                    memberName = member != null ? member.getOrDefault("studentName", "Not found") : "Not found";
                }
                return new String[]{bookTitle, memberName};
            }
            @Override protected void done() {
                try {
                    String[] r = get();
                    lblBookTitle.setText(r[0]);
                    lblMemberName.setText(r[1]);
                    statusBar.setText("Lookup complete.");
                } catch (Exception ex) { statusBar.setText("Error: " + ex.getMessage()); }
            }
        }.execute();
    }

    private void createReservation() {
        if (!UserSession.canManageReservations()) {
            UiTheme.showWarning(this, "Access denied."); return;
        }
        String bookId   = fldBookId.getText().trim();
        String memberId = fldMemberId.getText().trim();
        if (bookId.isEmpty() || memberId.isEmpty()) {
            UiTheme.showError(this, "Book ID and Member Reg No are required."); return;
        }
        if ("Not found".equals(lblBookTitle.getText()) || "–".equals(lblBookTitle.getText())) {
            UiTheme.showError(this, "Look up the book first."); return;
        }
        if ("Not found".equals(lblMemberName.getText()) || "–".equals(lblMemberName.getText())) {
            UiTheme.showError(this, "Look up the member first."); return;
        }

        statusBar.setText("Creating reservation…");
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                // Check for duplicate active reservation
                List<Map<String, String>> existing = FirebaseBootstrap.getActiveReservationsForMember(memberId);
                for (Map<String, String> r : existing) {
                    if (bookId.equalsIgnoreCase(r.getOrDefault("bookId", "")))
                        throw new IllegalStateException("Member already has an active reservation for this book.");
                }
                // Count pending reservations for queue position
                int queue = FirebaseBootstrap.getPendingReservationsForBook(bookId).size() + 1;
                String resId = "RES_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6);
                LocalDate today = LocalDate.now();
                int expiryDays = AppSettings.reservationExpiryDays();
                Map<String, String> fields = new HashMap<>();
                fields.put("reservationId",   resId);
                fields.put("bookId",          bookId);
                fields.put("bookTitle",       lblBookTitle.getText());
                fields.put("memberId",        memberId);
                fields.put("memberName",      lblMemberName.getText());
                fields.put("reservationDate", today.format(DATE_FMT));
                fields.put("expiryDate",      today.plusDays(expiryDays).format(DATE_FMT));
                fields.put("status",          "Pending");
                fields.put("queuePosition",   String.valueOf(queue));
                fields.put("createdAt",       java.time.Instant.now().toString());
                fields.put("updatedAt",       java.time.Instant.now().toString());
                FirebaseBootstrap.saveReservation(fields);
                AuditService.log(AuditService.ACTION_RESERVE_BOOK, AuditService.MODULE_RESERVATIONS,
                    "Reserved book " + bookId + " for member " + memberId + " (queue: " + queue + ")");
                return null;
            }
            @Override protected void done() {
                try { get(); UiTheme.showSuccess(ReservationsPanel.this, "Reservation created."); clearForm(); loadData(); }
                catch (Exception ex) { UiTheme.showError(ReservationsPanel.this, ex.getMessage()); statusBar.setText("Error."); }
            }
        }.execute();
    }

    private void updateStatus(String newStatus) {
        if (!UserSession.canManageReservations()) {
            UiTheme.showWarning(this, "Access denied."); return;
        }
        int row = table.getSelectedRow();
        if (row < 0) { UiTheme.showError(this, "Select a reservation first."); return; }
        int mr = table.convertRowIndexToModel(row);
        String resId  = tableModel.getValueAt(mr, 0).toString();
        String current= tableModel.getValueAt(mr, 7).toString();
        if (!"Pending".equals(current)) {
            UiTheme.showWarning(this, "Only Pending reservations can be updated."); return;
        }
        if (!UiTheme.confirm(this, "Mark reservation " + resId + " as " + newStatus + "?")) return;

        statusBar.setText("Updating…");
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                Map<String, String> res = FirebaseBootstrap.getReservation(resId);
                if (res == null) throw new IllegalStateException("Reservation not found.");
                res.put("status",    newStatus);
                res.put("updatedAt", java.time.Instant.now().toString());
                FirebaseBootstrap.saveReservation(res);
                AuditService.log(AuditService.ACTION_CANCEL_RESERVE, AuditService.MODULE_RESERVATIONS,
                    "Reservation " + resId + " marked as " + newStatus, "Pending", newStatus);
                return null;
            }
            @Override protected void done() {
                try { get(); UiTheme.showSuccess(ReservationsPanel.this, "Reservation updated."); loadData(); }
                catch (Exception ex) { UiTheme.showError(ReservationsPanel.this, ex.getMessage()); statusBar.setText("Error."); }
            }
        }.execute();
    }

    private void clearForm() {
        fldBookId.setText(""); fldMemberId.setText("");
        lblBookTitle.setText("–"); lblMemberName.setText("–");
    }

    private static JLabel infoLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UiTheme.BODY_FONT);
        lbl.setForeground(UiTheme.MUTED);
        return lbl;
    }
}
