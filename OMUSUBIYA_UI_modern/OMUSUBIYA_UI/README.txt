OMUSUBIYA POS & INVENTORY SYSTEM  (modernized)
==============================================

RUN:      double-click run.bat   (or:  java -jar RoleBasedPOSSystem.jar)
REBUILD:  double-click build.bat (needs JDK 14+). In NetBeans/Eclipse/IntelliJ just add all .java files.

LOGINS (demo)
  admin / admin123          -> all modules
  cashier / cashier123      -> POS only
  inventory / inventory123  -> Inventory + Stock-In
  stock / stock123          -> Stock-In only

MENU PICTURES
  Put photos in the images/ folder (see images/README.txt for file names).
  Edit menu.csv (name,category,price,image) to add items or change prices.
  Keep the images/ folder and menu.csv in the same folder as the .jar.

DISCOUNTS (POS screen -> Discount box)
  Senior Citizen (RA 9994)  20% discount + VAT exemption
  PWD (RA 10754)            20% discount + VAT exemption
      Computation:  price / 1.12  ->  x 20% = discount  ->  amount payable
      e.g. P112.00 item -> P100.00 net of VAT -> less P20.00 -> pay P80.00
      Needs customer name + ID number (printed on the receipt).
      Only for items the senior/PWD personally consumes: edit the "Disc. Qty" column
      in the cart (double-click) when the order is shared with other people.
  Student (10%)             store promo - NOT required by law. Change STUDENT_RATE
                            (and the label) in Discount.java if your shop uses another rate.
  Prices are VAT-inclusive (12% VAT). The receipt shows VATable / VAT-exempt sales.

SALES RELATIONSHIP
  One sales order/receipt can contain multiple product line items. Checkout snapshots the cart
  into a SalesOrder with its own receipt number and item list. Orders are currently in memory only;
  the system does not yet persist sales to a database.

FILES
  Discount.java      discount + VAT computation
  MenuCatalog.java   reads menu.csv         MenuImages.java  loads/crops pictures, draws placeholders
  SalesOrder.java    one sales order to many product line items
  PosFrame.java      picture menu, cart, checkout, receipt
  UITheme.java       colors, rounded buttons/cards/tables (no external libraries)
  Login / Dashboard / Inventory / StockIn frames - restyled
