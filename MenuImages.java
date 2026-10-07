import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads menu pictures from the images/ folder (next to the .jar or project folder) and scales them to fit
 * a card. If no picture exists yet, a drawn placeholder is shown so the menu never looks empty.
 */
public final class MenuImages {
    private static final String[] EXTENSIONS = {"jpg", "jpeg", "png", "gif", "bmp"};
    private static final Map<String, BufferedImage> SCALED = new HashMap<>();
    private static final Map<String, BufferedImage> ORIGINALS = new HashMap<>();
    private static final BufferedImage MISSING = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    private MenuImages() {}

    public static File appDir() {
        try {
            File f = new File(MenuImages.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (f.isFile()) return f.getParentFile();   // running from a .jar
        } catch (Exception ignored) { }
        return new File(System.getProperty("user.dir"));
    }

    public static File imagesDir() {
        File d = new File(appDir(), "images");
        if (!d.exists()) d.mkdirs();
        return d;
    }

    private static BufferedImage original(Product p) {
        String stem = p.imageStem();
        BufferedImage cached = ORIGINALS.get(stem);
        if (cached != null) return cached == MISSING ? null : cached;

        BufferedImage found = null;
        File dir = imagesDir();
        if (stem.contains(".")) {
            found = read(new File(dir, stem));
        } else {
            for (String ext : EXTENSIONS) {
                found = read(new File(dir, stem + "." + ext));
                if (found != null) break;
            }
        }
        ORIGINALS.put(stem, found == null ? MISSING : found);
        return found;
    }

    private static BufferedImage read(File f) {
        try { return f.isFile() ? ImageIO.read(f) : null; } catch (Exception e) { return null; }
    }

    /** Returns an image of exactly w x h (cover-cropped photo, or a placeholder). */
    public static BufferedImage get(Product p, int w, int h) {
        w = Math.max(1, w); h = Math.max(1, h);
        String key = p.imageStem() + "|" + w + "x" + h;
        BufferedImage out = SCALED.get(key);
        if (out != null) return out;

        out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        hints(g);
        BufferedImage src = original(p);
        if (src != null) {
            double scale = Math.max(w / (double) src.getWidth(), h / (double) src.getHeight());
            int sw = Math.max(w, (int) Math.ceil(src.getWidth() * scale));
            int sh = Math.max(h, (int) Math.ceil(src.getHeight() * scale));
            Image scaled = src.getScaledInstance(sw, sh, Image.SCALE_SMOOTH);
            g.drawImage(scaled, (w - sw) / 2, (h - sh) / 2, null);
        } else {
            paintPlaceholder(g, p, w, h);
        }
        g.dispose();
        SCALED.put(key, out);
        return out;
    }

    public static boolean hasPhoto(Product p) { return original(p) != null; }

    // ---------------------------------------------------------------- placeholder art

    private static void paintPlaceholder(Graphics2D g, Product p, int w, int h) {
        g.setPaint(new GradientPaint(0, 0, new Color(0xFFF4F5), w, h, new Color(0xF9D3D8)));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(255, 255, 255, 120));
        g.fillOval(-h / 3, -h / 3, h, h);
        g.fillOval(w - h / 2, h / 2, h, h);

        String key = (p.name() + " " + p.category()).toLowerCase();
        double size = Math.min(w, h) * 0.52;
        if (key.contains("drink") || key.contains("tea") || key.contains("coffee") || key.contains("juice")
                || key.contains("soda") || key.contains("water") || key.contains("milk")) {
            paintCup(g, w / 2.0, h / 2.0, size, key.contains("coffee") ? new Color(0x7A4B2A) : new Color(0xD98E2B));
        } else {
            paintOnigiri(g, w / 2.0, h / 2.0, size, fillingColor(key));
        }
    }

    private static Color fillingColor(String key) {
        if (key.contains("salmon")) return new Color(0xF4846A);
        if (key.contains("tuna")) return new Color(0xE9B872);
        if (key.contains("ume")) return new Color(0xB3263A);
        if (key.contains("katsuo") || key.contains("bonito")) return new Color(0xB07B50);
        return new Color(0xD9A441);
    }

    /** Draws a simple onigiri (rice triangle, nori band, filling dot). Also used for the logo. */
    public static void paintOnigiri(Graphics2D g0, double cx, double cy, double size, Color filling) {
        Graphics2D g = (Graphics2D) g0.create();
        hints(g);
        double half = size / 2, stroke = size * 0.16;
        Path2D tri = new Path2D.Double();
        tri.moveTo(cx, cy - half * 0.92);
        tri.lineTo(cx + half * 0.95, cy + half * 0.72);
        tri.lineTo(cx - half * 0.95, cy + half * 0.72);
        tri.closePath();

        g.translate(0, size * 0.05);                         // soft shadow
        g.setColor(new Color(0, 0, 0, 28));
        g.setStroke(new BasicStroke((float) stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.fill(tri); g.draw(tri);
        g.translate(0, -size * 0.05);

        g.setColor(Color.WHITE);                             // rice
        g.fill(tri); g.draw(tri);
        g.setColor(new Color(0xE5E7EB));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(tri);

        g.setColor(new Color(0x1F3A2E));                     // nori
        double nw = half * 0.78, nh = size * 0.34, ny = cy + half * 0.72 + stroke / 2 - nh;
        g.fill(new RoundRectangle2D.Double(cx - nw / 2, ny, nw, nh, size * 0.08, size * 0.08));

        double r = size * 0.10;                              // filling peeking out
        g.setColor(filling);
        g.fill(new Ellipse2D.Double(cx - r, cy - half * 0.15 - r, r * 2, r * 2));
        g.setColor(new Color(255, 255, 255, 110));
        g.fill(new Ellipse2D.Double(cx - r * 0.5, cy - half * 0.15 - r * 0.7, r * 0.7, r * 0.5));
        g.dispose();
    }

    private static void paintCup(Graphics2D g0, double cx, double cy, double size, Color liquid) {
        Graphics2D g = (Graphics2D) g0.create();
        hints(g);
        double topW = size * 0.70, botW = size * 0.50, h = size * 0.95, top = cy - h / 2;
        Path2D cup = new Path2D.Double();
        cup.moveTo(cx - topW / 2, top);
        cup.lineTo(cx + topW / 2, top);
        cup.lineTo(cx + botW / 2, top + h);
        cup.lineTo(cx - botW / 2, top + h);
        cup.closePath();

        g.setColor(new Color(0, 0, 0, 28));
        g.translate(0, size * 0.04); g.fill(cup); g.translate(0, -size * 0.04);

        g.setColor(new Color(255, 255, 255, 235)); g.fill(cup);
        Shape old = g.getClip();
        g.clip(cup);
        g.setColor(liquid);
        g.fill(new Rectangle2D.Double(cx - topW, top + h * 0.28, topW * 2, h));
        g.setColor(new Color(255, 255, 255, 90));
        g.fill(new RoundRectangle2D.Double(cx - topW * 0.36, top + h * 0.38, topW * 0.12, h * 0.48, 6, 6));
        g.setClip(old);

        g.setColor(new Color(0xC62B3B));                     // straw
        g.setStroke(new BasicStroke((float) (size * 0.06), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(cx + topW * 0.12, top + h * 0.25, cx + topW * 0.30, top - size * 0.22));
        g.setColor(new Color(0xE5E7EB));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(cup);
        g.dispose();
    }

    private static void hints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }
}
