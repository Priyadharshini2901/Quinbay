
import javax.swing.*;
import java.lang.reflect.Array;
import java.sql.SQLSyntaxErrorException;
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

    Product(String productName, double productPrice, int stock,boolean delete) {
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
}

class Purchase
{
    private Product product;
    private int quantity;

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Purchase(Product product,int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}

class Store {
    private ArrayList<Product> products = new ArrayList<>();
    private ArrayList<Purchase> cart = new ArrayList<>();


    private static int productIdCounter = 0;

    public void addProduct(Scanner sc) {
        System.out.print("Enter product name: ");
        String productName = sc.nextLine();
        System.out.print("Enter product price: ");
        double productPrice = sc.nextDouble();
        System.out.print("Enter initial stock: ");
        int stock = sc.nextInt();
        if(stock < 0)
            System.out.print("Enter valid stock value");
        else if(productPrice < 0)
            System.out.print("Enter valid price value");
        else {
            Product newProduct = new Product(productName, productPrice, stock, false);
            products.add(newProduct);
        }
    }

    public void viewProductById(Scanner sc) {
        System.out.print("Enter product ID to view details: ");
        int idToView = sc.nextInt();
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
                    ", Price: " + product.getProductPrice() + ", Stock: " + product.getStock()+" ,delete: "+product.isDelete());

        }
    }

    public void updateStockById(Scanner sc) {
        System.out.print("Enter product ID to update stock: ");
        int idToUpdateStock = sc.nextInt();

        // Check if product with given ID exists
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

        if (stockChoice == 1) {
            System.out.print("Enter the additional stock quantity: ");
            int additionalStock = sc.nextInt();
            if (additionalStock < 0) {
                System.out.println("Invalid stock quantity entered.");
                return;
            }
            productToUpdate.setStock(productToUpdate.getStock() + additionalStock);
            System.out.println("Stock overridden successfully.");
        } else if (stockChoice == 0) {
            System.out.print("Enter new stock quantity: ");
            int newStock = sc.nextInt();
            if (newStock < 0) {
                System.out.println("Invalid stock quantity entered.");
                return;
            }
            productToUpdate.setStock(newStock);
            System.out.println("Stock replaced successfully.");
        } else {
            System.out.println("Invalid choice. Please choose 0 or 1.");
        }
    }

    public void updatePriceById(Scanner sc) {
        System.out.print("Enter product ID to update price: ");
        int idToUpdatePrice = sc.nextInt();


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

        if (choice == 0) {
            System.out.print("Enter new price: ");
            double newPrice = sc.nextDouble();
            if (newPrice < 0) {
                System.out.println("Invalid price entered.");
                return;
            }
            productToUpdate.setProductPrice(newPrice);
            System.out.println("Price replaced successfully.");
        } else if (choice == 1) {
            System.out.print("Enter the additional price: ");
            double additionalPrice = sc.nextDouble();
            if (additionalPrice < 0) {
                System.out.println("Invalid price entered.");
                return;
            }
            productToUpdate.setProductPrice(productToUpdate.getProductPrice() + additionalPrice);
            System.out.println("Price overridden successfully.");
        } else {
            System.out.println("Invalid choice. Please choose 0 or 1.");
        }
    }
    public void updateProductName(Scanner sc) {
        System.out.print("Enter the product id to be updated: ");
        int idToBeUpdatedForName = sc.nextInt();


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
        String newName = sc.next();
        productToUpdate.setProductName(newName);
        System.out.println("Product name updated successfully.");
    }

    public void addToCart(Scanner sc) {
        System.out.print("Enter product ID to be added to cart: ");
        int cartId = sc.nextInt();

        Optional<Product> optionalProduct = products.stream()
                .filter(product -> product.getId() == cartId && !product.isDelete())
                .findFirst();

        if (optionalProduct.isEmpty()) {
            System.out.println("Product not found with ID " + cartId);
            return;
        }

        Product selectedProduct = optionalProduct.get();

        System.out.println("Enter valid quantity within stock " + selectedProduct.getStock());
        int quantity = sc.nextInt();

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

            // Optionally, mark product as deleted if stock reaches zero
//            if (selectedProduct.getStock() == 0) {
//                selectedProduct.setDelete(true);
//                System.out.println("Product marked as deleted due to zero stock.");
//            }
        }
    }

    public void removeFromCart(Scanner sc) {
        System.out.print("Enter product ID to remove from cart: ");
        int idToRemove = sc.nextInt();
        boolean found = false;

        for (Purchase product : cart) {
            if (product.getProduct().getId() == idToRemove) {
                cart.remove(product);
                found = true;
                break;
            }
        }

        if(!found)
        {
            System.out.println("Enter valid id to delete. Id not found for the given input");
            return;
        }
        for (Product product : products) {
            if (product.getId() == idToRemove && !product.isDelete()) {
                product.setDelete(true);
                System.out.println("Product marked as deleted from cart.");
                found = true;
                break;
            }
        }

    }

    public void displayCart() {
        System.out.println("Your cart items are:");

        for (Purchase product : cart) {
            System.out.println("ID: " + product.getProduct().getId() + ", Name: " + product.getProduct().getProductName() +
                    ", Price: " + product.getProduct().getProductPrice() + ", Quantity: "+product.getQuantity());
        }
    }

    public void exit() {
        System.out.println("Exiting...");
        System.exit(0);
    }
}

public class Main {

    public static void main(String[] args) {
        Main. whileLoop() ;
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
            System.out.println("7. Purchase from cart");
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
