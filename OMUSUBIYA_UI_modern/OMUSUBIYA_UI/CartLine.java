/** One line in the POS cart. discQty = how many units are covered by the customer's discount. */
public final class CartLine {
    public final Product product;
    public int qty;
    public int discQty;

    public CartLine(Product product, int qty, int discQty) {
        this.product = product;
        this.qty = qty;
        this.discQty = discQty;
    }
}
