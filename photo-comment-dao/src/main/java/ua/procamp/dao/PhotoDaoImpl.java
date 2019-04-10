package ua.procamp.dao;

import ua.procamp.model.Photo;
import ua.procamp.model.PhotoComment;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Please note that you should not use auto-commit mode for your implementation.
 */
public class PhotoDaoImpl implements PhotoDao {
    private EntityManagerFactory entityManagerFactory;

    public PhotoDaoImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void save(Photo photo) {
        executeInTx(em -> em.persist(photo));
    }

    @Override
    public Photo findById(long id) {
        return executeInTxAndGetResults(em -> em.find(Photo.class, id));
    }

    @Override
    public List<Photo> findAll() {
        return executeInTxAndGetResults(
                em -> em
                        .createQuery("select p from Photo p", Photo.class)
                        .getResultList()
        );
    }

    @Override
    public void remove(Photo photo) {
        executeInTx(em -> {
            Photo mergedPhoto = em.merge(photo);
            em.remove(mergedPhoto);
        });
    }

    @Override
    public void addComment(long photoId, String comment) {
        executeInTx(em -> {
            PhotoComment newComment = new PhotoComment(comment);
            newComment.setPhoto(em.getReference(Photo.class, photoId));
            em.persist(newComment);
        });
    }

    private void executeInTx(Consumer<EntityManager> consumer) {
        executeInTxAndGetResults(em -> {
            consumer.accept(em);
            return true;
        });
    }

    private <T> T executeInTxAndGetResults(Function<EntityManager, T> fun) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.setFlushMode(FlushModeType.COMMIT);
        try {
            T result = fun.apply(em);
            em.getTransaction().commit();
            return result;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException();
        } finally {
            em.close();
        }
    }
}
