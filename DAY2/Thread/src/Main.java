//import jdk.jfr.Category;
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
    private Category category;

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    Product(String productName, double productPrice, int stock, boolean delete, Category category) {
        this.id = ++count;
        this.productName = productName;
        this.productPrice = productPrice;
        this.stock = stock;
        this.delete = delete;
        this.category = category;
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
        Product product = new Product(productName, productPrice, stock, delete, new Category());
        product.id = id;
        return product;
    }
}

class Category {
    private int id;
    private String categoryName;
    Category(int id,String categoryName){
        this.id = id;
        this.categoryName = categoryName;
    }

    public Category() {

    }

    public String getName() {
        return categoryName;
    }

    public void setName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public static Purchase fromString(String line, ArrayList<Product> products) {
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
    private ArrayList<Category> cat = new ArrayList<>();

    public Store() {
        loadProducts();
        loadCart();
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

    private void loadCart() {
        Thread loadCartThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(CART_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Purchase purchase = Purchase.fromString(line, products);
                    if (purchase != null) {
                        synchronized (cart) {
                            cart.add(purchase);
                        }
                    } else {
                        System.out.println("Error loading cart item: " + line);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading cart file: " + e.getMessage());
            }
        });
        loadCartThread.start();
        try {
            loadCartThread.join();
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

    public int catid = 1;

    public void addProduct(Scanner sc) {
        System.out.print("Enter product name: ");
        String productName = sc.nextLine();
        System.out.print("Enter product price: ");
        double productPrice = sc.nextDouble();
        System.out.print("Enter initial stock: ");
        int stock = sc.nextInt();


        int i = 0;
        String caname = "";
        Category category = null;
        if(!cat.isEmpty()){
            System.out.println("Choose the Category");
            i = cat.size();
            for(int j=1;j<=i;j++){
                System.out.println(j+" "+cat.get(j-1).getName());
            }
            System.out.println("Enter 0 for adding a new Category");
            int catId = sc.nextInt();

            if(catId == 0){
                System.out.println("Enter the Category name");
                sc.nextLine();
                caname= sc.nextLine();
                Category newcat = new Category(catid++,caname);
                cat.add(newcat);
                category = newcat;
            }else{
                caname = cat.get(catId-1).getName();
                sc.nextLine();
                category = cat.get(catId - 1);
            }
        }else{
            System.out.println("There is no categories added please add a new one");
            System.out.println("Enter the categories name");
            sc.nextLine();
            caname = sc.nextLine();
            Category newcate = new Category(catid++,caname);
            cat.add(newcate);
            category = newcate;
        }

        if (stock < 0) {
            System.out.println("Enter valid stock value");
        } else if (productPrice < 0) {
            System.out.println("Enter valid price value");
        } else {
            Product newProduct = new Product(productName, productPrice, stock, false,category);

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
                    System.out.println("Category: " + product.getCategory().getName());
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
                        ", Price: " + product.getProductPrice() + ", Stock: " + product.getStock() + ",Category: "+ product.getCategory().getName() + ", Delete: " + product.isDelete());
            }

    }
    public void removeProductById(Scanner sc)
    {
        System.out.println("Enter id to remove");
        int idtoRemove = sc.nextInt();
        sc.nextLine();

        Optional<Product> optionalProduct;

        optionalProduct = products.stream()
                .filter(product -> product.getId() == idtoRemove)
                .findFirst();
        Product productToRemove= optionalProduct.get();
        productToRemove.setDelete(true);
        System.out.println("Product has been deleted");
        saveProducts();
        saveCart();

    }
    public void updateStockById(Scanner sc) {
        System.out.print("Enter product ID to update stock: ");
        int idToUpdateStock = sc.nextInt();
        sc.nextLine();

        Optional<Product> optionalProduct;

            optionalProduct = products.stream()
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

        Optional<Product> optionalProduct;

            optionalProduct = products.stream()
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

        Optional<Product> optionalProduct;

            optionalProduct = products.stream()
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


            for (int i = 0; i < products.size(); i++) {
                if (products.get(i).getId() == idToBeUpdatedForName) {
                    products.set(i, productToUpdate);
                    break;
                }

        }

        saveProducts();
    }

    public void addToCart(Scanner sc) {
        System.out.print("Enter product ID to be added to cart: ");
        int cartId = sc.nextInt();
        sc.nextLine();

        Optional<Product> optionalProduct;

            optionalProduct = products.stream()
                    .filter(product -> product.getId() == cartId && !product.isDelete())
                    .findFirst();


        if (optionalProduct.isEmpty()) {
            System.out.println("Product not found with ID " + cartId);
            return;
        }

        Product selectedProduct = optionalProduct.get();

        System.out.print("Enter valid quantity within stock " + selectedProduct.getStock() + ": ");
        int quantity = sc.nextInt();
        sc.nextLine();

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
        sc.nextLine();

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

                    System.out.println("Product marked as deleted from cart.");
                    break;
                }
            }
            saveCart();
            saveProducts();
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
            System.out.println("Choose an option:");
            System.out.println("1. Add a product");
            System.out.println("2. View product by ID");
            System.out.println("3. View all products");
            System.out.println("4. Update stock by ID");
            System.out.println("5. Update price by ID");
            System.out.println("6. Update product name by ID");
            System.out.println("7. purchase");
            System.out.println("8. Remove product");
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
                    store.removeProductById(scanner);
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
