import java.io.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

class Product {
    private int id;
    private String productName;
    private double productPrice;
    private int stock;
    private boolean delete;
    private static int count = 0;

    Product(String productName, double productPrice, int stock, boolean delete) {
        this.id = ++count;
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
        Product product = new Product(productName, productPrice, stock, delete);
        product.id = id;
//        count = Math.max(count, id);
        return product;
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
        return product.getId() + "," + quantity;
    }

    public static Purchase fromString(String line, ArrayList<Product> products) {
        String[] parts = line.split(",");
        int productId = Integer.parseInt(parts[0]);
        int quantity = Integer.parseInt(parts[1]);

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
    private static final String PRODUCT_FILE = "/Users/priyadharshini/Desktop/ProductManagement/product.txt";
    private static final String CART_FILE = "/Users/priyadharshini/Desktop/ProductManagement/cart.txt";

    private ArrayList<Product> products = new ArrayList<>();
    private ArrayList<Purchase> cart = new ArrayList<>();

    public Store() {
        loadProducts();
        loadCart();
    }

    private void loadProducts() {
        try (BufferedReader reader = new BufferedReader(new FileReader(PRODUCT_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Product product = Product.fromString(line);
                products.add(product);
            }
        } catch (IOException e) {
            System.out.println("Error reading product file: " + e.getMessage());
        }
    }

    private void loadCart() {
        try (BufferedReader reader = new BufferedReader(new FileReader(CART_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Purchase purchase = Purchase.fromString(line, products);
                if (purchase != null) {
                    cart.add(purchase);
                } else {
                    System.out.println("Error loading cart item: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading cart file: " + e.getMessage());
        }
    }

    private void saveProducts() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PRODUCT_FILE))) {
            for (Product product : products) {
                writer.write(product.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing to product file: " + e.getMessage());
        }
    }

    private void saveCart() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CART_FILE))) {
            for (Purchase purchase : cart) {
                writer.write(purchase.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing to cart file: " + e.getMessage());
        }
    }

    public void addProduct(Scanner sc) {
        System.out.print("Enter product name: ");
        String productName = sc.nextLine();
        System.out.print("Enter product price: ");
        double productPrice = sc.nextDouble();
        System.out.print("Enter initial stock: ");
        int stock = sc.nextInt();
        sc.nextLine();

        if (stock < 0) {
            System.out.println("Enter valid stock value");
        } else if (productPrice < 0) {
            System.out.println("Enter valid price value");
        } else {
            Product newProduct = new Product(productName, productPrice, stock, false);
            products.add(newProduct);
            saveProducts();
            System.out.println("Product added successfully.");
        }
    }

    public void viewProductById(Scanner sc) {
        System.out.print("Enter product ID to view details: ");
        int idToView = sc.nextInt();
        sc.nextLine();

        boolean found = false;
        for (Product product : products) {
            if (product.getId() == idToView) {
                System.out.println("Product ID: " + product.getId());
                System.out.println("Name: " + product.getProductName());
                System.out.println("Price: " + product.getProductPrice());
                System.out.println("Stock: " + product.getStock());
                System.out.println("Delete: " + product.isDelete());
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("Product not found with ID: " + idToView);
        }
    }

    public void viewAllProducts() {
        System.out.println("All products:");
        for (Product product : products) {
            System.out.println("ID: " + product.getId() + ", Name: " + product.getProductName() +
                    ", Price: " + product.getProductPrice() + ", Stock: " + product.getStock() + ", Delete: " + product.isDelete());
        }
    }

    public void updateStockById(Scanner sc) {
        System.out.print("Enter product ID to update stock: ");
        int idToUpdateStock = sc.nextInt();
        sc.nextLine();

        Optional<Product> optionalProduct = products.stream()
                .filter(product -> product.getId() == idToUpdateStock)
                .findFirst();

        if (optionalProduct.isEmpty()) {
            System.out.println("Product not found with ID: " + idToUpdateStock);
            return;
        }

        Product productToUpdate = optionalProduct.get();

        if (productToUpdate.isDelete()) {
            System.out.println("Product cannot be updated as it is marked as deleted.");
            return;
        }

        System.out.println("Do you want to replace or override the stock? [0] Replace | [1] Override");
        int stockChoice = sc.nextInt();
        sc.nextLine();

        if (stockChoice == 1) {
            System.out.print("Enter the additional stock quantity: ");
            int additionalStock = sc.nextInt();
            sc.nextLine();
            if (additionalStock < 0) {
                System.out.println("Invalid stock quantity entered.");
                return;
            }
            productToUpdate.setStock(productToUpdate.getStock() + additionalStock);
            System.out.println("Stock overridden successfully.");
        } else if (stockChoice == 0) {
            System.out.print("Enter new stock quantity: ");
            int newStock = sc.nextInt();
            sc.nextLine();
            if (newStock < 0) {
                System.out.println("Invalid stock quantity entered.");
                return;
            }
            productToUpdate.setStock(newStock);
            System.out.println("Stock replaced successfully.");
        } else {
            System.out.println("Invalid choice. Please choose 0 or 1.");
            return;
        }

        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == idToUpdateStock) {
                products.set(i, productToUpdate);
                break;
            }
        }

        saveProducts();
    }

    public void updatePriceById(Scanner sc) {
        System.out.print("Enter product ID to update price: ");
        int idToUpdatePrice = sc.nextInt();
        sc.nextLine();

        Optional<Product> optionalProduct = products.stream()
                .filter(product -> product.getId() == idToUpdatePrice)
                .findFirst();

        if (optionalProduct.isEmpty()) {
            System.out.println("Product not found with ID: " + idToUpdatePrice);
            return;
        }

        Product productToUpdate = optionalProduct.get();

        if (productToUpdate.isDelete()) {
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
            productToUpdate.setProductPrice(newPrice);
            System.out.println("Price replaced successfully.");
        } else if (choice == 1) {
            System.out.print("Enter the additional price: ");
            double additionalPrice = sc.nextDouble();
            sc.nextLine();
            if (additionalPrice < 0) {
                System.out.println("Invalid price entered.");
                return;
            }
            productToUpdate.setProductPrice(productToUpdate.getProductPrice() + additionalPrice);
            System.out.println("Price overridden successfully.");
        } else {
            System.out.println("Invalid choice. Please choose 0 or 1.");
        }
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == idToUpdatePrice) {
                products.set(i, productToUpdate);
                break;
            }
        }


        saveProducts();
    }

    public void updateProductName(Scanner sc) {
        System.out.print("Enter the product id to be updated: ");
        int idToBeUpdatedForName = sc.nextInt();
        sc.nextLine();

        Optional<Product> optionalProduct = products.stream()
                .filter(product -> product.getId() == idToBeUpdatedForName)
                .findFirst();

        if (optionalProduct.isEmpty()) {
            System.out.println("Product not found with ID: " + idToBeUpdatedForName);
            return;
        }

        Product productToUpdate = optionalProduct.get();

        if (productToUpdate.isDelete()) {
            System.out.println("Product cannot be updated as it is marked as deleted.");
            return;
        }

        System.out.print("Enter the new product name: ");
        String newName = sc.nextLine();
        productToUpdate.setProductName(newName);
        System.out.println("Product name updated successfully.");

        int index = products.indexOf(productToUpdate);
        if (index >= 0) {
            products.set(index, productToUpdate);
        }

//        products.set(products.indexOf(index),productToUpdate);
        saveProducts();
    }


    public void addToCart(Scanner sc) {
        System.out.print("Enter product ID to be added to cart: ");
        int cartId = sc.nextInt();
        sc.nextLine(); // Consume newline

        Optional<Product> optionalProduct = products.stream()
                .filter(product -> product.getId() == cartId && !product.isDelete())
                .findFirst();

        if (optionalProduct.isEmpty()) {
            System.out.println("Product not found with ID " + cartId);
            return;
        }

        Product selectedProduct = optionalProduct.get();

        System.out.print("Enter valid quantity within stock " + selectedProduct.getStock() + ": ");
        int quantity = sc.nextInt();
        sc.nextLine(); // Consume newline

        if (quantity <= 0) {
            System.out.println("Please enter a valid quantity greater than zero.");
            return;
        } else if (quantity > selectedProduct.getStock()) {
            System.out.println("Quantity exceeds available stock.");
            return;
        } else {
            cart.add(new Purchase(selectedProduct, quantity));
            selectedProduct.setStock(selectedProduct.getStock() - quantity);
            System.out.println("Product added to cart successfully.");
        }

        saveCart();
        saveProducts();
    }

    public void removeFromCart(Scanner sc) {
        System.out.print("Enter product ID to remove from cart: ");
        int idToRemove = sc.nextInt();
        sc.nextLine(); // Consume newline

        boolean found = false;

        for (int i = 0; i < cart.size(); i++) {
            if (cart.get(i).getProduct().getId() == idToRemove) {
                cart.remove(i);
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("Enter valid id to delete. Id not found for the given input");
            return;
        }

        for (Product product : products) {
            if (product.getId() == idToRemove && !product.isDelete()) {
                product.setDelete(true);
                System.out.println("Product marked as deleted from cart.");
                break;
            }
        }
        saveCart();
    }

    public void displayCart() {
        System.out.println("Your cart items are:");
        for (Purchase product : cart) {
            System.out.println("ID: " + product.getProduct().getId() + ", Name: " + product.getProduct().getProductName() +
                    ", Price: " + product.getProduct().getProductPrice() + ", Quantity: " + product.getQuantity());
        }
    }

    public void exit() {
        System.out.println("Exiting...");
        System.exit(0);
    }
}

public class Main {
    public static void main(String[] args) {
        Main.whileLoop();
    }

    public static void whileLoop() {
        Scanner scanner = new Scanner(System.in);
        Store store = new Store();

        while (true) {
            System.out.println("\nChoose an option:");
            System.out.println("1. Add a product");
            System.out.println("2. View product by ID");
            System.out.println("3. View all products");
            System.out.println("4. Update stock by ID");
            System.out.println("5. Update price by ID");
            System.out.println("6. Update product name by ID");
            System.out.println("7. Add to cart");
            System.out.println("8. Remove product from cart");
            System.out.println("9. View cart");
            System.out.println("10. Exit");

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
                    store.removeFromCart(scanner);
                    break;
                case 9:
                    store.displayCart();
                    break;
                case 10:
                    store.exit();
                    break;
                default:
                    System.out.print("Invalid choice");
                    break;
            }
        }
    }
}

