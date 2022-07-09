package cloud;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;


import models.FileInfo;
import models.requests.*;
import models.responses.GetFileListResponse;

import java.net.URL;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

import java.util.Optional;
import java.util.ResourceBundle;

public class ServerPanelController implements Initializable {
    @FXML
    TableView<FileInfo> serverPanelFilesTable;
    @FXML
    TextField serverPanelPathField;
    @FXML
    Button serverPanelButtonUp;

    private final Network network = Network.getInstance();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ControllerRegistry.register(this);

        Image fileImage = new Image("/file-icon.png");
        Image dirImage = new Image("/dir-icon.png");

        TableColumn<FileInfo, ImageView> serverFileTypeColumn = new TableColumn<>();
        serverFileTypeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getType() == FileInfo.FileType.DIRECTORY ? new ImageView(dirImage) : new ImageView(fileImage)));
        serverFileTypeColumn.setPrefWidth(30);

        TableColumn<FileInfo, String> serverFileNameColumn = new TableColumn<FileInfo, String>("Name");
        serverFileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        serverFileNameColumn.setPrefWidth(240);

        TableColumn<FileInfo, Long> serverFileSizeColumn = new TableColumn<>("Size");
        serverFileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty(param.getValue().getSize()));
        serverFileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        if (item == -1L) {
                            text = "";
                        }
                        setText(text);
                    }
                }
            };
        });
        serverFileSizeColumn.setPrefWidth(120);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TableColumn<FileInfo, String> serverFileDateModifiedColumn = new TableColumn<>("Modification time");
        serverFileDateModifiedColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModifiedTime().format(dtf)));
        serverFileDateModifiedColumn.setPrefWidth(120);

        serverPanelFilesTable.getColumns().addAll(serverFileTypeColumn, serverFileNameColumn, serverFileSizeColumn, serverFileDateModifiedColumn);
        serverPanelFilesTable.getSortOrder().add(serverFileNameColumn);


        try {
            serverPanelFilesTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        if (serverPanelFilesTable.getSelectionModel().getSelectedItem().getType() == FileInfo.FileType.DIRECTORY) {
//                            Path pathToOpen = Paths.get(serverPanelPathField.getText() + "\\" + serverPanelFilesTable.getSelectionModel().getSelectedItem().getFileName());
                            Path pathToOpen = Paths.get(ClientInfo.getCurrentServerPath() + "\\" + serverPanelFilesTable.getSelectionModel().getSelectedItem().getFileName());
                            try {
                                network.sendRequest(new OpenDirRequest(
                                        new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword()),
                                        pathToOpen
                                ));
//                                ClientInfo.setCurrentServerPath(pathToOpen);
                            } catch (InterruptedException e) {
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.ERROR, "Ошибка открытия директории", ButtonType.OK);
                                    alert.showAndWait();
                                });
                            }
                        }
                    }
                }
            });
        } catch (NullPointerException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Файл не выбран", ButtonType.OK);
                alert.showAndWait();
            });
        }


        try {
            network.sendRequest(new GetFirstFileListRequest(
                    new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword())
            ));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    public void updateServerList(GetFileListResponse fileListResponse) {
        try {
            MainController mainController = (MainController) ControllerRegistry.getControllerObject(MainController.class);
            String currentServerPathStr = fileListResponse.getPathOfFileInListStr();
            serverPanelPathField.setText(currentServerPathStr);
            ClientInfo.setCurrentServerPath(Paths.get(currentServerPathStr));
//            serverPanelButtonUp.setDisable(currentServerPathStr.equals(ClientInfo.getLogin() + "\\")); //  "log\"
//            mainController.createDirButton.setDisable(!checkMaxFolderDepth());
            serverPanelFilesTable.getItems().clear();
            serverPanelFilesTable.getItems().addAll(fileListResponse.getFileListResponse());
            serverPanelFilesTable.sort();
        } catch (RuntimeException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось обновить список файлов", ButtonType.OK);
                alert.showAndWait();
                serverPanelFilesTable.getItems().addAll(fileListResponse.getParentFileListResponse());
            });
        }
    }

    public void serverPanelButtonUpAction(ActionEvent actionEvent) {
//        Path upperPath = Paths.get(serverPanelPathField.getText()).getParent();
        Path upperPath = ClientInfo.getCurrentServerPath().getParent();
        if (upperPath != null) {
            try {
                network.sendRequest(new GetFileListRequest(
                        new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword()),
                        upperPath));
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Не удалось обновить список файлов");
            }
        }
    }


    public void serverPanelButtonRefreshAction(ActionEvent actionEvent) {
        try {
            network.sendRequest(new GetFileListRequest(
                    new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword()),
//                    Paths.get(serverPanelPathField.getText())
                    ClientInfo.getCurrentServerPath()
            ));
        } catch (InterruptedException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось обновить список файлов");
                alert.showAndWait();
            });
            System.out.println("Не удалось обновить список файл");
        }
    }





    public void serverPanelButtonCreateNewDirectory(ActionEvent actionEvent) {
        TextInputDialog textInputDialog = new TextInputDialog("Новая директория");
        textInputDialog.setTitle("Создание новой директории на сервере");
        textInputDialog.setHeaderText(null);
        textInputDialog.setContentText("Введите имя");
        Optional<String> resultDialog = textInputDialog.showAndWait();

        try {
            if (resultDialog.isPresent()) {
                String nameOfNewDir = resultDialog.get();
                Path pathToNewDir = Paths.get(ClientInfo.getCurrentServerPath() + "\\" + nameOfNewDir);

                try {
                    network.sendRequest(
                            new CreateDirRequest(
                                    new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword()),
                                    pathToNewDir));
                } catch (InterruptedException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось создать директорию на сервере", ButtonType.OK);
                    alert.showAndWait();
                }

            }
        } catch (RuntimeException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось создать директорию на сервере", ButtonType.OK);
                alert.showAndWait();
            });
        }


    }

    public void serverPanelButtonGoToTheRootDir(ActionEvent actionEvent) {
        try {
            network.sendRequest(new GetFileListRequest(
                    new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword()),
                    Path.of(ClientInfo.getRootDirectoryOnServerStr())
            ));
        } catch (InterruptedException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось перейти в начальную директорию на сервере");
                alert.showAndWait();
            });
        }
    }

    public boolean checkMaxFolderDepth() {
        int depth = 0;
        String rootDir = ClientInfo.getLogin();
        Path parentPath = ClientInfo.getCurrentServerPath();
        while (!parentPath.toString().equals(rootDir)) {
            depth++;
            parentPath = parentPath.getParent();
        }
        return depth < ClientInfo.getMaxFolderDepth();
    }


    public String getSelectedFilename() {
        if (serverPanelFilesTable == null) {
            return null;
        }
        if (!serverPanelFilesTable.isFocused()) {
            return null;
        }
        return serverPanelFilesTable.getSelectionModel().getSelectedItem().getFileName();
    }

    public String getCurrentPath() {
//        return serverPanelPathField.getText();
        return String.valueOf(ClientInfo.getCurrentServerPath());
    }
}
