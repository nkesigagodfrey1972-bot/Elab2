package library_management_system;

import javax.swing.JFrame;

public class STUDENT_RECORD extends JFrame {

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            LegacyWindowRouter.openPanel(this, MainWindow.PANEL_MEMBERS, "Legacy Student Record");
            return;
        }
        super.setVisible(false);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new STUDENT_RECORD().setVisible(true));
    }
}
