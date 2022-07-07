package cloud;

import java.nio.file.Path;


public class ClientInfo {

    private static final ClientInfo INSTANCE = new ClientInfo();
    private static String login;
    private static String password;
    private static Path currentServerPath;
    private static Path currentClientPath;
    private static int maxFolderDepth;

    public static ClientInfo getInstance() {
        return INSTANCE;
    }

    public static String getLogin() {
        return login;
    }

    public static void setLogin(String login) {
        ClientInfo.login = login;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        ClientInfo.password = password;
    }

    public static Path getCurrentServerPath() {
        return currentServerPath;
    }

    public static void setCurrentServerPath(Path currentServerPath) {
        ClientInfo.currentServerPath = currentServerPath;
    }

    public static Path getCurrentClientPath() {
        return currentClientPath;
    }

    public static void setCurrentClientPath(Path currentClientPath) {
        ClientInfo.currentClientPath = currentClientPath;
    }

    public static int getMaxFolderDepth() {
        return maxFolderDepth;
    }

    public static void setMaxFolderDepth(int maxFolderDepth) {
        ClientInfo.maxFolderDepth = maxFolderDepth;
    }
}
