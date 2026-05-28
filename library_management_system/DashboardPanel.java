package library_management_system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class DashboardPanel extends JPanel {

    private final MainWindow mainWindow;

    // Primary stat cards
    private final JLabel totalBooksVal      = bigStatLabel("–");
    private final JLabel availBooksVal      = bigStatLabel("–");
    private final JLabel issuedBooksVal     = bigStatLabel("–");
    private final JLabel totalMembersVal    = bigStatLabel("–");

    // Secondary stat cards
    private final JLabel pendingResVal      = bigStatLabel("–");
    private final JLabel pendingFinesVal    = bigStatLabel("–");
    private final JLabel overdueCountVal    = bigStatLabel("–");

    private final JLabel statusLabel = new JLabel("Click Refresh to load data");

    private final DefaultTableModel recentModel = new DefaultTableModel(
        new String[]{"Book ID", "Book Name", "Member", "Issue Date", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable recentTable = new JTable(recentModel);

    private final JPanel alertsPanel = new JPanel();

    public DashboardPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout(0, 0));
        setBackground(UiTheme.BACKGROUND);
        buildUI();
        refreshData();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(UiTheme.BACKGROUND);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel welcome = new JLabel("Welcome back, " + mainWindow.getUsername() + " \uD83D\uDC4B");
        welcome.setFont(UiTheme.TITLE_FONT);
        welcome.setForeground(UiTheme.TEXT);
        statusLabel.setFont(UiTheme.SMALL_FONT);
        statusLabel.setForeground(UiTheme.MUTED);
        JPanel headerLeft = new JPanel();
        headerLeft.setOpaque(false);
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.add(welcome);
        headerLeft.add(Box.createVerticalStrut(4));
        headerLeft.add(statusLabel);
        header.add(headerLeft, BorderLayout.WEST);
        JButton refreshBtn = UiTheme.makePrimaryButton("\u21BB  Refresh");
        refreshBtn.addActionListener(e -> refreshData());
        header.add(refreshBtn, BorderLayout.EAST);

        // ── Primary stat cards (row 1) ────────────────────────────────────────
        JPanel primaryStats = new JPanel(new GridLayout(1, 4, 16, 0));
        primaryStats.setOpaque(false);
        primaryStats.add(buildStatCard("Total Books",        totalBooksVal,   new Color(34, 91, 184),  "\uD83D\uDCDA"));
        primaryStats.add(buildStatCard("Available Books",    availBooksVal,   new Color(34, 139, 34),  "\u2705"));
        primaryStats.add(buildStatCard("Issued Books",       issuedBooksVal,  new Color(230, 126, 34), "\uD83D\uDCE4"));
        primaryStats.add(buildStatCard("Registered Members", totalMembersVal, new Color(142, 68, 173), "\uD83D\uDC65"));

        // ── Secondary stat cards (row 2) ──────────────────────────────────────
        JPanel secondaryStats = new JPanel(new GridLayout(1, 3, 16, 0));
        secondaryStats.setOpaque(false);
        secondaryStats.add(buildStatCard("Pending Reservations", pendingResVal,   new Color(52, 152, 219), "\uD83D\uDCC5"));
        secondaryStats.add(buildStatCard("Pending Fines",        pendingFinesVal, new Color(192, 57, 43),  "\uD83D\uDCB0"));
        secondaryStats.add(buildStatCard("Overdue Books",        overdueCountVal, new Color(211, 84, 0),   "\u23F0"));

        // ── Alerts panel ──────────────────────────────────────────────────────
        alertsPanel.setLayout(new BoxLayout(alertsPanel, BoxLayout.Y_AXIS));
        alertsPanel.setOpaque(false);

        // ── Quick actions ─────────────────────────────────────────────────────
        JPanel actionsCard = UiTheme.makeCard();
        actionsCard.setLayout(new BorderLayout(0, 12));
        JLabel actTitle = UiTheme.makeSectionTitle("Quick Actions");
        JPanel btnGrid = new JPanel(new GridLayout(2, 3, 10, 10));
        btnGrid.setOpaque(false);
        btnGrid.add(quickBtn("+ Add Book",          () -> mainWindow.navigateTo(MainWindow.PANEL_BOOKS)));
        btnGrid.add(quickBtn("\uD83D\uDCE4 Issue Book",      () -> mainWindow.navigateTo(MainWindow.PANEL_ISSUE)));
        btnGrid.add(quickBtn("\uD83D\uDCE5 Return Book",     () -> mainWindow.navigateTo(MainWindow.PANEL_RETURN)));
        btnGrid.add(quickBtn("+ Register Member",   () -> mainWindow.navigateTo(MainWindow.PANEL_MEMBERS)));
        btnGrid.add(quickBtn("\uD83D\uDCC5 Reservations",    () -> mainWindow.navigateTo(MainWindow.PANEL_RESERVATIONS)));
        btnGrid.add(quickBtn("\uD83D\uDCCA Reports",         () -> mainWindow.navigateTo(MainWindow.PANEL_REPORTS)));
        actionsCard.add(actTitle, BorderLayout.NORTH);
        actionsCard.add(btnGrid,  BorderLayout.CENTER);

        // ── Recent activity ───────────────────────────────────────────────────
        UiTheme.styleTable(recentTable);
        JScrollPane recentScroll = UiTheme.makeTableScrollPane(recentTable);
        recentScroll.setPreferredSize(new Dimension(0, 160));
        JPanel recentCard = UiTheme.makeCard();
        recentCard.setLayout(new BorderLayout(0, 10));
        recentCard.add(UiTheme.makeSectionTitle("Recent Issue Activity"), BorderLayout.NORTH);
        recentCard.add(recentScroll, BorderLayout.CENTER);

        // ── Layout assembly ───────────────────────────────────────────────────
        JPanel statsSection = new JPanel(new GridLayout(2, 1, 0, 12));
        statsSection.setOpaque(false);
        statsSection.add(primaryStats);
        statsSection.add(secondaryStats);

        JPanel lowerRow = new JPanel(new GridLayout(1, 2, 16, 0));
        lowerRow.setOpaque(false);
        lowerRow.add(actionsCard);
        lowerRow.add(recentCard);

        JPanel south = new JPanel(new BorderLayout(0, 12));
        south.setOpaque(false);
        south.add(alertsPanel, BorderLayout.NORTH);
        south.add(lowerRow,    BorderLayout.CENTER);

        root.add(header,       BorderLayout.NORTH);
        root.add(statsSection, BorderLayout.CENTER);
        root.add(south,        BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);
    }

    private JPanel buildStatCard(String label, JLabel valueLabel, Color accent, String emoji) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, accent, getWidth(), getHeight(), accent.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillOval(getWidth() - 60, -20, 100, 100);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.setPreferredSize(new Dimension(0, 110));

        JLabel emojiLbl = new JLabel(emoji);
        emojiLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(Color.WHITE);

        JLabel nameLbl = new JLabel(label);
        nameLbl.setFont(UiTheme.SMALL_FONT);
        nameLbl.setForeground(new Color(220, 235, 255));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.add(emojiLbl);
        inner.add(Box.createVerticalStrut(4));
        inner.add(valueLabel);
        inner.add(Box.createVerticalStrut(2));
        inner.add(nameLbl);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JButton quickBtn(String text, Runnable action) {
        JButton btn = UiTheme.makeStyledButton(text, new Color(240, 244, 252), UiTheme.ACCENT_BLUE);
        btn.setFont(UiTheme.BODY_FONT);
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private static JLabel bigStatLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    public void refreshData() {
        statusLabel.setText("Loading…");
        new SwingWorker<DashboardData, Void>() {
            @Override protected DashboardData doInBackground() throws Exception {
                DashboardData d = new DashboardData();
                d.totalBooks    = FirebaseBootstrap.countBooks();
                d.availBooks    = FirebaseBootstrap.countAvailableBooks();
                d.issuedBooks   = FirebaseBootstrap.countIssuedBooks();
                d.totalMembers  = FirebaseBootstrap.countStudents();
                d.pendingRes    = FirebaseBootstrap.countPendingReservations();
                d.issueRecords  = FirebaseBootstrap.listIssueRecords();

                // Count overdue and pending fines
                LocalDate today = LocalDate.now();
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                for (Map<String, String> r : d.issueRecords) {
                    if (!"yes".equalsIgnoreCase(r.getOrDefault("issued", ""))) continue;
                    String ds = r.getOrDefault("issueDate", "");
                    if (ds.isBlank()) continue;
                    try {
                        int borrowDays = AppSettings.borrowingDays();
                        if (LocalDate.parse(ds, fmt).plusDays(borrowDays).isBefore(today)) d.overdueCount++;
                    } catch (Exception ignored) {}
                }

                // Sum pending fines
                List<Map<String, String>> fines = FirebaseBootstrap.listCollectionFull("fines");
                for (Map<String, String> f : fines) {
                    if ("Pending".equalsIgnoreCase(f.getOrDefault("status", ""))) {
                        try { d.pendingFinesTotal += Double.parseDouble(f.getOrDefault("amount", "0")); }
                        catch (NumberFormatException ignored) {}
                    }
                }
                return d;
            }

            @Override protected void done() {
                try {
                    DashboardData d = get();
                    totalBooksVal.setText(String.valueOf(d.totalBooks));
                    availBooksVal.setText(String.valueOf(d.availBooks));
                    issuedBooksVal.setText(String.valueOf(d.issuedBooks));
                    totalMembersVal.setText(String.valueOf(d.totalMembers));
                    pendingResVal.setText(String.valueOf(d.pendingRes));
                    pendingFinesVal.setText(String.format("%.0f", d.pendingFinesTotal));
                    overdueCountVal.setText(String.valueOf(d.overdueCount));
                    statusLabel.setText("Last updated: " + LocalDate.now()
                        .format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
                    populateRecentTable(d.issueRecords);
                    buildAlerts(d);
                } catch (Exception ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void populateRecentTable(List<Map<String, String>> records) {
        recentModel.setRowCount(0);
        int count = 0;
        for (int i = records.size() - 1; i >= 0 && count < 5; i--, count++) {
            Map<String, String> r = records.get(i);
            recentModel.addRow(new Object[]{
                r.getOrDefault("bookId",      ""),
                r.getOrDefault("bookName",    ""),
                r.getOrDefault("studentName", ""),
                r.getOrDefault("issueDate",   ""),
                "yes".equalsIgnoreCase(r.getOrDefault("issued", "")) ? "Issued" : "Returned"
            });
        }
    }

    private void buildAlerts(DashboardData d) {
        alertsPanel.removeAll();

        if (d.overdueCount > 0) {
            alertsPanel.add(buildAlert(
                "\u26A0\uFE0F  " + d.overdueCount + " book(s) are overdue. Please follow up with members.",
                new Color(255, 243, 205), new Color(230, 126, 34), new Color(120, 60, 0)));
        }
        if (d.pendingRes > 0) {
            alertsPanel.add(buildAlert(
                "\uD83D\uDCC5  " + d.pendingRes + " pending reservation(s) awaiting fulfillment.",
                new Color(213, 234, 255), new Color(52, 152, 219), new Color(20, 60, 120)));
        }
        if (d.pendingFinesTotal > 0) {
            alertsPanel.add(buildAlert(
                "\uD83D\uDCB0  Pending fines total: " + String.format("%.2f", d.pendingFinesTotal) + ". Review in Fines module.",
                new Color(255, 220, 220), new Color(192, 57, 43), new Color(100, 20, 20)));
        }

        alertsPanel.revalidate();
        alertsPanel.repaint();
    }

    private JPanel buildAlert(String message, Color bg, Color border, Color fg) {
        JPanel alert = new JPanel(new BorderLayout());
        alert.setBackground(bg);
        alert.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        alert.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(border, 1, true),
            new EmptyBorder(8, 14, 8, 14)
        ));
        JLabel lbl = new JLabel(message);
        lbl.setFont(UiTheme.BODY_FONT);
        lbl.setForeground(fg);
        alert.add(lbl, BorderLayout.CENTER);
        return alert;
    }

    // ── Data container ────────────────────────────────────────────────────────

    private static class DashboardData {
        int totalBooks, availBooks, issuedBooks, totalMembers, pendingRes, overdueCount;
        double pendingFinesTotal;
        List<Map<String, String>> issueRecords;
    }
}
