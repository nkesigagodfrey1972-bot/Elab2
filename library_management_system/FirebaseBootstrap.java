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

public final class FirebaseBootstrap {

    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final String SCOPE = "https://www.googleapis.com/auth/datastore https://www.googleapis.com/auth/cloud-platform";

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
                {"passwordHash", hashPassword("admin123")},
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
        ServiceAccount serviceAccount = loadServiceAccount();
        if (serviceAccount == null) {
            throw new IllegalStateException("FIREBASE_SERVICE_ACCOUNT is not configured.");
        }

        String accessToken = fetchAccessToken(serviceAccount.clientEmail, serviceAccount.privateKeyPem);
        String timestamp = Instant.now().toString();
        String fieldsJson = documentFields(new Object[][]{
            {"employeeName", username},
            {"passwordHash", hashPassword(password)},
            {"seededAt", timestamp}
        });

        upsertDocument(serviceAccount.projectId, accessToken, "employees", username, fieldsJson);
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
    }

    public static void deleteIssueRecord(String bookId, String registrationNo) throws Exception {
        requireNonBlank(bookId, "Book ID");
        requireNonBlank(registrationNo, "Registration No");
        deleteDocument("issue_records", issueDocumentId(bookId, registrationNo));
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

        if (credentialsPath == null || credentialsPath.isBlank()) {
            return null;
        }

        String serviceAccountJson = Files.readString(Path.of(credentialsPath));
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

        for (String field : new String[]{"bookId", "bookName", "author", "category", "price", "issued", "registrationNo", "studentName", "mobileNo", "branch", "issueDate", "returnDate", "seededAt", "employeeName", "passwordHash"}) {
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