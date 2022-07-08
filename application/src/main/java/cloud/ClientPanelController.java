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

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ClientPanelController implements Initializable {

    @FXML
    public ComboBox<String> clientPanelDisksBox;
    @FXML
    public TextField clientPanelPathField;
    @FXML
    public TableView<FileInfo> clientPanelFilesTable;

    ClientPanelController clientPanelController = this;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ControllerRegistry.register(this);

        Image fileImage = new Image("/file-icon.png");
        Image dirImage = new Image("/dir-icon.png");

        TableColumn<FileInfo, ImageView> clientFileTypeColumn = new TableColumn<>();
        clientFileTypeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getType() == FileInfo.FileType.DIRECTORY ? new ImageView(dirImage) : new ImageView(fileImage)));
        clientFileTypeColumn.setPrefWidth(30);

        TableColumn<FileInfo, String> clientFileNameColumn = new TableColumn<FileInfo, String>("Name");
        clientFileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        clientFileNameColumn.setPrefWidth(240);

        TableColumn<FileInfo, Long> clientFileSizeColumn = new TableColumn<>("Size");
        clientFileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty(param.getValue().getSize()));
        clientFileSizeColumn.setCellFactory(column -> {
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
        clientFileSizeColumn.setPrefWidth(120);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TableColumn<FileInfo, String> clientFileDateModifiedColumn = new TableColumn<>("Modification time");
        clientFileDateModifiedColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModifiedTime().format(dtf)));
        clientFileDateModifiedColumn.setPrefWidth(120);

        clientPanelFilesTable.getColumns().addAll(clientFileTypeColumn, clientFileNameColumn, clientFileSizeColumn, clientFileDateModifiedColumn);
        clientPanelFilesTable.getSortOrder().add(clientFileNameColumn);

        clientPanelDisksBox.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            clientPanelDisksBox.getItems().add(p.toString());
        }
//        clientPanelDisksBox.getSelectionModel().select(0);

        clientPanelFilesTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
//                    Path path = Paths.get(clientPanelPathField.getText()).resolve(clientPanelFilesTable.getSelectionModel().getSelectedItem().getFileName());
                    Path path = Path.of(ClientInfo.getCurrentClientPath() + "\\" + (clientPanelFilesTable.getSelectionModel().getSelectedItem().getFileName()));
                    if (Files.isDirectory(path)) {
                        try {
                            updateClientList(path);
                        } catch (Exception e) {
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось открыть директорию на клиенте", ButtonType.OK);
                                alert.showAndWait();
                            });
                        }

//                        ClientInfo.setCurrentClientPath(path);
                    }
                }
            }
        });


        Path rootClientPath = (Paths.get("."));
        ClientInfo.setCurrentClientPath(rootClientPath);
        updateClientList(ClientInfo.getCurrentClientPath());
    }

    public void updateClientList(Path path) {
        Path parentPathStr = path.normalize().toAbsolutePath().getParent();
        clientPanelFilesTable.getItems().clear();

        try {
//            clientPanelPathField.setText(path.normalize().toAbsolutePath().toString());
            clientPanelFilesTable.getItems()
                    .addAll(Files.list(path)
                            .map(FileInfo::new)
                            .collect(Collectors.toList()));
            clientPanelFilesTable.sort();

            ClientInfo.setCurrentClientPath(Path.of(path.normalize().toAbsolutePath().toString()));
            clientPanelPathField.setText(path.normalize().toAbsolutePath().toString());
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось открыть директорию по адресу " + path, ButtonType.OK);
            alert.showAndWait();
            clientPanelFilesTable.getItems().clear();

            try {
                clientPanelPathField.setText(parentPathStr.toString());
                clientPanelFilesTable.getItems()
                        .addAll(Files.list(parentPathStr)
                                .map(FileInfo::new)
                                .collect(Collectors.toList()));
                clientPanelFilesTable.sort();

                ClientInfo.setCurrentClientPath(Path.of(path.normalize().toAbsolutePath().toString()).getParent());
            } catch (IOException | NullPointerException exception) {
                Platform.runLater(() -> {
                    Alert alert1 = new Alert(Alert.AlertType.ERROR, "Не удалось обновить список файлов на клиенте", ButtonType.OK);
                    alert1.showAndWait();
                });
            }
        }
    }

    public void clientPanelButtonUpAction(ActionEvent actionEvent) {
        Path clientUpperPath = Paths.get(clientPanelPathField.getText()).getParent();
        if (clientUpperPath != null) {
            updateClientList(clientUpperPath);
        }
    }

    public void clientPanelSelectDickAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateClientList(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    public String getSelectedFileName() {
        if (clientPanelFilesTable.getSelectionModel().getSelectedItem() == null) {
            return null;
        } else {
            String selectedFileName = clientPanelFilesTable.getSelectionModel().getSelectedItem().getFileName();
            return selectedFileName;
        }
    }

    public String getCurrentPath() {
        return clientPanelPathField.getText();
    }

    public void clientPanelButtonRefreshAction(ActionEvent actionEvent) {
//        updateClientList(Paths.get(clientPanelPathField.getText()));
        updateClientList(ClientInfo.getCurrentClientPath());
    }


    public void clientPanelButtonCreateNewDirectory(ActionEvent actionEvent) {
        TextInputDialog textInputDialog = new TextInputDialog("Новая директория");
        textInputDialog.setTitle("Создание новой директории на клиенте");
        textInputDialog.setHeaderText(null);
        textInputDialog.setContentText("Введите имя");
        Optional<String> resultDialog = textInputDialog.showAndWait();

        if (resultDialog.isPresent()) {
//                String nameOfNewDir = resultDialog.get().replaceAll("[A-Za-zA-Яа-я0-9]", "");
            String nameOfNewDir = resultDialog.get();

            try {
                Path pathToNewDir = Paths.get(ClientInfo.getCurrentClientPath() + "\\" + nameOfNewDir);

                if (Files.exists(pathToNewDir)) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Директория '" + pathToNewDir + "' уже существует");
                        alert.showAndWait();
                    });
                    return;
                }

                if (!Files.exists(pathToNewDir)) {
                    try {
                        Files.createDirectory(pathToNewDir);
                        clientPanelController.updateClientList(ClientInfo.getCurrentClientPath());
                    } catch (InvalidPathException e) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось создать директорию", ButtonType.OK);
                            alert.showAndWait();
                        });
                    } catch (RuntimeException | IOException error) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось создать директорию", ButtonType.OK);
                            alert.showAndWait();
                        });
                    }
                }

            } catch (RuntimeException e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось создать директорию", ButtonType.OK);
                    alert.showAndWait();
                });
            }
            return;
        }
    }
}

