package cloud;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;


import models.requests.OpenDirRequest;
import models.requests.OpenUpperDirRequest;

import java.io.File;

import java.net.URL;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ServerPanelController implements Initializable {

    @FXML
    TableView<FileInfo> serverPanelFilesTable;

    @FXML
    TextField serverPanelPathField;

    private Network network;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        ControllerRegistry.register(this);

        LoginPanelController controller = (LoginPanelController) ControllerRegistry.getControllerObject(LoginPanelController.class);
        network = controller.getNetwork();


        TableColumn<FileInfo, String> serverFileTypeColumn = new TableColumn<FileInfo, String>("Type");
        serverFileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        serverFileTypeColumn.setPrefWidth(60);

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
        serverPanelFilesTable.getSortOrder().add(serverFileTypeColumn);

        serverPanelFilesTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    if (serverPanelFilesTable.getSelectionModel().getSelectedItem().getType() == FileInfo.FileType.DIRECTORY) {
                        System.out.println("Кликнул 2 раза по файлу");
                        Path path = Paths.get(serverPanelPathField.getText() + "\\" + serverPanelFilesTable.getSelectionModel().getSelectedItem().getFileName());
                        try {
                            network.sendRequest(new OpenDirRequest(path));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });


    }

    public void updateServerList(String path, List<File> serverItemsList) {
        serverPanelPathField.setText(path);
        serverPanelFilesTable.getItems().clear();
        try {
            List<FileInfo> serverFileList = serverItemsList.stream()
                    .map(File::toPath)
                    .map(FileInfo::new)
                    .collect(Collectors.toList());
            serverPanelFilesTable.getItems().addAll(serverFileList);
            serverPanelFilesTable.sort();
        } catch (NullPointerException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update the file list", ButtonType.OK);
        }
    }

    public void renderServerFileList(List<File> serverItemsList, String pathOfList) {
        updateServerList(pathOfList, serverItemsList);

    }

    public void serverPanelButtonUpAction(ActionEvent actionEvent) {
        String pathStr = serverPanelPathField.getText();
        String upperPathStr = Paths.get(pathStr).toString();
        try {
            network.sendRequest(new OpenUpperDirRequest(upperPathStr));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

}
