package library_management_system;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class STUDENT_RECORD extends JFrame {

    private final JTextField prn = new JTextField();
    private final JTextField name = new JTextField();
    private final JTextField mob = new JTextField();
    private final JTextField branch = new JTextField();

    public STUDENT_RECORD() {
        initComponents();
        UiTheme.installFrameChrome(this, "Student Registry", "Manage member records cleanly");
    }

    private void initComponents() {
        setTitle("Student Record");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        form.add(new JLabel("REGISTRATION_NO"));
        form.add(prn);
        form.add(new JLabel("STUDENT_NAME"));
        form.add(name);
        form.add(new JLabel("MOBILE_NO"));
        form.add(mob);
        form.add(new JLabel("BRANCH"));
        form.add(branch);

        JPanel buttons = new JPanel(new GridLayout(2, 3, 8, 8));
        JButton searchButton = new JButton("SEARCH");
        JButton addButton = new JButton("ADD RECORD");
        JButton updateButton = new JButton("UPDATE");
        JButton deleteButton = new JButton("DELETE");
        JButton clearButton = new JButton("CLEAR");
        JButton homeButton = new JButton("HOME");

        searchButton.addActionListener(evt -> searchStudent());
        addButton.addActionListener(evt -> addStudent());
        updateButton.addActionListener(evt -> updateStudent());
        deleteButton.addActionListener(evt -> deleteStudent());
        clearButton.addActionListener(evt -> clearFields());
        homeButton.addActionListener(evt -> goHome());

        buttons.add(searchButton);
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);
        buttons.add(homeButton);

        setLayout(new BorderLayout(10, 10));
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void searchStudent() {
        try {
            Map<String, String> student = FirebaseBootstrap.getStudent(prn.getText().trim());
            if (student == null) {
                JOptionPane.showMessageDialog(this, "No record found");
                return;
            }

            prn.setText(student.getOrDefault("registrationNo", ""));
            name.setText(student.getOrDefault("studentName", ""));
            mob.setText(student.getOrDefault("mobileNo", ""));
            branch.setText(student.getOrDefault("branch", ""));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Search failed: " + ex.getMessage());
        }
    }

    private void addStudent() {
        try {
            FirebaseBootstrap.saveStudent(prn.getText().trim(), name.getText().trim(), mob.getText().trim(), branch.getText().trim());
            JOptionPane.showMessageDialog(this, "Record added successfully");
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Add failed: " + ex.getMessage());
        }
    }

    private void updateStudent() {
        try {
            FirebaseBootstrap.updateStudent(prn.getText().trim(), name.getText().trim(), mob.getText().trim(), branch.getText().trim());
            JOptionPane.showMessageDialog(this, "Updated successfully");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage());
        }
    }

    private void deleteStudent() {
        try {
            FirebaseBootstrap.deleteStudent(prn.getText().trim());
            JOptionPane.showMessageDialog(this, "Deleted successfully");
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage());
        }
    }

    private void clearFields() {
        prn.setText("");
        name.setText("");
        mob.setText("");
        branch.setText("");
    }

    private void goHome() {
        new HOME().setVisible(true);
        dispose();
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new STUDENT_RECORD().setVisible(true));
    }
}
