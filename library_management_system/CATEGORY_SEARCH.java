package library_management_system;

import javax.swing.JFrame;

public class CATEGORY_SEARCH extends JFrame {

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            LegacyWindowRouter.openPanel(this, MainWindow.PANEL_BOOKS, "Legacy Category Search");
            return;
        }
        super.setVisible(false);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new CATEGORY_SEARCH().setVisible(true));
    }
}
