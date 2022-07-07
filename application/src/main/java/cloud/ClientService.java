package cloud;



import javafx.application.Platform;
import models.responses.GetFileListResponse;


import java.io.IOException;


// выносим логику в сервисный слой (где происходит бизнес логика)
// network работает с сетью
// контроллеры обрабатывают фронт
// а сервисный слой это все связывает и какую то логику реализует в себе.
public class ClientService {

    public void loginSuccessful() {
            Platform.runLater(() -> {
                try {
                    App.setRoot("mainPanel");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
    }

    public void putServerFileList(GetFileListResponse fileListResponse) {
        ServerPanelController serverControllerObject = (ServerPanelController) ControllerRegistry.getControllerObject(ServerPanelController.class);
       serverControllerObject.updateServerList(fileListResponse);
    }



}
