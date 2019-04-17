package ua.procamp.dao;

import ua.procamp.exception.CompanyDaoException;
import ua.procamp.model.Company;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.function.Function;

public class CompanyDaoImpl implements CompanyDao {
    private EntityManagerFactory entityManagerFactory;

    public CompanyDaoImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public Company findByIdFetchProducts(Long id) {
        return executeAndReturn(em ->
                em.createQuery("select distinct c from Company c " +
                        "left join fetch c.products " +
                        "where c.id = :id", Company.class
                )
                        .setParameter("id", id)
                        .getSingleResult()
        );
    }

    private <T> T executeAndReturn(Function<EntityManager, T> fun) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            T result = fun.apply(entityManager);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new CompanyDaoException("Cannot execute query", e);
        } finally {
            entityManager.close();
        }
    }
}
