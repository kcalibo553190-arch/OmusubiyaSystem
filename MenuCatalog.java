import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads the menu from menu.csv (next to the .jar / project folder).
 * Format:  name,category,price,image     (image = file name inside images/, extension optional)
 * If menu.csv does not exist, a default one is created so you can edit it.
 */
public final class MenuCatalog {
    private MenuCatalog() {}

    public static List<Product> load() {
        File csv = new File(MenuImages.appDir(), "menu.csv");
        if (!csv.exists()) writeDefault(csv);

        List<Product> items = new ArrayList<>();
        try {
            for (String raw : Files.readAllLines(csv.toPath(), StandardCharsets.UTF_8)) {
                String line = raw.replace("\uFEFF", "").trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] p = line.split(",", -1);
                if (p.length < 3) continue;
                try {
                    BigDecimal price = new BigDecimal(p[2].trim().replace("\u20B1", ""));
                    items.add(new Product(p[0].trim(), p[1].trim(), price, p.length > 3 ? p[3].trim() : ""));
                } catch (NumberFormatException ignored) { /* header row or bad price: skip */ }
            }
        } catch (IOException ignored) { }
        return items.isEmpty() ? defaults() : items;
    }

    public static List<String> categories(List<Product> items) {
        Set<String> set = new LinkedHashSet<>();
        for (Product p : items) set.add(p.category());
        return new ArrayList<>(set);
    }

    private static List<Product> defaults() {
        List<Product> l = new ArrayList<>();
        l.add(new Product("Salmon Onigiri", "Onigiri", new BigDecimal("55.00"), ""));
        l.add(new Product("Tuna Mayo Onigiri", "Onigiri", new BigDecimal("50.00"), ""));
        l.add(new Product("Umeboshi Onigiri", "Onigiri", new BigDecimal("40.00"), ""));
        l.add(new Product("Katsuobushi Onigiri", "Onigiri", new BigDecimal("45.00"), ""));
        l.add(new Product("Iced Tea", "Drinks", new BigDecimal("35.00"), ""));
        return l;
    }

    private static void writeDefault(File csv) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Omusubiya menu  -  name,category,price (VAT-inclusive, in pesos),image\n");
        sb.append("# image = file name inside the images/ folder (.jpg / .png). Leave blank to use the item name,\n");
        sb.append("# e.g. \"Salmon Onigiri\" -> images/salmon-onigiri.jpg\n");
        sb.append("name,category,price,image\n");
        for (Product p : defaults()) {
            sb.append(p.name()).append(',').append(p.category()).append(',').append(p.price()).append(',')
              .append(p.imageStem()).append('\n');
        }
        try { Files.write(csv.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8)); } catch (IOException ignored) { }
    }
}
