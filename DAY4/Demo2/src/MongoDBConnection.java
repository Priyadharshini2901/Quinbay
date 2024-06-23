import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoDBConnection {
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "ProductManagement";
    private static final String COLLECTION_NAME = "Product";

    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> collection;

    static {
        try {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase(DATABASE_NAME);
            collection = database.getCollection(COLLECTION_NAME);
            System.out.println("Connected to MongoDB successfully.");
        } catch (Exception e) {
            System.out.println("Failed to connect to MongoDB.");
            e.printStackTrace();
        }
    }

    public static MongoDatabase getDatabase() {
        return database;
    }

    public List<Document> fetchProductsFromMongoDB(List<Integer> productIds) {
        List<Document> products = new ArrayList<>();
        MongoCollection<Document> productCollection = database.getCollection("Product");

        for (int productId : productIds) {
            Document productDoc = productCollection.find(new Document("productId", productId)).first();
            if (productDoc != null) {
                products.add(productDoc);
            }
        }
        return products;
    }
}
