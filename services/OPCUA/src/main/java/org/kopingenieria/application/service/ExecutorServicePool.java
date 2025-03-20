package org.kopingenieria.application.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServicePool {

    private final ExecutorService executorService;

    public ExecutorServicePool(int poolSize) {
        // Crea un pool de hilos del tamaño especificado
        this.executorService = Executors.newFixedThreadPool(poolSize);
    }

    public void submitTask(Process<?> processConnection) {
        // Ejecuta las operaciones en un hilo independiente
        executorService.submit(processConnection::);
    }

    public void shutdown() {
        System.out.println("Cerrando pool de hilos...");
        executorService.shutdown();
    }
}
