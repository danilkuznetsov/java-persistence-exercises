package ua.procamp.dao;

import ua.procamp.exception.AccountDaoException;
import ua.procamp.model.Account;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class AccountDaoImpl implements AccountDao {
    private EntityManagerFactory emf;

    public AccountDaoImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void save(Account account) {

        if (Objects.isNull(account.getEmail())) {
            throw new AccountDaoException("Account doesn't have email field");
        }

        executeInTx(em -> em.persist(account));
    }

    @Override
    public Account findById(Long id) {
        return executeAndReturnInTx(em -> em.find(Account.class, id));
    }

    @Override
    public Account findByEmail(String email) {
        return executeAndReturnInTx(
                em -> em.createQuery("select a from Account a where a.email=:email", Account.class)
                        .setParameter("email", email)
                        .getSingleResult()
        );
    }

    @Override
    public List<Account> findAll() {
        return executeAndReturnInTx(
                em -> em.createQuery("select a from Account a ", Account.class)
                        .getResultList()
        );
    }

    @Override
    public void update(Account account) {
        executeInTx(em -> {
            em.merge(account);
        });
    }

    @Override
    public void remove(Account account) {
        executeInTx(em -> {
            Account managedAccount = em.merge(account);
            em.remove(managedAccount);
        });
    }

    private void executeInTx(Consumer<EntityManager> consumer) {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            consumer.accept(entityManager);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Cannot execute query", e);
        } finally {
            entityManager.close();
        }
    }

    private <T> T executeAndReturnInTx(Function<EntityManager, T> fun) {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            T result = fun.apply(entityManager);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Cannot execute query", e);
        } finally {
            entityManager.close();
        }
    }
}

