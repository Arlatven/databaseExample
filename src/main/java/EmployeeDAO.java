import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class EmployeeDAO implements AutoCloseable {

    private static EmployeeDAO instance;
    private Connection connectWay;

    private EmployeeDAO(Connection connectWay) {
        this.connectWay = connectWay;
    }

    public static EmployeeDAO getInstance(Properties dbProperties) throws SQLException {
        if (instance == null) {
            Connection connection = connect(dbProperties);
            instance = new EmployeeDAO(connection);
        }
        return instance;
    }

    public static Connection connect(Properties dbProperties) throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(
                dbProperties.getProperty("db.url"),
                dbProperties.getProperty("db.login"),
                dbProperties.getProperty("db.password"));
    }

    public void updateRecord(Employee employee) throws SQLException {
        String sqlCommand = "update employee set vacancy_name = ?, salary = ? where id = ?";
        try (PreparedStatement preparedStatement = connectWay.prepareStatement(sqlCommand)) {
            preparedStatement.setString(1, employee.getVacancyName());
            preparedStatement.setInt(2, employee.getSalary());
            preparedStatement.setInt(3, employee.getId());
            int i = preparedStatement.executeUpdate();
        }

    }

    public void deleteRecord(int employeeId) throws SQLException {
        String sqlCommand = "delete from employee where id = ?";
        try (PreparedStatement preparedStatement = connectWay.prepareStatement(sqlCommand)) {
            preparedStatement.setInt(1, employeeId);
            int i = preparedStatement.executeUpdate();
        }
    }

    public void insertRecord(Employee employee) throws SQLException {
        String sqlCommand = "insert into employee values (?,?,?)";
        try (PreparedStatement preparedStatement = connectWay.prepareStatement(sqlCommand)) {
            preparedStatement.setInt(1, employee.getId());
            preparedStatement.setString(2, employee.getVacancyName());
            preparedStatement.setInt(3, employee.getSalary());
            int i = preparedStatement.executeUpdate();
        }
    }

    public Optional<Employee> selectOne(int id) throws SQLException {
        try (PreparedStatement preparedStatement = connectWay.prepareStatement("SELECT * FROM employee where id = ?")) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Optional<Employee> opt = Optional.of(new Employee(resultSet.getInt("id"), resultSet.getString("vacancy_name"), resultSet.getInt("salary")));
                return opt;
            }
            return Optional.empty();
        }
    }

    public List<Employee> selectAll() throws SQLException {
        try (Statement stmt = connectWay.createStatement()) {
            List<Employee> employeesList = new ArrayList<>();
            String sqlCommand = "SELECT * FROM employee ";
            ResultSet resSet = stmt.executeQuery(sqlCommand);
            while (resSet.next()) {
                employeesList.add(new Employee(resSet.getInt("id"), resSet.getString("vacancy_name"), resSet.getInt("salary")));
            }
            return employeesList;
        }
    }

    @Override
    public void close() throws SQLException {
        connectWay.close();
    }
}
