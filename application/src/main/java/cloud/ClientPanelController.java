package cloud;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ClientPanelController implements Initializable {

    public ComboBox<String> clientPanelDisksBox;

    public TextField clientPanelPathField;

    public TableView<FileInfo> clientPanelFilesTable;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ControllerRegistry.register(this);


        TableColumn<FileInfo, String> clientFileTypeColumn = new TableColumn<FileInfo, String>("Type");
        clientFileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        clientFileTypeColumn.setPrefWidth(60);

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
        clientPanelFilesTable.getSortOrder().add(clientFileTypeColumn);


        clientPanelDisksBox.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            clientPanelDisksBox.getItems().add(p.toString());
        }
        clientPanelDisksBox.getSelectionModel().select(0);

        clientPanelFilesTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    Path path = Paths.get(clientPanelPathField.getText()).resolve(clientPanelFilesTable.getSelectionModel().getSelectedItem().getFileName());
                    if (Files.isDirectory(path)) {
                        updateClientList(path);
                    }
                }
            }
        });
        updateClientList(Paths.get("."));
    }

    public void updateClientList(Path path) {
        clientPanelFilesTable.getItems().clear();
        try {
            clientPanelPathField.setText(path.normalize().toAbsolutePath().toString());
            clientPanelFilesTable.getItems()
                    .addAll(Files.list(path)
                            .map(FileInfo::new)
                            .collect(Collectors.toList()));
            clientPanelFilesTable.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Failed to update file list", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void clientPanelButtonUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(clientPanelPathField.getText()).getParent();
        if (upperPath != null) {
            updateClientList(upperPath);
        }
    }

    public void clientPanelSelectDickAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateClientList(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    public String getSelectedFileName() {
        if (!clientPanelFilesTable.isFocused()) {
            return null;
        }
        return clientPanelFilesTable.getSelectionModel().getSelectedItem().getFileName();
    }


}