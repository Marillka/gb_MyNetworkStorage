package cloud;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.Getter;
import models.requests.AuthRequest;
import models.requests.BasicRequest;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginPanelController implements Initializable {

    @Getter
    private final Network network = Network.getInstance();

    @FXML
    public TextField loginField;

    @FXML
    public PasswordField passwordField;

    @FXML
    public Label badLogin;

    public void buttonExitAction(ActionEvent actionEvent) {
        network.close();
        Platform.exit();
    }

    public void buttonLoginAction(ActionEvent actionEvent) throws InterruptedException, IOException {
        String log = loginField.getText();
        String pass = passwordField.getText();

        if (log == null || log.isBlank() || log.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Username not specified", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        if (pass == null || pass.isBlank() || pass.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Password not specified", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        BasicRequest request = new AuthRequest(log, pass);
        network.sendRequest(request);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ControllerRegistry.register(this);
    }

    public void buttonRegistrationAction(ActionEvent actionEvent) {
        // кнопка регистрации
    }

    public void setVisibleBadLogin() {
        badLogin.setVisible(true);
    }

    public void setVisibleBusyRegistration() {
        // сделать busyLogin
    }

    public void closeNetwork() {
        network.close();
    }

}
