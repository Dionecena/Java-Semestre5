package com.example.whatsapp_java.dao;

import com.example.whatsapp_java.entities.Message;
import com.example.whatsapp_java.entities.MessageStatus;
import com.example.whatsapp_java.entities.User;
import com.example.whatsapp_java.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class MessageDAO {

    public void save(Message message) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(message);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<Message> getHistorique(User u1, User u2) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "from Message m where (m.sender = :u1 and m.receiver = :u2) or (m.sender = :u2 and m.receiver = :u1) order by m.dateEnvoi asc",
                            Message.class)
                    .setParameter("u1", u1)
                    .setParameter("u2", u2)
                    .list();
        }
    }

    public List<Message> getNonLus(User receiver) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "from Message m where m.receiver = :receiver and m.statut <> :lu order by m.dateEnvoi asc",
                            Message.class)
                    .setParameter("receiver", receiver)
                    .setParameter("lu", MessageStatus.LU)
                    .list();
        }
    }

    public void updateStatus(Long messageId, MessageStatus status) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Message message = session.get(Message.class, messageId);
            if (message != null) {
                message.setStatut(status);
                session.merge(message);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }
}
