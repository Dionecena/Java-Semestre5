package com.example.whatsapp_java.utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.net.URL;

public class HibernateUtil {
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            synchronized (HibernateUtil.class) {
                if (sessionFactory == null) {
                    try {
                        URL cfg = HibernateUtil.class.getClassLoader().getResource("hibernate.cfg.xml");
                        System.out.println("[HIBERNATE] hibernate.cfg.xml loaded from: " + (cfg == null ? "NOT FOUND" : cfg));
                        sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
                    } catch (Throwable ex) {
                        System.err.println("Initial SessionFactory creation failed: " + ex.getMessage());
                        throw ex;
                    }
                }
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        try {
            if (sessionFactory != null) {
                sessionFactory.close();
                sessionFactory = null;
            }
        } catch (Exception ignored) {
        }
    }
}
