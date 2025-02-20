package org.kopingenieria.services;

import org.kopingenieria.tools.JsonSerializator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class SessionStorageService implements SessionStorage {

    private final File file;
    private SessionObject session;
    private final JsonSerializator<SessionObject> serializator;

    public SessionStorageService(SessionObject session, String filename) {
        this.file = new File(filename);
        this.serializator = new JsonSerializator<>();
        this.session = session;
    }

    public void storeSession() throws IOException {
        String json = serializator.serializeToString(session);
        Files.writeString(file.toPath(), json);
    }

    public SessionObject loadSession() throws IOException {
        if (file.exists()) {
            String json = Files.readString(file.toPath());
            return serializator.deserializeFromString(json, SessionObject.class);
        } else {
            throw new IOException("File not found: " + file.getPath());
        }
    }

    public BasicFileAttributes readMetadata() throws IOException {
        if (file.exists()) {
            Path filePath = file.toPath();
            return Files.readAttributes(filePath, BasicFileAttributes.class);
        } else {
            throw new IOException("File not found: " + file.getPath());
        }
    }

    public void updateSession(SessionObject updatedSession) throws IOException {
        this.session = updatedSession;
        storeSession();
    }

    public void deleteSession() throws IOException {
        if (file.exists()) {
            Files.delete(file.toPath());
        } else {
            throw new IOException("File not found: " + file.getPath());
        }
    }
}
