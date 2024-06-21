
import com.mongodb.client.*;
import org.bson.Document;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.io.*;
import java.net.StandardSocketOptions;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

class Product {
    private int id;
    private String productName;
    private double productPrice;
    private int stock;
    private boolean delete;
    private Category category;

    // private static int count = 0;

    Product(int id, String productName, double productPrice, int stock, boolean delete, Category category) {
        this.id = id;
        this.productName = productName;
        this.productPrice = productPrice;
        this.stock = stock;
        this.delete = delete;
        this.category = category;
    }

    Product(int id, String productName, double productPrice, int stock, boolean delete) {
        this.id = id;
        this.productName = productName;
        this.productPrice = productPrice;
        this.stock = stock;
        this.delete = delete;
    }

    public int getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    @Override
    public String toString() {
        return id + "," + productName + "," + productPrice + "," + stock + "," + delete;
    }

    public static Product fromString(String line) {
        String[] parts = line.split(",");
        int id = Integer.parseInt(parts[0]);
        String productName = parts[1];
        double productPrice = Double.parseDouble(parts[2]);
        int stock = Integer.parseInt(parts[3]);
        boolean delete = Boolean.parseBoolean(parts[4]);
        Product product = new Product(id, productName, productPrice, stock, delete);
        product.id = id;
        return product;
    }
}

class Order {
    private int orderId;
    private ArrayList<OrderItems> cartItems = new ArrayList<>();
    private BigDecimal total = BigDecimal.ZERO;

    // Constructors, getters, and setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public ArrayList<OrderItems> getCartItems() {
        return cartItems;
    }

    public void setCartItems(ArrayList<OrderItems> cartItems) {
        this.cartItems = cartItems;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public void addCartItem(OrderItems item) {
        this.cartItems.add(item);
        this.total = this.total.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
    }

    public void calculateTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (OrderItems item : cartItems) {
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            sum = sum.add(itemTotal);
        }
        this.total = sum;
    }
}

class OrderItems {
    private int orderItemId;
    private int orderId;
    private int productId;
    private int quantity;
    private BigDecimal price;

    // Constructors, getters, and setters
    public int getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}

class Category {
    private int id;
    private String categoryName;

    // Constructors
    public Category(int id, String categoryName) {
        this.id = id;
        this.categoryName = categoryName;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    // Override toString() method for better representation
    @Override
    public String toString() {
        return "Category{" + "id=" + id + ", categoryName='" + categoryName + '\'' + '}';
    }
}

class Purchase {
    private Product product;
    private int quantity;

    public Purchase(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return product.getId() + "," + product.getProductName() + "," + product.getProductPrice() + "," + quantity;
    }

    public Purchase fromString(String line, ArrayList<Product> products) {
        String[] parts = line.split(",");
        int productId = Integer.parseInt(parts[0]);
        int quantity = Integer.parseInt(parts[3]);
        Optional<Product> optionalProduct = products.stream()
                .filter(product -> product.getId() == productId)
                .findFirst();
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            return new Purchase(product, quantity);
        } else {
            return null;
        }
    }
}

class Store {
    private static final String PRODUCT_FILE = "/Users/priyadharshini/Desktop/Management/product.txt";
    private static final String CART_FILE = "/Users/priyadharshini/Desktop/Management/cart.txt";
    private ArrayList<Product> products = new ArrayList<>();
    private ArrayList<Purchase> cart = new ArrayList<>();
    int productid = 1;
    int categoryid = 1;
    MongoCollection<Document> productCollection;
    MongoCollection<Document> collection2;

    public Store(MongoCollection<Document> collection1, MongoCollection<Document> collection2) {
        this.productCollection = collection1;
        this.collection2 = collection2;
        loadProducts();
        // loadCart();
    }

    private void loadProducts() {
        Thread loadProductsThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(PRODUCT_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Product product = Product.fromString(line);
                    synchronized (products) {
                        products.add(product);
                    }
                }
                System.out.println("Products have been loaded");
            } catch (IOException e) {
                System.out.println("Error reading product file: " + e.getMessage());
            }
        });
        loadProductsThread.start();
        try {
            loadProductsThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void saveProducts() {
        Thread saveProductsThread = new Thread(() -> {
            System.out.println("save product thread is running");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(PRODUCT_FILE))) {
                for (Product product : products) {
                    writer.write(product.toString());
                    writer.newLine();
                }
            } catch (IOException e) {
                System.out.println("Error writing to product file: " + e.getMessage());
            }
        });
        saveProductsThread.start();
    }

    private void saveCart() {
        Thread saveCartThread = new Thread(() -> {
            synchronized (cart) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(CART_FILE))) {
                    for (Purchase purchase : cart) {
                        writer.write(purchase.toString());
                        writer.newLine();
                    }
                } catch (IOException e) {
                    System.out.println("Error writing to cart file: " + e.getMessage());
                }
            }
        });
        saveCartThread.start();
    }

    public int lastProductId() {
        Document lastProduct = productCollection.find().sort(new Document("productId", -1)).first();
        return (lastProduct != null) ? lastProduct.getInteger("productId") : 1;
    }

    public void addProduct(Scanner sc) {
        System.out.print("Enter product name: ");
        String productName = sc.nextLine();
        System.out.print("Enter product price: ");
        double productPrice = sc.nextDouble();
        System.out.print("Enter initial stock: ");
        int stock = sc.nextInt();
        sc.nextLine();
        System.out.println("Enter the category name");
        String categoryname = sc.nextLine();
        if (stock < 0 ) {
            System.out.println("Enter valid stock value");
        } if (productPrice < 0) {
            System.out.println("Enter valid price value");
        } else {
            Category category = new Category(categoryid++, categoryname);
            Product product = new Product(lastProductId() + 1, productName, productPrice, stock, false, category);
            Document categoryDoc = new Document("id", category.getId())
                    .append("name", category.getCategoryName());
            collection2.insertOne(categoryDoc);
            Document productDoc = new Document("productId", product.getId())
                    .append("productName", product.getProductName())
                    .append("productPrice", product.getProductPrice())
                    .append("stocksAvail", product.getStock())
                    .append("deletionStatus", product.isDelete())
                    .append("category", categoryDoc);
            productCollection.insertOne(productDoc);
            System.out.println("Product added successfully!!!");
            saveProducts();
            System.out.println("Product added successfully.");
        }
    }

    public void viewProductById(Scanner sc) {
        System.out.print("Enter product ID to view details: ");
        int idToView = sc.nextInt();
        sc.nextLine();
        Document query = new Document("productId", idToView);
        FindIterable<Document> iterable = productCollection.find(query);
        Document productDoc = iterable.first();
        if (productDoc != null) {
            int id = productDoc.getInteger("productId");
            String name = productDoc.getString("productName");
            double price = productDoc.getDouble("productPrice");
            int stock = productDoc.getInteger("stocksAvail");
            boolean isDelete = productDoc.getBoolean("deletionStatus");
            Document categoryDoc = (Document) productDoc.get("category");
            int categoryId = categoryDoc.getInteger("id");
            String categoryName = categoryDoc.getString("name");
            System.out.println("Product ID: " + id);
            System.out.println("Name: " + name);
            System.out.println("Price: " + price);
            System.out.println("Stock: " + stock);
            System.out.println("Category Name: " + categoryName);
            System.out.println("Delete: " + isDelete);
        } else {
            System.out.println("Product not found with ID: " + idToView);
        }
    }

    public void viewAllProducts() {
        System.out.println("All products:");
        FindIterable<Document> iterable = productCollection.find();
        Iterator<Document> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            Document productDoc = iterator.next();
            int id = productDoc.getInteger("productId");
            String name = productDoc.getString("productName");
            double price = productDoc.getDouble("productPrice");
            int stock = productDoc.getInteger("stocksAvail", 0);
            boolean isDelete = productDoc.getBoolean("deletionStatus");
            Document categoryDoc = (Document) productDoc.get("category");
            int categoryId = categoryDoc.getInteger("id");
            String categoryName = categoryDoc.getString("name");
            System.out.println("ID: " + id + ", Name: " + name + ", Price: " + price + ", Stock: " + stock + ", Delete: " + isDelete + ", Category Name: " + categoryName);
        }
    }

    public void removeProductById(Scanner sc) {
        System.out.print("Enter product ID to remove: ");
        int idToRemove = sc.nextInt();
        sc.nextLine();
        Document query = new Document("productId", idToRemove);
        Document update = new Document("$set", new Document("deletionStatus", true));
        productCollection.updateOne(query, update);
        System.out.println("Product has been marked as deleted.");
    }

    public void updateStockById(Scanner sc) {
        System.out.print("Enter product ID to update stock: ");
        int idToUpdateStock = sc.nextInt();
        sc.nextLine();

        // Find the product in MongoDB
        Optional<Document> optionalProduct = Optional.ofNullable(productCollection.find(Filters.eq("productId", idToUpdateStock)).first());
        if (optionalProduct.isEmpty()) {
            System.out.println("Product not found with ID: " + idToUpdateStock);
            return;
        }
        Document productToUpdate = optionalProduct.get();

        // Check if the product is marked as deleted
        boolean isDeleted = productToUpdate.getBoolean("deletionStatus");
        if (isDeleted) {
            System.out.println("Product cannot be updated as it is marked as deleted.");
            return;
        }

        System.out.println("Do you want to replace or override the stock? [0] Replace | [1] Override");
        int stockChoice = sc.nextInt();
        sc.nextLine();
        int currentStock = productToUpdate.getInteger("stocksAvail", 0);
        if (stockChoice == 1) {
            System.out.print("Enter the additional stock quantity: ");
            int additionalStock = sc.nextInt();
            sc.nextLine();
            if (additionalStock < 0) {
                System.out.println("Invalid stock quantity entered.");
                return;
            }
            productCollection.updateOne(Filters.eq("productId", idToUpdateStock), Updates.inc("stocksAvail", additionalStock));
            System.out.println("Stock overridden successfully.");
        } else if (stockChoice == 0) {
            System.out.print("Enter new stock quantity: ");
            int newStock = sc.nextInt();
            sc.nextLine();
            if (newStock < 0) {
                System.out.println("Invalid stock quantity entered.");
                return;
            }
            productCollection.updateOne(Filters.eq("productId", idToUpdateStock), Updates.set("stocksAvail", newStock));
            System.out.println("Stock replaced successfully.");
        } else {
            System.out.println("Invalid choice. Please choose 0 or 1.");
            return;
        }
    }

    public void updatePriceById(Scanner sc) {
        System.out.print("Enter product ID to update price: ");
        int idToUpdatePrice = sc.nextInt();
        sc.nextLine();
        Optional<Document> optionalProduct = Optional.ofNullable(productCollection.find(Filters.eq("productId", idToUpdatePrice)).first());
        if (optionalProduct.isEmpty()) {
            System.out.println("Product not found with ID: " + idToUpdatePrice);
            return;
        }
        Document productToUpdate = optionalProduct.get();

        // verify if validated for product is as deleted
        if (productToUpdate.getBoolean("deletionStatus")) {
            System.out.println("Product cannot be updated as it is marked as deleted.");
            return;
        }

        System.out.println("Do you want to replace or override the price? [0] Replace | [1] Override");
        int choice = sc.nextInt();
        sc.nextLine();
        if (choice == 0) {
            System.out.print("Enter new price: ");
            double newPrice = sc.nextDouble();
            sc.nextLine();
            if (newPrice < 0) {
                System.out.println("Invalid price entered.");
                return;
            }
            productToUpdate.put("productPrice", newPrice);
            System.out.println("Price replaced successfully.");
        } else if (choice == 1) {
            System.out.print("Enter the additional price: ");
            double additionalPrice = sc.nextDouble();
            sc.nextLine();
            if (additionalPrice < 0) {
                System.out.println("Invalid price entered.");
                return;
            }
            double currentPrice = productToUpdate.getDouble("productPrice");
            productToUpdate.put("productPrice", currentPrice + additionalPrice);
            System.out.println("Price overridden successfully.");
        } else {
            System.out.println("Invalid choice. Please choose 0 or 1.");
        }
        // Update the document in the collection
        productCollection.updateOne(Filters.eq("productId", idToUpdatePrice), Updates.set("productPrice", productToUpdate.getDouble("productPrice")));
        System.out.println("Product updated successfully.");
    }

    public void updateProductName(Scanner sc) {
        System.out.print("Enter the product ID to update name: ");
        int idToUpdateName = sc.nextInt();
        sc.nextLine();
        Optional<Document> optionalProduct = Optional.ofNullable(productCollection.find(Filters.eq("productId", idToUpdateName)).first());
        if (optionalProduct.isEmpty()) {
            System.out.println("Product not found with ID: " + idToUpdateName);
            return;
        }
        Document productToUpdate = optionalProduct.get();
        if (productToUpdate.getBoolean("deletionStatus")) {
            System.out.println("Product cannot be updated as it is marked as deleted.");
            return;
        }
        System.out.print("Enter the new product name: ");
        String newName = sc.nextLine();
        productToUpdate.put("productName", newName);
        System.out.println("Product name updated successfully.");
        productCollection.updateOne(Filters.eq("productId", idToUpdateName), Updates.set("productName", newName));
    }

    public void addToCart(Scanner sc) {
        try {
            while (true) {
                System.out.print("Enter product ID to be added to cart (or enter '0' to stop adding): ");
                int cartId = sc.nextInt();
                sc.nextLine();
                if (cartId == 0) {
                    break; // Exit loop if user enters '0'
                }
                // Fetch product from MongoDB
                Document productDoc = productCollection.find(Filters.eq("productId", cartId)).first();
                if (productDoc == null || productDoc.getBoolean("deletionStatus", false)) {
                    System.out.println("Product not found with ID " + cartId);
                    continue; // Continue to next iteration of loop
                }

                int productId = productDoc.getInteger("productId");
                String productName = productDoc.getString("productName");
                double productPrice = productDoc.getDouble("productPrice");
                int stock = productDoc.getInteger("stocksAvail");

                Product selectedProduct = new Product(productId, productName, productPrice, stock, false);

                System.out.print("Enter valid quantity within stock " + selectedProduct.getStock() + ": ");
                int quantity = sc.nextInt();
                sc.nextLine();

                if (selectedProduct.getStock() >= quantity) {
                    if (quantity <= 0) {
                        System.out.println("Please enter a valid quantity greater than zero.");
                        continue; // Continue to next iteration of loop
                    } else {
                        boolean productExistsInCart = false;
                        for (Purchase purchase : cart) {
                            if (purchase.getProduct().getId() == selectedProduct.getId()) {
                                // Update the quantity in the cart and adjust the stock
                                if (selectedProduct.getStock() >= quantity) {
                                    purchase.setQuantity(purchase.getQuantity() + quantity);
                                    if(purchase.getQuantity()>selectedProduct.getStock())
                                    {
                                        System.out.println("Quantity exceeds");
                                        return;
                                    }
                                    selectedProduct.setStock(selectedProduct.getStock() - quantity);
                                    productExistsInCart = true;
                                    System.out.println("Updated quantity of product in the cart.");
                                } else {
                                    System.out.println("Quantity exceeds available stock.");
                                }
                                break;
                            }
                        }

                        if (!productExistsInCart) {
                            if (quantity <= selectedProduct.getStock()) {
                                cart.add(new Purchase(selectedProduct, quantity));
                                selectedProduct.setStock(selectedProduct.getStock() - quantity);
                                System.out.println("Product added to cart successfully.");
                            } else {
                                System.out.println("Quantity exceeds available stock.");
                            }
                        }
                    }
                } else {
                    System.out.println("Stock is invalid");
                }
            }

            System.out.println("Your cart items:");
            for (Purchase purchase : cart) {
                System.out.println(purchase.getProduct().getProductName() + " - Quantity: " + purchase.getQuantity());
            }

            System.out.print("Confirm adding these items to cart? (yes/no): ");
            String confirmation = sc.nextLine().trim().toLowerCase();
            if (confirmation.equals("yes")) {
                // Create order and order items
                Order order = new Order();
                for (Purchase purchase : cart) {
                    OrderItems orderItem = new OrderItems();
                    orderItem.setProductId(purchase.getProduct().getId());
                    orderItem.setQuantity(purchase.getQuantity());
                    orderItem.setPrice(BigDecimal.valueOf(purchase.getProduct().getProductPrice()));
                    order.addCartItem(orderItem);
                }
                order.calculateTotal();
                // Save the order and order items to the database
                int orderId = saveOrder(order);
                saveOrderItems(order, orderId);
                // Clear the cart after saving
                cart.clear();
                saveCart();
                saveProducts();
                System.out.println("Order placed successfully with ID: " + orderId);
            } else {
                System.out.println("Operation cancelled. Cart items not added.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter numeric values for product ID and quantity.");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int saveOrder(Order order) throws SQLException {
        String insertOrderSQL = "INSERT INTO \"Order\" (total, created_at) VALUES (?, ?) RETURNING order_id";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(insertOrderSQL)) {
            ps.setBigDecimal(1, order.getTotal());
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("order_id");
            } else {
                throw new SQLException("Failed to retrieve order ID.");
            }
        }
    }

    private void saveOrderItems(Order order, int orderId) throws SQLException {
        String insertOrderItemsSQL = "INSERT INTO \"OrderItems\" (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(insertOrderItemsSQL)) {
            for (OrderItems item : order.getCartItems()) {
                ps.setInt(1, orderId);
                ps.setInt(2, item.getProductId());
                ps.setInt(3, item.getQuantity());
                ps.setBigDecimal(4, item.getPrice());
                ps.addBatch(); // Add the prepared statement to the batch
            }
            ps.executeBatch(); // Execute all statements in the batch
        }
    }

//    public void displayCart() {
//        System.out.println("Your cart items are:");
//        for (Purchase product : cart) {
//            System.out.println("ID: " + product.getProduct().getId() + ", Name: " + product.getProduct().getProductName() + ", Price: " + product.getProduct().getProductPrice() + ", Quantity: " + product.getQuantity());
//        }
//    }

    public void exit() {
        System.out.println("Exiting...");
        System.exit(0);
    }
}

public class Main {
    private final String DATABASE_NAME = "ProductManagement";
    private final String COLLECTION_NAME1 = "Product";
    private final String COLLECTION_NAME2 = "Category";
    private MongoClient mongoClient;
    private MongoDatabase database;
    private final MongoCollection<Document> collection1;
    private final MongoCollection<Document> collection2;

    public Main() {
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        database = mongoClient.getDatabase(DATABASE_NAME);
        this.collection1 = database.getCollection(COLLECTION_NAME1);
        this.collection2 = database.getCollection(COLLECTION_NAME2);
    }

    public static void main(String[] args) {
        // Disable MongoDB driver logging
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.OFF);
        Main main = new Main();
        main.whileLoop();
    }

    public void whileLoop() {
        Scanner scanner = new Scanner(System.in);
        Store store = new Store(collection1, collection2);
        while (true) {
            System.out.println("Choose an option:");
            System.out.println("1. Add a product");
            System.out.println("2. View product by ID");
            System.out.println("3. View all products");
            System.out.println("4. Update stock by ID");
            System.out.println("5. Update price by ID");
            System.out.println("6. Update product name by ID");
            System.out.println("7. Purchase");
            System.out.println("8. Remove product");
//            System.out.println("9. View cart");
            System.out.println("9. Exit");
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1:
                    store.addProduct(scanner);
                    break;
                case 2:
                    store.viewProductById(scanner);
                    break;
                case 3:
                    store.viewAllProducts();
                    break;
                case 4:
                    store.updateStockById(scanner);
                    break;
                case 5:
                    store.updatePriceById(scanner);
                    break;
                case 6:
                    store.updateProductName(scanner);
                    break;
                case 7:
                    store.addToCart(scanner);
                    break;
                case 8:
                    store.removeProductById(scanner);
                    break;
//                case 9:
//                    store.displayCart();
//                    break;
                case 9:
                    store.exit();
                    break;
                default:
                    System.out.print("Invalid choice");
                    break;
            }
        }
    }
}
