package controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Inventory;
import model.ModelHelper;


public class MainScreenController implements Initializable {

    public static final String VIEW_PATH_EDITPARTSCREEN = "/view/EditPartScreen.fxml";

    public static final String VIEW_PATH_EDITPRODUCTSCREEN = "/view/EditProductScreen.fxml";

    public static final String VIEW_PATH_MAINSCREEN = "/view/MainScreen.fxml";
    
    private boolean showingProductSearchResults = false;
    private boolean showingPartSearchResults = false;
    
    @FXML
    private TextField partsSearchTextField;

    @FXML
    private TableView<model.Part> partsTableView;

    @FXML
    private TableColumn<model.Part, Integer> partIdTableColumn;

    @FXML
    private TableColumn<model.Part, String> partNameTableColumn;

    @FXML
    private TableColumn<model.Part, Integer> partInventoryLevelTableColumn;

    @FXML
    private TableColumn<model.Part, Double> partPriceTableColumn;

    @FXML
    private TextField productsSearchTextField;

    @FXML
    private TableView<model.Product> productsTableView;

    @FXML
    private TableColumn<?, ?> productIdTableColumn;

    @FXML
    private TableColumn<?, ?> productNameTableColumn;

    @FXML
    private TableColumn<?, ?> productInventoryLevelTableColumn;

    @FXML
    private TableColumn<?, ?> productPriceTableColumn;

    @FXML
    private Label noPartMatchesLabel;

    @FXML
    private Button partsSearchButton;

    @FXML
    private Button productsSearchButton;

    @FXML
    private Label noProductMatchesLabel;

    @FXML
    void onAddPartButtonClick(ActionEvent event) throws MalformedURLException, IOException {
        changeScene((Node)event.getSource(), VIEW_PATH_EDITPARTSCREEN);
    }

    @FXML
    void onModifyPartButtonClick(ActionEvent event) throws MalformedURLException, IOException {
        model.Part sourcePart = partsTableView.getSelectionModel().getSelectedItem();
        if (sourcePart == null)
            ModelHelper.showNotificationDialog("Modify Part", "Nothing is selected", "You must select a part before it can be edited",
                    Alert.AlertType.WARNING);
        else
            changeScene((Node)event.getSource(), VIEW_PATH_EDITPARTSCREEN, (EditPartController controller) -> {
                controller.applyModel(sourcePart);
            });
    }

    @FXML
    void onDeletePartButtonClick(ActionEvent event) {
        model.Part part = partsTableView.getSelectionModel().getSelectedItem();
        if (part == null) {
            ModelHelper.showNotificationDialog("Delete Part", "Nothing is selected", "You must select a part before it can be deleted", Alert.AlertType.WARNING);
            return;
        }
        FilteredList<model.Product> violations = ModelHelper.getWhereLastAssociatedProduct(part.getId());
        if (!violations.isEmpty()) {
            String message = "Removing this part would remove the last part from ";
            if (violations.size() == 1)
                message += "1 product named \"" + violations.get(0).getName() + "\"";
            else {
                message += String.valueOf(violations.size()) + " products:\n" + ModelHelper.joinStrings(violations,
                        (model.Product p) -> "- " + p.getName(), "\n");
            }
            ModelHelper.showNotificationDialog("Delete Part", "Minimum part Constraint Error", message, Alert.AlertType.WARNING);
            return;
        }
        int count = model.Inventory.getAllProducts().filtered(p -> p.getAllAssociatedParts().contains(part)).size();
        String contentText = "This action cannot be undone!\n\nAre you sure you want to delete this part?";
        if (count > 0)
            contentText = ((count == 1) ? "1 product references this part, and it will be deleted from that one as well" :
                    String.valueOf(count) + " products reference this part, and it will be deleted from those as well") + ".\r\n" + contentText;
        
        Optional<ButtonType> result = ModelHelper.showConfirmationDialog("Delete Part", "Confirm Delete Part \"" + part.getName() + "\"", contentText,
                Alert.AlertType.CONFIRMATION);
        if (result.isPresent() && result.get() == ButtonType.YES)
            model.Inventory.deletePart(part);
    }

    @FXML
    void onAddProductButtonClick(ActionEvent event) throws MalformedURLException, IOException {
        changeScene((Node)event.getSource(), VIEW_PATH_EDITPRODUCTSCREEN);
    }

    @FXML
    void onModifyProductButtonClick(ActionEvent event) {
        model.Product sourceProduct = productsTableView.getSelectionModel().getSelectedItem();
        if (sourceProduct == null)
            ModelHelper.showNotificationDialog("Modify Product", "Nothing is selected", "You must select a product before it can be edited",
                    Alert.AlertType.WARNING);
        else
            changeScene((Node)event.getSource(), VIEW_PATH_EDITPRODUCTSCREEN, (EditProductController controller) -> {
                controller.applyModel(sourceProduct);
            });
    }

    @FXML
    void onDeleteProductButtonClick(ActionEvent event) {
        model.Product product = productsTableView.getSelectionModel().getSelectedItem();
        if (product == null) {
            ModelHelper.showNotificationDialog("Delete Product", "Nothing is selected", "You must select a product before it can be deleted",
                    Alert.AlertType.WARNING);
            return;
        }
        
        ObservableList<model.Product> allProducts = model.Inventory.getAllProducts();
        ArrayList<model.Part> toBeOrphaned = new ArrayList<model.Part>();
        for (model.Part part: product.getAllAssociatedParts()) {
            if (allProducts.filtered(p -> p.getAllAssociatedParts().contains(part)).size() < 2)
                toBeOrphaned.add(part);
        }
        Optional<ButtonType> result = ModelHelper.showConfirmationDialog("Delete Product", "Confirm Delete Part \"" + product.getName() + "\"",
                "This action cannot be undone!\n\nAre you sure you want to delete this product?", Alert.AlertType.CONFIRMATION);
        if (result.isPresent() && result.get() == ButtonType.YES) {
            if (toBeOrphaned.size() > 0) {
                result = ModelHelper.showConfirmationDialog("Delete Product", "Confirm Delete Part \"" + product.getName() + "\"",
                        ((toBeOrphaned.size() == 1) ? "1 part" : String.valueOf(toBeOrphaned.size()) + " parts") +
                        "will not belong to any product after this product is deleted.\r\nDo you want to delete those parts as well?",
                        Alert.AlertType.CONFIRMATION, true);
                if (!(result.isPresent() && result.get() != ButtonType.CANCEL))
                    return;
                if (result.get() == ButtonType.YES)
                    for (model.Part part: toBeOrphaned)
                        model.Inventory.deletePart(part);
            }
            model.Inventory.deleteProduct(product);
        }
    }

    @FXML
    void onExitClick(ActionEvent event) { ((Button)event.getSource()).getScene().getWindow().hide(); }

    public static <T> void changeScene(Node eventSource, String path, java.util.function.Consumer<T> initializeController) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainScreenController.class.getResource(path));
        Stage stage = (Stage)eventSource.getScene().getWindow();
        try {
            stage.setScene(new Scene(loader.load()));
            T controller = loader.getController();
            if (initializeController != null)
                initializeController.accept(controller);
        } catch (IOException ex) {
            Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        stage.show();
    }
    
    public static void changeScene(Node eventSource, String path) {
        changeScene(eventSource, path, null);
    }
    
    @FXML
    void onPartsSearchButtonClick(ActionEvent event) {
        if (showingPartSearchResults) {
            partsTableView.setItems(model.Inventory.getAllParts());
            noPartMatchesLabel.setVisible(false);
            partsTableView.setVisible(true);
            partsSearchButton.setText("Search");
            showingPartSearchResults = false;
            partsSearchTextField.setDisable(false);
            partsSearchTextField.setText("");
            return;
        }
        
        String searchText = partsSearchTextField.getText().trim().toLowerCase();
        if (searchText.length() == 0) {
            ModelHelper.showNotificationDialog("No text", "Nothing to search", "Enter a name to search for...", Alert.AlertType.WARNING);
            return;
        }
        ObservableList<model.Part> searchResults = FXCollections.observableArrayList();
        for (model.Part part : Inventory.getAllParts()) {
            if (part.getName().toLowerCase().contains(searchText))
                searchResults.add(part);
        }
        if (searchResults.isEmpty()) {
            noPartMatchesLabel.setVisible(true);
            partsTableView.setVisible(false);
        } else {
            noPartMatchesLabel.setVisible(false);
            partsTableView.setVisible(true);
            partsTableView.setItems(searchResults);
        }
        partsSearchTextField.setDisable(true);
        partsSearchButton.setText("Show All");
        showingPartSearchResults = true;
    }

    @FXML
    void onProductsSearchButtonClick(ActionEvent event) {
        if (showingProductSearchResults) {
            productsTableView.setItems(model.Inventory.getAllProducts());
            noProductMatchesLabel.setVisible(false);
            productsTableView.setVisible(true);
            productsSearchButton.setText("Search");
            showingProductSearchResults = false;
            productsSearchTextField.setDisable(false);
            productsSearchTextField.setText("");
            return;
        }
        String searchText = productsSearchTextField.getText().trim().toLowerCase();
        if (searchText.length() == 0) {
            ModelHelper.showNotificationDialog("No text", "Nothing to search", "Enter a name to search for...", Alert.AlertType.WARNING);
            return;
        }
        ObservableList<model.Product> searchResults = FXCollections.observableArrayList();
        for (model.Product product : Inventory.getAllProducts()) {
            if (product.getName().toLowerCase().contains(searchText))
                searchResults.add(product);
        }
        if (searchResults.isEmpty()) {
            noProductMatchesLabel.setVisible(true);
            productsTableView.setVisible(false);
        } else {
            noProductMatchesLabel.setVisible(false);
            productsTableView.setVisible(true);
            productsTableView.setItems(searchResults);
        }
        productsSearchTextField.setDisable(true);
        productsSearchButton.setText("Show All");
        showingProductSearchResults = true;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        partIdTableColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        partInventoryLevelTableColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        partNameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        partPriceTableColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        productIdTableColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        productInventoryLevelTableColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        productNameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        productPriceTableColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        partsTableView.setItems(model.Inventory.getAllParts());
        productsTableView.setItems(model.Inventory.getAllProducts());
    }
}
