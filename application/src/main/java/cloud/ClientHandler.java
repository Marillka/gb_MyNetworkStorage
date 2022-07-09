package cloud;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import models.requests.AuthRequest;
import models.requests.DeleteFileRequest;
import models.responses.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;


public class ClientHandler extends ChannelInboundHandlerAdapter {

    private final ClientService clientService = new ClientService();
    private final Network network = Network.getInstance();
    private LoginPanelController loginPanelController;
    private ClientPanelController clientPanelController;
    private ServerPanelController serverPanelController;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        loginPanelController = (LoginPanelController) ControllerRegistry.getControllerObject(LoginPanelController.class);

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object messageFromServer) throws Exception {
        BasicResponse responseFromServer = (BasicResponse) messageFromServer;

        if (responseFromServer instanceof AuthResponse && (((AuthResponse) responseFromServer).isAuthOK())) {
            ClientInfo.setMaxFolderDepth(((AuthResponse) responseFromServer).getMaxFolderDepth());
            ClientInfo.setRootDirectoryOnServerStr(((AuthResponse) responseFromServer).getRootDirectoryOnServerStr());
            clientService.loginSuccessful();
//            network.sendRequest(new GetFirstFileListRequest(
//                    new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword())
//            ));

            return;
        }
        if (responseFromServer instanceof AuthResponse && !(((AuthResponse) responseFromServer).isAuthOK())) {
            Platform.runLater(() -> {
                loginPanelController.setErrorLabel("Неверный логин или пароль");
            });
            return;
        }

        if (responseFromServer instanceof RegistrationResponse) {
            RegistrationResponse response = (RegistrationResponse) responseFromServer;

            if (response.isRegOk()) {
                ClientInfo.setMaxFolderDepth(response.getMaxFolderDepth());
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Клиент успешно зарегистрирован", ButtonType.OK);
                    alert.showAndWait();
                });
            }

            if (!response.isRegOk()) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Клиент с таким ником уже существует", ButtonType.OK);
                    alert.showAndWait();
                });

            }

            return;
        }

        if (responseFromServer instanceof GetFileListResponse) {
            GetFileListResponse response = (GetFileListResponse) responseFromServer;
            clientPanelController = (ClientPanelController) ControllerRegistry.getControllerObject(ClientPanelController.class);
            serverPanelController = (ServerPanelController) ControllerRegistry.getControllerObject(ServerPanelController.class);
            Platform.runLater(() -> {
                try {
                    serverPanelController.updateServerList((response));
                } catch (NullPointerException e) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось открыть файл по адресу " + response);
                        alert.showAndWait();
                    });
                }

            });

            return;
        }

        if (responseFromServer instanceof UploadFileResponse) {
            UploadFileResponse response = (UploadFileResponse) responseFromServer;
            String pathToClientFileStr = response.getPathToClientFileStr();
            String clientFileName = String.valueOf(Path.of(pathToClientFileStr).getFileName());
            String pathToServerDirStr = response.getPathToServerDirStr();
            String pathToServerFileStr = pathToServerDirStr + "\\" + clientFileName;
            boolean needToDeleteFile = response.isNeedToDeleteFile();
            boolean noFreeStorage = response.isNoFreeStorage();

            if (!needToDeleteFile && !noFreeStorage) {
                FileMessage fileMessage = new FileMessage(Paths.get(pathToClientFileStr), pathToServerDirStr);
                ctx.writeAndFlush(fileMessage);
                return;
            } else if (noFreeStorage) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Недостаточно свободного места", ButtonType.OK);
                    alert.showAndWait();
                });
            } else if (needToDeleteFile) {
                Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Удалить файл?");
                            alert.setHeaderText("Такой файл существует, удалить файл?");
                            alert.setContentText(pathToServerFileStr);
                            Optional<ButtonType> option = alert.showAndWait();

                            if (option.get() == ButtonType.OK) {
                                ctx.writeAndFlush(new DeleteFileRequest(
                                        new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword()),
                                        Path.of(pathToServerFileStr)));
                            }
                        }
                );
            }
        }

        if (messageFromServer instanceof FileMessage) {
            FileMessage fileMessage = (FileMessage) messageFromServer;

            Path pathToClientDir = ClientInfo.getCurrentClientPath();
            String fileName = fileMessage.getName();
            byte[] fileData = fileMessage.getData();
            long fileSize = fileMessage.getSize();

            File file = new File((pathToClientDir + "\\" + fileName));
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                outputStream.write(fileData);
            }

            clientPanelController.updateClientList(pathToClientDir);
            ClientInfo.setCurrentClientPath(pathToClientDir);

            return;
        }

        if (responseFromServer instanceof CreateDirResponse) {
            CreateDirResponse createDirResponse = (CreateDirResponse) responseFromServer;
            Platform.runLater(() -> {
                if (createDirResponse.isCreateDirOk()) {
//                    ServerPanelController serverPanelController = (ServerPanelController) ControllerRegistry.getControllerObject((ServerPanelController.class));
                    serverPanelController.updateServerList(createDirResponse.getGetFileListResponse());
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Ну удалось создать папку на сервере", ButtonType.OK);
                    alert.showAndWait();
                }
            });
            return;
        }

        if (responseFromServer instanceof DeleteFileResponse) {
            DeleteFileResponse deleteFileResponse = (DeleteFileResponse) responseFromServer;

            if (deleteFileResponse.isDeleteFileOk()) {
                serverPanelController.updateServerList(deleteFileResponse.getGetFileListResponse());
            }

            if (!deleteFileResponse.isDeleteFileOk()) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось удалить выбранный файл", ButtonType.OK);
                    alert.showAndWait();
                });
                serverPanelController.updateServerList(deleteFileResponse.getGetFileListResponse());
            }

        }

        if (responseFromServer instanceof FailToOpenDirectoryResponse) {
            FailToOpenDirectoryResponse failToOpenDirectoryResponse = (FailToOpenDirectoryResponse) responseFromServer;
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось открыть директорию по адресу " + failToOpenDirectoryResponse.getPathToOpenStr(), ButtonType.OK);
                alert.showAndWait();
            });

        }

        if (responseFromServer instanceof AccessErrorResponse) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Ошибка доступа", ButtonType.OK);
                alert.showAndWait();
            });
        }
    }


}


