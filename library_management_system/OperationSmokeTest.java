package library_management_system;

import java.time.Instant;

public final class OperationSmokeTest {

    private OperationSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        String suffix = String.valueOf(Instant.now().toEpochMilli());
        String bookId = "SMOKE_BOOK_" + suffix;
        String registrationNo = "SMOKE_STUDENT_" + suffix;

        FirebaseBootstrap.saveBook(bookId, "Smoke Test Book", "Smoke Author", "Smoke Category", "123");
        FirebaseBootstrap.saveStudent(registrationNo, "Smoke Student", "5550001", "IT");
        FirebaseBootstrap.saveIssueRecord(bookId, "Smoke Test Book", "Smoke Author", "Smoke Category", "123", registrationNo, "Smoke Student", "2026-05-24", "yes");

        if (FirebaseBootstrap.getBook(bookId) == null) {
            throw new IllegalStateException("Book lookup failed");
        }
        if (FirebaseBootstrap.getStudent(registrationNo) == null) {
            throw new IllegalStateException("Student lookup failed");
        }
        if (FirebaseBootstrap.getIssueRecord(bookId, registrationNo) == null) {
            throw new IllegalStateException("Issue record lookup failed");
        }

        FirebaseBootstrap.saveBook(bookId, "Smoke Test Book v2", "Smoke Author", "Smoke Category", "456");
        FirebaseBootstrap.updateStudent(registrationNo, "Smoke Student Updated", "5550002", "IT");
        FirebaseBootstrap.updateIssueRecordReturn(bookId, registrationNo, "2026-05-25", "no");

        FirebaseBootstrap.deleteBook(bookId);
        FirebaseBootstrap.deleteStudent(registrationNo);
        FirebaseBootstrap.deleteIssueRecord(bookId, registrationNo);

        System.out.println("Operation smoke test passed.");
    }
}