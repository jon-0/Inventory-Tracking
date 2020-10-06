package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import model.ModelHelper;


public class EditProductController implements Initializable {
    private int currentProductId = -1;
    private final ObservableList<model.Part> availableParts = FXCollections.observableArrayList();
    private boolean showingSearchResults = false;
    
    @FXML
    private Label editProductLabel;

    @FXML
    private TextField idTextField;

    @FXML
    private TextField nameTextField;

    @FXML
    private TextField inventoryTextField;

    @FXML
    private TextField priceTextField;

    @FXML
    private TextField maxTextField;

    @FXML
    private TextField minTextField;

    @FXML
    private Button partsSearchButton;
    
    @FXML
    private TextField searchTextField;

    @FXML
    private TableView<model.Part> unselectedPartsTableView;

    @FXML
    private TableColumn<model.Part, Integer> uPartIdTableColumn;

    @FXML
    private TableColumn<model.Part, String> uPartNameTableColumn;

    @FXML
    private TableColumn<model.Part, Integer> uInventoryLevelTableColumn;

    @FXML
    private TableColumn<model.Part, Double> uPricePerUnitTableColumn;

    @FXML
    private Label noMatchesLabel;
    
    @FXML
    private TableView<model.Part> selectedPartsTableView;

    @FXML
    private TableColumn<model.Part, Integer> sPartIdTableColumn;

    @FXML
    private TableColumn<model.Part, String> sPartNameTableColumn;

    @FXML
    private TableColumn<model.Part, Integer> sInventoryLevelTableColumn;

    @FXML
    private TableColumn<model.Part, Double> sPricePerUnitTableColumn;

    @FXML
    void cancelButtonClick(ActionEvent event) {
        Optional<ButtonType> result = ModelHelper.showConfirmationDialog("Cancel Changes", "All changes will be lost",
                "Are you sure you want to cancel?", Alert.AlertType.CONFIRMATION);
        if (result.isPresent() && result.get() == ButtonType.YES)
            MainScreenController.changeScene((Node)event.getSource(), MainScreenController.VIEW_PATH_MAINSCREEN);
    }

    @FXML
    void onSearchPartClick(ActionEvent event) {
        if (showingSearchResults) {
            unselectedPartsTableView.setItems(availableParts);
            showingSearchResults = false;
            unselectedPartsTableView.setVisible(true);
            noMatchesLabel.setVisible(false);
            searchTextField.setDisable(false);
            searchTextField.setText("");
            partsSearchButton.setText("Search");
            return;
        }
        String searchText = searchTextField.getText().trim().toLowerCase();
        if (searchText.length() == 0) {
            ModelHelper.showNotificationDialog("No text", "Nothing to search", "Enter a name to search for...", Alert.AlertType.WARNING);
            return;
        }
        
        ObservableList<model.Part> searchResults = FXCollections.observableArrayList();
        availableParts.stream().filter((part) -> (part.getName().toLowerCase().contains(searchText))).forEachOrdered((part) -> searchResults.add(part));
        
        if (searchResults.isEmpty()) {
            noMatchesLabel.setVisible(true);
            unselectedPartsTableView.setVisible(false);
        } else {
            noMatchesLabel.setVisible(false);
            unselectedPartsTableView.setVisible(true);
            unselectedPartsTableView.setItems(searchResults);
        }
        searchTextField.setDisable(true);
        showingSearchResults = true;
        partsSearchButton.setText("Show All");
    }

    @FXML
    void saveButtonClick(ActionEvent event) {
        ArrayList<Pair<String, String>> errors = new ArrayList<>();
        
        if (nameTextField.getText().trim().length() == 0)
            errors.add(new Pair(nameTextField.accessibleTextProperty().getValue(), "Text cannot be empty."));
        
        Optional<Double> price = ModelHelper.tryConvertToDouble(priceTextField.getText());
        Optional<Integer> min = ModelHelper.tryConvertToInteger(minTextField.getText());
        Optional<Integer> max = ModelHelper.tryConvertToInteger(maxTextField.getText());
        Optional<Integer> stock = ModelHelper.tryConvertToInteger(inventoryTextField.getText());
        
        ObservableList<model.Part> selectedParts = selectedPartsTableView.getItems();
        if (selectedParts.isEmpty())
            errors.add(new Pair("Selected Parts", "Products must have at least one part."));
        
        if (price.isPresent()) {
            if (price.get() < 0.0)
                errors.add(new Pair(priceTextField.getAccessibleText(), "Price cannot be less than zero."));
            else if (!selectedParts.isEmpty() && ModelHelper.getPriceSum(selectedParts) > price.get())
                errors.add(new Pair(priceTextField.getAccessibleText(),
                        "The sum of the price/cost of the associated parts cannot exceed the price of the product."));
        } else if (priceTextField.getText().trim().length() == 0)
            errors.add(new Pair(priceTextField.getAccessibleText(), "Value cannot be empty."));
        else
            errors.add(new Pair(priceTextField.getAccessibleText(), "Invalid number value."));
        
        if (min.isPresent()) {
            if (min.get() < 0)
                errors.add(new Pair(minTextField.getAccessibleText(), "Value cannot be less than zero."));
        } else if (minTextField.getText().trim().length() == 0)
            errors.add(new Pair(minTextField.getAccessibleText(), "Value cannot be empty."));
        else
            errors.add(new Pair(minTextField.getAccessibleText(), "Invalid number value."));
        
        if (max.isPresent()) {
            if (max.get() < 0)
                errors.add(new Pair(maxTextField.getAccessibleText(), "Value cannot be less than zero."));
            else if (min.isPresent() && min.get() > max.get())
                errors.add(new Pair(maxTextField.getAccessibleText(), "Value cannot be greater than " + minTextField.getAccessibleText() + "."));
        } else if (maxTextField.getText().trim().length() == 0)
            errors.add(new Pair(maxTextField.getAccessibleText(), "Value cannot be empty."));
        else
            errors.add(new Pair(maxTextField.getAccessibleText(), "Invalid number value."));
        
        if (stock.isPresent()) {
            if (stock.get() < 0)
                errors.add(new Pair(inventoryTextField.getAccessibleText(), "Value cannot be less than zero."));
            else if (min.isPresent() && (!max.isPresent() || max.get() >= min.get()) && stock.get() < min.get())
                errors.add(new Pair(inventoryTextField.getAccessibleText(), "Value cannot be less than " + minTextField.getAccessibleText() + "."));
            else if (max.isPresent() && stock.get() > max.get())
                errors.add(new Pair(inventoryTextField.getAccessibleText(), "Value cannot be greater than " + maxTextField.getAccessibleText() + "."));
        } else if (inventoryTextField.getText().trim().length() == 0)
            errors.add(new Pair(inventoryTextField.getAccessibleText(), "Value cannot be empty."));
        else
            errors.add(new Pair(inventoryTextField.getAccessibleText(), "Invalid number value."));
        
        if (!errors.isEmpty()) {
            if (errors.size() == 1)
                ModelHelper.showNotificationDialog("Field Value Error", "Error in \"" + errors.get(0).getKey() + "\"", errors.get(0).getValue(), 
                        Alert.AlertType.ERROR);
            else
                ModelHelper.showNotificationDialog("Field Value Errors", "Errors in " + String.valueOf(errors.size()) + " Fields", 
                        ModelHelper.joinStrings(errors, (Pair<String, String> p) -> p.getKey() + ": " + p.getValue(), "\n"), Alert.AlertType.ERROR);
            return;
        }
        
        model.Product product = (currentProductId < 0) ? null : model.Inventory.lookupProduct(currentProductId);
        if (product == null) {
            product = new model.Product(currentProductId, nameTextField.getText(), price.get(), stock.get(), min.get(), max.get());
            for (model.Part part : selectedParts)
                product.addAssociatedPart(part);
            model.Inventory.addProduct(product);
        } else {
            product.setName(nameTextField.getText());
            product.setPrice(price.get());
            product.setStock(stock.get());
            product.setMinMax(min.get(), max.get());
            product.setAllAssociatedParts(selectedParts);
        }
        
        MainScreenController.changeScene((Node)event.getSource(), MainScreenController.VIEW_PATH_MAINSCREEN);
    }
    
    @FXML
    void addPartButtonClick(ActionEvent event) {
        model.Part part = unselectedPartsTableView.getSelectionModel().getSelectedItem();
        if (part == null) {
            ModelHelper.showNotificationDialog("No selection", "Part must be selected", "You must select a part before it can be added.",
                    Alert.AlertType.WARNING);
            return;
        }
        
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Adding part id {0}", part.getId());
        
        Optional<Double> price = ModelHelper.tryConvertToDouble(priceTextField.getText());
        if (price.isPresent() && price.get() >= 0.0) {
            if (ModelHelper.getPriceSum(part.getId(), part.getPrice(), selectedPartsTableView.getItems()) > price.get()) {
                Optional<ButtonType> confirmation = ModelHelper.showConfirmationDialog("Price Constraint", "Sum of part prices exceeds product Price",
                        "Adding this new part will cause the sum of all part prices to exxceed the product price.\n\nAre you sure you want to add this part?",
                        Alert.AlertType.CONFIRMATION);
                if (!confirmation.isPresent() || confirmation.get() != ButtonType.YES)
                    return;
            }
        } else {
            Optional<ButtonType> confirmation = ModelHelper.showConfirmationDialog("Invalid Product Price", "Product price does not contain a valid number",
                    "Product / Part sum constraint cannot be validated.\n\nAre you sure you want to add this part?",
                    Alert.AlertType.CONFIRMATION);
            if (!confirmation.isPresent() || confirmation.get() != ButtonType.YES)
                return;
        }
        
        ObservableList<model.Part> items = unselectedPartsTableView.getItems();
        items.remove(part);
        selectedPartsTableView.getItems().add(part);
        if (showingSearchResults)
            availableParts.remove(part);
    }
    
    @FXML
    void deletePartButtonClick(ActionEvent event) {
        model.Part part = selectedPartsTableView.getSelectionModel().getSelectedItem();
        if (part == null) {
            ModelHelper.showNotificationDialog("No selection", "Part must be selected", "You must select a part before it can be removed.", 
                    Alert.AlertType.WARNING);
            return;
        }
        ObservableList<model.Part> items = selectedPartsTableView.getItems();
        if (items.size() == 1) {
            ModelHelper.showNotificationDialog("Minimum part constraint", "Cannot Remove Part", "Products must have at least one part.", 
                    Alert.AlertType.WARNING);
            return;
        }
        items.remove(part);
        unselectedPartsTableView.getItems().add(part);
        if (showingSearchResults)
            availableParts.add(part);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sInventoryLevelTableColumn.setCellValueFactory(new PropertyValueFactory<>(ModelHelper.PROP_STOCK));
        sPartIdTableColumn.setCellValueFactory(new PropertyValueFactory<>(ModelHelper.PROP_ID));
        sPartNameTableColumn.setCellValueFactory(new PropertyValueFactory<>(ModelHelper.PROP_NAME));
        sPricePerUnitTableColumn.setCellValueFactory(new PropertyValueFactory<>(ModelHelper.PROP_PRICE));
        uInventoryLevelTableColumn.setCellValueFactory(new PropertyValueFactory<>(ModelHelper.PROP_STOCK));
        uPartIdTableColumn.setCellValueFactory(new PropertyValueFactory<>(ModelHelper.PROP_ID));
        uPartNameTableColumn.setCellValueFactory(new PropertyValueFactory<>(ModelHelper.PROP_NAME));
        uPricePerUnitTableColumn.setCellValueFactory(new PropertyValueFactory<>(ModelHelper.PROP_PRICE));
        unselectedPartsTableView.setItems(availableParts);

        model.Inventory.getAllParts().forEach((part) -> availableParts.add(part));
        inventoryTextField.setText("0");
        minTextField.setText("1");
    }
    
    public void applyModel(model.Product product) {
        
        if (product == null)
            return;
        currentProductId = product.getId();
        editProductLabel.setText("Modify Product");
        idTextField.setText(String.valueOf(currentProductId = product.getId()));
        nameTextField.setText(product.getName());
        inventoryTextField.setText(String.valueOf(product.getStock()));
        priceTextField.setText(String.valueOf(product.getPrice()));
        maxTextField.setText(String.valueOf(product.getMax()));
        minTextField.setText(String.valueOf(product.getMin()));
        availableParts.clear();
        ObservableList<model.Part> selectedParts = FXCollections.observableArrayList();
        model.Inventory.getAllParts().forEach((part) -> {
            if (ModelHelper.isAssociatedPartAdded(part, product))
                selectedParts.add(part);
            else
                availableParts.add(part);
        });
        unselectedPartsTableView.setItems(availableParts);
        selectedPartsTableView.setItems(selectedParts);
    }
}
