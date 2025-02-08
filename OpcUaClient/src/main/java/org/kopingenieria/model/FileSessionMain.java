package org.kopingenieria.model;


import org.kopingenieria.services.SessionStorageService;

import java.io.IOException;

public class FileSessionMain {
    public static void main(String[] args) {
        SessionObject sessionObject = new SessionObject("123","session-1","user123", SessionStatus.ACTIVE,"2025-01-10T23:59:59","2025-01-10T23:59:59");
        SessionObject sessionObject1 = new SessionObject("987","session-2","user-2",SessionStatus.EXPIRED,"2025-01-10T23:59:59","2025-01-10T23:59:59");
        SessionStorageService sessionStorage = new SessionStorageService(sessionObject,"session1.json");
        SessionStorageService sessionStorage1 = new SessionStorageService(sessionObject1,"session2.json");
        try {
            sessionStorage.storeSession();
            sessionStorage1.storeSession();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
