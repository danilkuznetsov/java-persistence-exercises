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
        execute(em -> em.persist(photo));
    }

    @Override
    public Photo findById(long id) {
        return executeAndGetSingleResult(em -> em.find(Photo.class, id));
    }

    @Override
    public List<Photo> findAll() {
        return executeAndGetResults(
                em -> em.createQuery("select p from Photo p", Photo.class)
                        .getResultList()
        );
    }

    @Override
    public void remove(Photo photo) {
        execute(em -> {
            Photo mergedPhoto = em.merge(photo);
            em.remove(mergedPhoto);
        });
    }


    @Override
    public void addComment(long photoId, String comment) {
        execute(em -> {
            Photo photo = em.find(Photo.class, photoId);
            photo.addComment(new PhotoComment(comment));
        });
    }

    private void execute(Consumer<EntityManager> managerConsumer) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.setFlushMode(FlushModeType.COMMIT);
        try {
            managerConsumer.accept(em);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException();
        } finally {
            em.close();
        }
    }

    private Photo executeAndGetSingleResult(Function<EntityManager, Photo> fun) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.setFlushMode(FlushModeType.COMMIT);
        try {
            Photo photo = fun.apply(em);
            em.getTransaction().commit();
            return photo;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException();
        } finally {
            em.close();
        }
    }

    private List<Photo> executeAndGetResults(Function<EntityManager, List<Photo>> fun) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.setFlushMode(FlushModeType.COMMIT);
        try {
            List<Photo> photos = fun.apply(em);
            em.getTransaction().commit();
            return photos;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException();
        } finally {
            em.close();
        }
    }

}
