package library_management_system;

import javax.swing.JFrame;

public class SEARCH_BOOKNAME extends JFrame {

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            LegacyWindowRouter.openPanel(this, MainWindow.PANEL_BOOKS, "Legacy Book Name Search");
            return;
        }
        super.setVisible(false);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new SEARCH_BOOKNAME().setVisible(true));
    }
}
