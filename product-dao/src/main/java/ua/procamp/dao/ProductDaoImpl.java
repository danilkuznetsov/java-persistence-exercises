package ua.procamp.dao;

import ua.procamp.exception.DaoOperationException;
import ua.procamp.model.Product;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class ProductDaoImpl implements ProductDao {

    private static final String SQL_INSERT_PRODUCT = "INSERT INTO products (name,producer,price,expiration_date) VALUES (?,?,?,?)";
    private static final String SQL_UPDATE_PRODUCT_BY_ID = "UPDATE products SET name = ?, producer = ?, price = ?, expiration_date = ? WHERE id = ?";
    private static final String SQL_DELETE_PRODUCT_BY_ID = "DELETE FROM products WHERE id = ?";

    private static final String SQL_FIND_ALL_PRODUCTS = "SELECT * FROM products";
    private static final String SQL_FIND_PRODUCT_BY_ID = "SELECT * FROM products WHERE id = ?";

    private DataSource dataSource;

    public ProductDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Product product) {
        if (Objects.isNull(product)) {
            throw new DaoOperationException("Product cannot be null");
        }

        executeInTx(con -> executeInsert(product, con));
    }

    private void executeInsert(Product product, Connection con) {
        try (PreparedStatement stm = createInsertStm(con, product)) {
            stm.execute();
            generateId(product, stm);
        } catch (SQLException e) {
            throw new DaoOperationException("Error saving product: " + product, e);
        }
    }

    private PreparedStatement createInsertStm(Connection con, Product product) throws SQLException {
        PreparedStatement insertStatement = con.prepareStatement(SQL_INSERT_PRODUCT, Statement.RETURN_GENERATED_KEYS);
        mapToStatement(insertStatement, product);
        return insertStatement;
    }

    private void generateId(Product product, PreparedStatement stm) throws SQLException {
        ResultSet generatedKeys = stm.getGeneratedKeys();
        if (generatedKeys.next()) {
            Long generatedId = generatedKeys.getLong(1);
            product.setId(generatedId);
        } else {
            throw new DaoOperationException("Cannot generate Id for product");
        }
    }

    @Override
    public List<Product> findAll() {
        return executeAndGetResultInTx(this::executeSelectAll);
    }

    private List<Product> executeSelectAll(Connection con) {
        try (Statement stm = con.createStatement()) {
            return readProducts(stm);
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot find products", e);
        }
    }

    private List<Product> readProducts(Statement stm) throws SQLException {
        List<Product> products = new ArrayList<>();
        try (ResultSet result = stm.executeQuery(SQL_FIND_ALL_PRODUCTS)) {
            while (result.next()) {
                Product product = mapToProduct(result);
                products.add(product);
            }
        }
        return products;
    }

    @Override
    public Product findOne(Long id) {
        return executeAndGetResultInTx(con -> executeSelectById(con, id));
    }

    private Product executeSelectById(Connection con, Long id) {
        try (PreparedStatement stm = createSelectById(con, id)) {
            return readProduct(id, stm);
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot find products", e);
        }
    }

    private Product readProduct(Long id, PreparedStatement stm) throws SQLException {
        try (ResultSet result = stm.executeQuery()) {
            if (!result.next()) {
                throw new DaoOperationException("Product with id = " + id + " does not exist");
            }
            return mapToProduct(result);
        }
    }

    private PreparedStatement createSelectById(Connection con, Long id) throws SQLException {
        PreparedStatement selectByIdStm = con.prepareStatement(SQL_FIND_PRODUCT_BY_ID);
        selectByIdStm.setLong(1, id);
        return selectByIdStm;
    }

    private Product mapToProduct(ResultSet resultSet) throws SQLException {
        return Product.builder()
                .id(resultSet.getLong(1))
                .name(resultSet.getString(2))
                .producer(resultSet.getString(3))
                .price(resultSet.getBigDecimal(4))
                .expirationDate(resultSet.getDate(5).toLocalDate())
                .creationTime(resultSet.getTimestamp(6).toLocalDateTime())
                .build();
    }

    @Override
    public void update(Product product) {
        if (Objects.isNull(product.getId())) {
            throw new DaoOperationException("Product id cannot be null");
        }

        executeInTx(con -> executeUpdate(product, con));
    }

    private void executeUpdate(Product product, Connection con) {
        try (PreparedStatement stm = createUpdateStm(con, product)) {
            int countDeletedRow = stm.executeUpdate();
            if (countDeletedRow == 0) {
                throw new DaoOperationException("Product with id = " + product.getId() + " does not exist");
            }
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot update product", e);
        }
    }

    private PreparedStatement createUpdateStm(Connection con, Product product) throws SQLException {
        PreparedStatement updateStm = con.prepareStatement(SQL_UPDATE_PRODUCT_BY_ID);
        mapToStatement(updateStm, product);
        updateStm.setLong(5, product.getId());
        return updateStm;
    }

    private void mapToStatement(PreparedStatement stm, Product product) throws SQLException {
        stm.setString(1, product.getName());
        stm.setString(2, product.getProducer());
        stm.setBigDecimal(3, product.getPrice());
        stm.setDate(4, Date.valueOf(product.getExpirationDate()));
    }

    @Override
    public void remove(Product product) {
        if (Objects.isNull(product.getId())) {
            throw new DaoOperationException("Product id cannot be null");
        }
        executeInTx(con -> executeDelete(product, con));
    }

    private void executeDelete(Product product, Connection con) {
        try (PreparedStatement stm = createDeleteStm(con, product.getId())) {
            int countDeletedRow = stm.executeUpdate();
            if (countDeletedRow == 0) {
                con.rollback();
                throw new DaoOperationException("Product with id = " + product.getId() + " does not exist");
            }
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot delete product", e);
        }
    }

    private PreparedStatement createDeleteStm(Connection con, Long id) throws SQLException {
        PreparedStatement deleteStm = con.prepareStatement(SQL_DELETE_PRODUCT_BY_ID);
        deleteStm.setLong(1, id);
        return deleteStm;
    }

    private void executeInTx(Consumer<Connection> consumer) {
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            consumer.accept(con);
            con.commit();
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot execute query", e);
        }

    }

    private <T> T executeAndGetResultInTx(Function<Connection, T> consumer) {
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            T result = consumer.apply(con);
            con.commit();
            return result;
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot execute query", e);
        }

    }
}
