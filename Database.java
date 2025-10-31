import java.sql.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:campuscupid.db";
    private static final String PASS_SALT = "CampusCupidSalt_v1"; // Note: for demo only; use per-user salts + bcrypt/argon2 in production

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void init() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found on classpath");
        }
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            st.execute("CREATE TABLE IF NOT EXISTS users (\n" +
                    "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "  email TEXT UNIQUE NOT NULL,\n" +
                    "  password_hash TEXT NOT NULL,\n" +
                    "  name TEXT,\n" +
                    "  created_at INTEGER NOT NULL\n" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS profiles (\n" +
                    "  user_id INTEGER PRIMARY KEY,\n" +
                    "  bio TEXT,\n" +
                    "  interests TEXT,\n" +
                    "  hobbies TEXT,\n" +
                    "  occupation TEXT,\n" +
                    "  FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE\n" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS preferences (\n" +
                    "  user_id INTEGER PRIMARY KEY,\n" +
                    "  gender_pref TEXT DEFAULT 'Any',\n" +
                    "  age_pref INTEGER DEFAULT 24,\n" +
                    "  interests_pref TEXT DEFAULT '',\n" +
                    "  FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE\n" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS swipes (\n" +
                    "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "  user_id INTEGER NOT NULL,\n" +
                    "  target_user_id INTEGER NOT NULL,\n" +
                    "  liked INTEGER NOT NULL,\n" +
                    "  created_at INTEGER NOT NULL,\n" +
                    "  FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,\n" +
                    "  FOREIGN KEY(target_user_id) REFERENCES users(id) ON DELETE CASCADE\n" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS matches (\n" +
                    "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "  user1_id INTEGER NOT NULL,\n" +
                    "  user2_id INTEGER NOT NULL,\n" +
                    "  created_at INTEGER NOT NULL,\n" +
                    "  UNIQUE(user1_id, user2_id),\n" +
                    "  FOREIGN KEY(user1_id) REFERENCES users(id) ON DELETE CASCADE,\n" +
                    "  FOREIGN KEY(user2_id) REFERENCES users(id) ON DELETE CASCADE\n" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS messages (\n" +
                    "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "  from_user_id INTEGER NOT NULL,\n" +
                    "  to_user_id INTEGER NOT NULL,\n" +
                    "  body TEXT NOT NULL,\n" +
                    "  created_at INTEGER NOT NULL,\n" +
                    "  FOREIGN KEY(from_user_id) REFERENCES users(id) ON DELETE CASCADE,\n" +
                    "  FOREIGN KEY(to_user_id) REFERENCES users(id) ON DELETE CASCADE\n" +
                    ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean registerUser(String email, String password) {
        String sql = "INSERT INTO users(email, password_hash, created_at) VALUES(?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, hashPassword(password));
            ps.setLong(3, Instant.now().getEpochSecond());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false; // likely duplicate email
        }
    }

    public static Integer loginUser(String email, String password) {
        String sql = "SELECT id, password_hash FROM users WHERE email=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String expected = rs.getString("password_hash");
                    if (expected != null && expected.equals(hashPassword(password))) {
                        return rs.getInt("id");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Integer getUserIdByEmail(String email) {
        String sql = "SELECT id FROM users WHERE email=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (SQLException e) { }
        return null;
    }

    public static void upsertProfile(int userId, String name, String bio, String interests, String hobbies, String occupation) {
        String sqlUser = "UPDATE users SET name=? WHERE id=?";
        String sql = "INSERT INTO profiles(user_id, bio, interests, hobbies, occupation) VALUES(?,?,?,?,?)\n" +
                "ON CONFLICT(user_id) DO UPDATE SET bio=excluded.bio, interests=excluded.interests, hobbies=excluded.hobbies, occupation=excluded.occupation";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement upUser = conn.prepareStatement(sqlUser); PreparedStatement ps = conn.prepareStatement(sql)) {
                upUser.setString(1, name);
                upUser.setInt(2, userId);
                upUser.executeUpdate();

                ps.setInt(1, userId);
                ps.setString(2, bio);
                ps.setString(3, interests);
                ps.setString(4, hobbies);
                ps.setString(5, occupation);
                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void upsertPreferences(int userId, String gender, int age, String interests) {
        String sql = "INSERT INTO preferences(user_id, gender_pref, age_pref, interests_pref) VALUES(?,?,?,?)\n" +
                "ON CONFLICT(user_id) DO UPDATE SET gender_pref=excluded.gender_pref, age_pref=excluded.age_pref, interests_pref=excluded.interests_pref";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, gender);
            ps.setInt(3, age);
            ps.setString(4, interests);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Object> getPreferences(int userId) {
        Map<String, Object> map = new HashMap<>();
        String sql = "SELECT gender_pref, age_pref, interests_pref FROM preferences WHERE user_id=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    map.put("gender", rs.getString("gender_pref"));
                    map.put("age", rs.getInt("age_pref"));
                    map.put("interests", rs.getString("interests_pref"));
                }
            }
        } catch (SQLException e) { }
        return map;
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest((PASS_SALT + password).getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
