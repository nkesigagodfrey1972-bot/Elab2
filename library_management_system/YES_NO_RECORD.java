package library_management_system;

import javax.swing.JFrame;

public class YES_NO_RECORD extends JFrame {

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            LegacyWindowRouter.openPanel(this, MainWindow.PANEL_TRANSACTIONS, "Legacy Issue Status Search");
            return;
        }
        super.setVisible(false);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new YES_NO_RECORD().setVisible(true));
    }
}
