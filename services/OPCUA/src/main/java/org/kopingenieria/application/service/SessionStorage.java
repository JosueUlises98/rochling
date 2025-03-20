package org.kopingenieria.application.service;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;

public interface SessionStorage {
    void storeSession() throws IOException;
    SessionObject loadSession() throws IOException;
    BasicFileAttributes readMetadata() throws IOException;
    void updateSession(SessionObject updatedSession) throws IOException;
    void deleteSession() throws IOException;
}
