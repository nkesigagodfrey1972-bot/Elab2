package library_management_system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

public final class WelcomeScreen extends JFrame {

    private final Runnable continueAction;
    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private final Timer progressTimer;
    private final Timer autoContinueTimer;

    public WelcomeScreen(Runnable continueAction) {
        this.continueAction = continueAction;
        this.progressTimer = new Timer(35, createProgressUpdater());
        this.autoContinueTimer = new Timer(2600, event -> continueToLogin());
        this.autoContinueTimer.setRepeats(false);
        initComponents();
    }

    private void initComponents() {
        setTitle("Elab Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createLineBorder(UiTheme.ACCENT_DARK, 1));

        JPanel leftPanel = new GradientPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(new EmptyBorder(42, 42, 42, 42));

        JLabel badge = new JLabel(UiTheme.createLogoIcon(92));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brand = new JLabel("ELAB LIBRARY");
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setFont(brand.getFont().deriveFont(32f));

        JLabel subtitle = new JLabel("Management System");
        subtitle.setForeground(new Color(227, 255, 236));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setFont(subtitle.getFont().deriveFont(20f));

        JLabel message = new JLabel("Fast. Clean. Ready for staff use.");
        message.setForeground(new Color(210, 245, 223));
        message.setAlignmentX(Component.LEFT_ALIGNMENT);
        message.setFont(message.getFont().deriveFont(14f));

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(badge);
        leftPanel.add(Box.createVerticalStrut(18));
        leftPanel.add(brand);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(subtitle);
        leftPanel.add(Box.createVerticalStrut(24));
        leftPanel.add(message);
        leftPanel.add(Box.createVerticalStrut(18));
        leftPanel.add(featureLine("Books, students, and issue records in one place"));
        leftPanel.add(featureLine("Firestore-backed and easier to maintain"));
        leftPanel.add(featureLine("Built for simple day-to-day librarian work"));
        leftPanel.add(Box.createVerticalGlue());

        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(new Color(245, 249, 246));
        rightPanel.setBorder(new EmptyBorder(36, 32, 36, 32));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        JLabel welcome = new JLabel("Welcome");
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        welcome.setFont(welcome.getFont().deriveFont(28f));
        welcome.setForeground(new Color(33, 61, 45));

        JLabel hint = new JLabel("Click continue to access the login screen.");
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setForeground(new Color(85, 100, 90));
        hint.setFont(hint.getFont().deriveFont(14f));

        JButton continueButton = new JButton("Continue");
        continueButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        continueButton.setFocusPainted(false);
        continueButton.setBackground(new Color(26, 112, 62));
        continueButton.setForeground(Color.WHITE);
        continueButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        continueButton.addActionListener(event -> continueToLogin());

        progressBar.setStringPainted(false);
        progressBar.setValue(0);
        progressBar.setPreferredSize(new Dimension(260, 8));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        progressBar.setForeground(new Color(30, 140, 74));
        progressBar.setBackground(new Color(224, 232, 226));

        rightPanel.add(Box.createVerticalGlue());
        rightPanel.add(welcome);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(hint);
        rightPanel.add(Box.createVerticalStrut(28));
        rightPanel.add(continueButton);
        rightPanel.add(Box.createVerticalStrut(28));
        rightPanel.add(progressBar);
        rightPanel.add(Box.createVerticalStrut(12));
        rightPanel.add(new JLabel("Preparing your workspace...", SwingConstants.LEFT));
        rightPanel.add(Box.createVerticalGlue());

        root.add(leftPanel, BorderLayout.CENTER);
        root.add(rightPanel, BorderLayout.EAST);
        setContentPane(root);

        pack();
        setSize(880, 460);
        setLocationRelativeTo(null);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            progressTimer.start();
            autoContinueTimer.start();
        } else {
            progressTimer.stop();
            autoContinueTimer.stop();
        }
    }

    private ActionListener createProgressUpdater() {
        return event -> {
            int nextValue = Math.min(progressBar.getValue() + 3, progressBar.getMaximum());
            progressBar.setValue(nextValue);
            if (nextValue >= progressBar.getMaximum()) {
                progressTimer.stop();
            }
        };
    }

    private void continueToLogin() {
        if (!isDisplayable()) {
            return;
        }
        progressTimer.stop();
        autoContinueTimer.stop();
        dispose();
        if (continueAction != null) {
            continueAction.run();
        }
    }

    private JLabel featureLine(String text) {
        JLabel label = new JLabel("• " + text);
        label.setForeground(new Color(220, 238, 226));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(2, 0, 2, 0));
        return label;
    }

    private static final class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D graphics2d = (Graphics2D) graphics.create();
            graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint paint = new GradientPaint(0, 0, UiTheme.ACCENT_DARK, getWidth(), getHeight(), UiTheme.ACCENT_BLUE);
            graphics2d.setPaint(paint);
            graphics2d.fillRect(0, 0, getWidth(), getHeight());
            graphics2d.dispose();
        }
    }
}