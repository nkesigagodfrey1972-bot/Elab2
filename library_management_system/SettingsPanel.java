package library_management_system;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class SettingsPanel extends JPanel {

    // Library info
    private final JTextField fldLibraryName  = UiTheme.makeFormField("e.g. Kampala International University Library");

    // Borrowing
    private final JTextField fldBorrowDays   = UiTheme.makeFormField("Default loan period in days (e.g. 14)");
    private final JTextField fldMaxBooks     = UiTheme.makeFormField("Max books per member at once (e.g. 5)");

    // Fines
    private final JTextField fldFineRate     = UiTheme.makeFormField("Fine per overdue day (e.g. 0.50)");
    private final JCheckBox  chkBlockFines   = new JCheckBox("Block issuing to members with unpaid fines");

    // Renewals
    private final JTextField fldRenewalLimit = UiTheme.makeFormField("Max renewals per issue (e.g. 2)");
    private final JTextField fldRenewalDays  = UiTheme.makeFormField("Days added per renewal (e.g. 7)");

    // Reservations
    private final JTextField fldReservExpiry = UiTheme.makeFormField("Days before reservation expires (e.g. 3)");

    // Export
    private final JTextField fldExportFolder = UiTheme.makeFormField("Path to default export folder");

    private final JLabel statusLabel = new JLabel(" ");

    public SettingsPanel() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BACKGROUND);
        buildUI();
        loadSettings();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UiTheme.BACKGROUND);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel title = UiTheme.makeSectionTitle("\u2699\uFE0F  Settings");
        title.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel formCard = UiTheme.makeCard();
        formCard.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.gridx = 0; gbc.weightx = 1;

        int row = 0;

        // ── Library ───────────────────────────────────────────────────────────
        row = addSectionHeader(formCard, gbc, row, "Library Information");
        row = addField(formCard, gbc, row, "Library / Institution Name", fldLibraryName);

        // ── Borrowing ─────────────────────────────────────────────────────────
        row = addSectionHeader(formCard, gbc, row, "Borrowing Policy");
        row = addField(formCard, gbc, row, "Default Borrowing Period (days)", fldBorrowDays);
        row = addField(formCard, gbc, row, "Max Books Per Member",            fldMaxBooks);

        // ── Fines ─────────────────────────────────────────────────────────────
        row = addSectionHeader(formCard, gbc, row, "Fine Policy");
        row = addField(formCard, gbc, row, "Fine Rate Per Day (currency units)", fldFineRate);
        gbc.gridy = row++;
        chkBlockFines.setFont(UiTheme.BODY_FONT);
        chkBlockFines.setOpaque(false);
        formCard.add(chkBlockFines, gbc);

        // ── Renewals ──────────────────────────────────────────────────────────
        row = addSectionHeader(formCard, gbc, row, "Renewal Policy");
        row = addField(formCard, gbc, row, "Max Renewals Per Issue",   fldRenewalLimit);
        row = addField(formCard, gbc, row, "Days Added Per Renewal",   fldRenewalDays);

        // ── Reservations ──────────────────────────────────────────────────────
        row = addSectionHeader(formCard, gbc, row, "Reservation Policy");
        row = addField(formCard, gbc, row, "Reservation Expiry (days)", fldReservExpiry);

        // ── Export ────────────────────────────────────────────────────────────
        row = addSectionHeader(formCard, gbc, row, "Export");
        gbc.gridy = row++;
        formCard.add(UiTheme.makeFormLabel("Default Export Folder"), gbc);
        gbc.gridy = row++;
        JPanel exportRow = new JPanel(new BorderLayout(6, 0));
        exportRow.setOpaque(false);
        JButton browseBtn = UiTheme.makeSecondaryButton("Browse…");
        browseBtn.addActionListener(e -> browseExportFolder());
        exportRow.add(fldExportFolder, BorderLayout.CENTER);
        exportRow.add(browseBtn,       BorderLayout.EAST);
        formCard.add(exportRow, gbc);

        // ── Buttons ───────────────────────────────────────────────────────────
        gbc.gridy = row++; gbc.insets = new Insets(20, 8, 6, 8);
        JPanel btnRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        JButton saveBtn  = UiTheme.makeSuccessButton("\uD83D\uDCBE  Save Settings");
        JButton resetBtn = UiTheme.makeSecondaryButton("Reset to Defaults");
        saveBtn.addActionListener(e  -> saveSettings());
        resetBtn.addActionListener(e -> resetDefaults());

        // Only admins can save settings
        if (!UserSession.canAccessSettings()) {
            saveBtn.setEnabled(false);
            resetBtn.setEnabled(false);
        }

        btnRow.add(saveBtn);
        btnRow.add(resetBtn);
        formCard.add(btnRow, gbc);

        gbc.gridy = row++; gbc.insets = new Insets(4, 8, 4, 8);
        statusLabel.setFont(UiTheme.BODY_FONT);
        statusLabel.setForeground(UiTheme.MUTED);
        formCard.add(statusLabel, gbc);

        gbc.gridy = row; gbc.weighty = 1;
        formCard.add(Box.createVerticalGlue(), gbc);

        root.add(title,    BorderLayout.NORTH);
        root.add(formCard, BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);
    }

    private int addSectionHeader(JPanel panel, GridBagConstraints gbc, int row, String text) {
        gbc.gridy = row; gbc.insets = new Insets(14, 8, 4, 8);
        JLabel lbl = new JLabel(text);
        lbl.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD));
        lbl.setForeground(UiTheme.ACCENT_BLUE);
        lbl.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.CARD_BORDER));
        panel.add(lbl, gbc);
        gbc.insets = new Insets(5, 8, 5, 8);
        return row + 1;
    }

    private int addField(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridy = row;
        panel.add(UiTheme.makeFormLabel(label), gbc);
        gbc.gridy = row + 1;
        panel.add(field, gbc);
        return row + 2;
    }

    // ── Load / Save ───────────────────────────────────────────────────────────

    private void loadSettings() {
        Properties props = AppSettings.load();
        fldLibraryName.setText(props.getProperty("library.name",              "Elab Library"));
        fldBorrowDays.setText(props.getProperty("borrowing.period.days",      "14"));
        fldMaxBooks.setText(props.getProperty("max.books.per.member",         "5"));
        fldFineRate.setText(props.getProperty("fine.rate.per.day",            "0.50"));
        chkBlockFines.setSelected(Boolean.parseBoolean(props.getProperty("block.on.pending.fines", "false")));
        fldRenewalLimit.setText(props.getProperty("renewal.limit",            "2"));
        fldRenewalDays.setText(props.getProperty("renewal.days",              "7"));
        fldReservExpiry.setText(props.getProperty("reservation.expiry.days",  "3"));
        fldExportFolder.setText(props.getProperty("export.folder",            System.getProperty("user.home")));
        statusLabel.setText("Settings loaded.");
    }

    private void saveSettings() {
        if (!UserSession.canAccessSettings()) {
            UiTheme.showWarning(this, "Access denied. Only Admins can change settings."); return;
        }
        // Validate numeric fields
        if (!validateNumeric(fldBorrowDays,   "Borrowing period",  true))  return;
        if (!validateNumeric(fldMaxBooks,     "Max books",         true))  return;
        if (!validateNumeric(fldFineRate,     "Fine rate",         false)) return;
        if (!validateNumeric(fldRenewalLimit, "Renewal limit",     true))  return;
        if (!validateNumeric(fldRenewalDays,  "Renewal days",      true))  return;
        if (!validateNumeric(fldReservExpiry, "Reservation expiry",true))  return;

        Properties props = AppSettings.load();
        props.setProperty("library.name",              fldLibraryName.getText().trim());
        props.setProperty("borrowing.period.days",     fldBorrowDays.getText().trim());
        props.setProperty("max.books.per.member",      fldMaxBooks.getText().trim());
        props.setProperty("fine.rate.per.day",         fldFineRate.getText().trim());
        props.setProperty("block.on.pending.fines",    String.valueOf(chkBlockFines.isSelected()));
        props.setProperty("renewal.limit",             fldRenewalLimit.getText().trim());
        props.setProperty("renewal.days",              fldRenewalDays.getText().trim());
        props.setProperty("reservation.expiry.days",   fldReservExpiry.getText().trim());
        props.setProperty("export.folder",             fldExportFolder.getText().trim());

        try (var out = Files.newOutputStream(AppSettings.SETTINGS_FILE)) {
            props.store(out, "Elab Library System settings");
            statusLabel.setText("Settings saved successfully.");
            statusLabel.setForeground(UiTheme.SUCCESS);
            AuditService.log(AuditService.ACTION_CHANGE_SETTINGS, AuditService.MODULE_SETTINGS,
                "Settings updated by " + UserSession.getUsername());
            UiTheme.showSuccess(this, "Settings saved.");
        } catch (IOException ex) {
            UiTheme.showError(this, "Could not save settings: " + ex.getMessage());
        }
    }

    private boolean validateNumeric(JTextField field, String label, boolean mustBeInt) {
        String v = field.getText().trim();
        if (v.isEmpty()) return true;
        try {
            if (mustBeInt) {
                int n = Integer.parseInt(v);
                if (n <= 0) throw new NumberFormatException();
            } else {
                double d = Double.parseDouble(v);
                if (d < 0) throw new NumberFormatException();
            }
            return true;
        } catch (NumberFormatException e) {
            UiTheme.showError(this, label + " must be a positive " + (mustBeInt ? "integer" : "number") + ".");
            field.requestFocus();
            return false;
        }
    }

    private void resetDefaults() {
        if (!UiTheme.confirm(this, "Reset all settings to defaults?")) return;
        fldLibraryName.setText("Elab Library");
        fldBorrowDays.setText("14");
        fldMaxBooks.setText("5");
        fldFineRate.setText("0.50");
        chkBlockFines.setSelected(false);
        fldRenewalLimit.setText("2");
        fldRenewalDays.setText("7");
        fldReservExpiry.setText("3");
        fldExportFolder.setText(System.getProperty("user.home"));
        statusLabel.setText("Defaults restored. Click Save to persist.");
        statusLabel.setForeground(UiTheme.WARNING);
    }

    private void browseExportFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String current = fldExportFolder.getText().trim();
        if (!current.isEmpty()) chooser.setCurrentDirectory(new File(current));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fldExportFolder.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }
}
