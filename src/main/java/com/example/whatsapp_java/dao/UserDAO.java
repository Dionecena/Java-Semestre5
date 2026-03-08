package com.example.whatsapp_java.dao;

import com.example.whatsapp_java.entities.User;
import com.example.whatsapp_java.entities.UserStatus;
import com.example.whatsapp_java.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class UserDAO {

    public User findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from User u where u.username = :username", User.class)
                    .setParameter("username", username)
                    .uniqueResult();
        }
    }

    public User findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, id);
        }
    }

    public void save(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public void updateStatus(String username, UserStatus status) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User user = session.createQuery("from User u where u.username = :username", User.class)
                    .setParameter("username", username)
                    .uniqueResult();
            if (user != null) {
                user.setStatus(status);
                session.merge(user);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<User> findOnlineUsers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from User u where u.status = :status order by u.username", User.class)
                    .setParameter("status", UserStatus.ONLINE)
                    .list();
        }
    }

    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from User u order by u.username", User.class).list();
        }
    }
}
