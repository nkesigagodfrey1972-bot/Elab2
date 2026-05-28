package library_management_system;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

public class MainWindow extends JFrame {

    // Panel name constants
    public static final String PANEL_DASHBOARD    = "Dashboard";
    public static final String PANEL_BOOKS        = "Books";
    public static final String PANEL_MEMBERS      = "Members";
    public static final String PANEL_ISSUE        = "Issue Book";
    public static final String PANEL_RETURN       = "Return Book";
    public static final String PANEL_TRANSACTIONS = "Transactions";
    public static final String PANEL_REPORTS      = "Reports";
    public static final String PANEL_FINES        = "Fines";
    public static final String PANEL_RESERVATIONS = "Reservations";
    public static final String PANEL_AUDIT        = "Audit Logs";
    public static final String PANEL_SETTINGS     = "Settings";
    public static final String PANEL_ABOUT        = "Help / About";

    private final String username;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentArea   = new JPanel(cardLayout);
    private final JLabel datetimeLabel = new JLabel();
    private String activePanel = PANEL_DASHBOARD;

    // Sidebar nav buttons (null = hidden for this role)
    private JButton btnDashboard;
    private JButton btnBooks;
    private JButton btnMembers;
    private JButton btnIssue;
    private JButton btnReturn;
    private JButton btnTransactions;
    private JButton btnReports;
    private JButton btnFines;
    private JButton btnReservations;
    private JButton btnAudit;
    private JButton btnSettings;
    private JButton btnAbout;

    public MainWindow(String username) {
        this.username = username;
        initComponents();
        startClock();
        notifyReservationBookingsOnLogin();
    }

    private void initComponents() {
        setTitle("Elab Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        UiTheme.applyWindowIcon(this);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiTheme.BACKGROUND);

        root.add(buildTopBar(),  BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(contentArea,    BorderLayout.CENTER);

        contentArea.setBackground(UiTheme.BACKGROUND);

        // Always-visible panels
        contentArea.add(new DashboardPanel(this),    PANEL_DASHBOARD);
        contentArea.add(new AboutPanel(),            PANEL_ABOUT);

        // Role-gated panels — always add to CardLayout but access is checked at action time
        contentArea.add(new BooksPanel(),            PANEL_BOOKS);
        contentArea.add(new MembersPanel(),          PANEL_MEMBERS);
        contentArea.add(new IssuePanel(),            PANEL_ISSUE);
        contentArea.add(new ReturnPanel(),           PANEL_RETURN);
        contentArea.add(new TransactionsPanel(),     PANEL_TRANSACTIONS);
        contentArea.add(new ReportsPanel(),          PANEL_REPORTS);
        contentArea.add(new FinesPanel(),            PANEL_FINES);
        contentArea.add(new ReservationsPanel(),     PANEL_RESERVATIONS);
        contentArea.add(new AuditLogsPanel(),        PANEL_AUDIT);
        contentArea.add(new SettingsPanel(),         PANEL_SETTINGS);

        setContentPane(root);
        navigateTo(PANEL_DASHBOARD);
    }

    // ── Top bar ───────────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UiTheme.ACCENT_DARK);
        bar.setPreferredSize(new Dimension(0, 52));
        bar.setBorder(new EmptyBorder(0, 20, 0, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel logo = new JLabel(UiTheme.createLogoIcon(36));
        JLabel appName = new JLabel("Elab Library System");
        appName.setFont(UiTheme.HEADING_FONT);
        appName.setForeground(Color.WHITE);
        left.add(logo);
        left.add(appName);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);

        datetimeLabel.setFont(UiTheme.SMALL_FONT);
        datetimeLabel.setForeground(new Color(160, 180, 220));

        JLabel userLabel = new JLabel("\uD83D\uDC64 " + username);
        userLabel.setFont(UiTheme.BODY_FONT);
        userLabel.setForeground(new Color(200, 215, 240));

        // Role badge
        String role = UserSession.getRole();
        JLabel roleLabel = new JLabel(role);
        roleLabel.setFont(UiTheme.SMALL_FONT.deriveFont(Font.BOLD));
        roleLabel.setForeground(Color.WHITE);
        roleLabel.setOpaque(true);
        roleLabel.setBackground(UserSession.roleColor());
        roleLabel.setBorder(new EmptyBorder(3, 10, 3, 10));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(UiTheme.SMALL_FONT);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(new Color(196, 43, 28));
        logoutBtn.setOpaque(true);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorder(new EmptyBorder(5, 12, 5, 12));
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> logout());

        right.add(datetimeLabel);
        right.add(userLabel);
        right.add(roleLabel);
        right.add(logoutBtn);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UiTheme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, new Color(40, 55, 80)));

        sidebar.add(Box.createVerticalStrut(16));

        btnDashboard    = makeSidebarButton("\uD83C\uDFE0  Dashboard",    PANEL_DASHBOARD);
        btnBooks        = makeSidebarButton("\uD83D\uDCDA  Books",         PANEL_BOOKS);
        btnMembers      = makeSidebarButton("\uD83D\uDC65  Members",       PANEL_MEMBERS);
        btnIssue        = makeSidebarButton("\uD83D\uDCE4  Issue Book",    PANEL_ISSUE);
        btnReturn       = makeSidebarButton("\uD83D\uDCE5  Return Book",   PANEL_RETURN);
        btnTransactions = makeSidebarButton("\uD83D\uDCCB  Transactions",  PANEL_TRANSACTIONS);
        btnReports      = makeSidebarButton("\uD83D\uDCCA  Reports",       PANEL_REPORTS);
        btnFines        = makeSidebarButton("\uD83D\uDCB0  Fines",         PANEL_FINES);
        btnReservations = makeSidebarButton("\uD83D\uDCC5  Reservations",  PANEL_RESERVATIONS);
        btnAudit        = makeSidebarButton("\uD83D\uDDD2  Audit Logs",    PANEL_AUDIT);
        btnSettings     = makeSidebarButton("\u2699\uFE0F  Settings",      PANEL_SETTINGS);
        btnAbout        = makeSidebarButton("\u2753  Help / About",        PANEL_ABOUT);

        // Always visible
        sidebar.add(btnDashboard);

        // Visible to Viewer+
        sidebar.add(btnBooks);
        sidebar.add(btnMembers);

        // Visible to Assistant+
        if (UserSession.canIssueReturn()) {
            sidebar.add(btnIssue);
            sidebar.add(btnReturn);
        }

        if (UserSession.canCreateReservations()) {
            sidebar.add(btnReservations);
        }

        // Visible to Librarian+
        if (UserSession.canManageBooks()) {
            sidebar.add(btnTransactions);
            sidebar.add(btnFines);
        }

        // Visible to all
        sidebar.add(btnReports);

        sidebar.add(Box.createVerticalGlue());

        JLabel sep = new JLabel("─────────────────");
        sep.setForeground(new Color(50, 65, 90));
        sep.setFont(UiTheme.SMALL_FONT);
        sep.setBorder(new EmptyBorder(4, 16, 4, 16));
        sep.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(sep);

        // Admin-only
        if (UserSession.canViewAuditLogs()) {
            sidebar.add(btnAudit);
        }
        if (UserSession.canAccessSettings()) {
            sidebar.add(btnSettings);
        }
        sidebar.add(btnAbout);
        sidebar.add(Box.createVerticalStrut(16));

        return sidebar;
    }

    private JButton makeSidebarButton(String text, String panelName) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = panelName.equals(activePanel);
                boolean hover  = getModel().isRollover();
                if (active) {
                    g2.setColor(new Color(34, 91, 184, 200));
                    g2.fillRoundRect(6, 2, getWidth() - 12, getHeight() - 4, 8, 8);
                    g2.setColor(new Color(100, 180, 255));
                    g2.fillRoundRect(0, 4, 4, getHeight() - 8, 4, 4);
                } else if (hover) {
                    g2.setColor(new Color(255, 255, 255, 18));
                    g2.fillRoundRect(6, 2, getWidth() - 12, getHeight() - 4, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UiTheme.BODY_FONT);
        btn.setForeground(UiTheme.SIDEBAR_TEXT);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(11, 20, 11, 16));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> navigateTo(panelName));
        return btn;
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    public void navigateTo(String panelName) {
        // Access control check
        if (!hasAccess(panelName)) {
            UiTheme.showWarning(this, "Access denied. Your role (" + UserSession.getRole() +
                ") does not have permission to access: " + panelName);
            return;
        }
        activePanel = panelName;
        cardLayout.show(contentArea, panelName);
        repaintSidebarButtons();
    }

    private boolean hasAccess(String panelName) {
        return switch (panelName) {
            case PANEL_DASHBOARD, PANEL_BOOKS, PANEL_MEMBERS,
                 PANEL_REPORTS, PANEL_ABOUT -> true;
            case PANEL_ISSUE, PANEL_RETURN,
                 PANEL_RESERVATIONS         -> UserSession.canCreateReservations();
            case PANEL_TRANSACTIONS,
                 PANEL_FINES               -> UserSession.canManageBooks();
            case PANEL_AUDIT               -> UserSession.canViewAuditLogs();
            case PANEL_SETTINGS            -> UserSession.canAccessSettings();
            default                        -> true;
        };
    }

    private void repaintSidebarButtons() {
        JButton[] btns = {btnDashboard, btnBooks, btnMembers, btnIssue, btnReturn,
            btnTransactions, btnReports, btnFines, btnReservations, btnAudit, btnSettings, btnAbout};
        for (JButton b : btns) { if (b != null) b.repaint(); }
    }

    public String getUsername() { return username; }

    // ── Clock ─────────────────────────────────────────────────────────────────

    private void startClock() {
        updateDatetime();
        new Timer(1000, e -> updateDatetime()).start();
    }

    private void updateDatetime() {
        datetimeLabel.setText(LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy  HH:mm:ss")));
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    private void logout() {
        if (UiTheme.confirm(this, "Are you sure you want to logout?")) {
            AuditService.log(AuditService.ACTION_LOGOUT, AuditService.MODULE_AUTH,
                "User logged out: " + username);
            new LOGIN_FORM().setVisible(true);
            dispose();
        }
    }

    private void notifyReservationBookingsOnLogin() {
        if (!UserSession.canReceiveReservationNotifications()) {
            return;
        }
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                List<Map<String, String>> reservations = FirebaseBootstrap.listReservations();
                int pending = 0;
                int newToday = 0;
                String today = java.time.LocalDate.now().toString();
                for (Map<String, String> reservation : reservations) {
                    String status = reservation.getOrDefault("status", "");
                    if (!"Pending".equalsIgnoreCase(status)) {
                        continue;
                    }
                    pending++;
                    String reservedOn = reservation.getOrDefault("reservationDate", "");
                    if (today.equals(reservedOn)) {
                        newToday++;
                    }
                }
                if (pending == 0) {
                    return null;
                }
                if (newToday > 0) {
                    return newToday + " new booking(s) came in today.\n" +
                        pending + " pending booking(s) need librarian attention.";
                }
                return pending + " pending booking(s) need librarian attention.";
            }

            @Override
            protected void done() {
                try {
                    String message = get();
                    if (message != null && !message.isBlank()) {
                        JOptionPane.showMessageDialog(
                            MainWindow.this,
                            message,
                            "Reservation Notifications",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                } catch (Exception ignored) {
                    // Keep login flow smooth even if notifications fail to load.
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        UserSession.set("admin", UserSession.ROLE_ADMIN);
        java.awt.EventQueue.invokeLater(() -> new MainWindow("admin").setVisible(true));
    }
}
