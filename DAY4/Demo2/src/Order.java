//import java.math.BigDecimal;
//import java.util.ArrayList;
//
//class Order {
//    private int orderId;
//    private ArrayList<OrderItems> cartItems = new ArrayList<>();
//    private BigDecimal total = BigDecimal.ZERO;
//    private int totalProduct;
//    // Constructors, getters, and setters
//    public int getOrderId() {
//        return orderId;
//    }
//
//    public int getTotalProduct() {
//        return totalProduct;
//    }
//
//    public void setTotalProduct(int totalProduct) {
//        this.totalProduct = totalProduct;
//    }
//
//    public void setOrderId(int orderId) {
//        this.orderId = orderId;
//    }
//
//    public ArrayList<OrderItems> getCartItems() {
//        return cartItems;
//    }
//
//    public void setCartItems(ArrayList<OrderItems> cartItems) {
//        this.cartItems = cartItems;
//    }
//
//    public BigDecimal getTotal() {
//        return total;
//    }
//
//    public void setTotal(BigDecimal total) {
//        this.total = total;
//    }
//
//    public void addCartItem(OrderItems item) {
//        this.cartItems.add(item);
//        this.total = this.total.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
//    }
//
//    public void calculateTotal() {
//        BigDecimal sum = BigDecimal.ZERO;
//        for (OrderItems item : cartItems) {
//            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
//            sum = sum.add(itemTotal);
//        }
//        this.total = sum;
//    }
//
//    public int getTotalProductCount() {
//    }
//
////    public void calculateTotalProduct()
////    {
////        this.totalProduct = 0;
////
////    }
//}
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

class Order {
    private int orderId;
    private ArrayList<OrderItems> cartItems = new ArrayList<>();
    private BigDecimal total = BigDecimal.ZERO;
    private int totalProductCount; // New field

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

    public int getTotalProductCount() {
        return totalProductCount;
    }

    public void setTotalProductCount(int totalProductCount) {
        this.totalProductCount = totalProductCount;
    }

    public void addCartItem(OrderItems item) {
        this.cartItems.add(item);
        this.total = this.total.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
    }

    public void calculateTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        int totalCount = 0;
        for (OrderItems item : cartItems) {
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            sum = sum.add(itemTotal);
            totalCount += item.getQuantity();
        }
        this.total = sum;
        this.totalProductCount = totalCount; // Update total product count
    }

    public List<OrderItems> fetchOrderItemsFromPostgreSQL(int orderId) throws SQLException {
        List<OrderItems> orderItems = new ArrayList<>();
        String orderItemQuery = "SELECT * FROM \"OrderItems\" WHERE order_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement orderItemStmt = conn.prepareStatement(orderItemQuery)) {
            orderItemStmt.setInt(1, orderId);
            try (ResultSet orderItemRs = orderItemStmt.executeQuery()) {
                while (orderItemRs.next()) {
                    OrderItems orderItem = new OrderItems();
                    orderItem.setOrderItemId(orderItemRs.getInt("order_item_id"));
                    orderItem.setOrderId(orderItemRs.getInt("order_id"));
                    orderItem.setProductId(orderItemRs.getInt("product_id"));
                    orderItem.setQuantity(orderItemRs.getInt("quantity"));
                    orderItem.setPrice(orderItemRs.getBigDecimal("price"));
                    orderItems.add(orderItem);
                }
            }
        }
        return orderItems;
    }

}
