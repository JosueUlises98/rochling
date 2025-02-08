package org.kopingenieria.model;


public class MainSerialization {
    public static void main(String[] args) {
        JsonSerializator<SessionObject> serializator = new JsonSerializator<>();
        SessionObject sessionObject = new SessionObject(
                "session-123",
                "session-1",
                "123",
                SessionStatus.EXPIRED,
                "2025-01-10T23:59:59",
                "2025-01-15T14:30:00"
        );
        byte[] bytes = serializator.serializeToBytes(sessionObject);
        for (byte b : bytes) {
            System.out.print(b+" ");
        }
        SessionObject object = serializator.deserializeFromBytes(bytes, SessionObject.class);
        System.out.println(object);
    }
}
