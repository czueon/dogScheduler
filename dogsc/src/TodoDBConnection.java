import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TodoDBConnection {
    private Connection connection;
    private String todoDB = "jdbc:sqlite:src/database.sqlite";

    // 데이터베이스 연결 초기화
    public TodoDBConnection() {
        initializeDatabaseConnection();
    }

    private void initializeDatabaseConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(todoDB);
                connection.setAutoCommit(false);
                System.out.println("Todo 데이터베이스에 연결 중");

                // 테이블 생성 SQL 실행
                String createTableSQL = "CREATE TABLE IF NOT EXISTS todoDB (" +
                        "todoDate TEXT, " +
                        "todoText TEXT, " +
                        "is_completed INTEGER)";

                try (Statement statement = connection.createStatement()) {
                    statement.execute(createTableSQL);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            System.out.println("Todo 데이터베이스에 연결 안됨");
        }
    }

    // DB 연결
    public Connection getConnection() {
        return connection;
    }

    // 데이터베이스 연결 닫기
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Todo 데이터베이스 연결 닫힘");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 투두 추가
    public void addTodoDB(String todoText, Date todoDate) {
        try {
            initializeDatabaseConnection();
            String insertQuery = "INSERT INTO todoDB (todoDate, todoText, is_completed) VALUES (?, ?, 0);";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                preparedStatement.setString(1, new SimpleDateFormat("yyyy-MM-dd").format(todoDate));
                preparedStatement.setString(2, todoText);
                preparedStatement.executeUpdate();
                connection.commit();
                System.out.println("성공");
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // 체크박스 누를 때
    public void updateTodoChecked(String todoText, int is_completed) {
        try {
            initializeDatabaseConnection();
            String updateQuery = "UPDATE todoDB SET is_completed = ? WHERE todoText = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                preparedStatement.setInt(1, is_completed);
                preparedStatement.setString(2, todoText);
                preparedStatement.executeUpdate();
                connection.commit();
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // 해당 날짜의 투두 가져오기
    public List<String> getTodosForDate(Date date) {
        List<String> todos = new ArrayList<>();
        try {
            initializeDatabaseConnection();
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
            String selectSQL = "SELECT * FROM todoDB WHERE todoDate = ?";

            try (PreparedStatement statement = connection.prepareStatement(selectSQL)) {
                statement.setString(1, formattedDate);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String todoText = resultSet.getString("todoText");
                        todos.add(todoText);
                    }
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return todos;
    }

    // 체크박스 상태
    public int getTodoCompletedStatus(String todo) {
        int isCompleted = 0;
        try {
            initializeDatabaseConnection();
            String selectSQL = "SELECT is_completed FROM todoDB WHERE todoText=?";

            try (PreparedStatement statement = connection.prepareStatement(selectSQL)) {
                statement.setString(1, todo);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        isCompleted = resultSet.getInt("is_completed");
                    }
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return isCompleted;
    }

    // SQLException을 처리하는 메서드
    private void handleSQLException(SQLException e) {
        e.printStackTrace();
        System.out.println("Todo 데이터베이스 작업 중 오류 발생");
        try {
            connection.rollback();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
