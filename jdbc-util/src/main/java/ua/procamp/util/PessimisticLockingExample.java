package ua.procamp.util;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Danil Kuznetsov (kuznetsov.danil.v@gmail.com)
 */
public class PessimisticLockingExample {

    private static final String SELECT_FOR_UPDATE_PROGRAM_BY_ID = "SELECT * FROM program WHERE id = ? FOR UPDATE ";

    private static final String UPDATE_PROGRAM = "UPDATE program SET name = ?, description = ? WHERE id = ?";

    private static DataSource dataSource;

    public static void main(String[] args) {
        dataSource = JdbcUtil.createPostgresDataSource(
                "jdbc:postgresql://localhost:5432/test",
                "postgres",
                "pass"
        );

        Long programId = 1L;
        handleProgramUpdateWithPessimisticLocking(programId);
    }

    private static void handleProgramUpdateWithPessimisticLocking(Long programId) {
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);

            Program program = findAndLockById(programId, con);

            int updatedRow = updateProgram(program, con);

            if (updatedRow == 0) {
                con.rollback();
                throw new PessimisticLockingException("Cannot update program");
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
            preparedStatement.setLong(3, program.id);
            return preparedStatement.executeUpdate();
        }
    }

    private static Program findAndLockById(Long programId, Connection con) throws SQLException {
        try (PreparedStatement preparedStatement = con.prepareStatement(SELECT_FOR_UPDATE_PROGRAM_BY_ID)) {
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
                resultSet.getString("description")
        );
    }

    private static class Program {
        long id;
        String name;
        String description;

        Program(long id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
    }
}
