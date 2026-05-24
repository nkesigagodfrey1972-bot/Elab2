package library_management_system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JPasswordField;
import javax.swing.JLayeredPane;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

public final class UiTheme {

    public static final Color BACKGROUND = new Color(243, 247, 252);
    public static final Color CARD = Color.WHITE;
    public static final Color ACCENT = new Color(241, 133, 34);
    public static final Color ACCENT_DARK = new Color(20, 56, 126);
    public static final Color ACCENT_BLUE = new Color(34, 91, 184);
    public static final Color TEXT = new Color(30, 42, 58);
    public static final Color MUTED = new Color(96, 109, 126);

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 12);

    private UiTheme() {
    }

    public static void installFrameChrome(JFrame frame, String title, String subtitle) {
        Container content = frame.getContentPane();
        if (content instanceof JComponent component && Boolean.TRUE.equals(component.getClientProperty("ui.chrome.installed"))) {
            return;
        }

        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(BACKGROUND);
        shell.add(createHeader(title, subtitle), BorderLayout.NORTH);
        shell.add(content, BorderLayout.CENTER);
        frame.setContentPane(shell);

        if (content instanceof JComponent component) {
            component.putClientProperty("ui.chrome.installed", Boolean.TRUE);
        }

        styleTree(shell);
        frame.revalidate();
        frame.repaint();
    }

    public static JLabel createLogoBadge(String text) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER);
        badge.setOpaque(true);
        badge.setBackground(ACCENT_DARK);
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 18));
        badge.setPreferredSize(new Dimension(54, 54));
        badge.setMinimumSize(new Dimension(54, 54));
        badge.setMaximumSize(new Dimension(54, 54));
        badge.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 80), 1, true));
        return badge;
    }

    public static void styleTree(Component component) {
        if (component == null) {
            return;
        }

        if (component instanceof JPanel panel) {
            if (!Boolean.TRUE.equals(panel.getClientProperty("ui.theme.keepBackground"))) {
                panel.setBackground(BACKGROUND);
            }
        }
        if (component instanceof JLayeredPane layeredPane) {
            layeredPane.setBackground(BACKGROUND);
        }
        if (component instanceof JLabel label) {
            if (!Boolean.TRUE.equals(label.getClientProperty("ui.theme.header"))) {
                label.setFont(BODY_FONT);
                label.setForeground(TEXT);
            }
        }
        if (component instanceof JButton button) {
            button.setFont(BUTTON_FONT);
            button.setFocusPainted(false);
            button.setBackground(ACCENT);
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(211, 118, 31), 1, true),
                new EmptyBorder(9, 16, 9, 16)
            ));
            button.setOpaque(true);
        }
        if (component instanceof JTextComponent textComponent) {
            textComponent.setFont(BODY_FONT);
            textComponent.setBackground(CARD);
            textComponent.setForeground(TEXT);
            textComponent.setCaretColor(TEXT);
            textComponent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(202, 213, 207), 1, true),
                new EmptyBorder(8, 10, 8, 10)
            ));
        }
        if (component instanceof JComboBox<?> comboBox) {
            comboBox.setFont(BODY_FONT);
            comboBox.setBackground(CARD);
            comboBox.setForeground(TEXT);
        }
        if (component instanceof JTable table) {
            table.setFont(BODY_FONT);
            table.setRowHeight(24);
            table.setGridColor(new Color(214, 224, 236));
            table.setSelectionBackground(new Color(255, 230, 199));
        }
        if (component instanceof JScrollPane scrollPane) {
            scrollPane.setBorder(BorderFactory.createLineBorder(new Color(214, 224, 236), 1, true));
            if (scrollPane.getViewport() != null) {
                scrollPane.getViewport().setBackground(CARD);
            }
        }

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                styleTree(child);
            }
        }
    }

    private static JPanel createHeader(String title, String subtitle) {
        HeaderPanel header = new HeaderPanel();
        header.setBorder(new EmptyBorder(14, 18, 14, 18));
        header.setLayout(new BorderLayout(14, 0));

        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        brand.add(createLogoBadge("EL"), BorderLayout.WEST);

        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new GridBagLayout());

        JLabel titleLabel = new JLabel(title);
        titleLabel.putClientProperty("ui.theme.header", Boolean.TRUE);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.putClientProperty("ui.theme.header", Boolean.TRUE);
        subtitleLabel.setFont(SUBTITLE_FONT);
        subtitleLabel.setForeground(new Color(224, 236, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        brandText.add(titleLabel, gbc);
        gbc.gridy = 1;
        gbc.insets = new java.awt.Insets(4, 0, 0, 0);
        brandText.add(subtitleLabel, gbc);

        brand.add(brandText, BorderLayout.CENTER);

        JLabel chip = new JLabel("READY");
        chip.putClientProperty("ui.theme.header", Boolean.TRUE);
        chip.setOpaque(true);
        chip.setBackground(new Color(255, 255, 255, 34));
        chip.setForeground(Color.WHITE);
        chip.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chip.setBorder(new EmptyBorder(8, 12, 8, 12));

        header.add(brand, BorderLayout.WEST);
        header.add(chip, BorderLayout.EAST);
        return header;
    }

    private static final class HeaderPanel extends JPanel {
        private HeaderPanel() {
            setBackground(ACCENT_DARK);
        }
    }
}