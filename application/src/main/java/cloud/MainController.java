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
import models.responses.GetFileListResponse;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;


public class MainController implements Initializable {
    @FXML
    public VBox clientPanel;
    @FXML
    public VBox serverPanel;
    @FXML
    Button createDirButton;


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
            Path pathToFileOnClient = Paths.get(clientPanelController.getCurrentPath() + "\\" + fileName);
            Path pathToServerDir = Paths.get(serverPanelController.getCurrentPath());
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
        }

        //скачивание файла с сервера
        if (serverPanelController.getSelectedFilename() != null && serverPanelController.serverPanelFilesTable.isFocused()) {
            String fileNameToDownload = serverPanelController.getSelectedFilename();
            Path pathToFileOnServer = Paths.get(serverPanelController.getCurrentPath() + "\\" + fileNameToDownload);
            Path first = ClientInfo.getCurrentClientPath().toAbsolutePath().normalize();
            Path pathToClientFile = Path.of(first + "\\" + Paths.get(fileNameToDownload));
            Path pathToClientDir = ClientInfo.getCurrentClientPath().toAbsolutePath().normalize();

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
                            e.printStackTrace();
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
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert2 = new Alert(Alert.AlertType.ERROR, "Не удалось скачать файл с сервера", ButtonType.OK);
                    alert2.showAndWait();
                });
            }

        }


//            if (!Files.isDirectory(pathToFileOnServer)) {
////                File file = new File(String.valueOf(pathToFileOnServer));
//                try {
//                    network.sendRequest(
//                            new DownloadFileRequest(
//                                    new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword()),
//                                    pathToFileOnServer
//                            )
//                    );
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                Alert alert = new Alert(Alert.AlertType.ERROR, "Choose file, not directory", ButtonType.OK);
//                alert.showAndWait();
//            }
//        }

    }


    public void deleteButtonAction(ActionEvent actionEvent) {
        if (clientPanelController.getSelectedFileName() == null && serverPanelController.getSelectedFilename() == null) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Файл для удаления не выбран", ButtonType.OK);
                alert.showAndWait();
            });

            return;
        }

        if (clientPanelController.getSelectedFileName() != null) {
            Path pathToDeleteFile = Paths.get(clientPanelController.getCurrentPath()).resolve(clientPanelController.getSelectedFileName());
            if (!Files.isDirectory(pathToDeleteFile)) {
                try {
                    Files.delete(pathToDeleteFile);
                    clientPanelController.updateClientList(ClientInfo.getCurrentClientPath());
                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Выбранный файл не может быть удален");
                        alert.showAndWait();
                    });
                }
            }
        }

        if (serverPanelController.getSelectedFilename() != null) {
            Path pathToDeleteFile = Paths.get(serverPanelController.getCurrentPath()).resolve(serverPanelController.getSelectedFilename());
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
                e.printStackTrace();
            }
        }
    }


    public void createDirButtonAction(ActionEvent actionEvent) {

        if (clientPanelController.getSelectedFileName() == null && serverPanelController.getSelectedFilename() == null) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "Выберите где создать директорию.\n" +
                                "На сервере или на клиенте");
                alert.showAndWait();
            });
            return;
        }

        if (clientPanelController.getSelectedFileName() != null) {
            TextInputDialog textInputDialog = new TextInputDialog("Новая директория");
            textInputDialog.setTitle("Создание новой директории на клиенте");
            textInputDialog.setHeaderText(null);
            textInputDialog.setContentText("Введите имя");
            Optional<String> resultDialog = textInputDialog.showAndWait();

            if (resultDialog.isPresent()) {
                String nameOfNewDir = resultDialog.get().replaceAll("[A-Za-zA-Яа-я0-9]", "");
                Path pathToNewDir = Paths.get(ClientInfo.getCurrentClientPath() + "\\" + nameOfNewDir);
                if (Files.exists(pathToNewDir)) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Директория + " + pathToNewDir + " уже существует");
                        alert.showAndWait();
                    });
                    return;
                }

                if (!Files.exists(pathToNewDir)) {
                    try {
                        Files.createDirectory(pathToNewDir);
                        clientPanelController.updateClientList(ClientInfo.getCurrentClientPath());
                    } catch (RuntimeException | IOException e) {
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось создать директорию", ButtonType.OK);
                            alert.showAndWait();
                        });
                    }
                }
                return;
            }
        }
        
        if (serverPanelController.getSelectedFilename() != null) {
            TextInputDialog textInputDialog = new TextInputDialog("Новая директория");
            textInputDialog.setTitle("Создание новой директории на сервере");
            textInputDialog.setHeaderText(null);
            textInputDialog.setContentText("Введите имя");
            Optional<String> resultDialog = textInputDialog.showAndWait();
            if (resultDialog.isPresent()) {
                String nameOfNewDir = resultDialog.get().replaceAll("[A-Za-zA-Яа-я0-9]", "");
                Path pathToNewDir = Paths.get(ClientInfo.getCurrentServerPath() + "\\" + nameOfNewDir);
                try {
                    network.sendRequest(
                            new CreateDirRequest(
                                    new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword()),
                                    pathToNewDir
                            )
                    );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось создать новую директорию", ButtonType.OK);
                        alert.showAndWait();
                    });

                }
            }
            return;
        }

    }

    public void exitButtonAction(ActionEvent actionEvent) {
        Platform.exit();
        network.close();
    }


}
