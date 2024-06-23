
class Category {
    private static int count = 0;
    private int id;
    private String categoryName;

    // Constructors
    public Category(int id,String categoryName) {
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
