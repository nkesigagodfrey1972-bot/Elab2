package library_management_system;

import javax.swing.JFrame;

public class SEARCH_RECORD extends JFrame {

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            LegacyWindowRouter.openPanel(this, MainWindow.PANEL_BOOKS, "Legacy Book Record");
            return;
        }
        super.setVisible(false);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new SEARCH_RECORD().setVisible(true));
    }
}
