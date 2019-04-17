package ua.procamp.util;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Danil Kuznetsov (kuznetsov.danil.v@gmail.com)
 */
public class OptimisticLockingExample {

    private static final String SELECT_PROGRAM_BY_ID = "SELECT * FROM program where id = ?";

    private static final String UPDATE_PROGRAM = " UPDATE program set name = ? description = ? version = ?" +
            " where id = ? and version = ? ";

    private static DataSource dataSource;

    public static void main(String[] args) {
        dataSource = JdbcUtil.createPostgresDataSource(
                "jdbc:postgresql://localhost:5432/test",
                "postgres",
                "pass"
        );

        Long programId = 1L;
        handleProgramUpdateWithOptimisticLocking(programId);
    }

    private static void handleProgramUpdateWithOptimisticLocking(Long programId) {
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);

            Program program = findById(programId, con);
            int updatedRow = updateProgram(program, con);

            if (updatedRow == 0) {
                con.rollback();
                throw new OptimisticLockingException("Cannot update program, version was changed");
            }
            con.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot handle program update", e);
        }
    }

    private static int updateProgram(Program program, Connection con) throws SQLException {
        try (PreparedStatement preparedStatement = con.prepareStatement(UPDATE_PROGRAM)) {
            preparedStatement.setString(1, program.name);
            preparedStatement.setString(2, program.description);
            preparedStatement.setLong(3, program.version + 1);
            preparedStatement.setLong(4, program.id);
            preparedStatement.setLong(5, program.version);
            return preparedStatement.executeUpdate();
        }
    }

    private static Program findById(Long programId, Connection con) throws SQLException {
        try (PreparedStatement preparedStatement = con.prepareStatement(SELECT_PROGRAM_BY_ID)) {
            preparedStatement.setLong(1, programId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return buildProgram(resultSet);
            }
        }
    }

    private static Program buildProgram(ResultSet resultSet) throws SQLException {
        return new Program(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getInt("version")
        );
    }

    private static class Program {
        long id;
        String name;
        String description;
        int version;

        Program(long id, String name, String description, int version) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.version = version;
        }
    }
}
