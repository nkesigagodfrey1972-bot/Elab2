package library_management_system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class Dashboard extends JFrame {

    private final JLabel booksValue = createValueLabel();
    private final JLabel studentsValue = createValueLabel();
    private final JLabel issuedValue = createValueLabel();
    private final JLabel availableValue = createValueLabel();
    private final JLabel updatedValue = createSubtleLabel();
    private final JTextArea overview = new JTextArea(8, 28);

    public Dashboard() {
        initComponents();
        UiTheme.installFrameChrome(this, "Library Dashboard", "Overview, quick actions, and data exports");
        refreshStats();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setSize(1180, 760);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(24, 24));
        root.setBorder(new EmptyBorder(28, 28, 28, 28));
        root.setBackground(UiTheme.BACKGROUND);

        JPanel hero = new JPanel(new BorderLayout(20, 0));
        hero.setOpaque(false);
        hero.putClientProperty("ui.theme.keepBackground", Boolean.TRUE);

        JLabel logo = new JLabel(UiTheme.createLogoIcon(72));
        logo.setPreferredSize(new Dimension(72, 72));
        logo.setMaximumSize(new Dimension(72, 72));
        hero.add(logo, BorderLayout.WEST);

        JPanel heroText = new JPanel();
        heroText.setOpaque(false);
        heroText.setLayout(new BoxLayout(heroText, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Welcome back");
        title.setFont(title.getFont().deriveFont(28f));
        title.setForeground(UiTheme.TEXT);

        JLabel subtitle = new JLabel("Your books, students, and issue activity at a glance.");
        subtitle.setFont(subtitle.getFont().deriveFont(14f));
        subtitle.setForeground(UiTheme.MUTED);

        heroText.add(Box.createVerticalGlue());
        heroText.add(title);
        heroText.add(Box.createVerticalStrut(6));
        heroText.add(subtitle);
        heroText.add(Box.createVerticalStrut(10));
        heroText.add(updatedValue);
        heroText.add(Box.createVerticalGlue());

        hero.add(heroText, BorderLayout.CENTER);

        JButton refreshButton = createActionButton("Refresh stats", "refresh", UiTheme.ACCENT_BLUE, evt -> refreshStats());
        refreshButton.setPreferredSize(new Dimension(170, 44));
        hero.add(refreshButton, BorderLayout.EAST);

        JPanel statsGrid = new JPanel(new GridLayout(2, 1, 20, 20));
        statsGrid.setOpaque(false);
        statsGrid.putClientProperty("ui.theme.keepBackground", Boolean.TRUE);
        JPanel featuredRow = new JPanel(new GridLayout(1, 2, 20, 20));
        featuredRow.setOpaque(false);
        featuredRow.putClientProperty("ui.theme.keepBackground", Boolean.TRUE);
        featuredRow.add(createStatCard("Books in catalog", booksValue, UiTheme.ACCENT_BLUE, true));
        featuredRow.add(createStatCard("Issued items", issuedValue, UiTheme.ACCENT, true));

        JPanel supportRow = new JPanel(new GridLayout(1, 2, 20, 20));
        supportRow.setOpaque(false);
        supportRow.putClientProperty("ui.theme.keepBackground", Boolean.TRUE);
        supportRow.add(createStatCard("Registered students", studentsValue, new Color(62, 106, 214), false));
        supportRow.add(createStatCard("Available books", availableValue, new Color(245, 162, 59), false));

        statsGrid.add(featuredRow);
        statsGrid.add(supportRow);

        JPanel actionsPanel = new JPanel(new GridBagLayout());
        actionsPanel.putClientProperty("ui.theme.keepBackground", Boolean.TRUE);
        actionsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(214, 224, 236), 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        actionsPanel.setBackground(Color.WHITE);

        JLabel actionTitle = new JLabel("Quick actions");
        actionTitle.setFont(actionTitle.getFont().deriveFont(18f));
        actionTitle.setForeground(UiTheme.TEXT);

        JButton openHome = createActionButton("Open operations center", "home", UiTheme.ACCENT_BLUE, evt -> openWindow(new HOME()));
        JButton books = createActionButton("Books", "book", UiTheme.ACCENT, evt -> openWindow(new SEARCH_RECORD()));
        JButton students = createActionButton("Students", "users", UiTheme.ACCENT_BLUE, evt -> openWindow(new STUDENT_RECORD()));
        JButton issues = createActionButton("Issue desk", "issue", new Color(206, 100, 20), evt -> openWindow(new ISSUE_RECORD1()));
        JButton exportBooks = createActionButton("Export books CSV", "export", UiTheme.ACCENT_BLUE, evt -> exportBooksCsv());
        JButton exportIssues = createActionButton("Export issue CSV", "export", UiTheme.ACCENT, evt -> exportIssueCsv());

        JPanel buttonRow1 = new JPanel(new GridLayout(1, 3, 12, 12));
        buttonRow1.setOpaque(false);
        buttonRow1.add(openHome);
        buttonRow1.add(books);
        buttonRow1.add(students);

        JPanel buttonRow2 = new JPanel(new GridLayout(1, 3, 12, 12));
        buttonRow2.setOpaque(false);
        buttonRow2.add(issues);
        buttonRow2.add(exportBooks);
        buttonRow2.add(exportIssues);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 16, 0);
        actionsPanel.add(actionTitle, gbc);
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        actionsPanel.add(buttonRow1, gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(12, 0, 0, 0);
        actionsPanel.add(buttonRow2, gbc);

        overview.setEditable(false);
        overview.setLineWrap(true);
        overview.setWrapStyleWord(true);
        overview.setBorder(new EmptyBorder(14, 14, 14, 14));
        overview.setBackground(new Color(250, 252, 251));
        overview.setForeground(UiTheme.TEXT);
        overview.setText("This dashboard gives staff a quick operational snapshot and shortcuts to the core workflows. Use the export buttons to create CSV backups for reporting or spreadsheet review.");
        JScrollPane overviewPane = new JScrollPane(overview);
        overviewPane.putClientProperty("ui.theme.keepBackground", Boolean.TRUE);
        overviewPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(214, 224, 236), 1, true),
            new EmptyBorder(0, 0, 0, 0)
        ));

        JPanel lowerGrid = new JPanel(new GridLayout(1, 2, 20, 20));
        lowerGrid.setOpaque(false);
        lowerGrid.putClientProperty("ui.theme.keepBackground", Boolean.TRUE);
        lowerGrid.add(actionsPanel);
        lowerGrid.add(overviewPane);

        root.add(hero, BorderLayout.NORTH);
        root.add(statsGrid, BorderLayout.CENTER);
        root.add(lowerGrid, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void refreshStats() {
        try {
            int booksCount = FirebaseBootstrap.countBooks();
            int studentsCount = FirebaseBootstrap.countStudents();
            int issuedCount = FirebaseBootstrap.countIssuedBooks();
            int availableCount = FirebaseBootstrap.countAvailableBooks();
            booksValue.setText(String.valueOf(booksCount));
            studentsValue.setText(String.valueOf(studentsCount));
            issuedValue.setText(String.valueOf(issuedCount));
            availableValue.setText(String.valueOf(availableCount));
            updatedValue.setText("Last updated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not refresh stats: " + ex.getMessage(), "Dashboard", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JButton createActionButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        return button;
    }

    private JButton createActionButton(String text, String iconKind, Color iconColor, java.awt.event.ActionListener listener) {
        JButton button = createActionButton(text, listener);
        UiTheme.decorateIconButton(button, iconKind, iconColor);
        return button;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accent, boolean featured) {
        JPanel card = new RoundedCard(featured);
        card.putClientProperty("ui.theme.keepBackground", Boolean.TRUE);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(featured ? new Color(231, 240, 255) : UiTheme.MUTED);
        titleLabel.setFont(titleLabel.getFont().deriveFont(13f));

        valueLabel.setForeground(featured ? Color.WHITE : accent);
        valueLabel.setFont(valueLabel.getFont().deriveFont(32f));

        JLabel chip = new JLabel(featured ? "Featured" : "Live");
        chip.setOpaque(true);
        chip.putClientProperty("ui.theme.keepBackground", Boolean.TRUE);
        chip.setForeground(featured ? new Color(255, 255, 255) : UiTheme.ACCENT_DARK);
        chip.setBackground(featured ? new Color(255, 255, 255, 24) : new Color(255, 237, 214));
        chip.setBorder(new EmptyBorder(4, 10, 4, 10));
        chip.setFont(chip.getFont().deriveFont(11f));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.add(chip);
        inner.add(Box.createVerticalStrut(12));
        inner.add(titleLabel);
        inner.add(Box.createVerticalStrut(8));
        inner.add(valueLabel);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JLabel createValueLabel() {
        JLabel label = new JLabel("0");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createSubtleLabel() {
        JLabel label = new JLabel("Last updated: -");
        label.setForeground(UiTheme.MUTED);
        label.setFont(label.getFont().deriveFont(13f));
        return label;
    }

    private void openWindow(JFrame frame) {
        frame.setVisible(true);
        dispose();
    }

    private void exportCsv(String label, List<Map<String, String>> rows, Path targetPath) {
        try {
            if (rows.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No " + label + " available to export.");
                return;
            }

            StringBuilder csv = new StringBuilder();
            List<String> headers = new ArrayList<>(rows.get(0).keySet());
            csv.append(String.join(",", headers)).append(System.lineSeparator());
            for (Map<String, String> row : rows) {
                for (int index = 0; index < headers.size(); index++) {
                    if (index > 0) {
                        csv.append(',');
                    }
                    csv.append(csvEscape(row.getOrDefault(headers.get(index), "")));
                }
                csv.append(System.lineSeparator());
            }

            Files.writeString(targetPath, csv.toString(), StandardCharsets.UTF_8);
            JOptionPane.showMessageDialog(this, label + " exported to:\n" + targetPath);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Dashboard", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportBooksCsv() {
        try {
            exportCsv("books", FirebaseBootstrap.listBooks(), Path.of(System.getProperty("user.home"), "books-export.csv"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Dashboard", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportIssueCsv() {
        try {
            exportCsv("issue records", FirebaseBootstrap.listIssueRecords(), Path.of(System.getProperty("user.home"), "issue-records-export.csv"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Dashboard", JOptionPane.ERROR_MESSAGE);
        }
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
            java.awt.Graphics2D graphics2d = (java.awt.Graphics2D) graphics.create();
            graphics2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            java.awt.Paint paint = featured
                ? new java.awt.GradientPaint(0, 0, UiTheme.ACCENT_DARK, getWidth(), getHeight(), UiTheme.ACCENT_BLUE)
                : Color.WHITE;
            graphics2d.setPaint(paint);
            graphics2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
            if (featured) {
                graphics2d.setColor(new Color(255, 255, 255, 36));
                graphics2d.fillOval(getWidth() - 70, -8, 100, 100);
                graphics2d.setColor(new Color(255, 255, 255, 18));
                graphics2d.fillOval(-28, getHeight() - 62, 120, 120);
            } else {
                graphics2d.setColor(new Color(214, 224, 236));
                graphics2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
            }
            graphics2d.dispose();
        }
    }

    private String csvEscape(String value) {
        String safe = value == null ? "" : value;
        if (safe.contains(",") || safe.contains("\"") || safe.contains("\n")) {
            return '"' + safe.replace("\"", "\"\"") + '"';
        }
        return safe;
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new Dashboard().setVisible(true));
    }
}
