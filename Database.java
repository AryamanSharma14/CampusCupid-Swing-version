import java.sql.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;

public class Database {
    // Seed lots of demo users if DB is sparse (helps swipe screen have content)
    public static void seedDemoUsers() {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next()) {
                int count = rs.getInt(1);
                if (count >= 20) return; // Enough users already
            }
        } catch (Exception ignore) { return; }

        String[] femaleFirst = {"Alice","Carol","Emma","Grace","Ivy","Mia","Nina","Olivia","Paula","Queenie","Riya","Sara","Tina","Uma","Veda","Wendy","Xena","Yara","Zara","Bella","Chloe","Diana","Eva","Fiona","Hannah","Isla","Jade","Kira","Luna","Mila"};
        String[] maleFirst   = {"Bob","Dave","Frank","Harry","Jack","Liam","Noah","Owen","Paul","Quinn","Ryan","Sam","Tom","Uday","Vikram","Will","Xavier","Yash","Zane","Aarav","Arjun","Kabir","Rohan","Ishan","Aditya","Anish","Dev","Kunal","Neil","Varun"};
        String[] lastNames   = {"Sharma","Patel","Verma","Gupta","Rao","Iyer","Singh","Khan","Das","Ghosh","Agarwal","Kapoor","Bose","Kulkarni","Mehta","Naidu","Saxena","Chawla","Bhatt","Bajaj"};
        String[] interests   = {"music","reading","sports","tech","travel","movies","gaming","art","coffee","coding","fitness","food","dancing","nature","photography","yoga","books","memes","cricket","football"};
        String[] hobbies     = {"painting","football","dancing","gaming","cooking","reading","hiking","gym","photography","yoga"};
        String[] occupations = {"Student","Engineer","Designer","Developer","Artist","Trainer","Chef","Biologist","Athlete","Researcher"};

        int count = 0;
        for (int i = 0; i < 30; i++) {
            String first = femaleFirst[i % femaleFirst.length];
            String last  = lastNames[(i * 3) % lastNames.length];
            String name  = first + " " + last;
            String email = (first + "." + last + (i+1) + "@srmist.edu.in").toLowerCase();
            int age = 18 + (i % 10);
            String bio = "Hi, I'm " + first + "!";
            String ints = interests[i % interests.length] + "," + interests[(i+5) % interests.length];
            String hob  = hobbies[i % hobbies.length];
            String occ  = occupations[i % occupations.length];
            String photo = "https://randomuser.me/api/portraits/women/" + ((i % 80) + 1) + ".jpg";
            registerUser(email, "password123");
            Integer id = getUserIdByEmail(email);
            if (id != null) {
                upsertProfile(id, name, "Female", age, bio, ints, hob, occ, photo);
                count++;
            }
        }
        for (int i = 0; i < 30; i++) {
            String first = maleFirst[i % maleFirst.length];
            String last  = lastNames[(i * 5) % lastNames.length];
            String name  = first + " " + last;
            String email = (first + "." + last + (i+31) + "@srmist.edu.in").toLowerCase();
            int age = 19 + (i % 10);
            String bio = "Hey, I'm " + first + ".";
            String ints = interests[(i+2) % interests.length] + "," + interests[(i+7) % interests.length];
            String hob  = hobbies[(i+3) % hobbies.length];
            String occ  = occupations[(i+4) % occupations.length];
            String photo = "https://randomuser.me/api/portraits/men/" + ((i % 80) + 1) + ".jpg";
            registerUser(email, "password123");
            Integer id = getUserIdByEmail(email);
            if (id != null) {
                upsertProfile(id, name, "Male", age, bio, ints, hob, occ, photo);
                count++;
            }
        }
        System.out.println("Seeded demo users: " + count);
    }
    private static final String DB_URL = "jdbc:sqlite:campuscupid.db";
    private static final String PASS_SALT = "CampusCupidSalt_v1"; // Note: for demo only; use per-user salts + bcrypt/argon2 in production
    private static String REMOTE_BASE_URL = null; // When set, use HTTP instead of local DB

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
            "  name TEXT,\n" +
            "  gender TEXT,\n" +
            "  age INTEGER,\n" +
            "  bio TEXT,\n" +
            "  interests TEXT,\n" +
            "  hobbies TEXT,\n" +
            "  occupation TEXT,\n" +
            "  photo_url TEXT,\n" +
            "  FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE\n" +
            ")");

            // Migration for older DBs missing columns
            try (PreparedStatement ps = conn.prepareStatement("PRAGMA table_info(profiles)")) {
                java.util.Set<String> cols = new java.util.HashSet<>();
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        cols.add(rs.getString("name").toLowerCase());
                    }
                }
                if (!cols.contains("name")) {
                    st.execute("ALTER TABLE profiles ADD COLUMN name TEXT");
                }
                if (!cols.contains("gender")) {
                    st.execute("ALTER TABLE profiles ADD COLUMN gender TEXT");
                }
                if (!cols.contains("age")) {
                    st.execute("ALTER TABLE profiles ADD COLUMN age INTEGER");
                }
                if (!cols.contains("photo_url")) {
                    st.execute("ALTER TABLE profiles ADD COLUMN photo_url TEXT");
                }
            } catch (SQLException ignore) {}

            st.execute("CREATE TABLE IF NOT EXISTS preferences (\n" +
            "  user_id INTEGER PRIMARY KEY,\n" +
            "  gender_pref TEXT DEFAULT 'Any',\n" +
            "  age_pref INTEGER DEFAULT 24,\n" +
            "  min_age INTEGER DEFAULT 18,\n" +
            "  max_age INTEGER DEFAULT 60,\n" +
            "  interests_pref TEXT DEFAULT '',\n" +
                    "  FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE\n" +
                    ")");
            // Migration for preferences age range
            try (PreparedStatement ps = conn.prepareStatement("PRAGMA table_info(preferences)")) {
                java.util.Set<String> cols = new java.util.HashSet<>();
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) cols.add(rs.getString("name").toLowerCase());
                }
                if (!cols.contains("min_age")) st.execute("ALTER TABLE preferences ADD COLUMN min_age INTEGER DEFAULT 18");
                if (!cols.contains("max_age")) st.execute("ALTER TABLE preferences ADD COLUMN max_age INTEGER DEFAULT 60");
            } catch (SQLException ignore) {}

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

    // Remote mode configuration (set once at app start)
    public static void setRemoteBaseUrl(String baseUrl) {
        REMOTE_BASE_URL = (baseUrl == null || baseUrl.isEmpty()) ? null : baseUrl.replaceAll("/$", "");
        System.out.println("Remote mode: " + (REMOTE_BASE_URL == null ? "OFF (local SQLite)" : ("ON -> " + REMOTE_BASE_URL)));
    }

    public static boolean registerUser(String email, String password) {
        if (REMOTE_BASE_URL != null) {
            try {
                String resp = httpPostForm(REMOTE_BASE_URL + "/api/register", mapOf(
                        "email", email,
                        "password", password,
                        "name", email
                ));
                return resp.trim().equals("OK");
            } catch (Exception e) { return false; }
        }
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
        if (REMOTE_BASE_URL != null) {
            try {
                String resp = httpPostForm(REMOTE_BASE_URL + "/api/login", mapOf(
                        "email", email,
                        "password", password
                ));
                if (resp != null && resp.matches("\\d+")) return Integer.parseInt(resp.trim());
            } catch (Exception e) { }
            return null;
        }
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

    public static void upsertProfile(int userId, String name, String gender, Integer age, String bio, String interests, String hobbies, String occupation, String photoUrl) {
        if (REMOTE_BASE_URL != null) {
            try {
                httpPostForm(REMOTE_BASE_URL + "/api/profile", mapOf(
                        "userId", String.valueOf(userId),
                        "name", orEmpty(name),
                        "gender", orEmpty(gender),
                        "age", age == null ? "" : String.valueOf(age),
                        "bio", orEmpty(bio),
                        "interests", orEmpty(interests),
                        "hobbies", orEmpty(hobbies),
                        "occupation", orEmpty(occupation),
                        "photoUrl", orEmpty(photoUrl)
                ));
                return;
            } catch (Exception e) { /* fall through */ }
        }
        String sqlUser = "UPDATE users SET name=? WHERE id=?";
        String sql = "INSERT INTO profiles(user_id, name, gender, age, bio, interests, hobbies, occupation, photo_url) VALUES(?,?,?,?,?,?,?,?,?)\n" +
                "ON CONFLICT(user_id) DO UPDATE SET name=excluded.name, gender=excluded.gender, age=excluded.age, bio=excluded.bio, interests=excluded.interests, hobbies=excluded.hobbies, occupation=excluded.occupation, photo_url=excluded.photo_url";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement upUser = conn.prepareStatement(sqlUser); PreparedStatement ps = conn.prepareStatement(sql)) {
                upUser.setString(1, name);
                upUser.setInt(2, userId);
                upUser.executeUpdate();

                ps.setInt(1, userId);
                ps.setString(2, name);
                ps.setString(3, gender);
                if (age == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, age);
                ps.setString(5, bio);
                ps.setString(6, interests);
                ps.setString(7, hobbies);
                ps.setString(8, occupation);
                ps.setString(9, photoUrl);
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

    public static ResultSet listCandidatesRaw(Connection conn, int userId, String genderPref, Integer agePref, String interestsPref) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT u.id, COALESCE(p.name,u.name) AS name, p.gender, p.age, p.interests, p.bio, p.photo_url " +
                  "FROM users u LEFT JOIN profiles p ON p.user_id=u.id " +
                  "WHERE u.id<>? AND NOT EXISTS (SELECT 1 FROM swipes s WHERE s.user_id=? AND s.target_user_id=u.id) ");
        if (genderPref != null && !"Any".equalsIgnoreCase(genderPref)) {
            sb.append(" AND LOWER(p.gender)=LOWER(?)");
        }
        // Fetch; we'll filter age/interests in code for simplicity
        PreparedStatement ps = conn.prepareStatement(sb.toString());
        int idx = 1;
        ps.setInt(idx++, userId);
        ps.setInt(idx++, userId);
        if (genderPref != null && !"Any".equalsIgnoreCase(genderPref)) {
            ps.setString(idx++, genderPref);
        }
        return ps.executeQuery();
    }

    public static java.util.List<Map<String, Object>> listCandidates(int userId, String genderPref, int minAgePref, int maxAgePref, String interestsPref) {
        if (REMOTE_BASE_URL != null) {
            java.util.List<Map<String,Object>> out = new java.util.ArrayList<>();
            try {
                String qs = String.format("?userId=%d&gender=%s&minAge=%d&maxAge=%d&interests=%s", userId, urlEncode(genderPref==null?"":genderPref), minAgePref, maxAgePref, urlEncode(interestsPref==null?"":interestsPref));
                String resp = httpGet(REMOTE_BASE_URL + "/api/candidates" + qs);
                if (resp != null) {
                    String[] lines = resp.split("\r?\n");
                    for (String line : lines) {
                        if (line.trim().isEmpty()) continue;
                        String[] parts = line.split("\\|", -1);
                        Map<String,Object> m = new java.util.HashMap<>();
                        m.put("id", Integer.parseInt(parts[0]));
                        m.put("name", parts[1]);
                        m.put("gender", parts[2]);
                        m.put("age", parts[3].isEmpty()? null : Integer.parseInt(parts[3]));
                        m.put("interests", parts[4]);
                        m.put("bio", parts[5]);
                        m.put("photoUrl", parts.length>6? parts[6] : "");
                        out.add(m);
                    }
                }
            } catch (Exception e) { }
            return out;
        }
        java.util.List<Map<String, Object>> out = new java.util.ArrayList<>();
    try (Connection conn = getConnection(); ResultSet rs = listCandidatesRaw(conn, userId, genderPref, null, interestsPref == null ? "" : interestsPref.toLowerCase())) {
            while (rs.next()) {
                Integer age = (Integer) rs.getObject("age");
                String interests = rs.getString("interests");
        boolean ageMatch = (age == null) || (age >= minAgePref && age <= maxAgePref);
                boolean interestsMatch = (interestsPref == null || interestsPref.isEmpty()) || (interests != null && interests.toLowerCase().contains(interestsPref));
                if (ageMatch && interestsMatch) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", rs.getInt("id"));
                    m.put("name", rs.getString("name"));
                    m.put("gender", rs.getString("gender"));
                    m.put("age", age);
                    m.put("interests", interests);
                    m.put("bio", rs.getString("bio"));
                    m.put("photoUrl", rs.getString("photo_url"));
                    out.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    public static boolean recordSwipe(int userId, int targetUserId, boolean liked) {
        if (REMOTE_BASE_URL != null) {
            try {
                String resp = httpPostForm(REMOTE_BASE_URL + "/api/swipe", mapOf(
                        "userId", String.valueOf(userId),
                        "targetUserId", String.valueOf(targetUserId),
                        "liked", liked ? "1" : "0"
                ));
                return "MATCH".equals(resp.trim());
            } catch (Exception e) { return false; }
        }
        String insert = "INSERT INTO swipes(user_id, target_user_id, liked, created_at) VALUES(?,?,?,?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setInt(1, userId);
                ps.setInt(2, targetUserId);
                ps.setInt(3, liked ? 1 : 0);
                ps.setLong(4, Instant.now().getEpochSecond());
                ps.executeUpdate();
            }
            boolean matched = false;
            if (liked) {
                // Check reciprocal like
                String check = "SELECT 1 FROM swipes WHERE user_id=? AND target_user_id=? AND liked=1";
                try (PreparedStatement ps2 = conn.prepareStatement(check)) {
                    ps2.setInt(1, targetUserId);
                    ps2.setInt(2, userId);
                    try (ResultSet rs = ps2.executeQuery()) {
                        if (rs.next()) {
                            int a = Math.min(userId, targetUserId);
                            int b = Math.max(userId, targetUserId);
                            String mk = "INSERT OR IGNORE INTO matches(user1_id, user2_id, created_at) VALUES(?,?,?)";
                            try (PreparedStatement mkps = conn.prepareStatement(mk)) {
                                mkps.setInt(1, a);
                                mkps.setInt(2, b);
                                mkps.setLong(3, Instant.now().getEpochSecond());
                                mkps.executeUpdate();
                                matched = true;
                            }
                        }
                    }
                }
            }
            conn.commit();
            return matched;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static java.util.List<Map<String, Object>> getMatches(int userId) {
        if (REMOTE_BASE_URL != null) {
            java.util.List<Map<String,Object>> out = new java.util.ArrayList<>();
            try {
                String resp = httpGet(REMOTE_BASE_URL + "/api/matches?userId=" + userId);
                if (resp != null) {
                    String[] lines = resp.split("\r?\n");
                    for (String line : lines) {
                        if (line.trim().isEmpty()) continue;
                        String[] parts = line.split("\\|", -1);
                        Map<String,Object> m = new java.util.HashMap<>();
                        m.put("id", Integer.parseInt(parts[0]));
                        m.put("name", parts[1]);
                        out.add(m);
                    }
                }
            } catch (Exception e) { }
            return out;
        }
        java.util.List<Map<String, Object>> out = new java.util.ArrayList<>();
        String sql = "SELECT CASE WHEN m.user1_id=? THEN m.user2_id ELSE m.user1_id END AS other_id, COALESCE(p.name,u.name) AS name " +
                     "FROM matches m JOIN users u ON u.id=CASE WHEN m.user1_id=? THEN m.user2_id ELSE m.user1_id END " +
                     "LEFT JOIN profiles p ON p.user_id=u.id WHERE m.user1_id=? OR m.user2_id=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            ps.setInt(4, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", rs.getInt("other_id"));
                    m.put("name", rs.getString("name"));
                    out.add(m);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }

    public static java.util.List<Map<String, Object>> getMessagesBetween(int a, int b) {
        if (REMOTE_BASE_URL != null) {
            java.util.List<Map<String,Object>> out = new java.util.ArrayList<>();
            try {
                String resp = httpGet(REMOTE_BASE_URL + "/api/messages?userId=" + a + "&with=" + b);
                if (resp != null) {
                    String[] lines = resp.split("\r?\n");
                    for (String line : lines) {
                        if (line.trim().isEmpty()) continue;
                        String[] parts = line.split("\\|", -1);
                        Map<String,Object> m = new java.util.HashMap<>();
                        m.put("from", Integer.parseInt(parts[0]));
                        m.put("to", Integer.parseInt(parts[1]));
                        m.put("body", parts[2]);
                        m.put("ts", Long.parseLong(parts[3]));
                        out.add(m);
                    }
                }
            } catch (Exception e) { }
            return out;
        }
        java.util.List<Map<String, Object>> out = new java.util.ArrayList<>();
        String sql = "SELECT from_user_id, to_user_id, body, created_at FROM messages WHERE (from_user_id=? AND to_user_id=?) OR (from_user_id=? AND to_user_id=?) ORDER BY created_at ASC";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a);
            ps.setInt(2, b);
            ps.setInt(3, b);
            ps.setInt(4, a);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("from", rs.getInt("from_user_id"));
                    m.put("to", rs.getInt("to_user_id"));
                    m.put("body", rs.getString("body"));
                    m.put("ts", rs.getLong("created_at"));
                    out.add(m);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }

    // Fast helper to fetch only the most recent message between two users
    public static Map<String, Object> getLastMessageBetween(int a, int b) {
        if (REMOTE_BASE_URL != null) {
            // Best-effort fallback for remote: reuse full list and pick last
            java.util.List<Map<String, Object>> all = getMessagesBetween(a, b);
            if (all.isEmpty()) return null;
            return all.get(all.size() - 1);
        }
        String sql = "SELECT from_user_id, to_user_id, body, created_at FROM messages WHERE (from_user_id=? AND to_user_id=?) OR (from_user_id=? AND to_user_id=?) ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a);
            ps.setInt(2, b);
            ps.setInt(3, b);
            ps.setInt(4, a);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("from", rs.getInt("from_user_id"));
                    m.put("to", rs.getInt("to_user_id"));
                    m.put("body", rs.getString("body"));
                    m.put("ts", rs.getLong("created_at"));
                    return m;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static void sendMessage(int from, int to, String body) {
        if (REMOTE_BASE_URL != null) {
            try {
                httpPostForm(REMOTE_BASE_URL + "/api/messages", mapOf(
                        "from", String.valueOf(from),
                        "to", String.valueOf(to),
                        "body", body
                ));
                return;
            } catch (Exception e) { /* ignore */ }
        }
        String sql = "INSERT INTO messages(from_user_id, to_user_id, body, created_at) VALUES(?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, from);
            ps.setInt(2, to);
            ps.setString(3, body);
            ps.setLong(4, Instant.now().getEpochSecond());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void upsertPreferences(int userId, String gender, int minAge, int maxAge, String interests) {
        if (REMOTE_BASE_URL != null) {
            try {
                httpPostForm(REMOTE_BASE_URL + "/api/preferences", mapOf(
                        "userId", String.valueOf(userId),
                        "gender", orEmpty(gender),
                        "minAge", String.valueOf(minAge),
                        "maxAge", String.valueOf(maxAge),
                        "interests", orEmpty(interests)
                ));
                return;
            } catch (Exception e) { /* fall through */ }
        }
        String sql = "INSERT INTO preferences(user_id, gender_pref, age_pref, min_age, max_age, interests_pref) VALUES(?,?,?,?,?,?)\n" +
                "ON CONFLICT(user_id) DO UPDATE SET gender_pref=excluded.gender_pref, age_pref=excluded.age_pref, min_age=excluded.min_age, max_age=excluded.max_age, interests_pref=excluded.interests_pref";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, gender);
            // keep age_pref for backward compatibility as midpoint
            int midpoint = (minAge + maxAge) / 2;
            ps.setInt(3, midpoint);
            ps.setInt(4, minAge);
            ps.setInt(5, maxAge);
            ps.setString(6, interests);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // (duplicate removed)

    public static Map<String, Object> getPreferences(int userId) {
        Map<String, Object> map = new HashMap<>();
        String sql = "SELECT gender_pref, age_pref, min_age, max_age, interests_pref FROM preferences WHERE user_id=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    map.put("gender", rs.getString("gender_pref"));
                    map.put("age", rs.getInt("age_pref"));
                    map.put("minAge", rs.getObject("min_age") == null ? 18 : rs.getInt("min_age"));
                    map.put("maxAge", rs.getObject("max_age") == null ? 60 : rs.getInt("max_age"));
                    map.put("interests", rs.getString("interests_pref"));
                }
            }
        } catch (SQLException e) { }
        return map;
    }

    public static boolean hasProfile(int userId) {
        String sql = "SELECT 1 FROM profiles WHERE user_id=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) { return false; }
    }

    public static Map<String,Object> getProfile(int userId) {
        Map<String,Object> m = new HashMap<>();
        String sql = "SELECT name, gender, age, bio, interests, hobbies, occupation, photo_url FROM profiles WHERE user_id=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m.put("name", rs.getString("name"));
                    m.put("gender", rs.getString("gender"));
                    m.put("age", rs.getObject("age") == null ? null : rs.getInt("age"));
                    m.put("bio", rs.getString("bio"));
                    m.put("interests", rs.getString("interests"));
                    m.put("hobbies", rs.getString("hobbies"));
                    m.put("occupation", rs.getString("occupation"));
                    m.put("photoUrl", rs.getString("photo_url"));
                }
            }
        } catch (SQLException e) { }
        return m;
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

    // ---- HTTP helpers for remote mode (plain text protocol) ----
    private static String httpGet(String urlStr) throws Exception {
        java.net.URL url = java.net.URI.create(urlStr).toURL();
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(4000);
        conn.setReadTimeout(8000);
        try (java.io.InputStream in = conn.getInputStream()) {
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    private static String httpPostForm(String urlStr, java.util.Map<String,String> form) throws Exception {
        java.net.URL url = java.net.URI.create(urlStr).toURL();
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        String body = buildForm(form);
        byte[] bytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        conn.setFixedLengthStreamingMode(bytes.length);
        try (java.io.OutputStream os = conn.getOutputStream()) {
            os.write(bytes);
        }
        try (java.io.InputStream in = conn.getInputStream()) {
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    private static String buildForm(java.util.Map<String,String> form) throws Exception {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (var e : form.entrySet()) {
            if (!first) sb.append('&');
            first = false;
            sb.append(java.net.URLEncoder.encode(e.getKey(), "UTF-8"));
            sb.append('=');
            sb.append(java.net.URLEncoder.encode(e.getValue()==null?"":e.getValue(), "UTF-8"));
        }
        return sb.toString();
    }

    private static String urlEncode(String s) throws Exception {
        return java.net.URLEncoder.encode(s, "UTF-8");
    }

    private static Map<String,String> mapOf(String... kv) {
        Map<String,String> m = new java.util.HashMap<>();
        for (int i=0;i+1<kv.length;i+=2) m.put(kv[i], kv[i+1]);
        return m;
    }

    private static String orEmpty(String s) { return s==null?"":s; }
}
