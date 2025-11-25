package com.userfish.dao;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.userfish.model.User;
import com.userfish.util.HibernateUtil;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.transaction.SystemException;

public class UserDaoImpl implements UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public User save(User user) throws IllegalStateException, SystemException {
         Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.openSession();
            transaction = session.beginTransaction();
            
            session.persist(user);
            transaction.commit();
            
            logger.info("User saved successfully with ID: {}", user.get_id());
            return user;
        } catch (ConstraintViolationException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Constraint violation while saving user: {}", user.get_email(), e);
            throw new RuntimeException("Email already exists: " + user.get_email(), e);
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error saving user: {}", user, e);
            throw new RuntimeException("Failed to save user: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public Optional<User> findById(Long id) {
         try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            if (user != null) {
                logger.debug("User found by id {}: {}", id, user);
            } else {
                logger.debug("User not found by id: {}", id);
            }
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Error finding user by id: {}", id, e);
            throw new RuntimeException("Failed to find user by id: " + id, e);
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaQuery<User> criteriaQuery = session.getCriteriaBuilder()
                    .createQuery(User.class);
            criteriaQuery.from(User.class);
            List<User> users = session.createQuery(criteriaQuery).getResultList();
            logger.debug("Found {} users", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Error finding all users", e);
            throw new RuntimeException("Failed to retrieve users", e);
        }
    }

    @Override
    public User update(User user) throws IllegalStateException, SystemException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User updatedUser = session.merge(user);
            transaction.commit();
            logger.info("User updated successfully: {}", updatedUser);
            return updatedUser;
        } catch (ConstraintViolationException e) {
            if (transaction != null) transaction.rollback();
            logger.error("Constraint violation while updating user: {}", user.get_email(), e);
            throw new RuntimeException("Email already exists: " + user.get_email(), e);
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Error updating user: {}", user, e);
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    public void delete(Long id) throws IllegalStateException, SystemException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, id);
            if (user != null) {
                session.remove(user);
                logger.info("User deleted successfully: {}", user);
            } else {
                logger.warn("User not found for deletion with id: {}", id);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Error deleting user with id: {}", id, e);
            throw new RuntimeException("Failed to delete user with id: " + id, e);
        }
    }

}
