package ua.procamp.dao;

import ua.procamp.exception.AccountDaoException;
import ua.procamp.model.Account;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

public class AccountDaoImpl implements AccountDao {
    private EntityManagerFactory emf;

    public AccountDaoImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void save(Account account) {

        if (account.getEmail() == null){
            throw new AccountDaoException("Account doesn't have email field",null);
        }

        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();

        try {
            entityManager.persist(account);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public Account findById(Long id) {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();

        try {
            Account account = entityManager.find(Account.class, id);
            entityManager.getTransaction().commit();
            return account;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Cannot find account by id", e);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public Account findByEmail(String email) {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();

        try {
            Account account = entityManager
                    .createQuery("select a from Account a where a.email=:email", Account.class)
                    .setParameter("email",email)
                    .getSingleResult();

            entityManager.getTransaction().commit();

            return account;

        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Cannot find account by id", e);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<Account> findAll() {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();

        try {

            List<Account> accounts = entityManager
                    .createQuery("select a from Account a ", Account.class)
                    .getResultList();

            entityManager.getTransaction().commit();

            return accounts;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Cannot find account by id", e);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void update(Account account) {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();

        try {
            entityManager.merge(account);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Cannot find account by id", e);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void remove(Account account) {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        try {

            account = entityManager.merge(account);
            entityManager.remove(account);

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Cannot find account by id", e);
        } finally {
            entityManager.close();
        }
    }
}

