import java.util.ArrayList;
import java.util.Optional;

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

