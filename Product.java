import java.math.BigDecimal;

/** A menu item. Prices are VAT-inclusive (standard in the Philippines). */
public final class Product {
    private final String name;
    private final String category;
    private final BigDecimal price;
    private final String image; // file name (with or without extension) inside the images/ folder

    public Product(String name, String category, BigDecimal price, String image) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.image = image;
    }

    public String name() { return name; }
    public String category() { return category; }
    public BigDecimal price() { return price; }

    /** Image file name to look for in images/. Falls back to a slug of the product name. */
    public String imageStem() {
        if (image != null && !image.isBlank()) return image.trim();
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    @Override public String toString() { return name; }
}
