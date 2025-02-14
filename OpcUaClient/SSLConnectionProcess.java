@Component
public class SSLConnectionProcess extends Process<Boolean> {
    private final SSLConnection sslConnection;
    private CompletableFuture<Boolean> currentExecution;
    private final ProcessConfiguration processConfig;

    public SSLConnectionProcess(SSLConnection sslConnection) {
        super("SSLConnection");
        this.sslConnection = sslConnection;
        this.processConfig = setupProcessConfiguration();
        this.processData().setConfiguration(processConfig);
    }

    private ProcessConfiguration setupProcessConfiguration() {
        return new ProcessConfiguration()
            .setDetached(false)
            .setMaxCpuPercentage(25)
            .setMaxMemory(1024 * 1024 * 10) // 10MB
            .setNicePriority(5)
            .setAllowedOperations(Arrays.asList("CONNECT", "DISCONNECT"));
    }

    @Override
    protected Boolean start() {
        synchronized(processLock) {
            try {
                initialize();
                super.start();
                return true;
            } catch (Exception e) {
                fail("Error during process start: " + e.getMessage());
                return false;
            }
        }
    }

    @Override
    protected Boolean execute() {
        synchronized(processLock) {
            if (currentState() != ProcessState.RUNNING) {
                throw new IllegalStateException("Process must be in RUNNING state");
            }

            try {
                currentExecution = sslConnection.connect();
                updateResourceUsage(15.0, 5 * 1024 * 1024); // 15% CPU, 5MB RAM
                
                Boolean result = currentExecution.get(
                    processData().configuration().maxTimeout(), 
                    TimeUnit.MILLISECONDS
                );

                if (result) {
                    updateState(ProcessState.READY, "Connection established");
                } else {
                    fail("Connection failed");
                }
                return result;

            } catch (Exception e) {
                fail("Execution error: " + e.getMessage());
                return false;
            }
        }
    }

    @Override
    protected Boolean suspend() {
        synchronized(processLock) {
            try {
                super.suspend();
                if (currentExecution != null) {
                    currentExecution.cancel(true);
                }
                updateResourceUsage(0.0, memoryUsage());
                return true;
            } catch (Exception e) {
                fail("Suspension error: " + e.getMessage());
                return false;
            }
        }
    }

    @Override
    protected Boolean resume() {
        synchronized(processLock) {
            try {
                super.resume();
                updateResourceUsage(cpuUsage(), memoryUsage());
                return true;
            } catch (Exception e) {
                fail("Resume error: " + e.getMessage());
                return false;
            }
        }
    }

    @Override
    protected Boolean block() {
        synchronized(processLock) {
            try {
                super.block("Waiting for network resources");
                updateResourceUsage(0.0, memoryUsage());
                return true;
            } catch (Exception e) {
                fail("Block error: " + e.getMessage());
                return false;
            }
        }
    }

    @Override
    protected Boolean terminate() {
        synchronized(processLock) {
            try {
                if (currentExecution != null) {
                    currentExecution.cancel(true);
                }
                super.terminate();
                return true;
            } catch (Exception e) {
                fail("Termination error: " + e.getMessage());
                return false;
            }
        }
    }

    @Override
    protected Boolean fail() {
        synchronized(processLock) {
            try {
                if (currentExecution != null) {
                    currentExecution.cancel(true);
                }
                super.fail("Process execution failed");
                return false;
            } catch (Exception e) {
                log.error("Error during failure handling", e);
                return false;
            }
        }
    }
}