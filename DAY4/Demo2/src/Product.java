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
