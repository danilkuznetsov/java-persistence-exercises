package ua.procamp;

import ua.procamp.model.Account;
import ua.procamp.model.Card;
import ua.procamp.util.EntityManagerUtil;
import ua.procamp.util.TestDataGenerator;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * @author Danil Kuznetsov (kuznetsov.danil.v@gmail.com)
 */
public class RelationExample {

    public static void main(String[] args) {

//        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("SingleAccountEntityH2");
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("SingleAccountEntityPostgres");

        EntityManagerUtil emUtil = new EntityManagerUtil(entityManagerFactory);
        Account account = TestDataGenerator.generateAccount();

        emUtil.performWithinTx((em)->{
            em.persist(account);

            Card card =new Card();
            card.setName("monobank");
            card.setHolder(account);

            em.persist(card);

            Card card2 =new Card();
            card2.setName("privat");
            card2.setHolder(account);

            em.persist(card2);

        });

        emUtil.performWithinTx((em)->{
            Account foundAccount = em.find(Account.class, account.getId());
            System.out.println("I am here");
            foundAccount.getCards().forEach(System.out::println);
        });

        // lazy loading exception
//        Account account1 = emUtil.performReturningWithinTx((em) -> {
//
//            Account acc = em.find(Account.class, account.getId());
//            System.out.println("I am here");
//
//            return acc;
//        });
//
//        account1.getCards().forEach(System.out::println);

        // one of examples how we can fix lazy loading
        Account account1 = emUtil.performReturningWithinTx((em) -> {
            Account acc = em
                    .createQuery("select a from Account a join fetch a.cards", Account.class)
                    .getSingleResult();
            System.out.println("I am here");
            return acc;
        });

        account1.getCards().forEach(System.out::println);
        entityManagerFactory.close();
    }
}
