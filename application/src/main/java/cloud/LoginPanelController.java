package cloud;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.Getter;
import models.requests.AuthRequest;
import models.requests.RegistrationRequest;


import java.net.URL;
import java.util.ResourceBundle;

public class LoginPanelController implements Initializable {

    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public Label errorLabel;

    @Getter
    private final Network network = Network.getInstance();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ControllerRegistry.register(this);
        loginField.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    loginButtonAction(new ActionEvent());
                    keyEvent.consume();
                }
            }
        });
        passwordField.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    loginButtonAction(new ActionEvent());
                    keyEvent.consume();
                }
            }
        });
    }

    public void setErrorLabel(String authErrorLabel) {
        this.errorLabel.setText(authErrorLabel);
    }

    public void loginButtonAction(ActionEvent actionEvent) {
        String log = loginField.getText().trim();
        String password = passwordField.getText().trim();
        if (log == null || log.isBlank() || log.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Введите логин", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        if (password == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Введите пароль", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        ClientInfo.setLogin(log);
        ClientInfo.setPassword(password);
        try {
            network.sendRequest(new AuthRequest(log, password));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void registrationButtonAction(ActionEvent actionEvent) {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        if (login.isEmpty() || login.isBlank()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Имя пользователя не указанно", ButtonType.OK);
                alert.showAndWait();
            });
            return;
        }
        if (password.isEmpty() || password.isBlank()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Пароль не указан", ButtonType.OK);
                alert.showAndWait();
            });
            return;
        }
        try {
            network.sendRequest(new RegistrationRequest(login, password));
        } catch (InterruptedException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось зарегестрировать пользователя", ButtonType.OK);
                alert.showAndWait();
            });
        }
    }

    public void exitButtonAction(ActionEvent actionEvent) {
        network.close();
        Platform.exit();
    }
}
