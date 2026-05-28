package library_management_system;

import java.awt.GraphicsEnvironment;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public final class AppLauncher {

    private static final String SERVICE_ACCOUNT_KEY = "firebase.serviceAccount";
    private static final Path PROJECT_SERVICE_ACCOUNT = Path.of("library_management_system", "e-library.json");
    private static final Path ROOT_SERVICE_ACCOUNT = Path.of("e-library.json");

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

        configuredPath = AppSettings.get(SERVICE_ACCOUNT_KEY, "");
        if (configuredPath != null && !configuredPath.isBlank() && Files.exists(Path.of(configuredPath))) {
            System.setProperty(SERVICE_ACCOUNT_KEY, configuredPath);
            return;
        }

        Path localCredential = findLocalServiceAccount();
        if (localCredential != null) {
            String selectedPath = localCredential.toAbsolutePath().toString();
            System.setProperty(SERVICE_ACCOUNT_KEY, selectedPath);
            saveSetting(selectedPath);
            return;
        }

        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException("No external Firebase service-account JSON was configured.");
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Firebase service-account JSON");
        int result = chooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION || chooser.getSelectedFile() == null) {
            throw new IllegalStateException("Firebase service-account JSON is required to start the app.");
        }

        String selectedPath = chooser.getSelectedFile().getAbsolutePath();
        System.setProperty(SERVICE_ACCOUNT_KEY, selectedPath);
        saveSetting(selectedPath);
    }

    private static Path findLocalServiceAccount() {
        if (Files.exists(PROJECT_SERVICE_ACCOUNT)) {
            return PROJECT_SERVICE_ACCOUNT;
        }
        if (Files.exists(ROOT_SERVICE_ACCOUNT)) {
            return ROOT_SERVICE_ACCOUNT;
        }
        return null;
    }

    private static void saveSetting(String serviceAccountPath) {
        var properties = AppSettings.load();
        properties.setProperty(SERVICE_ACCOUNT_KEY, serviceAccountPath);
        AppSettings.save(properties);
    }
}
