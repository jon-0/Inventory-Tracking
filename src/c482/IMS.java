package c482;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Inventory;


public class IMS extends Application {
    
    // override
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/MainScreen.fxml"));
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        model.Product product = new model.Product(-1, "Toy Soldier", 25.99, 112, 50, 300);
        Inventory.addProduct(product);
        model.Part soldierHead = new model.Outsourced(-1, "Soldier Head", 1.99, 150, 50, 300, "Soldiers R Us");
        product.addAssociatedPart(soldierHead);
        product.addAssociatedPart(new model.Outsourced(-1, "Soldier Torso", 5.55, 296, 10, 100, "Soldiers R Us"));
        product.addAssociatedPart(new model.Outsourced(-1, "Soldier Leg", 3.20, 300, 5000, 30000, "3D Soldier Printing"));
        model.Part smallCombatBoots = new model.Outsourced(-1, "Small Combat Boots", 9.99, 500, 12, 400, "Outside Parts Co.");
        product.addAssociatedPart(smallCombatBoots);
        model.Part usCombatHelmet = new model.InHouse(-1, "US Combat Helmet", 10.49, 600, 300, 1400, 2);
        product.addAssociatedPart(usCombatHelmet);
        
        product = new model.Product(-1, "Xbox 360", 350.99, 104, 12, 400);
        Inventory.addProduct(product);
        model.Part coolingFan = new model.Outsourced(-1, "Cooling Fan", 16.75, 5, 12, 100, "The Coolers");
        product.addAssociatedPart(coolingFan);
        product.addAssociatedPart(new model.Outsourced(-1, "USB Port", 2.75, 5, 12, 100, "Port Makers"));
        model.Part hardDrive = new model.Outsourced(-1, "Hard Drive", 50.76, 12, 1, 50, "Storage Empire");
        product.addAssociatedPart(hardDrive);
        model.Part powerCable = new model.Outsourced(-1, "Power Cable", 45.8, 84, 10, 300, "Power and Light Co.");
        product.addAssociatedPart(powerCable);
        
        product = new model.Product(-1, "Playstation 3", 300.00, 199, 15, 200);
        Inventory.addProduct(product);
        product.addAssociatedPart(powerCable);
        product.addAssociatedPart(hardDrive);
        product.addAssociatedPart(coolingFan);
        
        launch(args);
    }
    
}
