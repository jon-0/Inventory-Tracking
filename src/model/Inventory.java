package model;

import java.security.InvalidParameterException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Inventory {
    private static final ObservableList<Part> allParts = FXCollections.observableArrayList();
    private static final ObservableList<Product> allProducts = FXCollections.observableArrayList();
    

    public static void addPart(Part part) throws NullPointerException {
        if (part == null)
            throw new NullPointerException();
        if (allParts.contains(part))
            return;
        part.ensureId();
        allParts.add(part);
    }
    
    public static void addProduct(Product product) throws NullPointerException {
        if (product == null)
            throw new NullPointerException();
        if (allProducts.contains(product))
            return;
        product.ensureId();
        allProducts.add(product);
    }

    public static Part lookupPart(int partId) { return (Part)ModelHelper.lookupPart(allParts, partId); }

    public static Product lookupProduct(int productId) { return (Product)ModelHelper.lookupProduct(allProducts, productId); }
 
    public static Part lookupPart(String partName) {
        if (partName == null || (partName = partName.trim()).length() == 0)
            return null;
        for (Part part : allParts) {
            if (part.getName().equalsIgnoreCase(partName))
                return part;
        }
        return null;
    }
    
    public static Product lookupProduct(String productName) {
        if (productName == null || (productName = productName.trim()).length() == 0)
            return null;
        for (Product product : allProducts) {
            if (product.getName().equalsIgnoreCase(productName))
                return product;
        }
        return null;
    }
    
    public static void updatePart(int index, Part part) throws NullPointerException, InvalidParameterException {
        if (part == null)
            throw new NullPointerException();
        int currentIndex = allParts.indexOf(part);
        if (currentIndex == index)
            return;
        Part existing = allParts.get(index);
        int id = existing.getId();
        if (id != part.getId())
            throw new InvalidParameterException("Unique identifier of part does not match the unique identifier at the specified index.");
        
        if (part instanceof InHouse) {
            if (existing instanceof Outsourced) {
                if (currentIndex > -1)
                    part = new InHouse(id, part.getName(), part.getPrice(), part.getStock(), part.getMin(), part.getMax(), ((InHouse)part).getMachineId());
                allParts.set(index, part);
                return;
            }
            ((InHouse)existing).setMachineId(((InHouse)part).getMachineId());
        } else if (existing instanceof InHouse) {
            if (currentIndex > -1)
                part = new Outsourced(id, part.getName(), part.getPrice(), part.getStock(), part.getMin(), part.getMax(), ((Outsourced)part).getCompanyName());
            allParts.set(index, part);
            return;
        } else
            ((Outsourced)existing).setCompanyName(((Outsourced)part).getCompanyName());
        existing.setName(part.getName());
        existing.setPrice(part.getPrice());
        existing.setStock(part.getStock());
        existing.setMinMax(part.getMin(), part.getMax());
    }
    
    public static void updateProduct(int index, Product product) {
        if (product == null)
            throw new NullPointerException();
        int currentIndex = allProducts.indexOf(product);
        if (currentIndex == index)
            return;
        Product existing = allProducts.get(index);
        if (existing.getId() != product.getId())
            throw new InvalidParameterException("Unique identifier of product does not match the unique identifier at the specified index.");
        existing.setName(product.getName());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());
        existing.setMinMax(product.getMin(), product.getMax());
        existing.setAllAssociatedParts(product.getAllAssociatedParts());
    }
    
    public static void deletePart(Part part) {
        if (part == null || !allParts.contains(part))
            return;
        allProducts.forEach((Product p) -> p.deleteAssociatedPart(part));
        allParts.remove(part);
    }
    
    public static void deleteProduct(Product product) {
        if (product != null && allProducts.contains(product))
            allProducts.remove(product);
    }
    
    public static ObservableList<Part> getAllParts() { return allParts; }
    
    public static ObservableList<Product> getAllProducts() { return allProducts; }
}
