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

public class ProductDaoImpl implements ProductDao {

    private static final String SQL_INSERT_PRODUCT = "INSERT INTO products (name,producer,price,expiration_date) VALUES (?,?,?,?)";

    private static final String SQL_FIND_ALL_PRODUCTS = "SELECT * FROM products";
    private static final String SQL_FIND_PRODUCT_BY_ID = "SELECT * FROM products WHERE id = ?";
    private static final String SQL_DELETE_PRODUCT_BY_ID = "DELETE FROM products WHERE id = ?";
    private static final String SQL_UPDATE_PRODUCT_BY_ID = "UPDATE products SET name = ?, producer = ?, price = ?, expiration_date = ? WHERE id = ?";

    private DataSource dataSource;

    public ProductDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Product product) {
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement stm = con.prepareStatement(SQL_INSERT_PRODUCT, Statement.RETURN_GENERATED_KEYS)) {
                stm.setString(1, product.getName());
                stm.setString(2, product.getProducer());
                stm.setBigDecimal(3, product.getPrice());
                stm.setDate(4, Date.valueOf(product.getExpirationDate()));
                stm.execute();
                ResultSet generatedKeys = stm.getGeneratedKeys();
                if (generatedKeys.next()) {
                    Long generatedId = generatedKeys.getLong(1);
                    product.setId(generatedId);
                } else {
                    throw new DaoOperationException("Cannot generate Id for product");
                }
            }
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot save product", e);
        }
    }

    @Override
    public List<Product> findAll() {
        try (Connection con = dataSource.getConnection()) {
            try (Statement stm = con.createStatement()) {
                try (ResultSet rawProducts = stm.executeQuery(SQL_FIND_ALL_PRODUCTS)) {

                    List<Product> products = new ArrayList<>();
                    while (rawProducts.next()) {

                        Product product = Product.builder()
                                .id(rawProducts.getLong(1))
                                .name(rawProducts.getString(2))
                                .producer(rawProducts.getString(3))
                                .price(rawProducts.getBigDecimal(4))
                                .expirationDate(rawProducts.getDate(5).toLocalDate())
                                .creationTime(rawProducts.getTimestamp(6).toLocalDateTime())
                                .build();

                        products.add(product);
                    }

                    return products;
                }
            }
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot find products", e);
        }
    }

    @Override
    public Product findOne(Long id) {
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement stm = con.prepareStatement(SQL_FIND_PRODUCT_BY_ID)) {
                stm.setLong(1, id);

                try (ResultSet rawProducts = stm.executeQuery()) {
                    while (rawProducts.next()) {

                        return Product.builder()
                                .id(rawProducts.getLong(1))
                                .name(rawProducts.getString(2))
                                .producer(rawProducts.getString(3))
                                .price(rawProducts.getBigDecimal(4))
                                .expirationDate(rawProducts.getDate(5).toLocalDate())
                                .creationTime(rawProducts.getTimestamp(6).toLocalDateTime())
                                .build();

                    }
                    throw new DaoOperationException("Product with id = " + id + " does not exist");
                }
            }
        } catch (SQLException e) {
            throw new DaoOperationException("Product with id = " + id + " does not exist", e);
        }
    }

    @Override
    public void update(Product product) {
        if (product.getId() == null){
            throw new DaoOperationException("Product id cannot be null");
        }

        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement stm = con.prepareStatement(SQL_UPDATE_PRODUCT_BY_ID)) {

                stm.setString(1, product.getName());
                stm.setString(2, product.getProducer());
                stm.setBigDecimal(3, product.getPrice());
                stm.setDate(4, Date.valueOf(product.getExpirationDate()));
                stm.setLong(5, product.getId());

                int countDeletedRow = stm.executeUpdate();
                if (countDeletedRow == 0) {
                    throw new DaoOperationException("Product with id = " + product.getId() + " does not exist");
                }
            }
        } catch (SQLException e) {
            throw new DaoOperationException("Product with id = " + product.getId() + " does not exist", e);
        }
    }

    @Override
    public void remove(Product product) {

        if (product.getId() == null){
            throw new DaoOperationException("Product id cannot be null");
        }

        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement stm = con.prepareStatement(SQL_DELETE_PRODUCT_BY_ID)) {


                stm.setLong(1, product.getId());
                int countDeletedRow = stm.executeUpdate();
                if (countDeletedRow == 0) {
                    throw new DaoOperationException("Product with id = " + product.getId() + " does not exist");
                }
            }
        } catch (SQLException e) {
            throw new DaoOperationException("Product with id = " + product.getId() + " does not exist", e);
        }
    }

}
