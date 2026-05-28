package library_management_system;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Fire-and-forget audit logging service.
 * All log writes are non-blocking (background thread) so they never slow the UI.
 */
public final class AuditService {

    private AuditService() {}

    /**
     * Log an action asynchronously. Never throws — failures are silently swallowed
     * so audit logging never breaks the main workflow.
     */
    public static void log(String action, String module, String description) {
        log(action, module, description, "", "");
    }

    public static void log(String action, String module, String description,
                           String oldValue, String newValue) {
        String logId   = "LOG_" + Instant.now().toEpochMilli() + "_" + UUID.randomUUID().toString().substring(0, 8);
        String user    = UserSession.getUsername();
        String role    = UserSession.getRole();
        String ts      = Instant.now().toString();

        Map<String, String> fields = new HashMap<>();
        fields.put("logId",       logId);
        fields.put("userId",      user);
        fields.put("userName",    user);
        fields.put("userRole",    role);
        fields.put("action",      action);
        fields.put("module",      module);
        fields.put("description", description);
        fields.put("oldValue",    oldValue  == null ? "" : oldValue);
        fields.put("newValue",    newValue  == null ? "" : newValue);
        fields.put("timestamp",   ts);

        // Fire and forget on a daemon thread
        Thread t = new Thread(() -> {
            try {
                FirebaseBootstrap.saveAuditLog(fields);
            } catch (Exception ignored) {
                // Audit failures must never surface to the user
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ── Convenience action constants ──────────────────────────────────────────

    public static final String ACTION_LOGIN          = "LOGIN";
    public static final String ACTION_LOGOUT         = "LOGOUT";
    public static final String ACTION_ADD_BOOK       = "ADD_BOOK";
    public static final String ACTION_EDIT_BOOK      = "EDIT_BOOK";
    public static final String ACTION_DELETE_BOOK    = "DELETE_BOOK";
    public static final String ACTION_ADD_MEMBER     = "ADD_MEMBER";
    public static final String ACTION_EDIT_MEMBER    = "EDIT_MEMBER";
    public static final String ACTION_DELETE_MEMBER  = "DELETE_MEMBER";
    public static final String ACTION_ISSUE_BOOK     = "ISSUE_BOOK";
    public static final String ACTION_RETURN_BOOK    = "RETURN_BOOK";
    public static final String ACTION_RENEW_BOOK     = "RENEW_BOOK";
    public static final String ACTION_ADD_FINE       = "ADD_FINE";
    public static final String ACTION_PAY_FINE       = "PAY_FINE";
    public static final String ACTION_WAIVE_FINE     = "WAIVE_FINE";
    public static final String ACTION_RESERVE_BOOK   = "RESERVE_BOOK";
    public static final String ACTION_CANCEL_RESERVE = "CANCEL_RESERVATION";
    public static final String ACTION_EXPORT_REPORT  = "EXPORT_REPORT";
    public static final String ACTION_CHANGE_SETTINGS= "CHANGE_SETTINGS";
    public static final String ACTION_ADD_COPY       = "ADD_BOOK_COPY";
    public static final String ACTION_EDIT_COPY      = "EDIT_BOOK_COPY";
    public static final String ACTION_DELETE_COPY    = "DELETE_BOOK_COPY";

    public static final String MODULE_AUTH           = "Authentication";
    public static final String MODULE_BOOKS          = "Books";
    public static final String MODULE_MEMBERS        = "Members";
    public static final String MODULE_ISSUE          = "Issue";
    public static final String MODULE_RETURN         = "Return";
    public static final String MODULE_FINES          = "Fines";
    public static final String MODULE_RESERVATIONS   = "Reservations";
    public static final String MODULE_REPORTS        = "Reports";
    public static final String MODULE_SETTINGS       = "Settings";
    public static final String MODULE_COPIES         = "BookCopies";
}
