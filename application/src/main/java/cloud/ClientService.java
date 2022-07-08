package cloud;

import javafx.application.Platform;

import java.io.IOException;

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

}
