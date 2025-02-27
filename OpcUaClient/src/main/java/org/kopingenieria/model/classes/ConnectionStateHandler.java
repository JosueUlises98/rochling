package org.kopingenieria.model.classes;

@FunctionalInterface
public interface ConnectionStateHandler {
    void handle(QualityNetwork quality);
}
