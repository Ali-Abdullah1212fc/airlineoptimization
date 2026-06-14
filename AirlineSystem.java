import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.io.*;
import javax.swing.Timer;

public class AirlineSystem {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            UserManager userManager = new UserManager();
            userManager.loadUsers();
            new LoginFrame(userManager).setVisible(true);
        });
    }
}


class LoginFrame extends JFrame {
    private final UserManager userManager;
    private JTabbedPane tabs;
    private JTextField loginUser;
    private JPasswordField loginPass;
    private JTextField regUser, regEmail;
    private JPasswordField regPass, regPass2;
    private JComboBox<String> regRole;

    private static final Color BG      = new Color(15, 23, 42);
    private static final Color CARD    = new Color(30, 41, 59);
    private static final Color ACCENT  = new Color(59, 130, 246);
    private static final Color TEXT    = new Color(148, 163, 184);
    private static final Color MUTED   = new Color(100, 116, 139);
    private static final Color SUCCESS = new Color(34, 197, 94);

    LoginFrame(UserManager um) {
        super("AeroRoute — Login");
        this.userManager = um;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(440, 580);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(BG);
        root.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel icon = new JLabel("\u2708", SwingConstants.CENTER);
        icon.setFont(new Font("SansSerif", Font.PLAIN, 42));
        icon.setForeground(ACCENT);
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("AeroRoute", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(TEXT);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Airline Route Optimization System", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        tabs = new JTabbedPane();
        tabs.setBackground(CARD);
        tabs.setForeground(TEXT);
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabs.addTab("  Login  ", buildLoginTab());
        tabs.addTab("  Register  ", buildRegisterTab());

        root.add(Box.createVerticalStrut(10));
        root.add(icon);
        root.add(Box.createVerticalStrut(6));
        root.add(title);
        root.add(Box.createVerticalStrut(4));
        root.add(subtitle);
        root.add(Box.createVerticalStrut(24));
        root.add(tabs);

        setContentPane(root);
        getContentPane().setBackground(BG);
    }

    private JPanel buildLoginTab() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        loginUser = createField();
        loginPass = createPassField();

        JButton btn = createButton("Log In", ACCENT);
        btn.addActionListener(e -> doLogin());
        loginPass.addActionListener(e -> doLogin());

        p.add(label("Username")); p.add(Box.createVerticalStrut(4)); p.add(loginUser);
        p.add(Box.createVerticalStrut(12));
        p.add(label("Password")); p.add(Box.createVerticalStrut(4)); p.add(loginPass);
        p.add(Box.createVerticalStrut(20));
        p.add(btn);
        p.add(Box.createVerticalStrut(14));

        JLabel hint = new JLabel("Default admin  →  admin / admin123", SwingConstants.CENTER);
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(MUTED);
        hint.setAlignmentX(CENTER_ALIGNMENT);
        p.add(hint);
        return p;
    }

    private JPanel buildRegisterTab() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        regUser  = createField();
        regEmail = createField();
        regPass  = createPassField();
        regPass2 = createPassField();
        regRole  = new JComboBox<>(new String[]{"passenger", "admin"});
        styleCombo(regRole);

        JButton btn = createButton("Create Account", SUCCESS);
        btn.addActionListener(e -> doRegister());

        p.add(label("Username"));        p.add(Box.createVerticalStrut(4)); p.add(regUser);
        p.add(Box.createVerticalStrut(10));
        p.add(label("Email"));           p.add(Box.createVerticalStrut(4)); p.add(regEmail);
        p.add(Box.createVerticalStrut(10));
        p.add(label("Password"));        p.add(Box.createVerticalStrut(4)); p.add(regPass);
        p.add(Box.createVerticalStrut(10));
        p.add(label("Confirm Password")); p.add(Box.createVerticalStrut(4)); p.add(regPass2);
        p.add(Box.createVerticalStrut(10));
        p.add(label("Role"));            p.add(Box.createVerticalStrut(4)); p.add(regRole);
        p.add(Box.createVerticalStrut(18));
        p.add(btn);
        return p;
    }

    private void doLogin() {
        String u = loginUser.getText().trim();
        String p = new String(loginPass.getPassword());
        if (u.isEmpty() || p.isEmpty()) { showError("Please fill in all fields."); return; }
        UserManager.User user = userManager.authenticate(u, p);
        if (user == null) { showError("Invalid username or password."); return; }
        dispose();
        AirportGraph graph = new AirportGraph();
        graph.loadFromFile();
        new MainFrame(user, userManager, graph).setVisible(true);
    }

    private void doRegister() {
        String u  = regUser.getText().trim();
        String em = regEmail.getText().trim();
        String p  = new String(regPass.getPassword());
        String p2 = new String(regPass2.getPassword());
        String r  = (String) regRole.getSelectedItem();
        if (u.isEmpty() || em.isEmpty() || p.isEmpty()) { showError("Please fill in all fields."); return; }
        if (!p.equals(p2)) { showError("Passwords do not match."); return; }
        if (p.length() < 6) { showError("Password must be at least 6 characters."); return; }
        if (userManager.userExists(u)) { showError("Username already taken."); return; }
        userManager.addUser(new UserManager.User(u, em, p, r));
        userManager.saveUsers();
        JOptionPane.showMessageDialog(this, "Account created! You can now log in.", "Success",
                JOptionPane.INFORMATION_MESSAGE);
        tabs.setSelectedIndex(0);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JTextField createField() {
        JTextField f = new JTextField();
        styleTextComp(f);
        return f;
    }

    private JPasswordField createPassField() {
        JPasswordField f = new JPasswordField();
        styleTextComp(f);
        return f;
    }

    private void styleTextComp(JTextComponent f) {
        f.setBackground(new Color(15, 23, 42));
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setAlignmentX(LEFT_ALIGNMENT);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(MUTED);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JButton createButton(String text, Color color) {
        JButton b = new JButton(text);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setAlignmentX(LEFT_ALIGNMENT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void styleCombo(JComboBox<String> c) {
        c.setBackground(new Color(15, 23, 42));
        c.setForeground(TEXT);
        c.setFont(new Font("SansSerif", Font.PLAIN, 13));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        c.setAlignmentX(LEFT_ALIGNMENT);
    }
}

class UserManager {
    private static final String FILE = "users.dat";
    private List<User> users = new ArrayList<>();

    static class User implements Serializable {
        String username, email, password, role;
        User(String u, String e, String p, String r) {
            username = u; email = e; password = p; role = r;
        }
    }

    void addUser(User u) { users.add(u); }

    boolean userExists(String username) {
        return users.stream().anyMatch(u -> u.username.equalsIgnoreCase(username));
    }

    User authenticate(String username, String password) {
        return users.stream()
                .filter(u -> u.username.equalsIgnoreCase(username) && u.password.equals(password))
                .findFirst().orElse(null);
    }

    @SuppressWarnings("unchecked")
    void loadUsers() {
        File f = new File(FILE);
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                users = (List<User>) ois.readObject();
            } catch (Exception ignored) {}
        }
        if (!userExists("admin"))
            users.add(new User("admin", "admin@aeroroute.com", "admin123", "admin"));
    }

    void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE))) {
            oos.writeObject(users);
        } catch (Exception e) { e.printStackTrace(); }
    }

    List<User> getAll() { return Collections.unmodifiableList(users); }
}

class MainFrame extends JFrame {
    private final UserManager.User currentUser;
    private final AirportGraph graph;

    private static final Color BG     = new Color(15, 23, 42);
    private static final Color CARD   = new Color(30, 41, 59);
    private static final Color TEXT   = new Color(148, 163, 184);
    private static final Color MUTED  = new Color(100, 116, 139);
    private static final Color ACCENT = new Color(59, 130, 246);

    MainFrame(UserManager.User user, UserManager um, AirportGraph graph) {
        super("AeroRoute  \u2708  " + user.username + " [" + user.role.toUpperCase() + "]");
        this.currentUser = user;
        this.graph = graph;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.setBackground(CARD);
        tabs.setForeground(TEXT);
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));

        GraphPanel graphPanel = new GraphPanel(graph);
        tabs.addTab("  \u25A6  Graph View  ", graphPanel);
        tabs.addTab("  \u2315  Find Routes  ", new PassengerPanel(graph, graphPanel));

        if ("admin".equals(currentUser.role)) {
            tabs.addTab("  \u2295  Airports  ", new AdminPanel(graph, graphPanel));
            tabs.addTab("  \u2708  Flights  ",  new FlightPanel(graph));
        }

        JPanel barWrap = new JPanel(new BorderLayout());
        barWrap.setBackground(CARD);
        barWrap.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(51, 65, 85)));

        JLabel userLbl = new JLabel("  \u2708  " + currentUser.username.toUpperCase() +
                "   |   " + currentUser.role.toUpperCase());
        userLbl.setForeground(ACCENT);
        userLbl.setFont(new Font("SansSerif", Font.BOLD, 12));

        JButton logout = new JButton("Logout");
        logout.setFont(new Font("SansSerif", Font.PLAIN, 11));
        logout.setForeground(MUTED);
        logout.setContentAreaFilled(false);
        logout.setBorderPainted(false);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> {
            dispose();
            UserManager fresh = new UserManager();
            fresh.loadUsers();
            new LoginFrame(fresh).setVisible(true);
        });

        barWrap.add(userLbl, BorderLayout.WEST);
        barWrap.add(logout, BorderLayout.EAST);

        getContentPane().setBackground(BG);
        getContentPane().add(tabs, BorderLayout.CENTER);
        getContentPane().add(barWrap, BorderLayout.SOUTH);
    }
}

class Airport implements Serializable {
    String code, name, city;
    int x, y;

    Airport(String code, String name, String city, int x, int y) {
        this.code = code; this.name = name; this.city = city;
        this.x = x; this.y = y;
    }

    @Override public String toString() { return code + "  —  " + city; }
}

class Flight implements Serializable {
    String from, to, airline;
    int distance;

    Flight(String from, String to, int distance, String airline) {
        this.from = from; this.to = to;
        this.distance = distance;
        this.airline = airline == null ? "" : airline;
    }
}

class AlgorithmResult {
    String algorithmName;
    List<String> path;
    int totalDistance;
    List<String> visitOrder;

    AlgorithmResult(String name, List<String> path, int dist, List<String> visitOrder) {
        this.algorithmName = name;
        this.path = path;
        this.totalDistance = dist;
        this.visitOrder = visitOrder;
    }

    String getPathString() {
        if (path == null || path.isEmpty()) return "No route found";
        return String.join(" \u2192 ", path);
    }

    int getStops() {
        if (path == null || path.size() < 2) return 0;
        return path.size() - 2;
    }
}

class AirportGraph implements Serializable {
    private static final String FILE = "graph.dat";
    private Map<String, Airport> airports = new LinkedHashMap<>();
    private List<Flight> flights = new ArrayList<>();
    private Map<String, List<Flight>> adj = new HashMap<>();

    void addAirport(Airport a) {
        airports.put(a.code, a);
        adj.putIfAbsent(a.code, new ArrayList<>());
    }

    void addFlight(Flight f) {
        flights.add(f);
        adj.computeIfAbsent(f.from, k -> new ArrayList<>()).add(f);
        Flight rev = new Flight(f.to, f.from, f.distance, f.airline);
        flights.add(rev);
        adj.computeIfAbsent(f.to, k -> new ArrayList<>()).add(rev);
    }

    void removeAirport(String code) {
        airports.remove(code);
        adj.remove(code);
        flights.removeIf(f -> f.from.equals(code) || f.to.equals(code));
        adj.values().forEach(list -> list.removeIf(f -> f.to.equals(code)));
    }

    void removeFlight(String from, String to) {
        flights.removeIf(f -> (f.from.equals(from) && f.to.equals(to)) ||
                (f.from.equals(to) && f.to.equals(from)));
        adj.getOrDefault(from, new ArrayList<>()).removeIf(f -> f.to.equals(to));
        adj.getOrDefault(to, new ArrayList<>()).removeIf(f -> f.to.equals(from));
    }

    Airport getAirport(String code) { return airports.get(code); }
    Collection<Airport> getAirports() { return airports.values(); }
    List<Flight> getAllFlights() { return Collections.unmodifiableList(flights); }

    public AlgorithmResult dijkstra(String src, String dst) {
        Map<String, Integer> dist = new HashMap<>();
        Map<String, String>  prev = new HashMap<>();
        Set<String> visited = new HashSet<>();
        List<String> order = new ArrayList<>();

        for (String k : airports.keySet()) dist.put(k, Integer.MAX_VALUE);
        dist.put(src, 0);

        while (true) {
            String u = null; int minD = Integer.MAX_VALUE;
            for (Map.Entry<String, Integer> e : dist.entrySet())
                if (!visited.contains(e.getKey()) && e.getValue() < minD)
                { minD = e.getValue(); u = e.getKey(); }
            if (u == null || u.equals(dst)) break;
            visited.add(u); order.add(u);
            for (Flight f : adj.getOrDefault(u, Collections.emptyList())) {
                int nd = dist.get(u) + f.distance;
                if (nd < dist.getOrDefault(f.to, Integer.MAX_VALUE))
                { dist.put(f.to, nd); prev.put(f.to, u); }
            }
        }

        if (!dist.containsKey(dst) || dist.get(dst) == Integer.MAX_VALUE)
            return new AlgorithmResult("Dijkstra", Collections.emptyList(), -1, order);

        List<String> path = new ArrayList<>();
        String cur = dst;
        while (cur != null) { path.add(0, cur); cur = prev.get(cur); }
        return new AlgorithmResult("Dijkstra (Shortest Distance)", path, dist.get(dst), order);
    }

    public AlgorithmResult bfs(String src, String dst) {
        Queue<List<String>> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        List<String> order = new ArrayList<>();

        queue.add(new ArrayList<>(Collections.singletonList(src)));
        visited.add(src);

        while (!queue.isEmpty()) {
            List<String> path = queue.poll();
            String cur = path.get(path.size() - 1);
            order.add(cur);
            if (cur.equals(dst))
                return new AlgorithmResult("BFS (Fewest Stops)", path, calcDist(path), order);
            for (Flight f : adj.getOrDefault(cur, Collections.emptyList())) {
                if (!visited.contains(f.to)) {
                    visited.add(f.to);
                    List<String> np = new ArrayList<>(path); np.add(f.to);
                    queue.add(np);
                }
            }
        }
        return new AlgorithmResult("BFS (Fewest Stops)", Collections.emptyList(), -1, order);
    }

    public List<AlgorithmResult> dfs(String src, String dst) {
        List<AlgorithmResult> results = new ArrayList<>();
        List<String> order = new ArrayList<>();
        dfsHelper(src, dst, new ArrayList<>(Collections.singletonList(src)),
                new HashSet<>(Collections.singleton(src)), results, order, 0);
        results.sort(Comparator.comparingInt(r -> r.totalDistance));
        return results.subList(0, Math.min(5, results.size()));
    }

    private void dfsHelper(String cur, String dst, List<String> path, Set<String> visited,
                           List<AlgorithmResult> results, List<String> order, int depth) {
        if (depth > 10 || results.size() >= 10) return;
        order.add(cur);
        if (cur.equals(dst)) {
            results.add(new AlgorithmResult("DFS Path " + (results.size() + 1),
                    new ArrayList<>(path), calcDist(path), new ArrayList<>(order)));
            return;
        }
        for (Flight f : adj.getOrDefault(cur, Collections.emptyList())) {
            if (!visited.contains(f.to)) {
                visited.add(f.to); path.add(f.to);
                dfsHelper(f.to, dst, path, visited, results, order, depth + 1);
                path.remove(path.size() - 1); visited.remove(f.to);
            }
        }
    }

    private int calcDist(List<String> path) {
        int total = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            String from = path.get(i), to = path.get(i + 1);
            for (Flight f : adj.getOrDefault(from, Collections.emptyList()))
                if (f.to.equals(to)) { total += f.distance; break; }
        }
        return total;
    }

    @SuppressWarnings("unchecked")
    void loadFromFile() {
        File f = new File(FILE);
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                AirportGraph loaded = (AirportGraph) ois.readObject();
                this.airports = loaded.airports;
                this.flights  = loaded.flights;
                this.adj      = loaded.adj;
                return;
            } catch (Exception ignored) {}
        }
        seedDefaults();
    }

    void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE))) {
            oos.writeObject(this);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void seedDefaults() {

        // =========================
        // 🌏 ASIA AIRPORT NETWORK
        // =========================

        addAirport(new Airport("KHI", "Jinnah International Airport", "Karachi", 120, 320));
        addAirport(new Airport("LHE", "Allama Iqbal International", "Lahore", 220, 180));
        addAirport(new Airport("ISB", "Islamabad International", "Islamabad", 260, 120));
        addAirport(new Airport("DXB", "Dubai International Airport", "Dubai", 420, 300));
        addAirport(new Airport("DOH", "Hamad International Airport", "Doha", 380, 340));
        addAirport(new Airport("AUH", "Abu Dhabi International", "Abu Dhabi", 400, 360));
        addAirport(new Airport("DEL", "Indira Gandhi International", "New Delhi", 300, 200));
        addAirport(new Airport("BOM", "Chhatrapati Shivaji Intl", "Mumbai", 260, 260));
        addAirport(new Airport("SIN", "Changi Airport", "Singapore", 600, 450));
        addAirport(new Airport("KUL", "Kuala Lumpur Intl", "Kuala Lumpur", 560, 420));
        addAirport(new Airport("BKK", "Suvarnabhumi Airport", "Bangkok", 650, 260));
        addAirport(new Airport("HKG", "Hong Kong International", "Hong Kong", 720, 200));
        addAirport(new Airport("NRT", "Narita International", "Tokyo", 820, 140));

        // =========================
        // ✈️ INTERNATIONAL HUB ROUTES
        // =========================

        addFlight(new Flight("KHI", "DXB", 1200, "Emirates"));
        addFlight(new Flight("KHI", "DOH", 1150, "Qatar Airways"));
        addFlight(new Flight("KHI", "ISB", 900, "PIA"));

        addFlight(new Flight("LHE", "DXB", 1300, "Emirates"));
        addFlight(new Flight("LHE", "DEL", 500, "Air India"));

        addFlight(new Flight("ISB", "DXB", 1100, "Emirates"));
        addFlight(new Flight("ISB", "DOH", 1050, "Qatar Airways"));
        addFlight(new Flight("ISB", "BOM", 1400, "PIA"));

        addFlight(new Flight("DXB", "DOH", 600, "Qatar Airways"));
        addFlight(new Flight("DXB", "AUH", 120, "Etihad"));
        addFlight(new Flight("DXB", "BOM", 1900, "Emirates"));
        addFlight(new Flight("DXB", "SIN", 5800, "Singapore Airlines"));

        addFlight(new Flight("DOH", "SIN", 6200, "Qatar Airways"));
        addFlight(new Flight("DOH", "HKG", 5900, "Qatar Airways"));

        addFlight(new Flight("DEL", "BOM", 1150, "IndiGo"));
        addFlight(new Flight("DEL", "BKK", 2900, "Thai Airways"));
        addFlight(new Flight("DEL", "HKG", 3800, "Cathay Pacific"));

        addFlight(new Flight("BOM", "SIN", 3900, "Singapore Airlines"));
        addFlight(new Flight("BOM", "KUL", 3600, "AirAsia"));

        addFlight(new Flight("SIN", "KUL", 350, "AirAsia"));
        addFlight(new Flight("SIN", "BKK", 1400, "Thai Airways"));
        addFlight(new Flight("SIN", "HKG", 2550, "Cathay Pacific"));
        addFlight(new Flight("SIN", "NRT", 5300, "ANA"));

        addFlight(new Flight("KUL", "BKK", 1200, "AirAsia"));
        addFlight(new Flight("KUL", "HKG", 2500, "Malaysia Airlines"));

        addFlight(new Flight("BKK", "HKG", 1700, "Thai Airways"));
        addFlight(new Flight("HKG", "NRT", 2900, "Japan Airlines"));

        // =========================
        // 💾 SAVE GRAPH
        // =========================
        saveToFile();
    }
    }


class GraphPanel extends JPanel {
    private final AirportGraph graph;
    private List<String> highlightPath = Collections.emptyList();
    private java.util.List<Plane> planes = new ArrayList<>();
    private ATC atc = new ATC();
    private double zoom = 1;
    private int offX = 0, offY = 0;

    private static final Color BG_CANVAS  = new Color(10, 18, 35);
    private static final Color EDGE_DIM   = new Color(51, 65, 85);
    private static final Color EDGE_HI    = new Color(59, 130, 246);
    private static final Color NODE_DEF   = new Color(30, 58, 138);
    private static final Color NODE_HI    = new Color(59, 130, 246);
    private static final Color NODE_RING  = new Color(96, 165, 250);
    private static final Color TEXT_COLOR = new Color(148, 163, 184);
    private static final Color DIST_COLOR = new Color(148, 163, 184);
    private static final int   R          = 22;

    GraphPanel(AirportGraph graph) {
        this.graph = graph;
        setBackground(BG_CANVAS);


        Timer timer = new Timer(30, e -> updatePlanes());
        timer.toString();

        MouseAdapter ma = new MouseAdapter() {
            int lx, ly;
            public void mousePressed(MouseEvent e){ lx=e.getX(); ly=e.getY(); }
            public void mouseDragged(MouseEvent e){
                offX += e.getX()-lx;
                offY += e.getY()-ly;
                lx=e.getX(); ly=e.getY();
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(e -> {
            zoom += -e.getWheelRotation()*0.1;
            zoom = Math.max(0.5, Math.min(2, zoom));
        });
    }

    public void highlight(List<String> path) {
        this.highlightPath = path == null ? Collections.emptyList() : path;
        repaint();
    }

    void addPlane(List<String> path) {
        planes.add(new Plane(path));
    }

    Point trans(int x, int y) {
        return new Point((int)(x*zoom)+offX, (int)(y*zoom)+offY);
    }

    void updatePlanes() {
        for (Plane p : planes) {
            if (p.done()) continue;
            String from = p.path.get(p.i);
            String to = p.path.get(p.i+1);
            if (!atc.allow(from)) {
                p.waiting = true;
                continue;
            }
            p.waiting = false;
            Airport a = graph.getAirport(from);
            Airport b = graph.getAirport(to);
            if (a == null || b == null) continue;
            int x = (int)(a.x + (b.x-a.x)*p.t);
            int y = (int)(a.y + (b.y-a.y)*p.t);
            p.trail.add(new Point(x,y));
            if (p.trail.size()>20) p.trail.remove(0);
            p.t += p.speed;
            if (p.t >= 1) {
                p.t = 0;
                p.i++;
                atc.release(from);
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Map<String, Airport> aMap = new HashMap<>();
        graph.getAirports().forEach(a -> aMap.put(a.code, a));

        Set<String> pathSet = new HashSet<>(highlightPath);
        Set<String> edgeSet = new HashSet<>();
        for (int i = 0; i < highlightPath.size() - 1; i++) {
            edgeSet.add(highlightPath.get(i) + "-" + highlightPath.get(i+1));
            edgeSet.add(highlightPath.get(i+1) + "-" + highlightPath.get(i));
        }

        double sx = getWidth() / 820.0, sy = getHeight() / 500.0;

        Set<String> drawn = new HashSet<>();
        for (Flight f : graph.getAllFlights()) {
            String key1 = f.from + "," + f.to, key2 = f.to + "," + f.from;
            if (drawn.contains(key1) || drawn.contains(key2)) continue;
            drawn.add(key1);
            Airport fa = aMap.get(f.from), ta = aMap.get(f.to);
            if (fa == null || ta == null) continue;
            int x1 = (int)(fa.x*sx*zoom)+offX, y1 = (int)(fa.y*sy*zoom)+offY;
            int x2 = (int)(ta.x*sx*zoom)+offX, y2 = (int)(ta.y*sy*zoom)+offY;
            boolean hi = edgeSet.contains(f.from+"-"+f.to);
            g2.setColor(hi ? EDGE_HI : EDGE_DIM);
            g2.setStroke(new BasicStroke(hi ? 2.8f : 1.2f));
            g2.drawLine(x1, y1, x2, y2);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g2.setColor(hi ? NODE_RING : DIST_COLOR);
            g2.drawString(f.distance + "km", (x1+x2)/2+3, (y1+y2)/2-4);
        }

        for (Airport a : graph.getAirports()) {
            int x = (int)(a.x*sx*zoom)+offX, y = (int)(a.y*sy*zoom)+offY;
            boolean hi = pathSet.contains(a.code);
            if (hi) {
                g2.setColor(new Color(59, 130, 246, 50));
                g2.fillOval(x-R-6, y-R-6, (R+6)*2, (R+6)*2);
            }
            g2.setColor(hi ? NODE_HI : NODE_DEF);
            g2.fillOval(x-R, y-R, R*2, R*2);
            g2.setColor(hi ? Color.WHITE : NODE_RING);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(x-R, y-R, R*2, R*2);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.setColor(TEXT_COLOR);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(a.code, x - fm.stringWidth(a.code)/2, y + fm.getAscent()/2 - 1);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.setColor(DIST_COLOR);
            g2.drawString(a.city, x - fm.stringWidth(a.city)/2, y + R + 13);
        }

        for (Plane p : planes) {
            if (p.done()) continue;
            String f = p.path.get(p.i);
            String t = p.path.get(p.i+1);
            Airport a = graph.getAirport(f);
            Airport b = graph.getAirport(t);
            if (a == null || b == null) continue;
            int x = (int)(a.x + (b.x-a.x)*p.t);
            int y = (int)(a.y + (b.y-a.y)*p.t);
            Point sp = trans(x,y);
            for (int i=1; i<p.trail.size(); i++) {
                Point p1 = trans(p.trail.get(i-1).x, p.trail.get(i-1).y);
                Point p2 = trans(p.trail.get(i).x, p.trail.get(i).y);
                g2.setColor(new Color(100,150,255,120));
                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
            g2.setColor(p.waiting ? Color.RED : Color.WHITE);
            g2.drawString("\u2708", sp.x, sp.y);
        }
    }
}

class Plane {
    List<String> path;
    int i = 0;
    float t = 0;
    float speed = 0.02f + new Random().nextFloat()*0.02f;
    List<Point> trail = new ArrayList<>();
    boolean waiting = false;

    Plane(List<String> p) { path = p; }

    boolean done() {
        return i >= path.size()-1;
    }
}

class ATC {
    Map<String,Integer> cap = new HashMap<>();
    int MAX = 2;

    boolean allow(String a) {
        int c = cap.getOrDefault(a,0);
        if (c < MAX) {
            cap.put(a,c+1);
            return true;
        }
        return false;
    }

    void release(String a) {
        cap.put(a, Math.max(0, cap.getOrDefault(a,0)-1));
    }
}

class AdminPanel extends JPanel {
    private final AirportGraph graph;
    private final GraphPanel graphPanel;
    private DefaultTableModel tableModel;

    private static final Color BG   = new Color(15, 23, 42);
    private static final Color CARD = new Color(30, 41, 59);
    private static final Color TEXT = new Color(50, 193, 28);
    private static final Color MUTED= new Color(100, 116, 139);
    private static final Color BLUE = new Color(59, 130, 246);
    private static final Color RED  = new Color(239, 68, 68);

    AdminPanel(AirportGraph graph, GraphPanel gp) {
        this.graph = graph; this.graphPanel = gp;
        setBackground(BG);
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("Manage Airports");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(TEXT);
        add(title, BorderLayout.NORTH);

        String[] cols = {"Code","Name","City","X","Y"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        styleTable(table);
        refreshTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(CARD);
        add(scroll, BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51,65,85)),
                BorderFactory.createEmptyBorder(12,16,12,16)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,6,4,6); gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField code = tf(), name = tf(), city = tf(), xf = tf(), yf = tf();
        row(form, gbc, 0, "IATA Code:", code);
        row(form, gbc, 1, "Name:", name);
        row(form, gbc, 2, "City:", city);
        row(form, gbc, 3, "X (0-800):", xf);
        row(form, gbc, 4, "Y (0-480):", yf);

        JButton addBtn = btn("Add Airport", BLUE);
        JButton delBtn = btn("Delete Selected", RED);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setBackground(CARD); btns.add(addBtn); btns.add(delBtn);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        form.add(btns, gbc);

        addBtn.addActionListener(e -> {
            String c = code.getText().trim().toUpperCase();
            String n = name.getText().trim(), ci = city.getText().trim();
            if (c.isEmpty() || n.isEmpty() || ci.isEmpty())
            { JOptionPane.showMessageDialog(this, "Fill Code, Name, City"); return; }
            if (graph.getAirport(c) != null)
            { JOptionPane.showMessageDialog(this, "Airport already exists"); return; }
            int x = parseInt(xf.getText(), 400), y = parseInt(yf.getText(), 240);
            graph.addAirport(new Airport(c, n, ci, x, y));
            graph.saveToFile(); refreshTable(); graphPanel.repaint();
            code.setText(""); name.setText(""); city.setText(""); xf.setText(""); yf.setText("");
        });

        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first"); return; }
            graph.removeAirport((String) tableModel.getValueAt(row, 0));
            graph.saveToFile(); refreshTable(); graphPanel.repaint();
        });

        add(form, BorderLayout.SOUTH);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Airport a : graph.getAirports())
            tableModel.addRow(new Object[]{a.code, a.name, a.city, a.x, a.y});
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private JTextField tf() {
        JTextField f = new JTextField(12);
        f.setBackground(BG); f.setForeground(TEXT); f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51,65,85)),
                BorderFactory.createEmptyBorder(4,8,4,8)));
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return f;
    }

    private JButton btn(String t, Color c) {
        JButton b = new JButton(t);
        b.setBackground(c); b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void row(JPanel p, GridBagConstraints gbc, int r, String lbl, JTextField f) {
        gbc.gridx=0; gbc.gridy=r; gbc.gridwidth=1;
        JLabel l = new JLabel(lbl); l.setForeground(MUTED);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        p.add(l, gbc); gbc.gridx=1; p.add(f, gbc);
    }

    private void styleTable(JTable t) {
        t.setBackground(CARD); t.setForeground(TEXT);
        t.setGridColor(new Color(51,65,85)); t.setRowHeight(26);
        t.setFont(new Font("Monospaced", Font.PLAIN, 12));
        t.getTableHeader().setBackground(BG);
        t.getTableHeader().setForeground(MUTED);
        t.setSelectionBackground(new Color(59,130,246,80));
    }
}

class FlightPanel extends JPanel {
    private final AirportGraph graph;
    private DefaultTableModel tableModel;

    private static final Color BG   = new Color(15, 23, 42);
    private static final Color CARD = new Color(30, 41, 59);
    private static final Color TEXT = new Color(50, 193, 28);
    private static final Color MUTED= new Color(100, 116, 139);
    private static final Color BLUE = new Color(59, 130, 246);
    private static final Color RED  = new Color(239, 68, 68);

    FlightPanel(AirportGraph graph) {
        this.graph = graph;
        setBackground(BG);
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("Manage Flights");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(TEXT);
        add(title, BorderLayout.NORTH);

        String[] cols = {"From","To","Distance (km)","Airline"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setBackground(CARD); table.setForeground(TEXT);
        table.setGridColor(new Color(51,65,85)); table.setRowHeight(26);
        table.setFont(new Font("Monospaced", Font.PLAIN, 12));
        table.getTableHeader().setBackground(BG);
        table.getTableHeader().setForeground(MUTED);
        table.setSelectionBackground(new Color(59,130,246,80));
        refreshTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(CARD);
        add(scroll, BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51,65,85)),
                BorderFactory.createEmptyBorder(12,16,12,16)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,6,4,6); gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField from = tf(), to = tf(), dist = tf(), airline = tf();
        row(form, gbc, 0, "From Code:", from);
        row(form, gbc, 1, "To Code:", to);
        row(form, gbc, 2, "Distance (km):", dist);
        row(form, gbc, 3, "Airline:", airline);

        JButton addBtn = btn("Add Flight", BLUE);
        JButton delBtn = btn("Delete Selected", RED);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setBackground(CARD); btns.add(addBtn); btns.add(delBtn);
        gbc.gridx=0; gbc.gridy=4; gbc.gridwidth=2;
        form.add(btns, gbc);

        addBtn.addActionListener(e -> {
            String f2 = from.getText().trim().toUpperCase();
            String t2 = to.getText().trim().toUpperCase();
            String al = airline.getText().trim();
            try {
                int d = Integer.parseInt(dist.getText().trim());
                if (f2.isEmpty() || t2.isEmpty())
                { JOptionPane.showMessageDialog(this, "Enter From and To codes"); return; }
                if (graph.getAirport(f2) == null || graph.getAirport(t2) == null)
                { JOptionPane.showMessageDialog(this, "Airport code not found — add airports first"); return; }
                graph.addFlight(new Flight(f2, t2, d, al));
                graph.saveToFile(); refreshTable();
                from.setText(""); to.setText(""); dist.setText(""); airline.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Distance must be a whole number");
            }
        });

        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first"); return; }
            String f2 = (String) tableModel.getValueAt(row, 0);
            String t2 = (String) tableModel.getValueAt(row, 1);
            graph.removeFlight(f2, t2);
            graph.saveToFile(); refreshTable();
        });

        add(form, BorderLayout.SOUTH);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        Set<String> seen = new HashSet<>();
        for (Flight f : graph.getAllFlights()) {
            String key1 = f.from + "," + f.to, key2 = f.to + "," + f.from;
            if (seen.contains(key1) || seen.contains(key2)) continue;
            seen.add(key1);
            tableModel.addRow(new Object[]{f.from, f.to, f.distance, f.airline});
        }
    }

    private JTextField tf() {
        JTextField f = new JTextField(12);
        f.setBackground(BG); f.setForeground(TEXT); f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51,65,85)),
                BorderFactory.createEmptyBorder(4,8,4,8)));
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return f;
    }

    private JButton btn(String t, Color c) {
        JButton b = new JButton(t);
        b.setBackground(c); b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void row(JPanel p, GridBagConstraints gbc, int r, String lbl, JTextField f) {
        gbc.gridx=0; gbc.gridy=r; gbc.gridwidth=1;
        JLabel l = new JLabel(lbl); l.setForeground(MUTED);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        p.add(l, gbc); gbc.gridx=1; p.add(f, gbc);
    }
}

class PassengerPanel extends JPanel {
    private final AirportGraph graph;
    private final GraphPanel graphCanvas;
    private JComboBox<Airport> fromBox, toBox;
    private JPanel resultsArea;

    private static final Color BG   = new Color(15, 23, 42);
    private static final Color CARD = new Color(30, 41, 59);
    private static final Color TEXT = new Color(21, 140, 51);
    private static final Color MUTED= new Color(100, 116, 139);
    private static final Color BLUE = new Color(59, 130, 246);

    PassengerPanel(AirportGraph graph, GraphPanel gp) {
        this.graph = graph; this.graphCanvas = gp;
        setBackground(BG);
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        buildUI();
    }

    private void buildUI() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        bar.setBackground(CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51,65,85)),
                BorderFactory.createEmptyBorder(8,12,8,12)));

        fromBox = airportCombo(); toBox = airportCombo();
        JButton searchBtn = new JButton("  Search Routes  ");
        searchBtn.setBackground(BLUE); searchBtn.setForeground(Color.WHITE);
        searchBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        searchBtn.setBorderPainted(false); searchBtn.setFocusPainted(false);
        searchBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        bar.add(lbl("From:")); bar.add(fromBox);
        bar.add(lbl("To:"));   bar.add(toBox);
        bar.add(searchBtn);
        add(bar, BorderLayout.NORTH);

        resultsArea = new JPanel();
        resultsArea.setLayout(new BoxLayout(resultsArea, BoxLayout.Y_AXIS));
        resultsArea.setBackground(BG);
        JScrollPane scroll = new JScrollPane(resultsArea);
        scroll.getViewport().setBackground(BG); scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        searchBtn.addActionListener(e -> doSearch());
    }

    private void doSearch() {
        Airport from = (Airport) fromBox.getSelectedItem();
        Airport to   = (Airport) toBox.getSelectedItem();
        if (from == null || to == null) return;
        if (from.code.equals(to.code))
        { JOptionPane.showMessageDialog(this, "Origin and destination must be different"); return; }

        AlgorithmResult dijkRes  = graph.dijkstra(from.code, to.code);
        AlgorithmResult bfsRes   = graph.bfs(from.code, to.code);
        List<AlgorithmResult> dfsRes = graph.dfs(from.code, to.code);

        graphCanvas.highlight(dijkRes.path);

        resultsArea.removeAll();
        resultsArea.add(Box.createVerticalStrut(6));

        JLabel header = new JLabel("  Results: " + from.code + "  \u2192  " + to.code);
        header.setFont(new Font("SansSerif", Font.BOLD, 15));
        header.setForeground(TEXT);
        header.setAlignmentX(LEFT_ALIGNMENT);
        resultsArea.add(header);
        resultsArea.add(Box.createVerticalStrut(10));

        resultsArea.add(card(dijkRes, new Color(59,130,246)));
        resultsArea.add(Box.createVerticalStrut(8));
        resultsArea.add(card(bfsRes, new Color(14,165,233)));
        resultsArea.add(Box.createVerticalStrut(8));

        JLabel dfsHdr = new JLabel("  DFS — All Paths Explored");
        dfsHdr.setFont(new Font("SansSerif", Font.BOLD, 13));
        dfsHdr.setForeground(new Color(52,211,153));
        dfsHdr.setAlignmentX(LEFT_ALIGNMENT);
        resultsArea.add(dfsHdr);
        resultsArea.add(Box.createVerticalStrut(6));

        if (dfsRes.isEmpty()) {
            JLabel none = new JLabel("  No paths found.");
            none.setForeground(MUTED); none.setFont(new Font("SansSerif", Font.ITALIC, 12));
            resultsArea.add(none);
        } else {
            for (AlgorithmResult r : dfsRes) {
                resultsArea.add(card(r, new Color(16,185,129)));
                resultsArea.add(Box.createVerticalStrut(6));
            }
        }
        resultsArea.revalidate(); resultsArea.repaint();
    }

    private JPanel card(AlgorithmResult res, Color accent) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, accent),
                BorderFactory.createEmptyBorder(10,12,10,12)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        p.setAlignmentX(LEFT_ALIGNMENT);

        JLabel nameL = new JLabel(res.algorithmName);
        nameL.setFont(new Font("SansSerif", Font.BOLD, 12));
        nameL.setForeground(TEXT);

        JLabel pathL = new JLabel(res.getPathString());
        pathL.setFont(new Font("Monospaced", Font.PLAIN, 11));
        pathL.setForeground(new Color(148,163,184));

        String info = res.path.isEmpty() ? "No route found"
                : res.totalDistance + " km   \u2022   " + res.getStops() + " stops";
        JLabel infoL = new JLabel(info);
        infoL.setFont(new Font("SansSerif", Font.PLAIN, 11));
        infoL.setForeground(MUTED);

        p.add(nameL); p.add(Box.createVerticalStrut(4));
        p.add(pathL); p.add(Box.createVerticalStrut(3));
        p.add(infoL);

        if (!res.path.isEmpty()) {
            JButton vizBtn = new JButton("Visualize on Graph");
            vizBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
            vizBtn.setForeground(accent); vizBtn.setBackground(CARD);
            vizBtn.setBorderPainted(false); vizBtn.setContentAreaFilled(false);
            vizBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            vizBtn.addActionListener(e -> graphCanvas.highlight(res.path));
            vizBtn.setAlignmentX(LEFT_ALIGNMENT);
            p.add(Box.createVerticalStrut(4)); p.add(vizBtn);
        }
        return p;
    }

    private JComboBox<Airport> airportCombo() {
        DefaultComboBoxModel<Airport> m = new DefaultComboBoxModel<>();
        graph.getAirports().forEach(m::addElement);
        JComboBox<Airport> cb = new JComboBox<>(m);
        cb.setBackground(BG); cb.setForeground(TEXT);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cb.setPreferredSize(new Dimension(200, 32));
        return cb;
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t); l.setForeground(MUTED);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return l;
    }
}