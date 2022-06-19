package cloud;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController {

    @FXML
    VBox serverPanel, clientPanel;

    @FXML
    ClientPanelController clientPanelController;

    @FXML
    ServerPanelController serverPanelController;

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        clientPanelController = (ClientPanelController) clientPanel.getUserData();
        serverPanelController = (ServerPanelController) serverPanel.getUserData();
    }

    public void copyButtonAction(ActionEvent actionEvent) {
        if (clientPanelController.getSelectedFileName() == null && serverPanelController.getSelectedFilename() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No file selected", ButtonType.OK);
            alert.showAndWait();
            return;
        }

    }

    public void moveButtonAction(ActionEvent actionEvent) {

    }

    public void deleteButtonAction(ActionEvent actionEvent) {

    }

    public void exitButtonAction(ActionEvent actionEvent) {
        Platform.exit();
    }


}
