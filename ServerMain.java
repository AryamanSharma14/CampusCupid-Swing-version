import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        Database.setRemoteBaseUrl(null); // ensure server uses local DB
    Database.init();
    // Optional: seed demo users only if explicitly requested via -Dseed=true or SEED=1
    if (parseSeedPref()) {
        Database.seedDemoUsers();
    }

        // Determine a port: prefer -Dport / -Dcampuscupid.port, then PORT/CAMPUSCUPID_PORT env,
        // else try 8082 first (your preferred default), then 8080..8090 as fallback
        int desired = parsePortPref();
        HttpServer server = null;
        int port = desired;
        if (port > 0) {
            try {
                server = HttpServer.create(new InetSocketAddress(port), 0);
            } catch (java.net.BindException be) {
                System.out.println("Port " + port + " busy, trying fallback range (8082, then 8080-8090)...");
                server = null; // will fall through to range
            }
        }
        if (server == null) {
            int[] candidates = buildPortCandidates();
            for (int p : candidates) {
                try {
                    server = HttpServer.create(new InetSocketAddress(p), 0);
                    port = p;
                    break;
                } catch (java.net.BindException be) {
                    // try next
                }
            }
        }
        if (server == null) throw new RuntimeException("No free port found in 8080-8090; set -Dport to an open port.");
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/login", new LoginHandler());
    server.createContext("/api/profile", new ProfileHandler());
        server.createContext("/api/preferences", new PreferencesHandler());
        server.createContext("/api/candidates", new CandidatesHandler());
        server.createContext("/api/swipe", new SwipeHandler());
        server.createContext("/api/matches", new MatchesHandler());
        server.createContext("/api/messages", new MessagesHandler());
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        System.out.println("Server running on http://localhost:" + port);
        server.start();
    }

    static class RegisterHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            Map<String,String> f = readForm(ex);
            boolean ok = Database.registerUser(f.get("email"), f.get("password"));
            write200(ex, ok ? "OK" : "ERR");
        }
    }
    static class LoginHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            Map<String,String> f = readForm(ex);
            Integer id = Database.loginUser(f.get("email"), f.get("password"));
            write200(ex, id == null ? "ERR" : String.valueOf(id));
        }
    }
    static class ProfileHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            Map<String,String> f = readForm(ex);
            Integer uid = parseInt(f.get("userId"));
            Integer age = parseInt(f.get("age"));
            // Accept either explicit userId (legacy) or rely on provided userId
            if (uid != null) {
        Database.upsertProfile(uid, f.get("name"), f.get("gender"), age,
            f.get("bio"), f.get("interests"), f.get("hobbies"), f.get("occupation"), f.getOrDefault("photoUrl", ""));
                write200(ex, "OK");
            } else write400(ex, "Missing userId");
        }
    }
    static class PreferencesHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            Map<String,String> f = readForm(ex);
            Integer uid = parseInt(f.get("userId"));
            Integer minAge = parseInt(f.get("minAge"));
            Integer maxAge = parseInt(f.get("maxAge"));
            if (uid != null && minAge != null && maxAge != null) {
                Database.upsertPreferences(uid, f.get("gender"), minAge, maxAge, f.get("interests"));
                write200(ex, "OK");
            } else write400(ex, "Missing fields");
        }
    }
    static class CandidatesHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            Map<String,String> q = readQuery(ex);
            Integer uid = parseInt(q.get("userId"));
            String gender = q.getOrDefault("gender", "Any");
            Integer minAge = parseInt(q.getOrDefault("minAge", "18"));
            Integer maxAge = parseInt(q.getOrDefault("maxAge", "60"));
            String interests = q.getOrDefault("interests", "");
            if (uid == null) { write400(ex, "Missing userId"); return; }
            if (minAge == null) minAge = 18; if (maxAge == null) maxAge = 60;
            List<Map<String,Object>> rows = Database.listCandidates(uid, gender, minAge, maxAge, interests);
            StringBuilder sb = new StringBuilder();
            for (Map<String,Object> r : rows) {
                sb.append(r.get("id")).append('|')
                  .append(nullToEmpty((String)r.get("name"))).append('|')
                  .append(nullToEmpty((String)r.get("gender"))).append('|')
                  .append(r.get("age")==null?"":String.valueOf(r.get("age"))).append('|')
                  .append(nullToEmpty((String)r.get("interests"))).append('|')
                  .append(nullToEmpty((String)r.get("bio"))).append('|')
                  .append(nullToEmpty((String)r.get("photoUrl"))).append('\n');
            }
            write200(ex, sb.toString());
        }
    }
    static class SwipeHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            Map<String,String> f = readForm(ex);
            Integer uid = parseInt(f.get("userId"));
            Integer tid = parseInt(f.get("targetUserId"));
            boolean liked = "1".equals(f.get("liked"));
            if (uid == null || tid == null) { write400(ex, "Missing ids"); return; }
            boolean match = Database.recordSwipe(uid, tid, liked);
            write200(ex, match?"MATCH":"OK");
        }
    }
    static class MatchesHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            Map<String,String> q = readQuery(ex);
            Integer uid = parseInt(q.get("userId"));
            if (uid == null) { write400(ex, "Missing userId"); return; }
            List<Map<String,Object>> rows = Database.getMatches(uid);
            StringBuilder sb = new StringBuilder();
            for (Map<String,Object> r : rows) {
                sb.append(r.get("id")).append('|')
                  .append(nullToEmpty((String)r.get("name"))).append('\n');
            }
            write200(ex, sb.toString());
        }
    }
    static class MessagesHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String,String> q = readQuery(ex);
                Integer uid = parseInt(q.get("userId"));
                Integer other = parseInt(q.get("with"));
                if (uid==null||other==null) { write400(ex, "Missing params"); return; }
                List<Map<String,Object>> msgs = Database.getMessagesBetween(uid, other);
                StringBuilder sb = new StringBuilder();
                for (Map<String,Object> m : msgs) {
                    sb.append(m.get("from")).append('|')
                      .append(m.get("to")).append('|')
                      .append(nullToEmpty((String)m.get("body"))).append('|')
                      .append(m.get("ts")).append('\n');
                }
                write200(ex, sb.toString());
                return;
            }
            if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String,String> f = readForm(ex);
                Integer from = parseInt(f.get("from"));
                Integer to = parseInt(f.get("to"));
                String body = f.get("body");
                if (from==null||to==null||body==null) { write400(ex, "Missing fields"); return; }
                Database.sendMessage(from, to, body);
                write200(ex, "OK");
                return;
            }
            write400(ex, "Unsupported method");
        }
    }

    // Helpers
    static Map<String,String> readForm(HttpExchange ex) throws IOException {
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return parseQueryString(body);
    }
    static Map<String,String> readQuery(HttpExchange ex) {
        String raw = ex.getRequestURI().getRawQuery();
        return parseQueryString(raw==null?"":raw);
    }
    static Map<String,String> parseQueryString(String s) {
        Map<String,String> m = new HashMap<>();
        if (s==null || s.isEmpty()) return m;
        for (String part : s.split("&")) {
            int i = part.indexOf('=');
            if (i<0) continue;
            String k = urlDecode(part.substring(0,i));
            String v = urlDecode(part.substring(i+1));
            m.put(k, v);
        }
        return m;
    }
    static String urlDecode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
    static Integer parseInt(String s) {
        try { return s==null||s.isEmpty()? null : Integer.parseInt(s); } catch(Exception e){ return null; }
    }
    static String nullToEmpty(String s) { return s==null?"":s; }

    static void write200(HttpExchange ex, String body) throws IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        ex.sendResponseHeaders(200, b.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(b);
        }
    }
    static void write400(HttpExchange ex, String body) throws IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        ex.sendResponseHeaders(400, b.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(b);
        }
    }

    // Port selection helpers
    static int parsePortPref() {
        Integer p = parseInt(System.getProperty("port"));
        if (p != null && p > 0 && p < 65536) return p;
        p = parseInt(System.getProperty("campuscupid.port"));
        if (p != null && p > 0 && p < 65536) return p;
        p = parseInt(System.getenv("PORT"));
        if (p != null && p > 0 && p < 65536) return p;
        p = parseInt(System.getenv("CAMPUSCUPID_PORT"));
        if (p != null && p > 0 && p < 65536) return p;
        return -1;
    }

    static int[] buildPortCandidates() {
        // Prefer 8082 first, then 8080..8090 excluding 8082
        java.util.List<Integer> list = new java.util.ArrayList<>();
        list.add(8082);
        for (int p = 8080; p <= 8090; p++) {
            if (p == 8082) continue;
            list.add(p);
        }
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    static boolean parseSeedPref() {
        String prop = System.getProperty("seed");
        if (prop != null) {
            return prop.equalsIgnoreCase("true") || prop.equals("1") || prop.equalsIgnoreCase("yes");
        }
        String env = System.getenv("SEED");
        return env != null && (env.equalsIgnoreCase("true") || env.equals("1") || env.equalsIgnoreCase("yes"));
    }
}
