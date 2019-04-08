package ua.procamp.util;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Danil Kuznetsov (kuznetsov.danil.v@gmail.com)
 */
public class OptimistickLockingExample {

    private static final String SELECT_PROGRAM_BY_ID = "SELECT * FROM program where id = ?";

    private static final String UPDATE_PROGRAM_NAME = "UPDATE program set name = ? version = ?   where id = ? and version = ?";

    private static DataSource dataSource;

    public static void main(String[] args) {

        // start tx
        // read prog_id
        // do some logic
        // update program using optimistic locking
        // commit

        // or throw Exception if

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

            Program program = findProgramById(programId, con);

            int updatedRow = updateProgramName(program, "newName", con);

            if (updatedRow == 0) {
                throw new RuntimeException("Cannot update program, ver is changed");
            }

            con.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot handle program update", e);
        }
    }

    private static int updateProgramName(Program program, String newName, Connection con) throws SQLException {
        PreparedStatement preparedStatement = con.prepareStatement(UPDATE_PROGRAM_NAME);

        preparedStatement.setString(1, newName);
        preparedStatement.setLong(2, program.version + 1);
        preparedStatement.setLong(3, program.id);
        preparedStatement.setLong(4, program.version);

        return preparedStatement.executeUpdate();
    }

    private static Program findProgramById(Long programId, Connection con) throws SQLException {
        PreparedStatement preparedStatement = con.prepareStatement(SELECT_PROGRAM_BY_ID);
        preparedStatement.setLong(1, programId);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return buildProgram(resultSet);
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
        private final long id;
        private final String name;
        private final String description;
        private final int version;

        Program(long id, String name, String description, int version) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.version = version;
        }
    }
}
