package library_management_system;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;

public final class UiTheme {

    // ── Base palette ──────────────────────────────────────────────────────────
    public static final Color BACKGROUND   = new Color(243, 247, 252);
    public static final Color CARD         = Color.WHITE;
    public static final Color ACCENT       = new Color(241, 133, 34);
    public static final Color ACCENT_DARK  = new Color(20, 56, 126);
    public static final Color ACCENT_BLUE  = new Color(34, 91, 184);
    public static final Color TEXT         = new Color(30, 42, 58);
    public static final Color MUTED        = new Color(96, 109, 126);

    // ── Extended palette ──────────────────────────────────────────────────────
    public static final Color SIDEBAR_BG     = new Color(20, 30, 48);
    public static final Color SIDEBAR_TEXT   = new Color(180, 195, 220);
    public static final Color SIDEBAR_ACTIVE = new Color(34, 91, 184);
    public static final Color SUCCESS        = new Color(34, 139, 34);
    public static final Color DANGER         = new Color(196, 43, 28);
    public static final Color WARNING        = new Color(230, 126, 34);
    public static final Color CARD_BORDER    = new Color(214, 224, 236);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    public static final Font TITLE_FONT   = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font HEADING_FONT = new Font("Segoe UI", Font.BOLD,  16);
    public static final Font BODY_FONT    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font SMALL_FONT   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font BUTTON_FONT  = new Font("Segoe UI", Font.BOLD,  12);

    // Legacy font aliases kept for backward compatibility
    private static final Font TITLE_FONT_LEGACY    = new Font("Segoe UI", Font.BOLD,  20);
    private static final Font SUBTITLE_FONT_LEGACY = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Path APP_ICON_PATH = Path.of("library_management_system", "icon (2).png");
    private static BufferedImage appIconImage;

    private UiTheme() {}

    // ── Button factory ────────────────────────────────────────────────────────

    public static JButton makeStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isPressed()  ? bg.darker()
                           : getModel().isRollover() ? bg.brighter()
                           : bg;
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(BUTTON_FONT);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.repaint(); }
        });
        return btn;
    }

    public static JButton makePrimaryButton(String text) {
        return makeStyledButton(text, ACCENT_BLUE, Color.WHITE);
    }

    public static JButton makeDangerButton(String text) {
        return makeStyledButton(text, DANGER, Color.WHITE);
    }

    public static JButton makeSecondaryButton(String text) {
        return makeStyledButton(text, new Color(108, 117, 125), Color.WHITE);
    }

    public static JButton makeSuccessButton(String text) {
        return makeStyledButton(text, SUCCESS, Color.WHITE);
    }

    // ── Form helpers ──────────────────────────────────────────────────────────

    public static JTextField makeFormField(String placeholder) {
        JTextField field = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(160, 170, 185));
                    g2.setFont(BODY_FONT.deriveFont(Font.ITALIC));
                    Insets ins = getInsets();
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, ins.left + 2, ins.top + fm.getAscent());
                    g2.dispose();
                }
            }
        };
        field.setFont(BODY_FONT);
        field.setForeground(TEXT);
        field.setBackground(CARD);
        field.setCaretColor(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    public static JLabel makeFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(BODY_FONT.deriveFont(Font.BOLD));
        lbl.setForeground(TEXT);
        return lbl;
    }

    public static JLabel makeSectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(HEADING_FONT);
        lbl.setForeground(TEXT);
        return lbl;
    }

    public static JPanel makeCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // shadow
                g2.setColor(new Color(0, 0, 0, 18));
                g2.fillRoundRect(2, 3, getWidth() - 4, getHeight() - 4, 12, 12);
                // card body
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 12, 12);
                g2.setColor(CARD_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        return card;
    }

    // ── Table helpers ─────────────────────────────────────────────────────────

    public static JScrollPane makeTableScrollPane(JTable table) {
        styleTable(table);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1, true));
        sp.getViewport().setBackground(CARD);
        return sp;
    }

    public static void styleTable(JTable table) {
        table.setFont(BODY_FONT);
        table.setRowHeight(30);
        table.setGridColor(new Color(230, 236, 244));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(210, 228, 255));
        table.setSelectionForeground(TEXT);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        // Alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? CARD : new Color(247, 250, 255));
                }
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        });

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(BODY_FONT.deriveFont(Font.BOLD));
        header.setBackground(ACCENT_DARK);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    }

    // ── Dialog helpers ────────────────────────────────────────────────────────

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, "Confirm", JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }

    // ── Frame chrome ──────────────────────────────────────────────────────────

    public static void installFrameChrome(JFrame frame, String title, String subtitle) {
        applyWindowIcon(frame);
        Container content = frame.getContentPane();
        if (content instanceof JComponent component
                && Boolean.TRUE.equals(component.getClientProperty("ui.chrome.installed"))) {
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
        BufferedImage sourceImage = getAppIconImage();
        if (sourceImage != null) {
            java.awt.Image scaled = sourceImage.getScaledInstance(size, size, java.awt.Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint background = new GradientPaint(0, 0, ACCENT_DARK, size, size, ACCENT_BLUE);
        graphics.setPaint(background);
        graphics.fill(new RoundRectangle2D.Double(1, 1, size - 2, size - 2, size * 0.28, size * 0.28));

        graphics.setColor(new Color(255, 255, 255, 26));
        graphics.fill(new RoundRectangle2D.Double(size * 0.1, size * 0.12, size * 0.8, size * 0.28, size * 0.18, size * 0.18));

        graphics.setColor(Color.WHITE);
        graphics.setStroke(new BasicStroke(Math.max(2f, size / 18f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int pad = Math.max(5, size / 8);
        int yTop = size / 2 - size / 10;
        int yBottom = size / 2 + size / 10;
        graphics.drawLine(pad, yTop, pad, size - pad);
        graphics.drawLine(size - pad, yTop, size - pad, size - pad);
        graphics.drawLine(pad, yTop, size - pad, yTop);
        graphics.drawLine(pad, yBottom, size - pad, yBottom);

        graphics.setStroke(new BasicStroke(Math.max(2f, size / 22f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
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

    public static void applyWindowIcon(JFrame frame) {
        BufferedImage sourceImage = getAppIconImage();
        if (frame != null && sourceImage != null) {
            frame.setIconImage(sourceImage);
        }
    }

    public static ImageIcon createActionIcon(String kind, Color color, int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(color);
        graphics.setStroke(new BasicStroke(Math.max(1.8f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (kind) {
            case "refresh" -> {
                graphics.draw(new Arc2D.Double(size * 0.15, size * 0.15, size * 0.7, size * 0.7, 45, 280, Arc2D.OPEN));
                graphics.drawLine((int)(size * 0.78), (int)(size * 0.28), (int)(size * 0.88), (int)(size * 0.26));
                graphics.drawLine((int)(size * 0.78), (int)(size * 0.28), (int)(size * 0.8),  (int)(size * 0.18));
            }
            case "home" -> {
                graphics.drawLine((int)(size * 0.18), (int)(size * 0.55), (int)(size * 0.5),  (int)(size * 0.2));
                graphics.drawLine((int)(size * 0.5),  (int)(size * 0.2),  (int)(size * 0.82), (int)(size * 0.55));
                graphics.drawRect((int)(size * 0.28), (int)(size * 0.52), (int)(size * 0.44), (int)(size * 0.28));
                graphics.drawLine((int)(size * 0.45), (int)(size * 0.8),  (int)(size * 0.45), (int)(size * 0.62));
            }
            case "book" -> {
                graphics.drawRoundRect((int)(size * 0.15), (int)(size * 0.18), (int)(size * 0.7), (int)(size * 0.64), size / 7, size / 7);
                graphics.drawLine((int)(size * 0.5),  (int)(size * 0.18), (int)(size * 0.5),  (int)(size * 0.82));
                graphics.drawLine((int)(size * 0.26), (int)(size * 0.34), (int)(size * 0.42), (int)(size * 0.34));
                graphics.drawLine((int)(size * 0.58), (int)(size * 0.34), (int)(size * 0.74), (int)(size * 0.34));
            }
            case "users" -> {
                graphics.draw(new java.awt.geom.Ellipse2D.Double(size * 0.2,  size * 0.2,  size * 0.22, size * 0.22));
                graphics.draw(new java.awt.geom.Ellipse2D.Double(size * 0.54, size * 0.16, size * 0.22, size * 0.22));
                graphics.draw(new Arc2D.Double(size * 0.1,  size * 0.45, size * 0.38, size * 0.32, 0, 180, Arc2D.OPEN));
                graphics.draw(new Arc2D.Double(size * 0.46, size * 0.41, size * 0.36, size * 0.34, 0, 180, Arc2D.OPEN));
            }
            case "issue" -> {
                graphics.drawRoundRect((int)(size * 0.18), (int)(size * 0.12), (int)(size * 0.44), (int)(size * 0.68), size / 8, size / 8);
                graphics.drawLine((int)(size * 0.4),  (int)(size * 0.32), (int)(size * 0.72), (int)(size * 0.32));
                graphics.drawLine((int)(size * 0.64), (int)(size * 0.24), (int)(size * 0.72), (int)(size * 0.32));
                graphics.drawLine((int)(size * 0.64), (int)(size * 0.4),  (int)(size * 0.72), (int)(size * 0.32));
                graphics.drawLine((int)(size * 0.4),  (int)(size * 0.52), (int)(size * 0.72), (int)(size * 0.52));
                graphics.drawLine((int)(size * 0.4),  (int)(size * 0.68), (int)(size * 0.72), (int)(size * 0.68));
            }
            case "export" -> {
                graphics.drawLine((int)(size * 0.5),  (int)(size * 0.18), (int)(size * 0.5),  (int)(size * 0.62));
                graphics.drawLine((int)(size * 0.36), (int)(size * 0.34), (int)(size * 0.5),  (int)(size * 0.18));
                graphics.drawLine((int)(size * 0.64), (int)(size * 0.34), (int)(size * 0.5),  (int)(size * 0.18));
                graphics.drawRoundRect((int)(size * 0.2), (int)(size * 0.58), (int)(size * 0.6), (int)(size * 0.22), size / 9, size / 9);
            }
            default -> graphics.draw(new Path2D.Double());
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
        if (component == null) return;

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

    // ── Private helpers ───────────────────────────────────────────────────────

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
        titleLabel.setFont(TITLE_FONT_LEGACY);
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.putClientProperty("ui.theme.header", Boolean.TRUE);
        subtitleLabel.setFont(SUBTITLE_FONT_LEGACY);
        subtitleLabel.setForeground(new Color(224, 236, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        brandText.add(titleLabel, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(4, 0, 0, 0);
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

    private static BufferedImage getAppIconImage() {
        if (appIconImage != null) {
            return appIconImage;
        }
        if (!Files.exists(APP_ICON_PATH)) {
            return null;
        }
        try {
            appIconImage = ImageIO.read(APP_ICON_PATH.toFile());
        } catch (IOException ignored) {
            appIconImage = null;
        }
        return appIconImage;
    }

    public static final class HeaderPanel extends JPanel {
        public HeaderPanel() {
            setBackground(ACCENT_DARK);
        }
    }
}
