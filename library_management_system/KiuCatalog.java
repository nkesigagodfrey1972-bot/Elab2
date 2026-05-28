package library_management_system;

import javax.swing.JComboBox;

/**
 * Shared KIU academic/unit catalog and library category helpers.
 */
public final class KiuCatalog {

    public static final String[] UNIVERSITY_UNITS = new String[]{
        "Faculty of Biomedical Sciences - Western Campus",
        "Faculty of Business and Management - Western Campus",
        "Faculty of Clinical Medicine and Dentistry - Western Campus",
        "Faculty of Education - Western Campus",
        "Faculty of Science and Technology - Western Campus",
        "School of Allied Health Sciences - Western Campus",
        "School of Nursing Sciences - Western Campus",
        "School of Pharmacy - Western Campus",
        "School of Engineering and Applied Sciences - Western Campus",
        "School of Law - Main/Western Campus",
        "School of Agriculture Sciences - Western Campus"
    };

    public static final String[] BOOK_CATEGORIES = new String[]{
        "Faculty of Biomedical Sciences",
        "Faculty of Business and Management",
        "Faculty of Clinical Medicine and Dentistry",
        "Faculty of Education",
        "Faculty of Science and Technology",
        "School of Allied Health Sciences",
        "School of Nursing Sciences",
        "School of Pharmacy",
        "School of Engineering and Applied Sciences",
        "School of Law",
        "School of Agriculture Sciences",
        "Science and Technology",
        "Business and Management",
        "Clinical Medicine and Dentistry",
        "Education",
        "Engineering",
        "Agriculture",
        "Law",
        "Health Sciences",
        "Nursing",
        "Pharmacy",
        "Reference",
        "General"
    };

    private KiuCatalog() {
    }

    public static JComboBox<String> createDepartmentCombo() {
        JComboBox<String> combo = new JComboBox<>(UNIVERSITY_UNITS);
        combo.setEditable(false);
        return combo;
    }

    public static JComboBox<String> createBookCategoryCombo() {
        JComboBox<String> combo = new JComboBox<>(BOOK_CATEGORIES);
        combo.setEditable(true);
        return combo;
    }
}
