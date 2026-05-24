package library_management_system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
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

    public static ImageIcon createLogoIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint background = new GradientPaint(0, 0, ACCENT_DARK, size, size, ACCENT_BLUE);
        graphics.setPaint(background);
        graphics.fill(new RoundRectangle2D.Double(1, 1, size - 2, size - 2, size * 0.28, size * 0.28));

        graphics.setColor(new Color(255, 255, 255, 26));
        graphics.fill(new RoundRectangle2D.Double(size * 0.1, size * 0.12, size * 0.8, size * 0.28, size * 0.18, size * 0.18));

        graphics.setColor(Color.WHITE);
        graphics.setStroke(new java.awt.BasicStroke(Math.max(2f, size / 18f), java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        int pad = Math.max(5, size / 8);
        int yTop = size / 2 - size / 10;
        int yBottom = size / 2 + size / 10;
        graphics.drawLine(pad, yTop, pad, size - pad);
        graphics.drawLine(size - pad, yTop, size - pad, size - pad);
        graphics.drawLine(pad, yTop, size - pad, yTop);
        graphics.drawLine(pad, yBottom, size - pad, yBottom);

        graphics.setStroke(new java.awt.BasicStroke(Math.max(2f, size / 22f), java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        graphics.setColor(new Color(255, 255, 255, 230));
        graphics.draw(new Arc2D.Double(size * 0.23, size * 0.62, size * 0.22, size * 0.18, 205, 130, Arc2D.OPEN));
        graphics.draw(new Arc2D.Double(size * 0.55, size * 0.62, size * 0.22, size * 0.18, 205, 130, Arc2D.OPEN));

        graphics.setFont(new Font("Segoe UI", Font.BOLD, Math.max(16, size / 2)));
        FontMetrics metrics = graphics.getFontMetrics();
        int textWidth = metrics.stringWidth("EL");
        int textX = (size - textWidth) / 2;
        int textY = size / 2 + metrics.getAscent() / 3;
        graphics.setColor(new Color(255, 255, 255, 244));
        graphics.drawString("EL", textX, textY);

        graphics.dispose();
        return new ImageIcon(image);
    }

    public static ImageIcon createActionIcon(String kind, Color color, int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(color);
        graphics.setStroke(new java.awt.BasicStroke(Math.max(1.8f, size / 10f), java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));

        switch (kind) {
            case "refresh":
                graphics.draw(new Arc2D.Double(size * 0.15, size * 0.15, size * 0.7, size * 0.7, 45, 280, Arc2D.OPEN));
                graphics.drawLine((int) (size * 0.78), (int) (size * 0.28), (int) (size * 0.88), (int) (size * 0.26));
                graphics.drawLine((int) (size * 0.78), (int) (size * 0.28), (int) (size * 0.8), (int) (size * 0.18));
                break;
            case "home":
                graphics.drawLine((int) (size * 0.18), (int) (size * 0.55), (int) (size * 0.5), (int) (size * 0.2));
                graphics.drawLine((int) (size * 0.5), (int) (size * 0.2), (int) (size * 0.82), (int) (size * 0.55));
                graphics.drawRect((int) (size * 0.28), (int) (size * 0.52), (int) (size * 0.44), (int) (size * 0.28));
                graphics.drawLine((int) (size * 0.45), (int) (size * 0.8), (int) (size * 0.45), (int) (size * 0.62));
                break;
            case "book":
                graphics.drawRoundRect((int) (size * 0.15), (int) (size * 0.18), (int) (size * 0.7), (int) (size * 0.64), size / 7, size / 7);
                graphics.drawLine((int) (size * 0.5), (int) (size * 0.18), (int) (size * 0.5), (int) (size * 0.82));
                graphics.drawLine((int) (size * 0.26), (int) (size * 0.34), (int) (size * 0.42), (int) (size * 0.34));
                graphics.drawLine((int) (size * 0.58), (int) (size * 0.34), (int) (size * 0.74), (int) (size * 0.34));
                break;
            case "users":
                graphics.draw(new java.awt.geom.Ellipse2D.Double(size * 0.2, size * 0.2, size * 0.22, size * 0.22));
                graphics.draw(new java.awt.geom.Ellipse2D.Double(size * 0.54, size * 0.16, size * 0.22, size * 0.22));
                graphics.draw(new java.awt.geom.Arc2D.Double(size * 0.1, size * 0.45, size * 0.38, size * 0.32, 0, 180, Arc2D.OPEN));
                graphics.draw(new java.awt.geom.Arc2D.Double(size * 0.46, size * 0.41, size * 0.36, size * 0.34, 0, 180, Arc2D.OPEN));
                break;
            case "issue":
                graphics.drawRoundRect((int) (size * 0.18), (int) (size * 0.12), (int) (size * 0.44), (int) (size * 0.68), size / 8, size / 8);
                graphics.drawLine((int) (size * 0.4), (int) (size * 0.32), (int) (size * 0.72), (int) (size * 0.32));
                graphics.drawLine((int) (size * 0.64), (int) (size * 0.24), (int) (size * 0.72), (int) (size * 0.32));
                graphics.drawLine((int) (size * 0.64), (int) (size * 0.4), (int) (size * 0.72), (int) (size * 0.32));
                graphics.drawLine((int) (size * 0.4), (int) (size * 0.52), (int) (size * 0.72), (int) (size * 0.52));
                graphics.drawLine((int) (size * 0.4), (int) (size * 0.68), (int) (size * 0.72), (int) (size * 0.68));
                break;
            case "export":
                graphics.drawLine((int) (size * 0.5), (int) (size * 0.18), (int) (size * 0.5), (int) (size * 0.62));
                graphics.drawLine((int) (size * 0.36), (int) (size * 0.34), (int) (size * 0.5), (int) (size * 0.18));
                graphics.drawLine((int) (size * 0.64), (int) (size * 0.34), (int) (size * 0.5), (int) (size * 0.18));
                graphics.drawRoundRect((int) (size * 0.2), (int) (size * 0.58), (int) (size * 0.6), (int) (size * 0.22), size / 9, size / 9);
                break;
            default:
                graphics.draw(new Path2D.Double());
                break;
        }

        graphics.dispose();
        return new ImageIcon(image);
    }

    public static void decorateIconButton(JButton button, String iconKind, Color color) {
        button.setIcon(createActionIcon(iconKind, color, 16));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(10);
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
        JLabel logo = new JLabel(createLogoIcon(52));
        brand.add(logo, BorderLayout.WEST);

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