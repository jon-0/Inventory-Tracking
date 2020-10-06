package model;

import java.security.InvalidKeyException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import javax.management.openmbean.KeyAlreadyExistsException;

public class ModelHelper {
    public static final String PROP_ID = "id";
    public static final String PROP_NAME = "name";
    public static final String PROP_PRICE = "price";
    public static final String PROP_STOCK = "stock";
    public static final String PROP_MIN = "min";
    public static final String PROP_MAX = "max";
    public static final String PROP_MACHINEID = "machineId";
    public static final String PROP_COMPANYNAME = "companyName";
    public static final String PROP_ASSOCIATEDPARTS = "associatedParts";
    
    public static <T extends Part> T lookupPart(Iterable<T> source, int id) {
        for (T item : source) {
            if (item != null && item.getId() == id)
                return item;
        }
        return null;
    }

    public static Product lookupProduct(Iterable<Product> source, int id) {
        for (Product item : source) {
            if (item != null && item.getId() == id)
                return item;
        }
        return null;
    }
 
    public static <T extends Part> FilteredList<T> lookupParts(ObservableList<T> source, String name) {
        final String text;
        if (name == null || (text = name.trim().toLowerCase()).length() == 0)
            return new FilteredList(FXCollections.observableArrayList());
        return source.filtered(item -> item.getName().toLowerCase().contains(text));
    }
    
    public static FilteredList<Product> lookupProducts(ObservableList<Product> source, String name) {
        final String text;
        if (name == null || (text = name.trim().toLowerCase()).length() == 0)
            return new FilteredList(FXCollections.observableArrayList());
        return source.filtered(item -> item.getName().toLowerCase().contains(text));
    }
    
    public static double getPriceSum(int partId, double newPrice, Stream<Part> parts) {
        return newPrice + parts.filter((Part p) -> p.getId() != partId).mapToDouble((Part p) -> p.getPrice()).sum();
    }

    public static double getPriceSum(int partId, double newPrice, List<Part> parts) { return getPriceSum(partId, newPrice, parts.stream()); }

    public static double getPriceSum(Stream<Part> parts) { return parts.mapToDouble((Part p) -> p.getPrice()).sum(); }

    public static double getPriceSum(List<Part> parts) { return getPriceSum(parts.stream()); }

    public static Part lookupPart(Product product, int partId) {
        if (product == null || partId < 0)
            return null;
        for (Part p: product.getAllAssociatedParts()){
            if (p.getId() == partId)
                return p;
        }
        return null;
    }
    
    public static FilteredList<Product> getAssociatedProducts(int partId) {
        return Inventory.getAllProducts().filtered((Product p) -> lookupPart(p, partId) != null);
    }
    
    public static FilteredList<Product> getPotentialPriceSumViolations(int partId, double newPrice) {
        return Inventory.getAllProducts().filtered((Product p) -> {
            return lookupPart(p, partId) != null && getPriceSum(partId, newPrice, p.getAllAssociatedParts()) > p.getPrice();
        });
    }
    
    public static FilteredList<Product> getWhereLastAssociatedProduct(int partId) {
        return Inventory.getAllProducts().filtered((Product p) -> {
            ObservableList<Part> parts = p.getAllAssociatedParts();
            return parts.size() == 1 && parts.get(0).getId() == partId;
        });
    }
    
    public Stream<Pair<Integer, ObservableList<Part>>> getPartAssociations() {
        return Inventory.getAllProducts().stream().map((Product p) -> new Pair(p.getId(), p.getAllAssociatedParts()));
    }
    
    public static <T> String joinStrings(Iterable<T> source, Function<T, String> mapper, String separator) {
        Iterator<T> iterator = source.iterator();
        if (!iterator.hasNext())
            return "";
        StringBuilder result = new StringBuilder();
        result.append(mapper.apply(iterator.next()));
        while (iterator.hasNext())
            result.append(separator).append(mapper.apply(iterator.next()));
        return result.toString();
    }

    public static String joinStrings(Iterable<String> source, String separator) {
        Iterator<String> iterator = source.iterator();
        if (!iterator.hasNext())
            return "";
        StringBuilder result = new StringBuilder();
        result.append(iterator.next());
        while (iterator.hasNext())
            result.append(separator).append(iterator.next());
        return result.toString();
    }

    public static Optional<Integer> tryConvertToInteger(Object value) {
        if (value == null)
            return Optional.empty();
        if (value instanceof Integer)
            return Optional.of((int)value);
        if (value instanceof String) {
            String s = ((String)value).trim();
            if (s.length() > 0)
                try { return Optional.of(Integer.parseInt(s)); } catch (NumberFormatException e) { }
        } else
            try { return Optional.of((int)value); } catch (Exception e) { }
        return Optional.empty();
    }
    
    public static Optional<Double> tryConvertToDouble(Object value) {
        if (value == null)
            return Optional.empty();
        if (value instanceof Integer)
            return Optional.of((double)value);
        if (value instanceof String) {
            String s = ((String)value).trim();
            if (s.length() > 0)
                try { return Optional.of(Double.parseDouble(s)); } catch (NumberFormatException e) { }
        } else
            try { return Optional.of((double)value); } catch (Exception e) { }
        return Optional.empty();
    }

    public static Object tryConvertToInt(Object value) {
        if (value == null || value instanceof Integer)
            return value;

        if (value instanceof String){
            String s = ((String)value).trim();
            if (s.length() == 0)
                return s;
            try {
                int i = Integer.parseInt((String)value);
                try {
                    if (Double.parseDouble(s) != (double)i)
                        return s;
                } catch (NumberFormatException e) { }
                return i;
            } catch (NumberFormatException e) { }
            return value;
        }

        try { return (int)value; } catch (Exception e) { }
        return value;
    }

    public static void assertValidIdChange(Part part, int newId) throws InvalidKeyException {
        if (part == null)
            throw new NullPointerException();
        if (newId < 0)
            throw new InvalidKeyException();
        if (part.getId() == newId)
            return;
        ObservableList<Part> allParts = Inventory.getAllParts();
        int index = allParts.indexOf(part);
        if (index < 0)
            return;
        for (int i = 0; i < allParts.size(); i++) {
            if (i != index && allParts.get(i).getId() == newId)
                throw new KeyAlreadyExistsException();
        }
    }
    
    public static void assertValidIdChange(Product product, int newId) throws InvalidKeyException {
        if (product == null)
            throw new NullPointerException();
        if (newId < 0)
            throw new InvalidKeyException();
        if (product.getId() == newId)
            return;
        ObservableList<Product> allProducts = Inventory.getAllProducts();
        int index = allProducts.indexOf(product);
        if (index < 0)
            return;
        for (int i = 0; i < allProducts.size(); i++) {
            if (i != index && allProducts.get(i).getId() == newId)
                throw new KeyAlreadyExistsException();
        }
    }

    public static boolean isAssociatedPartAdded(Part part, Product product) { return part != null && product != null && product.getAllAssociatedParts().contains(part); }
    
    public static boolean isPartAdded(Part part) { return part != null && Inventory.getAllParts().contains(part); }
    
    public static boolean isProductAdded(Product product) { return product != null && Inventory.getAllProducts().contains(product); }

    public static void showNotificationDialog(String title, String headerText, String contentText, Alert.AlertType type) {
        Alert alert = new Alert(type, contentText, ButtonType.OK);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.showAndWait();
    }
    
    public static Optional<ButtonType> showConfirmationDialog(String title, String headerText, String contentText, Alert.AlertType type, boolean showCancel) {
        Alert alert = (showCancel) ? new Alert(type, contentText, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL) : 
                new Alert(type, contentText, ButtonType.YES, ButtonType.NO);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        return alert.showAndWait();
    }
    
    public static Optional<ButtonType> showConfirmationDialog(String title, String headerText, String contentText, Alert.AlertType type) {
        return showConfirmationDialog(title, headerText, contentText, type, false);
    }
}