package cloud;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;


import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import models.requests.*;
import models.responses.DeleteFileResponse;
import models.responses.GetFileListResponse;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;
import org.apache.commons.io.FileUtils;


public class MainController implements Initializable {
    @FXML
    public VBox clientPanel;
    @FXML
    public VBox serverPanel;

    ServerPanelController serverPanelController;
    ClientPanelController clientPanelController;

    Network network = Network.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ControllerRegistry.register(this);
        clientPanelController = (ClientPanelController) ControllerRegistry.getControllerObject(ClientPanelController.class);
        serverPanelController = (ServerPanelController) ControllerRegistry.getControllerObject(ServerPanelController.class);
    }

    public void uploadDownloadButtonAction(ActionEvent actionEvent) {
        if (clientPanelController.getSelectedFileName() == null && serverPanelController.getSelectedFilename() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Файл не выбран", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // загрузка не сервер
        if (clientPanelController.getSelectedFileName() != null && clientPanelController.clientPanelFilesTable.isFocused()) {
            String fileName = clientPanelController.getSelectedFileName();
            Path pathToFileOnClient = Paths.get(ClientInfo.getCurrentClientPath() + "\\" + fileName);
            Path pathToServerDir = Paths.get(String.valueOf(ClientInfo.getCurrentServerPath()));
            if (!Files.isDirectory(pathToFileOnClient)) {
                File file = new File(String.valueOf(pathToFileOnClient));
                try {
                    network.sendRequest(
                            new UploadFileRequest(
                                    new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword()),
                                    pathToServerDir,
                                    pathToFileOnClient,
                                    file.length())

                    );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось отправить файл", ButtonType.OK);
                        alert.showAndWait();
                    });
                }
            } else {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Выберите файл, а не директорию", ButtonType.OK);
                    alert.showAndWait();
                });
            }
            return;
        }

        //скачивание файла с сервера
        if (serverPanelController.getSelectedFilename() != null && serverPanelController.serverPanelFilesTable.isFocused()) {
            String fileNameToDownload = serverPanelController.getSelectedFilename();
            Path pathToFileOnServer = Paths.get(ClientInfo.getCurrentServerPath() + "\\" + fileNameToDownload);
            Path pathToCurrentClientDirectory = ClientInfo.getCurrentClientPath().toAbsolutePath().normalize();
            Path pathToClientFile = Path.of(pathToCurrentClientDirectory + "\\" + Paths.get(fileNameToDownload));

            if (Files.exists(pathToClientFile)) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Такой файл уже существует");
                alert.setHeaderText("Файл " + fileNameToDownload + " уже существует. Заменить файл?");
                alert.setContentText(pathToClientFile.toAbsolutePath().normalize().toString());
                Optional<ButtonType> option = alert.showAndWait();
                if (option.isPresent()) {
                    if (option.get() == ButtonType.OK) {
                        try {
                            Files.delete(pathToClientFile);
                            clientPanelController.updateClientList(ClientInfo.getCurrentClientPath());
                            network.sendRequest(new DownloadFileRequest(
                                    new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword()),
                                    pathToFileOnServer
                            ));
                        } catch (IOException | InterruptedException e) {
                            Platform.runLater(() -> {
                                Alert alert1 = new Alert(Alert.AlertType.ERROR, "Не удалось удалить файл на клиенте", ButtonType.OK);
                                alert1.showAndWait();
                            });
                        }
                    }
                }
            }

            try {
                network.sendRequest(
                        new DownloadFileRequest(
                                new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword()),
                                pathToFileOnServer));
            } catch (InterruptedException e) {
                Platform.runLater(() -> {
                    Alert alert2 = new Alert(Alert.AlertType.ERROR, "Не удалось скачать файл с сервера", ButtonType.OK);
                    alert2.showAndWait();
                });
            }

            return;

        }
    }

    public void deleteButtonAction(ActionEvent actionEvent) {
        if (clientPanelController.getSelectedFileName() == null && serverPanelController.getSelectedFilename() == null) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Файл для удаления не выбран", ButtonType.OK);
                alert.showAndWait();
            });
            return;
        }

        if (clientPanelController.getSelectedFileName() != null && clientPanelController.clientPanelFilesTable.isFocused()) {
            String currentClientPath = ClientInfo.getCurrentClientPath().toString();
            String fileNameToDelete = clientPanelController.getSelectedFileName();
            Path pathToDeleteFile = Paths.get(currentClientPath + "\\" + fileNameToDelete);
            if (!Files.isDirectory(pathToDeleteFile)) {
                try {
                    Files.delete(pathToDeleteFile);
                    clientPanelController.updateClientList(ClientInfo.getCurrentClientPath());
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Выбранный файл не может быть удален");
                        alert.showAndWait();
                    });
                }
            } else {
                try {
                    FileUtils.deleteDirectory(new File(String.valueOf(pathToDeleteFile)));
                    clientPanelController.updateClientList(ClientInfo.getCurrentClientPath());
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Выбранный файл не может быть удален");
                        alert.showAndWait();
                    });
                }
            }
        }

        if (serverPanelController.getSelectedFilename() != null && serverPanelController.serverPanelFilesTable.isFocused()) {
            String currentServerPath = ClientInfo.getCurrentServerPath().toString();
//            String fileNameToDelete = serverPanelController.getSelectedFilename();
//            serverPanelFilesTable.getSelectionModel().getSelectedItem().getFileName();
            String fileNameToDelete = serverPanelController.serverPanelFilesTable.getSelectionModel().getSelectedItem().getFileName();
            Path pathToDeleteFile = Paths.get(currentServerPath + "\\" + fileNameToDelete);
            try {
                network.sendRequest(
                        new DeleteFileRequest(
                                new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword()),
                                pathToDeleteFile
                        )
                );
            } catch (InterruptedException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось удалить файл", ButtonType.OK);
                alert.showAndWait();
            }
        }

    }

    public void exitButtonAction(ActionEvent actionEvent) {
        Platform.exit();
        network.close();
    }


}
