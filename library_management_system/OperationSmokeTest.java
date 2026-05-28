package library_management_system;

import java.time.Instant;
import java.util.Map;
import java.util.Properties;

public final class OperationSmokeTest {

    private OperationSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        String suffix = String.valueOf(Instant.now().toEpochMilli());
        String bookId = "SMOKE_BOOK_" + suffix;
        String copyId = "SMOKE_COPY_" + suffix;
        String barcode = "BARCODE_" + suffix;
        String registrationNo = "SMOKE_STUDENT_" + suffix;
        String fineId = "SMOKE_FINE_" + suffix;
        String reservationId = "SMOKE_RES_" + suffix;
        String auditId = "SMOKE_AUDIT_" + suffix;

        FirebaseBootstrap.saveBook(bookId, "Smoke Test Book", "Smoke Author", "Smoke Category", "123");
        FirebaseBootstrap.saveStudent(registrationNo, "Smoke Student", "5550001", "IT");
        FirebaseBootstrap.saveIssueRecord(bookId, "Smoke Test Book", "Smoke Author", "Smoke Category", "123", registrationNo, "Smoke Student", "2026-05-24", "yes");
        FirebaseBootstrap.saveBookCopy(Map.of(
            "copyId", copyId,
            "bookId", bookId,
            "barcode", barcode,
            "status", "Available",
            "condition", "Good"
        ));
        FirebaseBootstrap.saveFine(Map.of(
            "fineId", fineId,
            "memberId", registrationNo,
            "bookId", bookId,
            "amount", "4.50",
            "status", "Pending"
        ));
        FirebaseBootstrap.saveReservation(Map.of(
            "reservationId", reservationId,
            "bookId", bookId,
            "memberId", registrationNo,
            "status", "Pending"
        ));
        FirebaseBootstrap.saveAuditLog(Map.of(
            "logId", auditId,
            "action", "SMOKE_TEST",
            "module", "Smoke",
            "username", "smoke",
            "details", "Smoke test entry"
        ));
        FirebaseBootstrap.saveFirestoreSettings(Map.of(
            "library.name", "Elab Library Smoke",
            "borrow.days", "14"
        ));

        if (FirebaseBootstrap.getBook(bookId) == null) {
            throw new IllegalStateException("Book lookup failed");
        }
        if (FirebaseBootstrap.getStudent(registrationNo) == null) {
            throw new IllegalStateException("Student lookup failed");
        }
        if (FirebaseBootstrap.getIssueRecord(bookId, registrationNo) == null) {
            throw new IllegalStateException("Issue record lookup failed");
        }
        if (FirebaseBootstrap.getBookCopy(copyId) == null) {
            throw new IllegalStateException("Book copy lookup failed");
        }
        if (FirebaseBootstrap.getBookCopyByBarcode(barcode) == null) {
            throw new IllegalStateException("Barcode lookup failed");
        }
        if (FirebaseBootstrap.getFine(fineId) == null) {
            throw new IllegalStateException("Fine lookup failed");
        }
        if (FirebaseBootstrap.getReservation(reservationId) == null) {
            throw new IllegalStateException("Reservation lookup failed");
        }
        if (FirebaseBootstrap.listAuditLogs().stream().noneMatch(log -> auditId.equals(log.get("logId")))) {
            throw new IllegalStateException("Audit log lookup failed");
        }
        if (FirebaseBootstrap.getPendingFineTotal(registrationNo) <= 0) {
            throw new IllegalStateException("Pending fine total failed");
        }

        FirebaseBootstrap.saveBook(bookId, "Smoke Test Book v2", "Smoke Author", "Smoke Category", "456");
        FirebaseBootstrap.updateStudent(registrationNo, "Smoke Student Updated", "5550002", "IT");
        FirebaseBootstrap.renewIssueRecord(bookId, registrationNo, "2026-05-30", "smoke-tester");
        FirebaseBootstrap.updateIssueRecordReturn(bookId, registrationNo, "2026-05-25", "no");

        Properties settings = AppSettings.load();
        settings.setProperty("smoke.test.key", suffix);
        AppSettings.save(settings);
        if (!suffix.equals(AppSettings.get("smoke.test.key", ""))) {
            throw new IllegalStateException("AppSettings persistence failed");
        }

        FirebaseBootstrap.deleteBook(bookId);
        FirebaseBootstrap.deleteBookCopy(copyId);
        FirebaseBootstrap.deleteStudent(registrationNo);
        FirebaseBootstrap.deleteIssueRecord(bookId, registrationNo);
        FirebaseBootstrap.deleteFine(fineId);
        FirebaseBootstrap.deleteReservation(reservationId);
        FirebaseBootstrap.removeDocument("auditLogs", auditId);

        System.out.println("Operation smoke test passed.");
    }
}
