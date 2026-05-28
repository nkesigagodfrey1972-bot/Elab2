package library_management_system;

import javax.swing.JFrame;

public class Dashboard extends JFrame {

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            LegacyWindowRouter.openPanel(this, MainWindow.PANEL_DASHBOARD, "Legacy Dashboard");
            return;
        }
        super.setVisible(false);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new Dashboard().setVisible(true));
    }
}
