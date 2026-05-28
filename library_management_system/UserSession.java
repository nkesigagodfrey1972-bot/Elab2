package library_management_system;

/**
 * Holds the currently authenticated user's identity and role for the session.
 * Set once at login; read throughout the application for RBAC.
 *
 * Roles (in descending privilege order):
 *   Admin              – full access
 *   Librarian          – books, members, issue/return, reports
 *   Assistant Librarian – issue/return, view/search
 *   Viewer             – dashboard, books, members, reports (read-only)
 */
public final class UserSession {

    public static final String ROLE_ADMIN     = "Admin";
    public static final String ROLE_LIBRARIAN = "Librarian";
    public static final String ROLE_ASSISTANT = "Assistant Librarian";
    public static final String ROLE_VIEWER    = "Viewer";

    private static String username = "guest";
    private static String role     = ROLE_VIEWER;

    private UserSession() {}

    public static void set(String user, String userRole) {
        username = user  == null ? "guest"     : user;
        role     = userRole == null ? ROLE_VIEWER : userRole;
    }

    public static String getUsername() { return username; }
    public static String getRole()     { return role; }

    // ── Permission checks ─────────────────────────────────────────────────────

    public static boolean isAdmin()     { return ROLE_ADMIN.equalsIgnoreCase(role); }
    public static boolean isLibrarian() { return ROLE_ADMIN.equalsIgnoreCase(role) || ROLE_LIBRARIAN.equalsIgnoreCase(role); }
    public static boolean isAssistant() { return isLibrarian() || ROLE_ASSISTANT.equalsIgnoreCase(role); }
    public static boolean isViewer()    { return true; } // everyone can view

    /** Can manage books (add/edit/delete) */
    public static boolean canManageBooks()    { return isLibrarian(); }
    /** Can manage members (add/edit/delete) */
    public static boolean canManageMembers()  { return isLibrarian(); }
    /** Can issue or return books */
    public static boolean canIssueReturn()    { return isAssistant(); }
    /** Can view/export reports */
    public static boolean canViewReports()    { return isViewer(); }
    /** Can manage fines (mark paid/waived) */
    public static boolean canManageFines()    { return isLibrarian(); }
    /** Can create reservations/bookings */
    public static boolean canCreateReservations() { return isViewer(); }
    /** Can manage reservations */
    public static boolean canManageReservations() { return isAssistant(); }
    /** Can receive login notifications for incoming bookings */
    public static boolean canReceiveReservationNotifications() { return isLibrarian(); }
    /** Can access settings */
    public static boolean canAccessSettings() { return isAdmin(); }
    /** Can view audit logs */
    public static boolean canViewAuditLogs()  { return isAdmin(); }
    /** Can manage book copies */
    public static boolean canManageCopies()   { return isLibrarian(); }

    /** Role badge color for display */
    public static java.awt.Color roleColor() {
        return switch (role) {
            case ROLE_ADMIN     -> new java.awt.Color(196, 43, 28);
            case ROLE_LIBRARIAN -> new java.awt.Color(34, 91, 184);
            case ROLE_ASSISTANT -> new java.awt.Color(34, 139, 34);
            default             -> new java.awt.Color(108, 117, 125);
        };
    }
}
