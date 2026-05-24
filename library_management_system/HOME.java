package library_management_system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class HOME extends JFrame {

    private final JLabel booksValue = createMetricValue();
    private final JLabel studentsValue = createMetricValue();
    private final JLabel issuedValue = createMetricValue();
    private final JLabel availableValue = createMetricValue();
    private final JTextArea guidance = new JTextArea(8, 30);

    public HOME() {
        initComponents();
        UiTheme.installFrameChrome(this, "Operations Center", "Manage books, students, and issue workflows");
        refreshMetrics();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1160, 760));
        setSize(1220, 800);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(24, 24));
        root.setBorder(new EmptyBorder(28, 28, 28, 28));
        root.setBackground(UiTheme.BACKGROUND);

        JPanel hero = new JPanel(new BorderLayout(20, 0));
        hero.setOpaque(false);
        hero.putClientProperty("ui.theme.keepBackground", Boolean.TRUE);

        JLabel logo = new JLabel(UiTheme.createLogoIcon(72));
        hero.add(logo, BorderLayout.WEST);

        JPanel heroText = new JPanel();
        heroText.setOpaque(false);
        heroText.setLayout(new BoxLayout(heroText, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Operations Center");
        title.setFont(title.getFont().deriveFont(28f));
        title.setForeground(UiTheme.TEXT);

        JLabel subtitle = new JLabel("Everything staff need to run the library from one place.");
        subtitle.setFont(subtitle.getFont().deriveFont(14f));
        subtitle.setForeground(UiTheme.MUTED);

        heroText.add(Box.createVerticalGlue());
        heroText.add(title);
        heroText.add(Box.createVerticalStrut(6));
        heroText.add(subtitle);
        heroText.add(Box.createVerticalStrut(10));
        heroText.add(createHintLabel("Use the quick actions to open the relevant module immediately."));
        heroText.add(Box.createVerticalGlue());

        hero.add(heroText, BorderLayout.CENTER);

        JButton refreshButton = createActionButton("Refresh", "refresh", UiTheme.ACCENT_BLUE, evt -> refreshMetrics());
        refreshButton.setPreferredSize(new Dimension(170, 44));
        hero.add(refreshButton, BorderLayout.EAST);

        JPanel metricsGrid = new JPanel(new GridLayout(1, 4, 20, 20));
        metricsGrid.setOpaque(false);
        metricsGrid.putClientProperty("ui.theme.keepBackground", Boolean.TRUE);
        metricsGrid.add(createMetricCard("Books", booksValue, UiTheme.ACCENT_BLUE));
        metricsGrid.add(createMetricCard("Students", studentsValue, UiTheme.ACCENT));
        metricsGrid.add(createMetricCard("Issued", issuedValue, new Color(62, 106, 214)));
        metricsGrid.add(createMetricCard("Available", availableValue, new Color(245, 162, 59)));

        JPanel navigationPanel = new JPanel(new BorderLayout(16, 16));
        navigationPanel.putClientProperty("ui.theme.keepBackground", Boolean.TRUE);
        navigationPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(214, 224, 236), 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        navigationPanel.setBackground(Color.WHITE);

        JLabel navTitle = new JLabel("Quick navigation");
        navTitle.setFont(navTitle.getFont().deriveFont(18f));
        navTitle.setForeground(UiTheme.TEXT);

        JPanel navGrid = new JPanel(new GridLayout(3, 3, 12, 12));
        navGrid.setOpaque(false);
        navGrid.add(createNavButton("Books Catalog", "book", UiTheme.ACCENT, evt -> openWindow(new SEARCH_RECORD())));
        navGrid.add(createNavButton("Book ID Search", "book", UiTheme.ACCENT_BLUE, evt -> openWindow(new SEARCH_BOOKID())));
        navGrid.add(createNavButton("Book Name Search", "book", UiTheme.ACCENT_BLUE, evt -> openWindow(new SEARCH_BOOKNAME())));
        navGrid.add(createNavButton("Category Search", "book", UiTheme.ACCENT, evt -> openWindow(new CATEGORY_SEARCH())));
        navGrid.add(createNavButton("Students", "users", UiTheme.ACCENT_BLUE, evt -> openWindow(new STUDENT_RECORD())));
        navGrid.add(createNavButton("Issue Desk", "issue", new Color(206, 100, 20), evt -> openWindow(new ISSUE_RECORD1())));
        navGrid.add(createNavButton("Issued / Returned", "issue", new Color(34, 91, 184), evt -> openWindow(new YES_NO_RECORD())));
        navGrid.add(createNavButton("Dashboard", "home", UiTheme.ACCENT_BLUE, evt -> openWindow(new Dashboard())));
        navGrid.add(createNavButton("Logout", "home", new Color(206, 100, 20), evt -> logout()));

        JPanel navWrap = new JPanel(new BorderLayout(0, 12));
        navWrap.setOpaque(false);
        navWrap.add(navTitle, BorderLayout.NORTH);
        navWrap.add(navGrid, BorderLayout.CENTER);

        guidance.setEditable(false);
        guidance.setLineWrap(true);
        guidance.setWrapStyleWord(true);
        guidance.setBorder(new EmptyBorder(14, 14, 14, 14));
        guidance.setBackground(new Color(250, 252, 251));
        guidance.setForeground(UiTheme.TEXT);
        guidance.setText("This screen acts as the staff operations center. Open a module, complete the task, then return here for the next workflow. The dashboard is the main overview screen for live counts and exports.");

        JScrollPane guidancePane = new JScrollPane(guidance);
        guidancePane.putClientProperty("ui.theme.keepBackground", Boolean.TRUE);
        guidancePane.setBorder(BorderFactory.createLineBorder(new Color(214, 224, 236), 1, true));

        JPanel lowerGrid = new JPanel(new GridLayout(1, 2, 20, 20));
        lowerGrid.setOpaque(false);
        lowerGrid.putClientProperty("ui.theme.keepBackground", Boolean.TRUE);
        lowerGrid.add(navigationPanel);
        lowerGrid.add(guidancePane);

        navigationPanel.add(navWrap, BorderLayout.CENTER);

        root.add(hero, BorderLayout.NORTH);
        root.add(metricsGrid, BorderLayout.CENTER);
        root.add(lowerGrid, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void refreshMetrics() {
        try {
            booksValue.setText(String.valueOf(FirebaseBootstrap.countBooks()));
            studentsValue.setText(String.valueOf(FirebaseBootstrap.countStudents()));
            issuedValue.setText(String.valueOf(FirebaseBootstrap.countIssuedBooks()));
            availableValue.setText(String.valueOf(FirebaseBootstrap.countAvailableBooks()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not refresh metrics: " + ex.getMessage(), "Operations Center", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JButton createActionButton(String text, String iconKind, Color iconColor, ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        UiTheme.decorateIconButton(button, iconKind, iconColor);
        return button;
    }

    private JButton createNavButton(String text, String iconKind, Color iconColor, ActionListener listener) {
        JButton button = createActionButton(text, iconKind, iconColor, listener);
        button.setPreferredSize(new Dimension(0, 50));
        return button;
    }

    private JPanel createMetricCard(String label, JLabel valueLabel, Color accent) {
        RoundedCard card = new RoundedCard(false);
        card.putClientProperty("ui.theme.keepBackground", Boolean.TRUE);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel(label);
        title.setForeground(UiTheme.MUTED);

        valueLabel.setForeground(accent);
        valueLabel.setFont(valueLabel.getFont().deriveFont(32f));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.add(title);
        inner.add(Box.createVerticalStrut(10));
        inner.add(valueLabel);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private static final class RoundedCard extends JPanel {

        private final boolean featured;

        private RoundedCard(boolean featured) {
            this.featured = featured;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(java.awt.Graphics graphics) {
            super.paintComponent(graphics);
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) graphics.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            java.awt.Paint paint = Color.WHITE;
            g2.setPaint(paint);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g2.setColor(new Color(214, 224, 236));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g2.dispose();
        }
    }

    private JLabel createMetricValue() {
        JLabel label = new JLabel("0");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createHintLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(UiTheme.MUTED);
        label.setFont(label.getFont().deriveFont(13f));
        return label;
    }

    private void openWindow(JFrame frame) {
        frame.setVisible(true);
        dispose();
    }

    private void logout() {
        new LOGIN_FORM().setVisible(true);
        dispose();
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new HOME().setVisible(true));
    }
}
