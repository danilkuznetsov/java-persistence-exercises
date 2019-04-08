package ua.procamp;

import ua.procamp.model.Account;
import ua.procamp.util.TestDataGenerator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * @author Danil Kuznetsov (kuznetsov.danil.v@gmail.com)
 */
public class EntityManagerExample {
    public static void main(String[] args) {
        // usually this is generated when application will be started and will be closed after shutdown
        // It is thread safe factory
        // It looks like DataSource from jdbc world
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("SingleAccountEntityH2");


        // in some case is called UnitOfWork,
        // This is not thread safe
        // It looks like connection from jdbc world
        EntityManager entityManager = entityManagerFactory.createEntityManager(); //session start

        // This is similar to setAutocommit(false);
        entityManager.getTransaction().begin();

        try {

            // state is new
            Account account = TestDataGenerator.generateAccount();
            System.out.println(account);

            // state is persistent
            entityManager.persist(account);
            System.out.println(account);

            //
            entityManager.find(Account.class, account.getId());
            System.out.println(account);


            // This is example with jpql
//            List<Account> accounts = entityManager
//                    .createQuery("select a from Account a where a.email=:email", Account.class)
//                    .setParameter("email", "email@email.com")
//                    .getResultList();

            // state is detached
            entityManager.detach(account);

            // back to session
            Account managedAccount = entityManager.merge(account);

            entityManager.remove(account);

            entityManager.getTransaction().commit();
        } catch (Exception e) {

            entityManager.getTransaction().rollback();
        } finally {
            entityManager.close();
        }

        // usually this is generated when application will be started and will be closed after shutdown
        entityManagerFactory.close();
    }
}
