package library_management_system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class AboutPanel extends JPanel {

    public AboutPanel() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BACKGROUND);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UiTheme.BACKGROUND);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        // ── Hero banner ───────────────────────────────────────────────────────
        JPanel hero = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UiTheme.ACCENT_DARK, getWidth(), getHeight(), UiTheme.ACCENT_BLUE);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        hero.setOpaque(false);
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setBorder(new EmptyBorder(32, 40, 32, 40));

        JLabel logoLabel = new JLabel(UiTheme.createLogoIcon(80));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appName = new JLabel("Elab Library Management System");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 26));
        appName.setForeground(Color.WHITE);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel version = new JLabel("Version 2.0.0");
        version.setFont(UiTheme.BODY_FONT);
        version.setForeground(new Color(200, 220, 255));
        version.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("Smart digital library management for academic institutions");
        desc.setFont(UiTheme.BODY_FONT);
        desc.setForeground(new Color(180, 205, 245));
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        hero.add(logoLabel);
        hero.add(Box.createVerticalStrut(16));
        hero.add(appName);
        hero.add(Box.createVerticalStrut(6));
        hero.add(version);
        hero.add(Box.createVerticalStrut(8));
        hero.add(desc);

        // ── Info cards row ────────────────────────────────────────────────────
        JPanel infoRow = new JPanel(new java.awt.GridLayout(1, 3, 16, 0));
        infoRow.setOpaque(false);
        infoRow.setBorder(new EmptyBorder(16, 0, 16, 0));

        infoRow.add(infoCard("Technologies",
            "Java 17+\nJava Swing (GUI)\nFirebase Firestore\nHTTP REST API"));
        infoRow.add(infoCard("Developer",
            "Nkesiga Godfrey\nKampala International University\nFaculty of Computing\n& Information Technology"));
        infoRow.add(infoCard("License",
            "Academic Use License\n\u00A9 2026 Elab Team\nAll rights reserved\nKIU – Uganda"));

        // ── Usage guide ───────────────────────────────────────────────────────
        JPanel guideCard = UiTheme.makeCard();
        guideCard.setLayout(new BorderLayout(0, 10));
        guideCard.add(UiTheme.makeSectionTitle("Quick Usage Guide"), BorderLayout.NORTH);

        JTextArea guide = new JTextArea();
        guide.setEditable(false);
        guide.setLineWrap(true);
        guide.setWrapStyleWord(true);
        guide.setFont(UiTheme.BODY_FONT);
        guide.setForeground(UiTheme.TEXT);
        guide.setBackground(UiTheme.CARD);
        guide.setBorder(new EmptyBorder(8, 4, 8, 4));
        guide.setText(
            "DASHBOARD\n" +
            "  The dashboard shows live statistics: total books, available books, issued books, and registered members.\n" +
            "  Use Quick Actions to jump to any module. The Recent Activity table shows the last 5 issue records.\n\n" +
            "BOOKS\n" +
            "  Add, update, or delete books. Use the Auto ID button to generate a unique Book ID.\n" +
            "  Books can be grouped by KIU faculties, schools, or custom library categories. Click a row to populate the form.\n\n" +
            "MEMBERS\n" +
            "  Register students/members with their registration number, name, mobile, and KIU department or school.\n" +
            "  Use 'View History' to see all issue records for a selected member.\n\n" +
            "ISSUE BOOK\n" +
            "  Enter a Book ID and look it up to verify availability. Enter a Registration No and look up the member.\n" +
            "  The issue date defaults to today and the due date to today + borrowing period (configurable in Settings).\n\n" +
            "RETURN BOOK\n" +
            "  Enter the Book ID and Registration No to find the active issue record.\n" +
            "  The system calculates overdue days and fine automatically. Click 'Process Return' to complete.\n\n" +
            "TRANSACTIONS\n" +
            "  View all issue and return records. Filter by status or search text. Export to CSV.\n\n" +
            "REPORTS\n" +
            "  Generate reports for books, members, issue records, overdue books, or available books.\n" +
            "  Preview the data then export to a date-stamped CSV file.\n\n" +
            "SETTINGS\n" +
            "  Configure library name, fine rate per day, default borrowing period, and export folder.\n" +
            "  Settings are saved to ~/.elab-library.properties."
        );

        JScrollPane guideScroll = new JScrollPane(guide);
        guideScroll.setBorder(null);
        guideCard.add(guideScroll, BorderLayout.CENTER);

        root.add(hero,     BorderLayout.NORTH);
        root.add(infoRow,  BorderLayout.CENTER);
        root.add(guideCard, BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);
    }

    private JPanel infoCard(String title, String content) {
        JPanel card = UiTheme.makeCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel titleLbl = UiTheme.makeSectionTitle(title);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLbl.setBorder(new EmptyBorder(0, 0, 8, 0));
        card.add(titleLbl);

        for (String line : content.split("\n")) {
            JLabel lbl = new JLabel(line.isBlank() ? " " : line);
            lbl.setFont(UiTheme.BODY_FONT);
            lbl.setForeground(UiTheme.MUTED);
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(lbl);
        }
        return card;
    }
}
