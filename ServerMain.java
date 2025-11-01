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
    Database.seedDemoUsers();

        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
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
            Integer age = parseInt(f.get("age"));
            if (uid != null && age != null) {
                Database.upsertPreferences(uid, f.get("gender"), age, f.get("interests"));
                write200(ex, "OK");
            } else write400(ex, "Missing fields");
        }
    }
    static class CandidatesHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            Map<String,String> q = readQuery(ex);
            Integer uid = parseInt(q.get("userId"));
            String gender = q.getOrDefault("gender", "Any");
            Integer age = parseInt(q.getOrDefault("age", "24"));
            String interests = q.getOrDefault("interests", "");
            if (uid == null || age == null) { write400(ex, "Missing userId/age"); return; }
            List<Map<String,Object>> rows = Database.listCandidates(uid, gender, age, interests);
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
}
