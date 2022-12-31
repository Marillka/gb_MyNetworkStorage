package server;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;


import models.requests.*;
import models.responses.*;
import org.apache.commons.io.FileUtils;


public class BasicHandler extends ChannelInboundHandlerAdapter {
    private final DbService dbService = DbService.getInstance();
    private static final int MAX_FOLDER_DEPTH = 10;
    private static long maxUserStorageSize;
    private static final String ROOT_DIR = System.getProperty("user.home") + "\\Network_Storage";

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        if (!Files.exists(Path.of(ROOT_DIR))) {
            try {
                Files.createDirectory(Path.of(ROOT_DIR));
            } catch (RuntimeException e) {
                e.printStackTrace();
                System.out.println("Не удалось создать директорию для пользователей на сервере");
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object messageFromClient) throws Exception {

        if (messageFromClient instanceof AuthRequest) {
            AuthRequest request = (AuthRequest) messageFromClient;

            if (checkAuth(request)) {
                AuthResponse authResponse = new AuthResponse(true);
                authResponse.setMaxFolderDepth(MAX_FOLDER_DEPTH);
                maxUserStorageSize = dbService.getMaxStorageSizeByLogin((request).getLogin());
                authResponse.setRootDirectoryOnServerStr(String.valueOf(Path.of(ROOT_DIR + "\\" + request.getLogin())));
                ctx.writeAndFlush(authResponse);
                return;
            } else {
                ctx.writeAndFlush(new AuthResponse(false));
            }

            return;
        }

        if (messageFromClient instanceof RegistrationRequest) {
            RegistrationRequest request = (RegistrationRequest) messageFromClient;
            RegistrationResponse registrationResponse = new RegistrationResponse(false);
            if (!dbService.isInDatabase(request.getLogin())) {
                if (dbService.registration(request.getLogin(), request.getPassword())) {
                    registrationResponse.setRegOk(true);
                    maxUserStorageSize = dbService.getMaxStorageSizeByLogin(request.getLogin());
                    registrationResponse.setMaxFolderDepth(MAX_FOLDER_DEPTH);
                    ctx.writeAndFlush(registrationResponse);
                } else {
                    ctx.writeAndFlush(registrationResponse);
                }
            } else {
                ctx.writeAndFlush(registrationResponse);
            }
            return;
        }

        if (messageFromClient instanceof GetFirstFileListRequest) {
            GetFirstFileListRequest request = (GetFirstFileListRequest) messageFromClient;
            AuthRequest authRequest = request.getAuthRequest();
            Path userRootDirPath = Paths.get(ROOT_DIR + "\\" + authRequest.getLogin());

            if (Files.exists(userRootDirPath)) {
                GetFileListResponse getFileListResponse = new GetFileListResponse(userRootDirPath);
                ctx.writeAndFlush(getFileListResponse);
                // добавление в бд
                dbService.setRootServerDirectory(String.valueOf(userRootDirPath), authRequest.getLogin());
                return;
            } else {
                try {
                    Files.createDirectory(userRootDirPath);
                    GetFileListResponse getFileListResponse = new GetFileListResponse(userRootDirPath);
                    ctx.writeAndFlush(getFileListResponse);
                    dbService.setRootServerDirectory(String.valueOf(userRootDirPath), authRequest.getLogin());
                    return;
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    System.out.println("Не удалось создать директория для пользователя");
                }
            }
            return;
        }

        if (messageFromClient instanceof GetFileListRequest) {
            GetFileListRequest request = (GetFileListRequest) messageFromClient;
            AuthRequest authRequest = request.getAuthRequest();
            String pathToOpenStr = request.getPathStr();

            if (checkAuth(authRequest) && checkPathRights(authRequest.getLogin(), pathToOpenStr)) {
                Path pathToOpen = Paths.get(pathToOpenStr);
                GetFileListResponse getFileListResponse = new GetFileListResponse(pathToOpen);
                ctx.writeAndFlush(getFileListResponse);
            } else {
                AccessErrorResponse accessErrorResponse = new AccessErrorResponse();
                ctx.writeAndFlush(accessErrorResponse);
            }

            return;
        }

        if (messageFromClient instanceof OpenDirRequest) {
            OpenDirRequest request = (OpenDirRequest) messageFromClient;
            AuthRequest authRequest = request.getAuthRequest();
            Path serverPathToOpen = Paths.get(((OpenDirRequest) messageFromClient).getServerPathToOpen());

            if (checkAuth(authRequest) && checkPathRights(authRequest.getLogin(), String.valueOf(serverPathToOpen))) {
                try {
                    ctx.writeAndFlush(new GetFileListResponse(serverPathToOpen));
                } catch (Exception e) {
                    ctx.writeAndFlush(new FailToOpenDirectoryResponse(serverPathToOpen));
                }
            }

            return;

        }

        if (messageFromClient instanceof UploadFileRequest) {
            UploadFileRequest request = (UploadFileRequest) messageFromClient;
            AuthRequest authRequest = request.getAuthRequest();
            String pathToServerDirStr = request.getPathToServerDirStr();
            String pathToFileOnClientStr = request.getPathToFileOnClientStr();
            String fileNameOnServer = String.valueOf(Path.of(pathToFileOnClientStr).getFileName());
            String pathToServerFileStr = pathToServerDirStr + "\\" + fileNameOnServer;
            long fileLength = request.getFileLength();

            if (checkAuth(authRequest) && checkPathRights(authRequest.getLogin(), pathToServerDirStr)) {
                if (!Files.exists(Path.of(pathToServerFileStr))) {
                    ctx.writeAndFlush(new UploadFileResponse(
                            pathToFileOnClientStr,
                            pathToServerDirStr,
                            false,
                            false));
                } else if (Files.exists(Path.of(pathToServerFileStr))) {
                    ctx.writeAndFlush(new UploadFileResponse(
                            pathToFileOnClientStr,
                            pathToServerDirStr,
                            true,
                            false));
                }
            }
            return;
        }

        if (messageFromClient instanceof DownloadFileRequest) {
            DownloadFileRequest request = (DownloadFileRequest) messageFromClient;
            AuthRequest authRequest = request.getAuthRequest();

            String pathToFileOnServer = request.getPathToFileOnServer();
//            if (checkAuth(authRequest) && checkPathRights(authRequest.getLogin(), pathToFileOnServer)) {
            if (checkAuth(authRequest)) {
                FileMessage fileMessage = new FileMessage(Paths.get(pathToFileOnServer));
                ctx.writeAndFlush(fileMessage);
            }

            return;
        }

        if (messageFromClient instanceof FileMessage) {
            System.out.println("Прилетел FileMessage");
            FileMessage fileMessage = (FileMessage) messageFromClient;
            long fileSize = fileMessage.getSize();
            String fileName = fileMessage.getName();
            byte[] fileData = fileMessage.getData();
            Path pathToServerDir = Paths.get(fileMessage.getDirectory());
            File file = new File((pathToServerDir + "\\" + fileName));
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                outputStream.write(fileData);
            }
            ctx.writeAndFlush(new GetFileListResponse(pathToServerDir));
            return;
        }

        if (messageFromClient instanceof CreateDirRequest) {
            CreateDirRequest request = (CreateDirRequest) messageFromClient;
            AuthRequest authRequest = request.getAuthRequest();
            String pathToTheNewDir = request.getPathToTheNewDir();

            if (checkAuth(authRequest)) {
                File newDir = new File(String.valueOf(Path.of(pathToTheNewDir)));

                boolean isCreate = newDir.mkdir();

                if (isCreate) {
                    ctx.writeAndFlush(
                            new CreateDirResponse(
                                    new GetFileListResponse(Paths.get(pathToTheNewDir).getParent()),
                                    true));
                } else {
                    ctx.writeAndFlush(
                            new CreateDirResponse(
                                    new GetFileListResponse(Paths.get(pathToTheNewDir).getParent()),
                                    false));

                }
                return;
            }

        }

        if (messageFromClient instanceof DeleteFileRequest) {
            DeleteFileRequest request = (DeleteFileRequest) messageFromClient;
            AuthRequest authRequest = request.getAuthRequest();
            Path pathToTheDeleteFile = Paths.get(request.getPathToTheDeleteFileStr());
            Path parentPathOfDeletedPath = pathToTheDeleteFile.getParent();

            if (checkAuth(authRequest)) {

                if (!Files.isDirectory(pathToTheDeleteFile)) {
                    try {
                        Files.delete(pathToTheDeleteFile);
                        ctx.writeAndFlush(
                                new DeleteFileResponse(
                                        new GetFileListResponse(parentPathOfDeletedPath),
                                        true));
                    } catch (IOException e) {
                        ctx.writeAndFlush(
                                new DeleteFileResponse(
                                        new GetFileListResponse(parentPathOfDeletedPath),
                                        false));
                    }
                }

                if (Files.isDirectory(pathToTheDeleteFile)) {
                    try {
                        FileUtils.deleteDirectory(new File(String.valueOf(pathToTheDeleteFile)));
                        ctx.writeAndFlush(
                                new DeleteFileResponse(
                                        new GetFileListResponse(parentPathOfDeletedPath),
                                        true));
                        return;
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        ctx.writeAndFlush(
                                new DeleteFileResponse(
                                        new GetFileListResponse(parentPathOfDeletedPath),
                                        false));
                    }
                }
            }

            return;
        }

        if (messageFromClient instanceof ChangeLoginRequest) {
            ChangeLoginRequest request = (ChangeLoginRequest) messageFromClient;
            AuthRequest authRequest = request.getAuthRequest();
            if (checkAuth(authRequest)) {
                System.out.println("запрос прилетел");
                System.out.println(request.getNewLogin());
            }
        }

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("Client " + ctx.channel().remoteAddress() + " disconnected");
        ctx.close();
        cause.printStackTrace();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.close();
        System.out.println("Client channel " + ctx.channel().remoteAddress() + " closed");
    }


    public boolean checkAuth(AuthRequest authRequest) {
        String login = authRequest.getLogin();
        String password = authRequest.getPassword();
        return login.equals(dbService.getLoginByPass(login, password.hashCode()));
    }


    public boolean checkPathRights(String login, String pathToOpenStr) {
        String rootServerDirStr = dbService.getRootServerPathByLogin(login);
        try {
            return pathToOpenStr.startsWith(rootServerDirStr);
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println(rootServerDirStr);
            System.out.println(pathToOpenStr);
            return false;
        }

    }

    public long getSizeOfAllUserFiles(Path path) throws IOException {
        try (Stream<Path> walk = Files.walk(path)) {
            return walk.map(Path::toFile)
                    .filter(File::isFile)
                    .mapToLong(File::length)
                    .sum();
        }
    }


}

