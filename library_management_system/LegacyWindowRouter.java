package library_management_system;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Routes legacy standalone windows into the current role-based MainWindow.
 */
public final class LegacyWindowRouter {

    private LegacyWindowRouter() {
    }

    public static void openPanel(JFrame source, String panelName, String legacyName) {
        if (source != null) {
            source.dispose();
        }

        if (!hasActiveSession()) {
            LOGIN_FORM loginForm = new LOGIN_FORM();
            loginForm.setVisible(true);
            if (legacyName != null && !legacyName.isBlank()) {
                JOptionPane.showMessageDialog(loginForm,
                    legacyName + " now runs inside the main application.\nPlease sign in to continue.",
                    "Elab Library System",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            return;
        }

        MainWindow window = new MainWindow(UserSession.getUsername());
        window.setVisible(true);
        window.navigateTo(panelName);
        if (legacyName != null && !legacyName.isBlank()) {
            JOptionPane.showMessageDialog(window,
                legacyName + " now opens in the main application for a consistent workflow.",
                "Elab Library System",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static boolean hasActiveSession() {
        String username = UserSession.getUsername();
        return username != null && !username.isBlank() && !"guest".equalsIgnoreCase(username);
    }
}
