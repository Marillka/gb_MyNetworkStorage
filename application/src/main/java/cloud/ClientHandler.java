package cloud;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import models.requests.AuthRequest;
import models.requests.GetFirstFileListRequest;
import models.responses.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;


public class ClientHandler extends ChannelInboundHandlerAdapter {

    private final ClientService clientService = new ClientService();
    private final Network network = Network.getInstance();
    private LoginPanelController loginPanelController;
    private  ClientPanelController clientPanelController;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        loginPanelController = (LoginPanelController) ControllerRegistry.getControllerObject(LoginPanelController.class);

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object messageFromServer) throws Exception {
        BasicResponse responseFromServer = (BasicResponse) messageFromServer;
        System.out.println("Получено " + responseFromServer.getType());

        // обработка ответов на авторизацию
        if (responseFromServer instanceof AuthResponse && (((AuthResponse) responseFromServer).isAuthOK())) {
            ClientInfo.setMaxFolderDepth(((AuthResponse) responseFromServer).getMaxFolderDepth());
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

        // обработка ответов на регистрацию
        if (responseFromServer instanceof RegistrationResponse && ((RegistrationResponse) responseFromServer).isRegOk()) {
            ClientInfo.setMaxFolderDepth(((RegistrationResponse) responseFromServer).getMaxFolderDepth());
//            clientService.loginSuccessful();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Клиент успешно зарегестрирован", ButtonType.OK);
                alert.showAndWait();
            });
            return;
        }
        if (responseFromServer instanceof RegistrationResponse && !((RegistrationResponse) responseFromServer).isRegOk()) {
            Platform.runLater(() -> {
                loginPanelController.setErrorLabel("Registration failed");
            });
        }

        // обработка ответов на получение листа файлов
        if (responseFromServer instanceof GetFileListResponse) {
            GetFileListResponse response = (GetFileListResponse) responseFromServer;
            ServerPanelController serverPanelController = (ServerPanelController) ControllerRegistry.getControllerObject(ServerPanelController.class);
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
            clientPanelController = (ClientPanelController) ControllerRegistry.getControllerObject(ClientPanelController.class);
            return;
        }

        // обработка ответов на загрузку файла на сервер
        if (responseFromServer instanceof UploadFileResponse) {
            UploadFileResponse response = (UploadFileResponse) responseFromServer;
            String pathToClientFileStr = response.getPathToClientFileStr();
            String pathToServerFileStr = response.getPathToServerDirStr();
            boolean needToDeleteFile = response.isNeedToDeleteFile();
            boolean noFreeStorage = response.isNoFreeStorage();

            if (!needToDeleteFile && !noFreeStorage) {
                FileMessage fileMessage = new FileMessage(Paths.get(pathToClientFileStr), pathToServerFileStr);
                ctx.writeAndFlush(fileMessage);
                return;
            }


            if (noFreeStorage) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Недостаточно свободного места", ButtonType.OK);
                    alert.showAndWait();
                });
                return;
            }

//            if (needToDeleteFile) {
//                Platform.runLater(() -> {
//                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//                    alert.setTitle("Заменить файл?");
//                    alert.setHeaderText("Такой файл существует, заменить файл?");
//                    alert.setContentText(pathToServerFileStr);
//                    Optional<ButtonType> option = alert.showAndWait();
//                    if (option.get() == ButtonType.OK) {
//                        ctx.writeAndFlush(new Dele)
//                });
//
//            }
        }

        // обработка получения файла
        if (messageFromServer instanceof FileMessage) {
            FileMessage fileMessage = (FileMessage) messageFromServer;

//            Path pathToClientDir = Paths.get(clientPanelController.getCurrentPath());
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

        // обработка ответов на создание директории на сервере
        if (responseFromServer instanceof CreateDirResponse) {
            CreateDirResponse createDirResponse = (CreateDirResponse) responseFromServer;
            Platform.runLater(() -> {
                if (createDirResponse.isCreateDirOk()) {
                    ServerPanelController serverPanelController = (ServerPanelController) ControllerRegistry.getControllerObject((ServerPanelController.class));
                    serverPanelController.updateServerList(createDirResponse.getGetFileListResponse());
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Ну удалось создать папку на сервере", ButtonType.OK);
                    alert.showAndWait();
                }
            });
            return;
        }

        // Обработка ответов на удаление файла
        if (responseFromServer instanceof DeleteFileResponse) {
            DeleteFileResponse deleteFileResponse = (DeleteFileResponse) responseFromServer;
            Platform.runLater(() -> {
                if (deleteFileResponse.isDeleteFileOk()) {
                    ServerPanelController serverPanelController = (ServerPanelController) ControllerRegistry.getControllerObject(ServerPanelController.class);
                    serverPanelController.updateServerList(deleteFileResponse.getGetFileListResponse());
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось удалить выбранный файл", ButtonType.OK);
                    alert.showAndWait();
                }
            });
            return;
        }

        if (responseFromServer instanceof FailToOpenDirectoryResponse) {
            FailToOpenDirectoryResponse failToOpenDirectoryResponse = (FailToOpenDirectoryResponse) responseFromServer;
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось открыть директорию по адресу " + failToOpenDirectoryResponse.getPathToOpenStr(), ButtonType.OK);
                alert.showAndWait();
            });

        }
    }


}


