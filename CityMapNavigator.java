import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.Timer;

public class CityMapNavigator extends JPanel {
    static class Edge {
        String target;
        int weight;
        Edge(String target, int weight) {
            this.target = target;
            this.weight = weight;
        }
    }

    static class Graph {
        Map<String, List<Edge>> adj = new HashMap<>();
        void addNode(String node) {
            adj.putIfAbsent(node, new ArrayList<>());
        }
        void addEdge(String src, String dest, int weight) {
            adj.get(src).add(new Edge(dest, weight));
            adj.get(dest).add(new Edge(src, weight));
        }
        List<String> dijkstra(String start, String end) {
            Map<String, Integer> dist = new HashMap<>();
            Map<String, String> prev = new HashMap<>();
            PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));

            for (String node : adj.keySet()) {
                dist.put(node, Integer.MAX_VALUE);
                prev.put(node, null);
            }
            dist.put(start, 0);
            pq.add(start);

            while (!pq.isEmpty()) {
                String u = pq.poll();
                if (u.equals(end)) break;
                for (Edge e : adj.get(u)) {
                    int alt = dist.get(u) + e.weight;
                    if (alt < dist.get(e.target)) {
                        dist.put(e.target, alt);
                        prev.put(e.target, u);
                        pq.add(e.target);
                    }
                }
            }

            List<String> path = new ArrayList<>();
            for (String at = end; at != null; at = prev.get(at)) {
                path.add(at);
            }
            Collections.reverse(path);
            return dist.get(end) == Integer.MAX_VALUE ? new ArrayList<>() : path;
        }
    }
    Map<String, Point> nodePositions = new LinkedHashMap<>();
    List<String[]> edges = new ArrayList<>();
    List<String> path = new ArrayList<>();
    Graph graph = new Graph();
    BufferedImage background, carImage;
    ImageIcon pinIcon;
    Timer animationTimer;
    int pathIndex = 0;
    double t = 0.0;
    Point carPosition = null;
    boolean isAnimating = false;

    double zoomFactor = 1.0;
    final double zoomStep = 0.1;
    final double minZoom = 0.5;
    final double maxZoom = 2.0;

    public CityMapNavigator() {
        setPreferredSize(new Dimension(2000, 1500));
        setLayout(null);

        try {
            background = ImageIO.read(new File("C:\\Users\\divya\\OneDrive\\Music\\In HOuse project\\city_background.jpg.png"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Background image not found!");
        }
        try {
            Image img = new ImageIcon("C:\\Users\\divya\\OneDrive\\Music\\In HOuse project\\pin.png.png").getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            pinIcon = new ImageIcon(img);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Pin image not found!");
        }
        try {
            carImage = ImageIO.read(new File("C:\\Users\\divya\\OneDrive\\Music\\In HOuse project\\car.png.png"));
            carImage = resizeImage(carImage, 40, 40);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Car image not found!");
        }
        setupGraph();
        addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                double delta = -e.getPreciseWheelRotation();
                zoomFactor += delta * zoomStep;
                zoomFactor = Math.max(minZoom, Math.min(maxZoom, zoomFactor));
                revalidate();
                repaint();
            }
        });
    }
    private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(originalImage, 0, 0, width, height, null);
        g2d.dispose();
        return resized;
    }
    void connect(String a, String b, int w) {
        if (nodePositions.containsKey(a) && nodePositions.containsKey(b)) {
            graph.addEdge(a, b, w);
            edges.add(new String[]{a, b, String.valueOf(w)});
        }
    }
    void setupGraph() {
        nodePositions.put("Pool", new Point(100, 400));
        nodePositions.put("School", new Point(350, 300));
        nodePositions.put("Hospital", new Point(550, 200));
        nodePositions.put("Shop", new Point(250, 550));
        nodePositions.put("Police Station", new Point(450, 500));
        nodePositions.put("ATM", new Point(400, 600));
        nodePositions.put("Restaurant", new Point(650, 550));
        nodePositions.put("Fire Station", new Point(800, 400));
        nodePositions.put("Petrol Pump", new Point(950, 300));
        nodePositions.put("Bus Stand", new Point(750, 600));
        nodePositions.put("Metro Station", new Point(1000, 600));
        nodePositions.put("Playground", new Point(850, 700));
        nodePositions.put("Library", new Point(550, 400));
        nodePositions.put("Saiyam House", new Point(180, 320));
        nodePositions.put("Sonam House", new Point(980, 200));
        nodePositions.put("Jashan House", new Point(700, 680));
        nodePositions.put("Divyansh House", new Point(120, 280));
        nodePositions.put("Bike Showroom", new Point(620, 140));
        nodePositions.put("Service Center", new Point(1050, 530));

        for (String name : nodePositions.keySet()) {
            graph.addNode(name);
        }

        connect("Pool", "Shop", 2);
        connect("Shop", "ATM", 1);
        connect("ATM", "Police Station", 2);
        connect("Police Station", "Library", 2);
        connect("Library", "Hospital", 3);
        connect("Hospital", "School", 2);
        connect("Library", "Fire Station", 4);
        connect("Fire Station", "Petrol Pump", 3);
        connect("Petrol Pump", "Metro Station", 2);
        connect("Metro Station", "Playground", 3);
        connect("Playground", "Bus Stand", 2);
        connect("Bus Stand", "Restaurant", 2);
        connect("Saiyam House", "School", 2);
        connect("Sonam House", "Petrol Pump", 3);
        connect("Jashan House", "Bus Stand", 2);
        connect("Divyansh House", "Saiyam House", 3);
        connect("Jashan House", "Police Station", 3);
        connect("Restaurant", "School", 3);
        connect("Bike Showroom", "Hospital", 2);
        connect("Service Center", "Metro Station", 1);
    }

    void startCarAnimation() {
        if (path.size() < 2) return;
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        pathIndex = 0;
        t = 0.0;
        isAnimating = true;
        animationTimer = new Timer(30, e -> {
            if (pathIndex >= path.size() - 1) {
                animationTimer.stop();
                isAnimating = false;
                repaint();
                return;
            }
            Point p1 = nodePositions.get(path.get(pathIndex));
            Point p2 = nodePositions.get(path.get(pathIndex + 1));
            int x = (int) ((1 - t) * p1.x + t * p2.x);
            int y = (int) ((1 - t) * p1.y + t * p2.y);
            carPosition = new Point(x, y);
            t += 0.02;
            if (t >= 1.0) {
                t = 0.0;
                pathIndex++;
            }
            repaint();
        });
        animationTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.scale(zoomFactor, zoomFactor);

        if (background != null) {
            g2d.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        }
        for (String[] edge : edges) {
            Point p1 = nodePositions.get(edge[0]);
            Point p2 = nodePositions.get(edge[1]);

            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);

            int midX = (p1.x + p2.x) / 2;
            int midY = (p1.y + p2.y) / 2 - 10;
            g2d.setColor(Color.BLUE);
            g2d.drawString(edge[2] + " km", midX, midY);
        }
        if (path.size() > 1) {
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(4));
            for (int i = 0; i < path.size() - 1; i++) {
                Point p1 = nodePositions.get(path.get(i));
                Point p2 = nodePositions.get(path.get(i + 1));
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }

        if (carImage != null && isAnimating && carPosition != null) {
            g2d.drawImage(carImage, carPosition.x - 20, carPosition.y - 20, null);
        }
        for (Map.Entry<String, Point> entry : nodePositions.entrySet()) {
            Point p = entry.getValue();
            if (pinIcon != null) {
                pinIcon.paintIcon(this, g2d, p.x - 20, p.y - 40);
            } else {
                g2d.setColor(Color.ORANGE);
                g2d.fillRect(p.x - 10, p.y - 10, 20, 20);
            }
            g2d.setColor(Color.BLACK);
            g2d.drawString(entry.getKey(), p.x - 45, p.y - 50);
        }

        g2d.dispose();
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("City Map Navigator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            CityMapNavigator mapPanel = new CityMapNavigator();
            JScrollPane scrollPane = new JScrollPane(mapPanel);

            JPanel controlPanel = new JPanel();
            JComboBox<String> sourceBox = new JComboBox<>(mapPanel.nodePositions.keySet().toArray(new String[0]));
            JComboBox<String> destBox = new JComboBox<>(mapPanel.nodePositions.keySet().toArray(new String[0]));
            JButton findPath = new JButton("Find Path");

            controlPanel.add(new JLabel("From:"));
            controlPanel.add(sourceBox);
            controlPanel.add(new JLabel("To:"));
            controlPanel.add(destBox);
            controlPanel.add(findPath);

            findPath.addActionListener(e -> {
                String from = (String) sourceBox.getSelectedItem();
                String to = (String) destBox.getSelectedItem();
                mapPanel.path = mapPanel.graph.dijkstra(from, to);
                mapPanel.startCarAnimation();
            });

            frame.setLayout(new BorderLayout());
            frame.add(controlPanel, BorderLayout.NORTH);
            frame.add(scrollPane, BorderLayout.CENTER);

            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);
        });
    }
}