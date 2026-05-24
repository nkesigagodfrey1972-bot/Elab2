package library_management_system;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public final class AppLauncher {

    private static final Path SETTINGS_FILE = Path.of(System.getProperty("user.home"), ".elab-library.properties");
    private static final String SERVICE_ACCOUNT_KEY = "firebase.serviceAccount";

    private AppLauncher() {
    }

    public static void main(String[] args) {
        try {
            configureLookAndFeel();
            ensureServiceAccountConfigured();
            FirebaseBootstrap.ensureSeedData();
            java.awt.EventQueue.invokeLater(() -> new WelcomeScreen(() -> new LOGIN_FORM().setVisible(true)).setVisible(true));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Startup failed: " + ex.getMessage(), "Library System", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void configureLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (Exception ignored) {
            // Fall back to the platform default if Nimbus is unavailable.
        }
    }

    private static void ensureServiceAccountConfigured() throws Exception {
        if (System.getenv("FIREBASE_SERVICE_ACCOUNT") != null && !System.getenv("FIREBASE_SERVICE_ACCOUNT").isBlank()) {
            return;
        }

        String configuredPath = System.getProperty(SERVICE_ACCOUNT_KEY);
        if (configuredPath != null && !configuredPath.isBlank() && Files.exists(Path.of(configuredPath))) {
            return;
        }

        Properties properties = loadSettings();
        configuredPath = properties.getProperty(SERVICE_ACCOUNT_KEY);
        if (configuredPath != null && !configuredPath.isBlank() && Files.exists(Path.of(configuredPath))) {
            System.setProperty(SERVICE_ACCOUNT_KEY, configuredPath);
            return;
        }

        Path localKey = Path.of("library_management_system", "e-library-service-account.json");
        if (Files.exists(localKey)) {
            System.setProperty(SERVICE_ACCOUNT_KEY, localKey.toString());
            properties.setProperty(SERVICE_ACCOUNT_KEY, localKey.toString());
            saveSettings(properties);
            return;
        }

        Path rootKey = Path.of("e-library-service-account.json");
        if (Files.exists(rootKey)) {
            System.setProperty(SERVICE_ACCOUNT_KEY, rootKey.toString());
            properties.setProperty(SERVICE_ACCOUNT_KEY, rootKey.toString());
            saveSettings(properties);
            return;
        }

        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException("No Firebase service-account JSON found.");
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Firebase service-account JSON");
        int result = chooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION || chooser.getSelectedFile() == null) {
            throw new IllegalStateException("Firebase service-account JSON is required to start the app.");
        }

        String selectedPath = chooser.getSelectedFile().getAbsolutePath();
        System.setProperty(SERVICE_ACCOUNT_KEY, selectedPath);
        properties.setProperty(SERVICE_ACCOUNT_KEY, selectedPath);
        saveSettings(properties);
    }

    private static Properties loadSettings() {
        Properties properties = new Properties();
        if (Files.exists(SETTINGS_FILE)) {
            try (var input = Files.newInputStream(SETTINGS_FILE)) {
                properties.load(input);
            } catch (IOException ignored) {
                // Start with empty settings if the file cannot be read.
            }
        }
        return properties;
    }

    private static void saveSettings(Properties properties) {
        try {
            try (var output = Files.newOutputStream(SETTINGS_FILE)) {
                properties.store(output, "Elab Library System settings");
            }
        } catch (IOException ignored) {
            // Best effort only; startup still continues.
        }
    }
}
