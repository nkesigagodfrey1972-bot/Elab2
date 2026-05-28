package library_management_system;

import javax.swing.JFrame;

public class HOME extends JFrame {

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            LegacyWindowRouter.openPanel(this, MainWindow.PANEL_DASHBOARD, "Operations Center");
            return;
        }
        super.setVisible(false);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new HOME().setVisible(true));
    }
}
