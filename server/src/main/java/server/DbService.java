package server;

import java.sql.*;

public class DbService {

    private static final DbService INSTANCE = new DbService();
    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement psInsert;
    private static PreparedStatement psGetLogin;
    private static PreparedStatement psCheckLogin;
    private static PreparedStatement psGetMaxStorageSize;

    private DbService() {
        try {
            connect();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
//        connection = DriverManager.getConnection("jdbc:sqlite:database/users.db");
        connection = DriverManager.getConnection("jdbc:sqlite:Z:\\gb_MyNetworkStorage\\server\\src\\main\\java\\database\\users.db");
        statement = connection.createStatement();
        System.out.println("Database 'Users' - connected");
    }

    public void disconnect() {
        try {
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Не удалось закрыть statement");
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Не удалось закрыть соединение");
        }
    }

    public static DbService getInstance() {
        return INSTANCE;
    }

    public static void prepareInsert() throws SQLException {
        psInsert = connection.prepareStatement("insert into users (login, password) values (?, ?);");
    }

    public static void prepareGetLoginByPass() throws SQLException {
        psGetLogin = connection.prepareStatement("select login from users where login = ? and password = ?;");
    }

    public static void prepareCheckLogin() throws SQLException {
        psCheckLogin = connection.prepareStatement("select login from users where login = ?");
    }

    public static void prepareGetMaxStorageSizeByLogin() throws SQLException {
        psGetMaxStorageSize = connection.prepareStatement("select max_storage_size from users where login = ?");
    }

//    public String getLoginByPass(String login, Integer password) {
//        if (isInDatabase(login)) {
//            try {
//                prepareGetLoginByPass();
//                psGetLogin.setString(1, login);
//                psGetLogin.setInt(2, password.hashCode());
//                ResultSet resultSet = psGetLogin.executeQuery();
//                resultSet.
//                String rss = resultSet.getString("login");
//                resultSet.close();
//                return rss;
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//        return null;
//    }

    public String getLoginByPass(String login, Integer password) {
        if (isInDatabase(login)) {
            try {
                prepareGetLoginByPass();
                psGetLogin.setString(1, login);
                psGetLogin.setInt(2, password.hashCode());
                ResultSet resultSet = psGetLogin.executeQuery();
                String resultSetString = resultSet.getString("login");
                resultSet.close();
                return resultSetString;
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Не удалось получить логин по паролю");
            }
            return null;
        }
        return null;
    }

    public long getMaxStorageSizeByLogin (String login) {
        if (isInDatabase(login)) {
            try {
                prepareGetMaxStorageSizeByLogin();
                psGetMaxStorageSize.setString(1, login);
                ResultSet resultSet = psGetMaxStorageSize.executeQuery();
                long rss = resultSet.getLong("max_storage_size");
                resultSet.close();
                return rss;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        }
        return 0;
    }

    public boolean isInDatabase(String login) {
        try {
            prepareCheckLogin();
            psCheckLogin.setString(1, login);
            ResultSet resultSet = psCheckLogin.executeQuery();

            if(resultSet.next()) {
                resultSet.close();
                return true;
            } else {
                resultSet.close();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean registration(String login, String password) {
        try {
            prepareInsert();
            psInsert.setString(1, login);
            psInsert.setInt(2, password.hashCode());
            psInsert.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }




}
