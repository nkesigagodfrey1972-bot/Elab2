package library_management_system;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.InputStream;

public final class FirebaseBootstrap {

    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final String SCOPE = "https://www.googleapis.com/auth/datastore https://www.googleapis.com/auth/cloud-platform";
    private static final String BUNDLED_SERVICE_ACCOUNT_RESOURCE = "/library_management_system/e-library-service-account.json";

    private FirebaseBootstrap() {
    }

    public static void ensureSeedData() {
        try {
            ServiceAccount serviceAccount = loadServiceAccount();
            if (serviceAccount == null) {
                System.out.println("Firebase bootstrap skipped: set FIREBASE_SERVICE_ACCOUNT to your service-account JSON path.");
                return;
            }

            String accessToken = fetchAccessToken(serviceAccount.clientEmail, serviceAccount.privateKeyPem);
            String timestamp = Instant.now().toString();

            seedCollection(serviceAccount.projectId, accessToken, "employees", "seed-admin", documentFields(new Object[][]{
                {"employeeName", "admin"},
                {"username", "admin"},
                {"fullName", "System Administrator"},
                {"email", ""},
                {"phoneNumber", ""},
                {"department", "Administration"},
                {"passwordHash", hashPassword("admin123")},
                {"role", "Admin"},
                {"seededAt", timestamp}
            }));

            seedCollection(serviceAccount.projectId, accessToken, "students", "seed-student", documentFields(new Object[][]{
                {"registrationNo", "0000"},
                {"studentName", "sample student"},
                {"mobileNo", ""},
                {"branch", ""},
                {"seededAt", timestamp}
            }));

            seedCollection(serviceAccount.projectId, accessToken, "books", "seed-book", documentFields(new Object[][]{
                {"bookId", 0},
                {"bookName", "sample book"},
                {"author", ""},
                {"category", ""},
                {"price", 0},
                {"issued", "no"},
                {"seededAt", timestamp}
            }));

            seedCollection(serviceAccount.projectId, accessToken, "issue_records", "seed-issue-record", documentFields(new Object[][]{
                {"bookId", 0},
                {"bookName", "sample book"},
                {"author", ""},
                {"category", ""},
                {"price", 0},
                {"registrationNo", "0000"},
                {"studentName", "sample student"},
                {"issueDate", ""},
                {"returnDate", ""},
                {"issued", "no"},
                {"seededAt", timestamp}
            }));

            System.out.println("Firebase collections seeded successfully.");
        } catch (Exception ex) {
            System.out.println("Firebase bootstrap skipped: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        ensureSeedData();
    }

    private static String fetchAccessToken(String clientEmail, String privateKeyPem) throws Exception {
        long now = Instant.now().getEpochSecond();
        String headerJson = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";
        String claimJson = "{" +
            "\"iss\":\"" + escapeJson(clientEmail) + "\"," +
            "\"scope\":\"" + escapeJson(SCOPE) + "\"," +
            "\"aud\":\"https://oauth2.googleapis.com/token\"," +
            "\"iat\":" + now + "," +
            "\"exp\":" + (now + 3600) +
            "}";

        String unsignedJwt = base64Url(headerJson.getBytes(StandardCharsets.UTF_8)) + "." +
            base64Url(claimJson.getBytes(StandardCharsets.UTF_8));

        PrivateKey privateKey = loadPrivateKey(privateKeyPem);
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(unsignedJwt.getBytes(StandardCharsets.UTF_8));
        String jwt = unsignedJwt + "." + base64Url(signature.sign());

        String form = "grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=" +
            urlEncode(jwt);

        HttpRequest request = HttpRequest.newBuilder(URI.create("https://oauth2.googleapis.com/token"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(form))
            .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("OAuth token request failed: " + response.body());
        }

        return extractJsonString(response.body(), "access_token");
    }

    public static boolean validateEmployee(String username, String password) throws Exception {
        ServiceAccount serviceAccount = loadServiceAccount();
        if (serviceAccount == null) {
            throw new IllegalStateException("FIREBASE_SERVICE_ACCOUNT is not configured.");
        }

        String accessToken = fetchAccessToken(serviceAccount.clientEmail, serviceAccount.privateKeyPem);
        String documentJson = getDocument(serviceAccount.projectId, accessToken, "employees", username);
        if (documentJson == null) {
            return false;
        }

        String storedHash = extractDocumentStringField(documentJson, "passwordHash");
        return storedHash != null && storedHash.equals(hashPassword(password));
    }

    public static void createOrUpdateEmployee(String username, String password) throws Exception {
        createOrUpdateEmployee(username, password, "Librarian");
    }

    public static Map<String, String> getEmployee(String username) throws Exception {
        Map<String, String> fields = getDocumentFields("employees", username);
        if (fields == null) {
            return null;
        }
        fields.put("username", username);
        return fields;
    }

    public static boolean employeeExists(String username) throws Exception {
        return getEmployee(username) != null;
    }

    public static Map<String, String> getBook(String bookId) throws Exception {
        Map<String, String> fields = getDocumentFields("books", bookId);
        if (fields == null) {
            return null;
        }
        fields.put("bookId", bookId);
        return fields;
    }

    public static List<Map<String, String>> searchBooksByNamePrefix(String prefix) throws Exception {
        return filterDocs("books", book -> startsWithIgnoreCase(book.get("bookName"), prefix));
    }

    public static List<Map<String, String>> searchBooksByCategoryPrefix(String prefix) throws Exception {
        return filterDocs("books", book -> startsWithIgnoreCase(book.get("category"), prefix));
    }

    public static void saveBook(String bookId, String bookName, String author, String category, String price) throws Exception {
        requireNonBlank(bookId, "Book ID");
        upsertFirestoreDocument("books", bookId, mapOf(
            "bookId", bookId,
            "bookName", bookName,
            "author", author,
            "category", category,
            "price", price,
            "issued", "no"
        ));
    }

    public static void updateBook(String bookId, String bookName, String author, String category, String price) throws Exception {
        saveBook(bookId, bookName, author, category, price);
    }

    public static void deleteBook(String bookId) throws Exception {
        deleteDocument("books", bookId);
    }

    public static Map<String, String> getStudent(String registrationNo) throws Exception {
        Map<String, String> fields = getDocumentFields("students", registrationNo);
        if (fields == null) {
            return null;
        }
        fields.put("registrationNo", registrationNo);
        return fields;
    }

    public static void saveStudent(String registrationNo, String studentName, String mobileNo, String branch) throws Exception {
        requireNonBlank(registrationNo, "Registration No");
        upsertFirestoreDocument("students", registrationNo, mapOf(
            "registrationNo", registrationNo,
            "studentName", studentName,
            "mobileNo", mobileNo,
            "branch", branch
        ));
    }

    public static void updateStudent(String registrationNo, String studentName, String mobileNo, String branch) throws Exception {
        saveStudent(registrationNo, studentName, mobileNo, branch);
    }

    public static void deleteStudent(String registrationNo) throws Exception {
        deleteDocument("students", registrationNo);
    }

    public static Map<String, String> getIssueRecord(String bookId, String registrationNo) throws Exception {
        Map<String, String> fields = getDocumentFields("issue_records", issueDocumentId(bookId, registrationNo));
        if (fields == null) {
            return null;
        }
        fields.put("bookId", bookId);
        fields.put("registrationNo", registrationNo);
        return fields;
    }

    public static List<Map<String, String>> searchIssueRecordsByBookId(String bookId) throws Exception {
        return filterDocs("issue_records", record -> equalsIgnoreCase(record.get("bookId"), bookId));
    }

    public static List<Map<String, String>> searchIssueRecordsByBookNamePrefix(String prefix) throws Exception {
        return filterDocs("issue_records", record -> startsWithIgnoreCase(record.get("bookName"), prefix));
    }

    public static List<Map<String, String>> searchIssueRecordsByCategoryPrefix(String prefix) throws Exception {
        return filterDocs("issue_records", record -> startsWithIgnoreCase(record.get("category"), prefix));
    }

    public static List<Map<String, String>> searchIssueRecordsByStatus(String status) throws Exception {
        return filterDocs("issue_records", record -> equalsIgnoreCase(record.get("issued"), status));
    }

    public static List<Map<String, String>> searchIssueRecordsByRegistrationNo(String registrationNo) throws Exception {
        return filterDocs("issue_records", record -> equalsIgnoreCase(record.get("registrationNo"), registrationNo));
    }

    public static void saveIssueRecord(String bookId, String bookName, String author, String category, String price, String registrationNo, String studentName, String issueDate, String issued) throws Exception {
        requireNonBlank(bookId, "Book ID");
        requireNonBlank(registrationNo, "Registration No");
        upsertFirestoreDocument("issue_records", issueDocumentId(bookId, registrationNo), mapOf(
            "bookId", bookId,
            "bookName", bookName,
            "author", author,
            "category", category,
            "price", price,
            "registrationNo", registrationNo,
            "studentName", studentName,
            "issueDate", issueDate,
            "returnDate", "",
            "issued", issued
        ));
        // mark the book as issued in the books collection
        setBookIssuedStatus(bookId, true);
    }

    public static void updateIssueRecordReturn(String bookId, String registrationNo, String returnDate, String issued) throws Exception {
        requireNonBlank(bookId, "Book ID");
        requireNonBlank(registrationNo, "Registration No");
        Map<String, String> current = getIssueRecord(bookId, registrationNo);
        if (current == null) {
            throw new IllegalStateException("Issue record not found");
        }

        upsertFirestoreDocument("issue_records", issueDocumentId(bookId, registrationNo), mapOf(
            "bookId", bookId,
            "bookName", current.getOrDefault("bookName", ""),
            "author", current.getOrDefault("author", ""),
            "category", current.getOrDefault("category", ""),
            "price", current.getOrDefault("price", ""),
            "registrationNo", registrationNo,
            "studentName", current.getOrDefault("studentName", ""),
            "issueDate", current.getOrDefault("issueDate", ""),
            "returnDate", returnDate,
            "issued", issued
        ));
        // update book issued flag based on issued status
        setBookIssuedStatus(bookId, !"no".equalsIgnoreCase(issued));
    }

    public static void deleteIssueRecord(String bookId, String registrationNo) throws Exception {
        requireNonBlank(bookId, "Book ID");
        requireNonBlank(registrationNo, "Registration No");
        deleteDocument("issue_records", issueDocumentId(bookId, registrationNo));
        // when issue record deleted, mark book as available
        try {
            setBookIssuedStatus(bookId, false);
        } catch (Exception ex) {
            // non-fatal: don't fail delete if book update cannot be applied
        }
    }

    public static void setBookIssuedStatus(String bookId, boolean issued) throws Exception {
        requireNonBlank(bookId, "Book ID");
        Map<String, String> book = getBook(bookId);
        if (book == null) {
            throw new IllegalStateException("Book not found: " + bookId);
        }
        book.put("issued", issued ? "yes" : "no");
        // ensure we persist the updated issued flag
        upsertFirestoreDocument("books", bookId, book);
    }

    public static String generateBookId() throws Exception {
        // Try to generate a numeric incrementing ID based on existing numeric bookIds.
        List<Map<String, String>> books = listBooks();
        long max = -1;
        for (Map<String, String> b : books) {
            String id = b.getOrDefault("bookId", b.getOrDefault("_id", ""));
            if (id == null || id.isBlank()) continue;
            try {
                long v = Long.parseLong(id);
                if (v > max) max = v;
            } catch (NumberFormatException ignored) {
                // ignore non-numeric ids
            }
        }
        if (max >= 0) {
            return String.valueOf(max + 1);
        }
        // fallback: timestamp-based id
        return String.valueOf(System.currentTimeMillis());
    }

    private static ServiceAccount loadServiceAccount() throws Exception {
        String credentialsPath = System.getenv("FIREBASE_SERVICE_ACCOUNT");
        if (credentialsPath == null || credentialsPath.isBlank()) {
            credentialsPath = System.getProperty("firebase.serviceAccount");
        }

        if (credentialsPath != null && !credentialsPath.isBlank() && !Files.exists(Path.of(credentialsPath))) {
            credentialsPath = null;
        }

        if (credentialsPath == null || credentialsPath.isBlank()) {
            Path projectLocalKey = Path.of("library_management_system", "e-library-service-account.json");
            if (Files.exists(projectLocalKey)) {
                credentialsPath = projectLocalKey.toString();
            }
        }

        if (credentialsPath == null || credentialsPath.isBlank()) {
            Path rootLocalKey = Path.of("e-library-service-account.json");
            if (Files.exists(rootLocalKey)) {
                credentialsPath = rootLocalKey.toString();
            }
        }

        String serviceAccountJson = null;
        if (credentialsPath != null && !credentialsPath.isBlank()) {
            serviceAccountJson = Files.readString(Path.of(credentialsPath));
        } else {
            try (InputStream bundledCredentials = FirebaseBootstrap.class.getResourceAsStream(BUNDLED_SERVICE_ACCOUNT_RESOURCE)) {
                if (bundledCredentials == null) {
                    return null;
                }
                serviceAccountJson = new String(bundledCredentials.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        return new ServiceAccount(
            extractJsonString(serviceAccountJson, "project_id"),
            extractJsonString(serviceAccountJson, "client_email"),
            extractJsonString(serviceAccountJson, "private_key")
        );
    }

    private static Map<String, String> getDocumentFields(String collection, String documentId) throws Exception {
        ServiceAccount serviceAccount = loadServiceAccount();
        if (serviceAccount == null) {
            throw new IllegalStateException("FIREBASE_SERVICE_ACCOUNT is not configured.");
        }

        String accessToken = fetchAccessToken(serviceAccount.clientEmail, serviceAccount.privateKeyPem);
        String documentJson = getDocument(serviceAccount.projectId, accessToken, collection, documentId);
        if (documentJson == null) {
            return null;
        }

        return parseDocumentFields(documentJson);
    }

    private static List<Map<String, String>> filterDocs(String collection, java.util.function.Predicate<Map<String, String>> predicate) throws Exception {
        List<Map<String, String>> results = new ArrayList<>();
        for (Map<String, String> document : listCollectionDocuments(collection)) {
            if (predicate.test(document)) {
                results.add(document);
            }
        }
        return results;
    }

    private static List<Map<String, String>> listCollectionDocuments(String collection) throws Exception {
        ServiceAccount serviceAccount = loadServiceAccount();
        if (serviceAccount == null) {
            throw new IllegalStateException("FIREBASE_SERVICE_ACCOUNT is not configured.");
        }

        String accessToken = fetchAccessToken(serviceAccount.clientEmail, serviceAccount.privateKeyPem);
        String url = "https://firestore.googleapis.com/v1/projects/" + serviceAccount.projectId +
            "/databases/(default)/documents/" + collection + "?pageSize=1000";

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("Firestore list failed for " + collection + ": " + response.body());
        }

        return parseDocumentsArray(response.body());
    }

    public static List<Map<String, String>> listBooks() throws Exception {
        return listCollectionDocuments("books");
    }

    public static List<Map<String, String>> listStudents() throws Exception {
        return listCollectionDocuments("students");
    }

    public static List<Map<String, String>> listIssueRecords() throws Exception {
        return listCollectionDocuments("issue_records");
    }

    public static int countBooks() throws Exception {
        return listBooks().size();
    }

    public static int countStudents() throws Exception {
        return listStudents().size();
    }

    public static int countIssueRecords() throws Exception {
        return listIssueRecords().size();
    }

    public static int countIssuedBooks() throws Exception {
        int count = 0;
        for (Map<String, String> record : listIssueRecords()) {
            if ("yes".equalsIgnoreCase(record.get("issued"))) {
                count++;
            }
        }
        return count;
    }

    public static int countAvailableBooks() throws Exception {
        int count = 0;
        for (Map<String, String> book : listBooks()) {
            if ("no".equalsIgnoreCase(book.get("issued"))) {
                count++;
            }
        }
        return count;
    }

    private static List<Map<String, String>> parseDocumentsArray(String json) {
        List<Map<String, String>> documents = new ArrayList<>();
        int markerIndex = json.indexOf("\"documents\"");
        if (markerIndex < 0) {
            return documents;
        }

        int arrayStart = json.indexOf('[', markerIndex);
        if (arrayStart < 0) {
            return documents;
        }

        int index = arrayStart + 1;
        while (index < json.length()) {
            index = skipWhitespace(json, index);
            if (index >= json.length() || json.charAt(index) == ']') {
                break;
            }

            if (json.charAt(index) != '{') {
                index++;
                continue;
            }

            int end = findMatchingBrace(json, index);
            if (end < 0) {
                break;
            }

            documents.add(parseDocumentFields(json.substring(index, end + 1)));
            index = end + 1;
        }

        return documents;
    }

    private static Map<String, String> parseDocumentFields(String documentJson) {
        Map<String, String> fields = new HashMap<>();
        String name = extractStringField(documentJson, "name");
        if (name != null) {
            int lastSlash = name.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < name.length() - 1) {
                fields.put("_id", name.substring(lastSlash + 1));
            }
        }

        for (String field : new String[]{"bookId", "bookName", "author", "category", "price", "issued", "registrationNo", "studentName", "mobileNo", "branch", "issueDate", "returnDate", "seededAt", "employeeName", "username", "fullName", "email", "phoneNumber", "department", "passwordHash", "role", "createdAt", "updatedAt"}) {
            String value = extractFieldValue(documentJson, field);
            if (value != null) {
                fields.put(field, value);
            }
        }

        return fields;
    }

    private static String extractFieldValue(String documentJson, String fieldName) {
        String marker = "\"" + fieldName + "\"";
        int fieldIndex = documentJson.indexOf(marker);
        if (fieldIndex < 0) {
            return null;
        }

        int objectStart = documentJson.indexOf('{', fieldIndex);
        if (objectStart < 0) {
            return null;
        }

        int objectEnd = findMatchingBrace(documentJson, objectStart);
        if (objectEnd < 0) {
            return null;
        }

        return extractFirestoreValue(documentJson.substring(objectStart, objectEnd + 1));
    }

    private static String extractFirestoreValue(String fieldObject) {
        String stringMarker = "\"stringValue\"";
        int index = fieldObject.indexOf(stringMarker);
        if (index >= 0) {
            int startQuote = fieldObject.indexOf('"', index + stringMarker.length());
            if (startQuote >= 0) {
                return readJsonString(fieldObject, startQuote + 1);
            }
        }

        String integerMarker = "\"integerValue\"";
        index = fieldObject.indexOf(integerMarker);
        if (index >= 0) {
            int startQuote = fieldObject.indexOf('"', index + integerMarker.length());
            if (startQuote >= 0) {
                return readJsonString(fieldObject, startQuote + 1);
            }
        }

        String doubleMarker = "\"doubleValue\"";
        index = fieldObject.indexOf(doubleMarker);
        if (index >= 0) {
            int startQuote = fieldObject.indexOf('"', index + doubleMarker.length());
            if (startQuote >= 0) {
                return readJsonString(fieldObject, startQuote + 1);
            }
        }

        String booleanMarker = "\"booleanValue\"";
        index = fieldObject.indexOf(booleanMarker);
        if (index >= 0) {
            int colon = fieldObject.indexOf(':', index + booleanMarker.length());
            if (colon >= 0) {
                int start = skipWhitespace(fieldObject, colon + 1);
                int end = start;
                while (end < fieldObject.length() && Character.isLetter(fieldObject.charAt(end))) {
                    end++;
                }
                return fieldObject.substring(start, end);
            }
        }

        return null;
    }

    private static int skipWhitespace(String text, int index) {
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
            index++;
        }
        return index;
    }

    private static int findMatchingBrace(String text, int openIndex) {
        int depth = 0;
        boolean inString = false;
        boolean escaping = false;
        for (int index = openIndex; index < text.length(); index++) {
            char current = text.charAt(index);
            if (inString) {
                if (escaping) {
                    escaping = false;
                } else if (current == '\\') {
                    escaping = true;
                } else if (current == '"') {
                    inString = false;
                }
                continue;
            }

            if (current == '"') {
                inString = true;
            } else if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return index;
                }
            }
        }

        return -1;
    }

    private static String extractStringField(String json, String key) {
        String marker = "\"" + key + "\"";
        int keyIndex = json.indexOf(marker);
        if (keyIndex < 0) {
            return null;
        }

        int colonIndex = json.indexOf(':', keyIndex + marker.length());
        if (colonIndex < 0) {
            return null;
        }

        int startQuote = json.indexOf('"', colonIndex + 1);
        if (startQuote < 0) {
            return null;
        }

        return readJsonString(json, startQuote + 1);
    }

    private static String issueDocumentId(String bookId, String registrationNo) {
        requireNonBlank(bookId, "Book ID");
        requireNonBlank(registrationNo, "Registration No");
        return bookId + "_" + registrationNo;
    }

    private static void requireNonBlank(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
    }

    private static Map<String, String> mapOf(String... pairs) {
        Map<String, String> map = new HashMap<>();
        for (int index = 0; index + 1 < pairs.length; index += 2) {
            map.put(pairs[index], pairs[index + 1]);
        }
        return map;
    }

    private static void upsertFirestoreDocument(String collection, String documentId, Map<String, String> fields) throws Exception {
        requireNonBlank(documentId, "Document ID");
        ServiceAccount serviceAccount = loadServiceAccount();
        if (serviceAccount == null) {
            throw new IllegalStateException("FIREBASE_SERVICE_ACCOUNT is not configured.");
        }

        String accessToken = fetchAccessToken(serviceAccount.clientEmail, serviceAccount.privateKeyPem);
        String url = "https://firestore.googleapis.com/v1/projects/" + serviceAccount.projectId +
            "/databases/(default)/documents/" + collection + "/" + urlEncode(documentId);

        String body = "{\"fields\":" + toFirestoreJson(fields) + "}";
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
            .header("Authorization", "Bearer " + accessToken)
            .header("Content-Type", "application/json")
            .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("Firestore upsert failed for " + collection + ": " + response.body());
        }
    }

    private static void deleteDocument(String collection, String documentId) throws Exception {
        ServiceAccount serviceAccount = loadServiceAccount();
        if (serviceAccount == null) {
            throw new IllegalStateException("FIREBASE_SERVICE_ACCOUNT is not configured.");
        }

        String accessToken = fetchAccessToken(serviceAccount.clientEmail, serviceAccount.privateKeyPem);
        String url = "https://firestore.googleapis.com/v1/projects/" + serviceAccount.projectId +
            "/databases/(default)/documents/" + collection + "/" + urlEncode(documentId);

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
            .header("Authorization", "Bearer " + accessToken)
            .DELETE()
            .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("Firestore delete failed for " + collection + ": " + response.body());
        }
    }

    private static String toFirestoreJson(Map<String, String> fields) {
        StringBuilder builder = new StringBuilder("{");
        int index = 0;
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if (index++ > 0) {
                builder.append(',');
            }
            builder.append('"').append(escapeJson(entry.getKey())).append('"').append(':');
            builder.append("{\"stringValue\":\"").append(escapeJson(entry.getValue() == null ? "" : entry.getValue())).append("\"}");
        }
        builder.append('}');
        return builder.toString();
    }

    private static boolean startsWithIgnoreCase(String value, String prefix) {
        return value != null && prefix != null && value.toLowerCase().startsWith(prefix.toLowerCase());
    }

    private static boolean equalsIgnoreCase(String value, String expected) {
        return value != null && expected != null && value.equalsIgnoreCase(expected);
    }

    private static String getDocument(String projectId, String accessToken, String collection, String documentId) throws Exception {
        String url = "https://firestore.googleapis.com/v1/projects/" + projectId +
            "/databases/(default)/documents/" + collection + "/" + urlEncode(documentId);

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 404) {
            return null;
        }
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("Firestore read failed for " + collection + ": " + response.body());
        }

        return response.body();
    }

    private static void upsertDocument(String projectId, String accessToken, String collection, String documentId, String fieldsJson) throws Exception {
        String url = "https://firestore.googleapis.com/v1/projects/" + projectId +
            "/databases/(default)/documents/" + collection + "/" + urlEncode(documentId);

        String body = "{\"fields\":" + fieldsJson + "}";
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
            .header("Authorization", "Bearer " + accessToken)
            .header("Content-Type", "application/json")
            .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("Firestore upsert failed for " + collection + ": " + response.body());
        }
    }

    private static String extractDocumentStringField(String documentJson, String fieldName) {
        String marker = "\"" + fieldName + "\"";
        int fieldIndex = documentJson.indexOf(marker);
        if (fieldIndex < 0) {
            return null;
        }

        int valueIndex = documentJson.indexOf("\"stringValue\"", fieldIndex);
        if (valueIndex < 0) {
            return null;
        }

        int startQuote = documentJson.indexOf('"', valueIndex + 13);
        if (startQuote < 0) {
            return null;
        }

        return readJsonString(documentJson, startQuote + 1);
    }

    private static String hashPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        for (byte value : hash) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }

    private static PrivateKey loadPrivateKey(String privateKeyPem) throws Exception {
        String normalized = privateKeyPem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\n", "")
            .replace("\n", "")
            .replace("\r", "")
            .replace(" ", "");

        byte[] der = Base64.getDecoder().decode(normalized);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private static void seedCollection(String projectId, String accessToken, String collection, String documentId, String fieldsJson) throws Exception {
        String url = "https://firestore.googleapis.com/v1/projects/" + projectId +
            "/databases/(default)/documents/" + collection + "/" + urlEncode(documentId);

        String body = "{\"fields\":" + fieldsJson + "}";
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
            .header("Authorization", "Bearer " + accessToken)
            .header("Content-Type", "application/json")
            .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("Firestore seed failed for " + collection + ": " + response.body());
        }
    }

    private static String documentFields(Object[][] entries) {
        StringBuilder builder = new StringBuilder("{");
        for (int index = 0; index < entries.length; index++) {
            Object[] entry = entries[index];
            if (index > 0) {
                builder.append(',');
            }
            builder.append('"').append(escapeJson(String.valueOf(entry[0]))).append('"').append(':');
            builder.append(toFirestoreValue(entry[1]));
        }
        builder.append('}');
        return builder.toString();
    }

    private static String toFirestoreValue(Object value) {
        if (value instanceof Number) {
            return "{\"integerValue\":\"" + value + "\"}";
        }
        if (value instanceof Boolean) {
            return "{\"booleanValue\":" + value + "}";
        }
        if (value == null) {
            return "{\"nullValue\":null}";
        }
        return "{\"stringValue\":\"" + escapeJson(String.valueOf(value)) + "\"}";
    }

    private static String extractJsonString(String json, String key) {
        String marker = "\"" + key + "\"";
        int keyIndex = json.indexOf(marker);
        if (keyIndex < 0) {
            throw new IllegalStateException("Missing JSON key: " + key);
        }

        int colonIndex = json.indexOf(':', keyIndex + marker.length());
        if (colonIndex < 0) {
            throw new IllegalStateException("Missing JSON key: " + key);
        }

        int startQuote = json.indexOf('"', colonIndex + 1);
        if (startQuote < 0) {
            throw new IllegalStateException("Missing JSON key: " + key);
        }

        return readJsonString(json, startQuote + 1);
    }

    private static String readJsonString(String json, int startIndex) {
        StringBuilder builder = new StringBuilder();
        boolean escaping = false;
        for (int index = startIndex; index < json.length(); index++) {
            char current = json.charAt(index);
            if (escaping) {
                switch (current) {
                    case 'r' -> builder.append('\r');
                    case 'n' -> builder.append('\n');
                    case 't' -> builder.append('\t');
                    case '"' -> builder.append('"');
                    case '\\' -> builder.append('\\');
                    case 'u' -> {
                        if (index + 4 >= json.length()) {
                            throw new IllegalStateException("Invalid unicode escape in JSON string");
                        }
                        String hex = json.substring(index + 1, index + 5);
                        builder.append((char) Integer.parseInt(hex, 16));
                        index += 4;
                    }
                    default -> builder.append(current);
                }
                escaping = false;
            } else if (current == '\\') {
                escaping = true;
            } else if (current == '"') {
                return builder.toString();
            } else {
                builder.append(current);
            }
        }

        throw new IllegalStateException("Unterminated JSON string");
    }

    private static String escapeJson(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "\\r")
            .replace("\n", "\\n")
            .replace("\t", "\\t");
    }

    private static String base64Url(byte[] input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input);
    }

    private static String urlEncode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    // ── Employee role support ─────────────────────────────────────────────────

    /** Returns the role stored for an employee, defaulting to "Viewer" if absent. */
    public static String getEmployeeRole(String username) throws Exception {
        Map<String, String> fields = getEmployee(username);
        if (fields == null) return "Viewer";
        return fields.getOrDefault("role", "Viewer");
    }

    /** Creates/updates an employee with a role field. */
    public static void createOrUpdateEmployee(String username, String password, String role) throws Exception {
        createOrUpdateEmployee(username, password, role, "", "", "", "");
    }

    /** Creates/updates an employee with role and profile fields. */
    public static void createOrUpdateEmployee(String username, String password, String role,
                                              String fullName, String email, String phoneNumber,
                                              String department) throws Exception {
        ServiceAccount sa = loadServiceAccount();
        if (sa == null) throw new IllegalStateException("FIREBASE_SERVICE_ACCOUNT is not configured.");
        if ("Admin".equalsIgnoreCase(role)) {
            throw new IllegalArgumentException("Admin role cannot be assigned from signup. Set it directly in Firebase.");
        }
        String token = fetchAccessToken(sa.clientEmail, sa.privateKeyPem);
        String now = Instant.now().toString();
        Map<String, String> existing = getEmployee(username);
        String createdAt = existing != null
            ? existing.getOrDefault("createdAt", existing.getOrDefault("seededAt", now))
            : now;
        upsertDocument(sa.projectId, token, "employees", username, documentFields(new Object[][]{
            {"employeeName", username},
            {"username", username},
            {"fullName", fullName == null ? "" : fullName},
            {"email", email == null ? "" : email},
            {"phoneNumber", phoneNumber == null ? "" : phoneNumber},
            {"department", department == null ? "" : department},
            {"passwordHash", hashPassword(password)},
            {"role", role == null ? "Viewer" : role},
            {"createdAt", createdAt},
            {"updatedAt", now}
        }));
    }

    // ── Generic collection helpers (public) ───────────────────────────────────

    /** Upsert any document in any collection with a string field map. */
    public static void upsertDocumentMap(String collection, String documentId, Map<String, String> fields) throws Exception {
        upsertFirestoreDocument(collection, documentId, fields);
    }

    /** Delete any document from any collection. */
    public static void removeDocument(String collection, String documentId) throws Exception {
        deleteDocument(collection, documentId);
    }

    /** List all documents in any collection. */
    public static List<Map<String, String>> listCollection(String collection) throws Exception {
        return listCollectionDocuments(collection);
    }

    // ── Audit logs ────────────────────────────────────────────────────────────

    public static void saveAuditLog(Map<String, String> fields) throws Exception {
        requireNonBlank(fields.getOrDefault("logId", ""), "logId");
        upsertFirestoreDocument("auditLogs", fields.get("logId"), fields);
    }

    public static List<Map<String, String>> listAuditLogs() throws Exception {
        return listCollectionDocuments("auditLogs");
    }

    // ── Fines ─────────────────────────────────────────────────────────────────

    public static void saveFine(Map<String, String> fields) throws Exception {
        requireNonBlank(fields.getOrDefault("fineId", ""), "fineId");
        upsertFirestoreDocument("fines", fields.get("fineId"), fields);
    }

    public static List<Map<String, String>> listFines() throws Exception {
        return listCollectionDocuments("fines");
    }

    public static Map<String, String> getFine(String fineId) throws Exception {
        return getDocumentFields("fines", fineId);
    }

    public static void deleteFine(String fineId) throws Exception {
        deleteDocument("fines", fineId);
    }

    /** Returns all pending fines for a member. */
    public static List<Map<String, String>> getPendingFinesForMember(String registrationNo) throws Exception {
        return filterDocs("fines", f ->
            equalsIgnoreCase(f.get("memberId"), registrationNo) &&
            equalsIgnoreCase(f.get("status"), "Pending"));
    }

    /** Sum of pending fine amounts for a member. */
    public static double getPendingFineTotal(String registrationNo) throws Exception {
        double total = 0;
        for (Map<String, String> f : getPendingFinesForMember(registrationNo)) {
            try { total += Double.parseDouble(f.getOrDefault("amount", "0")); }
            catch (NumberFormatException ignored) {}
        }
        return total;
    }

    // ── Reservations ──────────────────────────────────────────────────────────

    public static void saveReservation(Map<String, String> fields) throws Exception {
        requireNonBlank(fields.getOrDefault("reservationId", ""), "reservationId");
        upsertFirestoreDocument("reservations", fields.get("reservationId"), fields);
    }

    public static List<Map<String, String>> listReservations() throws Exception {
        return listCollectionDocuments("reservations");
    }

    public static Map<String, String> getReservation(String reservationId) throws Exception {
        return getDocumentFields("reservations", reservationId);
    }

    public static void deleteReservation(String reservationId) throws Exception {
        deleteDocument("reservations", reservationId);
    }

    public static List<Map<String, String>> getPendingReservationsForBook(String bookId) throws Exception {
        return filterDocs("reservations", r ->
            equalsIgnoreCase(r.get("bookId"), bookId) &&
            equalsIgnoreCase(r.get("status"), "Pending"));
    }

    public static List<Map<String, String>> getActiveReservationsForMember(String memberId) throws Exception {
        return filterDocs("reservations", r ->
            equalsIgnoreCase(r.get("memberId"), memberId) &&
            (equalsIgnoreCase(r.get("status"), "Pending")));
    }

    public static int countPendingReservations() throws Exception {
        int count = 0;
        for (Map<String, String> r : listReservations()) {
            if ("Pending".equalsIgnoreCase(r.getOrDefault("status", ""))) count++;
        }
        return count;
    }

    // ── Book copies ───────────────────────────────────────────────────────────

    public static void saveBookCopy(Map<String, String> fields) throws Exception {
        requireNonBlank(fields.getOrDefault("copyId", ""), "copyId");
        upsertFirestoreDocument("bookCopies", fields.get("copyId"), fields);
    }

    public static List<Map<String, String>> listBookCopies() throws Exception {
        return listCollectionDocuments("bookCopies");
    }

    public static List<Map<String, String>> getCopiesForBook(String bookId) throws Exception {
        return filterDocs("bookCopies", c -> equalsIgnoreCase(c.get("bookId"), bookId));
    }

    public static Map<String, String> getBookCopy(String copyId) throws Exception {
        return getDocumentFields("bookCopies", copyId);
    }

    public static void deleteBookCopy(String copyId) throws Exception {
        deleteDocument("bookCopies", copyId);
    }

    public static Map<String, String> getBookCopyByBarcode(String barcode) throws Exception {
        List<Map<String, String>> matches = filterDocs("bookCopies",
            c -> equalsIgnoreCase(c.get("barcode"), barcode));
        return matches.isEmpty() ? null : matches.get(0);
    }

    /** Count available copies for a book. */
    public static int countAvailableCopies(String bookId) throws Exception {
        int count = 0;
        for (Map<String, String> c : getCopiesForBook(bookId)) {
            if ("Available".equalsIgnoreCase(c.getOrDefault("status", ""))) count++;
        }
        return count;
    }

    // ── Issue record with renewal support ─────────────────────────────────────

    /** Save issue record with renewal fields. */
    public static void saveIssueRecordFull(Map<String, String> fields) throws Exception {
        requireNonBlank(fields.getOrDefault("bookId", ""), "Book ID");
        requireNonBlank(fields.getOrDefault("registrationNo", ""), "Registration No");
        String docId = fields.get("bookId") + "_" + fields.get("registrationNo");
        upsertFirestoreDocument("issue_records", docId, fields);
        setBookIssuedStatus(fields.get("bookId"), true);
    }

    /** Renew an issue record: extend dueDate, increment renewalCount. */
    public static void renewIssueRecord(String bookId, String registrationNo,
                                        String newDueDate, String renewedBy) throws Exception {
        Map<String, String> rec = getIssueRecord(bookId, registrationNo);
        if (rec == null) throw new IllegalStateException("Issue record not found.");
        if (!"yes".equalsIgnoreCase(rec.getOrDefault("issued", "")))
            throw new IllegalStateException("This book has already been returned.");

        int count = 0;
        try { count = Integer.parseInt(rec.getOrDefault("renewalCount", "0")); }
        catch (NumberFormatException ignored) {}

        int limit = AppSettings.renewalLimit();
        if (count >= limit)
            throw new IllegalStateException("Renewal limit (" + limit + ") reached.");

        rec.put("dueDate",      newDueDate);
        rec.put("renewalCount", String.valueOf(count + 1));
        rec.put("renewedAt",    Instant.now().toString());
        rec.put("renewedBy",    renewedBy == null ? "" : renewedBy);
        upsertFirestoreDocument("issue_records", bookId + "_" + registrationNo, rec);
    }

    // ── Firestore settings collection ─────────────────────────────────────────

    public static void saveFirestoreSettings(Map<String, String> fields) throws Exception {
        upsertFirestoreDocument("settings", "app", fields);
    }

    public static Map<String, String> getFirestoreSettings() throws Exception {
        return getDocumentFields("settings", "app");
    }

    // ── parseDocumentFields extension (handles new field names) ──────────────

    // The existing parseDocumentFields only handles a fixed list of field names.
    // This method parses ALL string/integer/double/boolean fields from a document.
    public static Map<String, String> parseAllFields(String documentJson) {
        Map<String, String> fields = new HashMap<>();
        // Extract document ID from name field
        String name = extractStringField(documentJson, "name");
        if (name != null) {
            int lastSlash = name.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < name.length() - 1) {
                fields.put("_id", name.substring(lastSlash + 1));
            }
        }
        // Find "fields" object
        int fieldsMarker = documentJson.indexOf("\"fields\"");
        if (fieldsMarker < 0) return fields;
        int fieldsStart = documentJson.indexOf('{', fieldsMarker);
        if (fieldsStart < 0) return fields;
        int fieldsEnd = findMatchingBrace(documentJson, fieldsStart);
        if (fieldsEnd < 0) return fields;
        String fieldsJson = documentJson.substring(fieldsStart + 1, fieldsEnd);

        // Iterate over key-value pairs
        int idx = 0;
        while (idx < fieldsJson.length()) {
            // Find next quoted key
            int keyStart = fieldsJson.indexOf('"', idx);
            if (keyStart < 0) break;
            String key = readJsonString(fieldsJson, keyStart + 1);
            int afterKey = fieldsJson.indexOf('"', keyStart) + 1 + key.length() + 1; // skip closing quote
            // Find the value object
            int objStart = fieldsJson.indexOf('{', afterKey);
            if (objStart < 0) break;
            int objEnd = findMatchingBrace(fieldsJson, objStart);
            if (objEnd < 0) break;
            String valueObj = fieldsJson.substring(objStart, objEnd + 1);
            String value = extractFirestoreValue(valueObj);
            if (value != null) fields.put(key, value);
            idx = objEnd + 1;
        }
        return fields;
    }

    // ── List collection with full field parsing ───────────────────────────────

    public static List<Map<String, String>> listCollectionFull(String collection) throws Exception {
        ServiceAccount sa = loadServiceAccount();
        if (sa == null) throw new IllegalStateException("FIREBASE_SERVICE_ACCOUNT is not configured.");
        String token = fetchAccessToken(sa.clientEmail, sa.privateKeyPem);
        String url = "https://firestore.googleapis.com/v1/projects/" + sa.projectId +
            "/databases/(default)/documents/" + collection + "?pageSize=1000";
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
            .header("Authorization", "Bearer " + token).GET().build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2) return new ArrayList<>();

        List<Map<String, String>> docs = new ArrayList<>();
        String json = resp.body();
        int markerIndex = json.indexOf("\"documents\"");
        if (markerIndex < 0) return docs;
        int arrayStart = json.indexOf('[', markerIndex);
        if (arrayStart < 0) return docs;
        int index = arrayStart + 1;
        while (index < json.length()) {
            while (index < json.length() && Character.isWhitespace(json.charAt(index))) index++;
            if (index >= json.length() || json.charAt(index) == ']') break;
            if (json.charAt(index) != '{') { index++; continue; }
            int end = findMatchingBrace(json, index);
            if (end < 0) break;
            docs.add(parseAllFields(json.substring(index, end + 1)));
            index = end + 1;
        }
        return docs;
    }

    private static final class ServiceAccount {
        private final String projectId;
        private final String clientEmail;
        private final String privateKeyPem;

        private ServiceAccount(String projectId, String clientEmail, String privateKeyPem) {
            this.projectId = projectId;
            this.clientEmail = clientEmail;
            this.privateKeyPem = privateKeyPem;
        }
    }
}
