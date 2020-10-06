package model;

import java.beans.PropertyChangeSupport;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Product /*implements InventoryItemModel*/ {
    private int id;
    private String name;
    private double price;
    private int stock;
    private int min;
    private int max;
    private final ObservableList<Part> associatedParts = FXCollections.observableArrayList();
    
    public Product(int id, String name, double price, int stock, int min, int max) throws NullPointerException, InvalidParameterException {
        this.id = id;
        if (name == null)
            throw new NullPointerException("Part name cannot be null.");
        if ((name = name.trim()).length() == 0)
            throw new InvalidParameterException("Part name cannot be empty.");
        this.name = name;
        if (price < 0)
            throw new InvalidParameterException("Price cannot be less than zero.");
        this.price = price;
        if (stock < 0)
            throw new InvalidParameterException("Inventory (stock) cannot be less than zero.");
        this.stock = stock;
        if (min < 0)
            throw new InvalidParameterException("Minimum inventory level cannot be less than zero.");
        this.min = 0;
        if (max < 0)
            throw new InvalidParameterException("Maximum inventory level cannot be less than zero.");
        if (max <= min)
            throw new InvalidParameterException("Maximum inventory must be greater than the minimum inventory level.");
        this.max = max;
    }

    public int getId() { return id; }

    public void setId(int id) throws InvalidKeyException {
        int oldId = this.id;
        // Ensure that the id is unique and valid.
        ModelHelper.assertValidIdChange(this, id);
        this.id = id;
        propertyChangeSupport.firePropertyChange(ModelHelper.PROP_ID, oldId, id);
    }
    
    public String getName() { return name; }

    public void setName(String name) {
        if (name == null)
            throw new NullPointerException("Product name cannot be null.");
        if ((name = name.trim()).length() == 0)
            throw new InvalidParameterException("Product name cannot be empty.");
        String oldName = this.name;
        if (name.equals(oldName))
            return;
        this.name = name;
        propertyChangeSupport.firePropertyChange(ModelHelper.PROP_NAME, oldName, name);
    }

    public double getPrice() { return price; }

    public void setPrice(double price) throws InvalidParameterException {
        double oldPrice = this.price;
        if (oldPrice == price)
            return;
        if (price < 0)
            throw new InvalidParameterException("Price cannot be less than zero.");
            
        this.price = price;
        propertyChangeSupport.firePropertyChange(ModelHelper.PROP_PRICE, oldPrice, price);
    }

    public int getStock() { return stock; }

    public void setStock(int stock) {
        int oldStock = this.stock;
        this.stock = stock;
        propertyChangeSupport.firePropertyChange(ModelHelper.PROP_STOCK, oldStock, stock);
    }
 
    public int getMin() { return min; }

    public void setMin(int min) {
        if (min < 0)
            throw new InvalidParameterException("Minimum stock cannot be less than zero.");
        int oldMin = this.min;
        if (oldMin == min)
            return;
        if (min >= this.max)
            throw new InvalidParameterException("Minimum stock level must be less than the maximum stock level.");
        this.min = min;
        propertyChangeSupport.firePropertyChange(ModelHelper.PROP_MIN, oldMin, min);
    }

    public int getMax() { return max; }

    public void setMax(int max) {
        int oldMax = this.max;
        if (oldMax == max)
            return;
        if (max <= this.min)
            throw new InvalidParameterException("Maximum stock level must be greater than the maximum stock level.");
        this.max = max;
        propertyChangeSupport.firePropertyChange(ModelHelper.PROP_MAX, oldMax, max);
    }

    public void setMinMax(int min, int max) {
        if (min < 0)
            throw new InvalidParameterException("Minimum stock cannot be less than zero.");
        if (min >= max)
            throw new InvalidParameterException("Minimum stock level must be less than the maximum stock level.");
        int oldMin = this.min;
        int oldMax = this.max;
        if (oldMin == min) {
            if (oldMax == max)
                return;
        } else {
            this.min = min;
            propertyChangeSupport.firePropertyChange(ModelHelper.PROP_MIN, oldMin, min);
            if (oldMax == max)
                return;
        }
        this.max = max;
        propertyChangeSupport.firePropertyChange(ModelHelper.PROP_MAX, oldMax, max);
    }

    public void addAssociatedPart(Part part) {
        if (part == null)
            throw new NullPointerException();
        if (associatedParts.contains(part))
            return;
        // Make sure it exists in the 'all parts' list beforehand.
        if (!ModelHelper.isPartAdded(part))
            Inventory.addPart(part);
        associatedParts.add(part);
    }

    public void deleteAssociatedPart(Part part) {
        if (part != null && associatedParts.contains(part))
            associatedParts.remove(part);
    }

    public ObservableList<Part> getAllAssociatedParts() { return associatedParts; }

    public void setAllAssociatedParts(Iterable<Part> parts) {
        associatedParts.clear();
        for (Part p : parts)
            addAssociatedPart(p);
    }

    public final void ensureId() {
        if (ModelHelper.isProductAdded(this))
            return;
        int oldId = getId();
        if (oldId < 0 || Inventory.lookupProduct(oldId) != null) {
            ObservableList<Product> allProducts = Inventory.getAllProducts();
            int nextId = allProducts.size();
            if (Inventory.lookupProduct(nextId) != null)
            {
                for (int i = 0; i < nextId; i++) {
                    if (Inventory.lookupProduct(i) != null) {
                        id = i;
                        propertyChangeSupport.firePropertyChange(ModelHelper.PROP_ID, oldId, i);
                        return;
                    }
                }
                do { nextId++; } while (Inventory.lookupProduct(nextId) != null);
            }
            id = nextId;
            propertyChangeSupport.firePropertyChange(ModelHelper.PROP_ID, oldId, nextId);
        }
    }

    public final transient PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);
}
