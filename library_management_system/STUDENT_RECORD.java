package library_management_system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(18, 18, 12, 18));
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        form.add(new JLabel("REGISTRATION NO"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        prn.setPreferredSize(new Dimension(220, 28));
        form.add(prn, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        form.add(new JLabel("STUDENT NAME"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        name.setPreferredSize(new Dimension(360, 28));
        form.add(name, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        form.add(new JLabel("MOBILE NO"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        form.add(mob, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        form.add(new JLabel("BRANCH"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        form.add(branch, gbc);

        JPanel buttons = new JPanel();
        buttons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 12, 6));
        JButton searchButton = new JButton("Search");
        JButton addButton = new JButton("Add record");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear");
        JButton homeButton = new JButton("Home");
        UiTheme.decorateIconButton(searchButton, "users", UiTheme.ACCENT_BLUE);
        UiTheme.decorateIconButton(addButton, "users", UiTheme.ACCENT);
        UiTheme.decorateIconButton(updateButton, "refresh", UiTheme.ACCENT_BLUE);
        UiTheme.decorateIconButton(deleteButton, "issue", new Color(206, 100, 20));
        UiTheme.decorateIconButton(clearButton, "export", UiTheme.ACCENT_DARK);
        UiTheme.decorateIconButton(homeButton, "home", UiTheme.ACCENT_BLUE);

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
            String id = prn.getText().trim();
            String studentName = name.getText().trim();
            if (id.isBlank()) {
                JOptionPane.showMessageDialog(this, "Registration number is required", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (studentName.isBlank()) {
                JOptionPane.showMessageDialog(this, "Student name is required", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            FirebaseBootstrap.saveStudent(id, studentName, mob.getText().trim(), branch.getText().trim());
            JOptionPane.showMessageDialog(this, "Record added successfully");
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Add failed: " + ex.getMessage());
        }
    }

    private void updateStudent() {
        try {
            String id = prn.getText().trim();
            if (id.isBlank()) {
                JOptionPane.showMessageDialog(this, "Registration number is required to update", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            FirebaseBootstrap.updateStudent(id, name.getText().trim(), mob.getText().trim(), branch.getText().trim());
            JOptionPane.showMessageDialog(this, "Updated successfully");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage());
        }
    }

    private void deleteStudent() {
        try {
            String id = prn.getText().trim();
            if (id.isBlank()) {
                JOptionPane.showMessageDialog(this, "Registration number is required to delete", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            FirebaseBootstrap.deleteStudent(id);
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
