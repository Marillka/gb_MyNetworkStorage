package cloud;

import javafx.application.Application;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.util.List;

// выносим логику в сервисный слой (где происходит бизнес логика)
// network работает с сетью
// контроллеры обрабатывают фронт
// а сервисный слой это все связывает и какую то логику реализует в себе.
public class ClientService {

    public void loginSuccessful() {
        try {
            App.setRoot("mainPanel");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void putServerFileList(List<File> serverItemsList, String pathOfList) {
        ServerPanelController serverControllerObject = (ServerPanelController) ControllerRegistry.getControllerObject(ServerPanelController.class);
       serverControllerObject.renderServerFileList(serverItemsList, pathOfList);

    }

}
