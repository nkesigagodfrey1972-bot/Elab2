package library_management_system;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Central settings helper. Reads from ~/.elab-library.properties with safe defaults.
 * All panels should use this instead of reading the file directly.
 */
public final class AppSettings {

    public static final Path SETTINGS_FILE =
        Path.of(System.getProperty("user.home"), ".elab-library.properties");

    private AppSettings() {}

    // ── Read helpers ──────────────────────────────────────────────────────────

    public static Properties load() {
        Properties props = new Properties();
        if (Files.exists(SETTINGS_FILE)) {
            try (var in = Files.newInputStream(SETTINGS_FILE)) {
                props.load(in);
            } catch (IOException ignored) {}
        }
        return props;
    }

    public static void save(Properties props) {
        try (var out = Files.newOutputStream(SETTINGS_FILE)) {
            props.store(out, "Elab Library System settings");
        } catch (IOException ignored) {}
    }

    public static String get(String key, String defaultValue) {
        return load().getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        try { return Integer.parseInt(get(key, String.valueOf(defaultValue))); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    public static double getDouble(String key, double defaultValue) {
        try { return Double.parseDouble(get(key, String.valueOf(defaultValue))); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    public static boolean getBool(String key, boolean defaultValue) {
        String v = get(key, null);
        if (v == null) return defaultValue;
        return "true".equalsIgnoreCase(v) || "yes".equalsIgnoreCase(v) || "1".equals(v);
    }

    // ── Typed accessors ───────────────────────────────────────────────────────

    public static String libraryName()          { return get("library.name",              "Elab Library"); }
    public static double finePerDay()           { return getDouble("fine.rate.per.day",   0.50); }
    public static int    borrowingDays()        { return getInt("borrowing.period.days",  14); }
    public static int    renewalLimit()         { return getInt("renewal.limit",          2); }
    public static int    renewalDays()          { return getInt("renewal.days",           7); }
    public static int    reservationExpiryDays(){ return getInt("reservation.expiry.days",3); }
    public static int    maxBooksPerMember()    { return getInt("max.books.per.member",   5); }
    public static boolean blockOnPendingFines() { return getBool("block.on.pending.fines",false); }
    public static String exportFolder()         { return get("export.folder", System.getProperty("user.home")); }
}
