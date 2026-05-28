package library_management_system;

import javax.swing.JFrame;

public class SEARCH_BOOKID extends JFrame {

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            LegacyWindowRouter.openPanel(this, MainWindow.PANEL_BOOKS, "Legacy Book ID Search");
            return;
        }
        super.setVisible(false);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new SEARCH_BOOKID().setVisible(true));
    }
}
