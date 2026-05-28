package library_management_system;

import javax.swing.JFrame;

public class ISSUE_RECORD1 extends JFrame {

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            LegacyWindowRouter.openPanel(this, MainWindow.PANEL_ISSUE, "Legacy Issue Desk");
            return;
        }
        super.setVisible(false);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new ISSUE_RECORD1().setVisible(true));
    }
}
