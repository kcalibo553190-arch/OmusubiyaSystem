import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Philippine-standard discounts.
 *
 *  - Senior Citizen (RA 9994) and PWD (RA 10754): 20% discount AND exemption from the 12% VAT.
 *    Computation: price / 1.12 (remove VAT)  ->  x 20% = discount  ->  VAT-exempt price - discount = amount payable.
 *    Only applies to the items the senior/PWD personally consumes.
 *  - Student: NOT required by national law. It is a store promo; change STUDENT_RATE below to whatever
 *    your shop gives. VAT is still charged on promo discounts.
 */
public enum Discount {
    NONE("No Discount", "0", false, null, false),
    SENIOR("Senior Citizen  (20% + VAT-exempt)", "0.20", true, "Senior Citizen ID No.", true),
    PWD("PWD  (20% + VAT-exempt)", "0.20", true, "PWD ID No.", true),
    STUDENT("Student  (10% store promo)", "0.10", false, "School ID No.", false);

    /** Change this to adjust the student promo (0.10 = 10%). Keep the label above in sync. */
    public static final String STUDENT_RATE = "0.10";

    public static final BigDecimal VAT_DIVISOR = new BigDecimal("1.12");

    private final String label;
    private final BigDecimal rate;
    private final boolean vatExempt;
    private final String idLabel;
    private final boolean nameRequired;

    Discount(String label, String rate, boolean vatExempt, String idLabel, boolean nameRequired) {
        this.label = label;
        this.rate = new BigDecimal(rate);
        this.vatExempt = vatExempt;
        this.idLabel = idLabel;
        this.nameRequired = nameRequired;
    }

    public BigDecimal rate() { return this == STUDENT ? new BigDecimal(STUDENT_RATE) : rate; }
    public boolean isVatExempt() { return vatExempt; }
    public String idLabel() { return idLabel; }
    public boolean isNameRequired() { return nameRequired; }
    public String shortName() {
        switch (this) {
            case SENIOR: return "Senior Citizen";
            case PWD: return "PWD";
            case STUDENT: return "Student";
            default: return "";
        }
    }
    public String percentText() { return rate().multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString() + "%"; }

    @Override public String toString() { return label; }

    /** Result of pricing a cart. All money values are rounded to centavos. */
    public static final class Totals {
        public BigDecimal gross = BigDecimal.ZERO;          // sum of VAT-inclusive prices
        public BigDecimal vatRemoved = BigDecimal.ZERO;     // VAT exempted (senior/PWD only)
        public BigDecimal discount = BigDecimal.ZERO;
        public BigDecimal total = BigDecimal.ZERO;          // amount due
        public BigDecimal vatableSales = BigDecimal.ZERO;
        public BigDecimal vat = BigDecimal.ZERO;
        public BigDecimal vatExemptSales = BigDecimal.ZERO;
    }

    public Totals compute(List<CartLine> lines) {
        BigDecimal gross = BigDecimal.ZERO;
        BigDecimal discountedGross = BigDecimal.ZERO; // VAT-inclusive price of the units that get the discount
        for (CartLine l : lines) {
            BigDecimal price = l.product.price();
            gross = gross.add(price.multiply(BigDecimal.valueOf(l.qty)));
            int dq = this == NONE ? 0 : Math.max(0, Math.min(l.discQty, l.qty));
            discountedGross = discountedGross.add(price.multiply(BigDecimal.valueOf(dq)));
        }

        Totals t = new Totals();
        t.gross = money(gross);
        BigDecimal vatableGross;
        if (vatExempt) {
            BigDecimal exemptNet = discountedGross.divide(VAT_DIVISOR, 2, RoundingMode.HALF_UP);
            t.vatRemoved = money(discountedGross.subtract(exemptNet));
            t.discount = money(exemptNet.multiply(rate()));
            t.vatExemptSales = exemptNet;
            vatableGross = gross.subtract(discountedGross);
            t.total = money(t.gross.subtract(t.vatRemoved).subtract(t.discount));
        } else {
            t.discount = money(discountedGross.multiply(rate()));
            vatableGross = gross.subtract(t.discount);
            t.total = money(t.gross.subtract(t.discount));
        }
        t.vatableSales = vatableGross.divide(VAT_DIVISOR, 2, RoundingMode.HALF_UP);
        t.vat = money(vatableGross.subtract(t.vatableSales));
        return t;
    }

    private static BigDecimal money(BigDecimal v) { return v.setScale(2, RoundingMode.HALF_UP); }
}
