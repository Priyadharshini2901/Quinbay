import com.mongodb.client.*;
import org.bson.Document;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.logging.Level;
class Store {
    private ArrayList<Product> products = new ArrayList<>();
    private ArrayList<Purchase> cart = new ArrayList<>();
    private static ArrayList<Category> categoryList = new ArrayList<>();
    MongoCollection<Document> productCollection;
    MongoCollection<Document> collection2;
    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final Lock lock = new ReentrantLock();

    public Store(MongoCollection<Document> collection1, MongoCollection<Document> collection2) {
        this.productCollection = collection1;
        this.collection2 = collection2;

    }

    public int lastProductId() {
        Document lastProduct = productCollection.find().sort(new Document("productId", -1)).first();
        return (lastProduct != null) ? lastProduct.getInteger("productId") : 1;
    }
    public void addProduct(int id,String productName, double productPrice, int stock,  boolean delete,Category category) {
        executorService.submit(new AddProductTask(id,productName, productPrice, stock, delete,category));
    }

    public void displayPurchase(Scanner sc) {
        Order order = new Order();
        MongoDBConnection mg = new MongoDBConnection();
        System.out.println("enter the id to be displayed");
        int orderId = sc.nextInt();
        try {
            // Fetch order items from PostgreSQL
            List<OrderItems> orderItems = order.fetchOrderItemsFromPostgreSQL(orderId);

            // Extract product IDs from order items
            List<Integer> productIds = new ArrayList<>();
            for (OrderItems orderItem : orderItems) {
                productIds.add(orderItem.getProductId());
            }

            List<Document> products = mg.fetchProductsFromMongoDB(productIds);

            // Display combined data
            System.out.println("Order ID: " + orderId);
            for (OrderItems orderItem : orderItems) {
                System.out.println("Order Item ID: " + orderItem.getOrderItemId());
                System.out.println("Product ID: " + orderItem.getProductId());
                System.out.println("Quantity: " + orderItem.getQuantity());
                System.out.println("Price: " + orderItem.getPrice());

                for (Document product : products) {
                    if (product.getInteger("productId").equals(orderItem.getProductId())) {
                        System.out.println("Product Name: " + product.getString("productName"));
                        System.out.println("Product Description: " + product.getString("productDescription"));
                        // Display other product details as needed
                    }
                }
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private class AddProductTask implements Runnable {
        private int id;
        private String productName;
        private double productPrice;
        private int stock;
        private boolean delete;
        private Category category;

        AddProductTask(int id,String productName, double productPrice, int stock, boolean delete,Category category) {
            this.id = id;
            this.productName = productName;
            this.productPrice = productPrice;
            this.stock = stock;
            this.delete = delete;
            this.category = category;
        }

        @Override
        public void run() {
            lock.lock();
            try {
                Document catDoc = collection2.find(Filters.eq("name",this.category.getCategoryName())).first();
                if (this.stock < 0) {
                    System.out.println("Enter valid stock value");
                    return;
                } else if (this.productPrice < 0) {
                    System.out.println("Enter valid price value");
                    return;
                } else if (catDoc == null) {
                    System.out.println("Category do not exists");
                } else {
//                    Category category = new Category(lastCatId() + 1,categoryName);
                    Product product = new Product(lastProductId() + 1, productName, productPrice, stock, false, category);
//                    Document categoryDoc = new Document("id", category.getId()).append("name", category.getCategoryName());
//                    collection2.insertOne(categoryDoc);
                    Document productDoc = new Document("productId", product.getId())
                            .append("productName", product.getProductName())
                            .append("productPrice", product.getProductPrice())
                            .append("stocksAvail", product.getStock())
                            .append("deletionStatus", product.isDelete())
                            .append("category", catDoc);
                    productCollection.insertOne(productDoc);
                    System.out.println("Product added successfully!!!");
                }

            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
            finally {
                System.out.println("Thread running");
                lock.unlock();
            }
        }
    }
    public void displayCategory(Scanner scanner) {
        System.out.println("Categories available are");
        FindIterable<Document> iterable = collection2.find();
        Iterator<Document> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            Document catDoc = iterator.next();
            int id = catDoc.getInteger("id");
            String name = catDoc.getString("name");

            System.out.println("ID: " + id + ", Name: "+name);
        }
    }

//    public void addProduct(Scanner sc) {
//        System.out.print("Enter product name: ");
//        String productName = sc.nextLine();
//        System.out.print("Enter product price: ");
//        double productPrice = sc.nextDouble();
//        System.out.print("Enter initial stock: ");
//        int stock = sc.nextInt();
//        sc.nextLine();
//        System.out.println("Enter the category name");
//        String categoryname = sc.nextLine();
//        if (stock < 0 ) {
//            System.out.println("Enter valid stock value");
//        } if (productPrice < 0) {
//            System.out.println("Enter valid price value");
//        } else {
//            Category category = new Category(categoryid++, categoryname);
//            Product product = new Product(lastProductId() + 1, productName, productPrice, stock, false, category);
//            Document categoryDoc = new Document("id", category.getId())
//                    .append("name", category.getCategoryName());
//            collection2.insertOne(categoryDoc);
//            Document productDoc = new Document("productId", product.getId())
//                    .append("productName", product.getProductName())
//                    .append("productPrice", product.getProductPrice())
//                    .append("stocksAvail", product.getStock())
//                    .append("deletionStatus", product.isDelete())
//                    .append("category", categoryDoc);
//            productCollection.insertOne(productDoc);
//            System.out.println("Product added successfully!!!");
////            saveProducts();
////            System.out.println("Product added successfully.");
//        }
//    }

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

        Optional<Document> optionalProduct = Optional.ofNullable(productCollection.find(Filters.eq("productId", idToUpdateStock)).first());
        if (optionalProduct.isEmpty()) {
            System.out.println("Product not found with ID: " + idToUpdateStock);
            return;
        }

        Document productToUpdate = optionalProduct.get();

        boolean isDeleted = productToUpdate.getBoolean("deletionStatus");
        if (isDeleted) {
            System.out.println("Product cannot be updated as it is marked as deleted.");
            return;
        }

        System.out.println("Do you want to replace or override the stock? [0] Replace | [1] Override");
        int stockChoice = sc.nextInt();
        sc.nextLine();

        int currentStock = productToUpdate.getInteger("stocksAvail", 0);
        if(stockChoice == 1) {
            System.out.print("Enter the additional stock quantity: ");
            int additionalStock = sc.nextInt();
            int a = currentStock + additionalStock;
            sc.nextLine();
            if(a < 0)
            {
                System.out.println("Stock cannot be negative");
                return;
            }
//            if (additionalStock < 0) {
//                System.out.println("Invalid stock quantity entered.");
//                return;
//            }
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
//
        int choice = sc.nextInt();
        sc.nextLine();
        if (choice == 0) {
            System.out.print("Enter new price: ");
            double newPrice = sc.nextDouble();
//
            sc.nextLine();
            if (newPrice < 0) {
                System.out.println("Invalid price entered.");
                return;
            }
            productToUpdate.put("productPrice", newPrice);
            System.out.println("Price replaced successfully.");
        } else if (choice == 1) {
            double currentPrice = productToUpdate.getDouble("productPrice");
            System.out.print("Enter the additional price: ");
            double additionalPrice = sc.nextDouble();
            double val = currentPrice + additionalPrice;
            sc.nextLine();
            if(val < 0)
            {
                System.out.println("Invalid price entered.");
                return;
            }
//            double currentPrice = productToUpdate.getDouble("productPrice");
            productToUpdate.put("productPrice", val);
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
                for (Purchase purchase : cart) {
                    System.out.println(purchase.getProduct().getProductName());
                    System.out.println(purchase.getQuantity());
                }
                System.out.print("Enter product ID to be added to cart (or enter '0' to stop adding): ");
                int cartId = sc.nextInt();
                sc.nextLine();
                if (cartId == 0) {
                    break; // Exit loop if user enters '0'
                }
                // Fetch product from MongoDB
                Document productDoc = productCollection.find(Filters.eq("productId", cartId)).first();
                if (productDoc == null || productDoc.getBoolean("deletionStatus", true)) {
                    System.out.println("Product not found with ID " + cartId);
                    continue; // Continue to next iteration of loop
                }

                int productId = productDoc.getInteger("productId");
                String productName = productDoc.getString("productName");
                double productPrice = productDoc.getDouble("productPrice");
                int stock = productDoc.getInteger("stocksAvail");

                Product selectedProduct = new Product(productId, productName, productPrice, stock, false);

                System.out.print("Enter valid quantity within stock ");
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
                                    if (purchase.getQuantity() > selectedProduct.getStock()) {
                                        System.out.println("Quantity exceeds");
                                        cart.clear();
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
                    productCollection.updateOne(Filters.eq("productId", purchase.getProduct().getId()),Updates.inc("stocksAvail", - purchase.getQuantity()));

                }
                order.calculateTotal();
                // Save the order and order items to the database
                int orderId = saveOrder(order);
                saveOrderItems(order, orderId);
                // Clear the cart after saving

                cart.clear();
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
        cart.clear();
    }

    private int saveOrder(Order order) throws SQLException {
        String insertOrderSQL = "INSERT INTO \"Order\" (total, total_products, created_at) VALUES (?, ?, ?) RETURNING order_id";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(insertOrderSQL)) {
            ps.setBigDecimal(1, order.getTotal());
            ps.setInt(2, order.getTotalProductCount());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
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


    public void addCategory(Scanner sc)
    {
        Category catGory;
        boolean exit = false;
        while(!exit)
        {
            System.out.println("Do u want to add new category y/n");

            char ch = sc.next().charAt(0);
            ch = Character.toLowerCase(ch);
            sc.nextLine();
            if(ch == 'y')
            {
                System.out.println("Enter category name");
                String catname = sc.nextLine();
                boolean flag = false;
                for(Category category : categoryList)
                {
                    if(category.getCategoryName().equals(catname))
                    {
                        flag = true;
                        System.out.println("Already present with id "+category.getId() +" try adding another");
                        break;
                    }
                }
                if(!flag) {
//                    System.out.println("before add");
                    catGory = new Category(lastCatId() + 1,catname);
                    categoryList.add(catGory);
                    Document categoryDoc = new Document("id", catGory.getId()).append("name", catGory.getCategoryName());
                    collection2.insertOne(categoryDoc);
//                   System.out.println("after add");
                    System.out.println("category Added successfully");
                }
            }
            else {
                exit = true;

            }
        }
    }
    public static boolean getCatName(String cat){
        for(Category cats : categoryList)
        {
            if(cats.getCategoryName().equals(cat))
            {
                System.out.println("cat is found");
               return true;
            }
        }
        return false;
    }
    public void exit() {
        System.out.println("Exiting...");
        System.exit(0);
    }
    public int lastCatId()
    {
        Document lastcat = collection2.find().sort(new Document("id", -1)).first();
        return (lastcat != null) ? lastcat.getInteger("id") : 1;
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
        boolean exit = false;

        while (!exit) {
            System.out.println("Choose an option:");
            System.out.println("1. Add a product");
            System.out.println("2. View product by ID");
            System.out.println("3. View all products");
            System.out.println("4. Update stock by ID");
            System.out.println("5. Update price by ID");
            System.out.println("6. Update product name by ID");
            System.out.println("7. Purchase");
            System.out.println("8. Remove product");
            System.out.println("10. Add Category");
            System.out.println("11. Display Categories");
            System.out.println("12. Exit");
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1:
                        System.out.print("Enter product name: ");
                        String productName = scanner.nextLine();
                        System.out.print("Enter product price: ");
                        double productPrice = Double.parseDouble(scanner.nextLine());
                        System.out.print("Enter initial stock: ");
                        int stock = Integer.parseInt(scanner.nextLine());
                        System.out.print("Enter the category name within limit: ");
                        store.displayCategory(scanner);
                        String categoryName = scanner.nextLine();
                        Document catDoc = collection2.find(Filters.eq("name",categoryName)).first();
                        Category category;
                        if(catDoc == null)
                        {
                            System.out.println("Enter category within the list");
                        }
                        else {
                            category = new Category(catDoc.getInteger("id"),catDoc.getString("name"));
                            store.addProduct(store.lastProductId() + 1, productName,productPrice,stock,false,category);
                        }
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
                    case 9:
                        store.displayPurchase(scanner);
                    case 10:
                        store.addCategory(scanner);
                        break;
                    case 11:
                        store.displayCategory(scanner);
                        break;
                    case 12:
                        store.exit();
                        break;
                    default:
                        exit = true;
                        System.out.print("Invalid choice");
                        break;
                }
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
        }
    }


}
