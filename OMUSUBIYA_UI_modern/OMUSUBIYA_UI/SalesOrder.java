import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** A single receipt/order containing one or more product lines. */
public final class SalesOrder {
    private static final AtomicInteger NEXT_NUMBER = new AtomicInteger(1);

    private final String receiptNumber;
    private final List<CartLine> items;

    private SalesOrder(List<CartLine> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("A sales order must contain at least one item.");
        }
        this.receiptNumber = String.format("OR-%06d", NEXT_NUMBER.getAndIncrement());

        List<CartLine> snapshot = new ArrayList<>(items.size());
        for (CartLine item : items) {
            snapshot.add(new CartLine(item.product, item.qty, item.discQty));
        }
        this.items = Collections.unmodifiableList(snapshot);
    }

    public static SalesOrder fromCart(List<CartLine> items) {
        return new SalesOrder(items);
    }

    public String receiptNumber() {
        return receiptNumber;
    }

    public List<CartLine> items() {
        List<CartLine> snapshot = new ArrayList<>(items.size());
        for (CartLine item : items) {
            snapshot.add(new CartLine(item.product, item.qty, item.discQty));
        }
        return Collections.unmodifiableList(snapshot);
    }
}
