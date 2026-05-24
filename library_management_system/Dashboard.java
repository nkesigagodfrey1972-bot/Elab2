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

        JPanel root = new JPanel(new BorderLayout(20, 20));
        root.setBorder(new EmptyBorder(22, 22, 22, 22));
        root.setBackground(UiTheme.BACKGROUND);

        JPanel hero = new JPanel(new BorderLayout(18, 0));
        hero.setOpaque(false);
        hero.putClientProperty("ui.theme.keepBackground", Boolean.TRUE);

        JLabel logo = UiTheme.createLogoBadge("EL");
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

        JButton refreshButton = createActionButton("Refresh stats", evt -> refreshStats());
        refreshButton.setPreferredSize(new Dimension(160, 40));
        hero.add(refreshButton, BorderLayout.EAST);

        JPanel statsGrid = new JPanel(new GridLayout(2, 2, 16, 16));
        statsGrid.setOpaque(false);
        statsGrid.putClientProperty("ui.theme.keepBackground", Boolean.TRUE);
        statsGrid.add(createStatCard("Books in catalog", booksValue, UiTheme.ACCENT_BLUE));
        statsGrid.add(createStatCard("Registered students", studentsValue, UiTheme.ACCENT));
        statsGrid.add(createStatCard("Issued items", issuedValue, new Color(62, 106, 214)));
        statsGrid.add(createStatCard("Available books", availableValue, new Color(245, 162, 59)));

        JPanel actionsPanel = new JPanel(new GridBagLayout());
        actionsPanel.putClientProperty("ui.theme.keepBackground", Boolean.TRUE);
        actionsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(214, 224, 236), 1, true),
            new EmptyBorder(18, 18, 18, 18)
        ));
        actionsPanel.setBackground(Color.WHITE);

        JLabel actionTitle = new JLabel("Quick actions");
        actionTitle.setFont(actionTitle.getFont().deriveFont(18f));
        actionTitle.setForeground(UiTheme.TEXT);

        JButton openHome = createActionButton("Open operations center", evt -> openWindow(new HOME()));
        JButton books = createActionButton("Books", evt -> openWindow(new SEARCH_RECORD()));
        JButton students = createActionButton("Students", evt -> openWindow(new STUDENT_RECORD()));
        JButton issues = createActionButton("Issue desk", evt -> openWindow(new ISSUE_RECORD1()));
        JButton exportBooks = createActionButton("Export books CSV", evt -> exportBooksCsv());
        JButton exportIssues = createActionButton("Export issue CSV", evt -> exportIssueCsv());

        JPanel buttonRow1 = new JPanel(new GridLayout(1, 3, 10, 10));
        buttonRow1.setOpaque(false);
        buttonRow1.add(openHome);
        buttonRow1.add(books);
        buttonRow1.add(students);

        JPanel buttonRow2 = new JPanel(new GridLayout(1, 3, 10, 10));
        buttonRow2.setOpaque(false);
        buttonRow2.add(issues);
        buttonRow2.add(exportBooks);
        buttonRow2.add(exportIssues);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 14, 0);
        actionsPanel.add(actionTitle, gbc);
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        actionsPanel.add(buttonRow1, gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 0, 0);
        actionsPanel.add(buttonRow2, gbc);

        overview.setEditable(false);
        overview.setLineWrap(true);
        overview.setWrapStyleWord(true);
        overview.setBorder(new EmptyBorder(12, 12, 12, 12));
        overview.setBackground(new Color(250, 252, 251));
        overview.setForeground(UiTheme.TEXT);
        overview.setText("This dashboard gives staff a quick operational snapshot and shortcuts to the core workflows. Use the export buttons to create CSV backups for reporting or spreadsheet review.");
        JScrollPane overviewPane = new JScrollPane(overview);
        overviewPane.putClientProperty("ui.theme.keepBackground", Boolean.TRUE);
        overviewPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(214, 224, 236), 1, true),
            new EmptyBorder(0, 0, 0, 0)
        ));

        JPanel lowerGrid = new JPanel(new GridLayout(1, 2, 16, 16));
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

    private JPanel createStatCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.putClientProperty("ui.theme.keepBackground", Boolean.TRUE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(214, 224, 236), 1, true),
            new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(UiTheme.MUTED);
        titleLabel.setFont(titleLabel.getFont().deriveFont(13f));

        valueLabel.setForeground(accent);
        valueLabel.setFont(valueLabel.getFont().deriveFont(32f));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
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
