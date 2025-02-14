package org.kopingenieria.services;


import jakarta.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents an abstract process that can be executed asynchronously. This class
 * provides a framework for defining processes with a unique name and identifier,
 * execution control, and lifecycle tracking (start and end times).
 *
 * @param <T> The type of the result produced by this process.
 */
public abstract class Process<T> {

    // Identificación del proceso
    private final String processId;
    private String name;
    private String description;

    // Información temporal
    private LocalDateTime creationTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime lastUpdateTime;

    // Estado y ciclo de vida
    private ProcessState currentState;
    private List<ProcessStateTransition> stateHistory;

    // Recursos y métricas
    private int priority;
    private double cpuUsage;
    private long memoryUsage;
    private String owner;

    // Datos del proceso
    private ProcessData processData;
    private List<ProcessDependency> dependencies;

    //Lock de los subprocesos
    protected final Object processLock = new Object();

    protected Process(String name) {
        this.processId = UUID.randomUUID().toString();
        this.name = name;
        this.creationTime = LocalDateTime.now();
        this.currentState = ProcessState.CREATED;
        this.stateHistory = new ArrayList<>();
        this.dependencies = new ArrayList<>();
        this.processData = new ProcessData();
        addStateTransition(ProcessState.CREATED);
    }

    // Enumeración de estados posibles del proceso
    public enum ProcessState {
        CREATED,        // Proceso creado pero no iniciado
        INITIALIZED,    // Proceso inicializado con recursos
        READY,         // Listo para ejecutar
        RUNNING,       // En ejecución
        WAITING,       // Esperando recursos o eventos
        SUSPENDED,     // Suspendido temporalmente
        BLOCKED,       // Bloqueado por dependencias
        TERMINATED,    // Terminado normalmente
        FAILED,        // Terminado con error
        ZOMBIE        // Terminado pero con recursos sin liberar
    }

    // Clase para almacenar transiciones de estado
    public static class ProcessStateTransition {

        private final ProcessState fromState;
        private final ProcessState toState;
        private final LocalDateTime transitionTime;
        private final String reason;

        public ProcessStateTransition(ProcessState fromState, ProcessState toState, String reason) {
            this.fromState = fromState;
            this.toState = toState;
            this.transitionTime = LocalDateTime.now();
            this.reason = reason;
        }

        public ProcessState fromState() {
            return fromState;
        }

        public ProcessState toState() {
            return toState;
        }

        public LocalDateTime transitionTime() {
            return transitionTime;
        }

        public String reason() {
            return reason;
        }
    }

    // Clase para almacenar datos específicos del proceso
    public static class ProcessData {

        private Map<String, Object> variables;
        private List<String> arguments;
        private String workingDirectory;
        private Map<String, String> environmentVariables;
        private ProcessConfiguration configuration;

        public ProcessData() {
            this.variables = new HashMap<>();
            this.arguments = new ArrayList<>();
            this.environmentVariables = new HashMap<>();
            this.configuration = new ProcessConfiguration();
        }

        public Map<String, Object> variables() {
            return variables;
        }

        public ProcessData setVariables(Map<String, Object> variables) {
            this.variables = variables;
            return this;
        }

        public List<String> arguments() {
            return arguments;
        }

        public ProcessData setArguments(List<String> arguments) {
            this.arguments = arguments;
            return this;
        }

        public String workingDirectory() {
            return workingDirectory;
        }

        public ProcessData setWorkingDirectory(String workingDirectory) {
            this.workingDirectory = workingDirectory;
            return this;
        }

        public Map<String, String> environmentVariables() {
            return environmentVariables;
        }

        public ProcessData setEnvironmentVariables(Map<String, String> environmentVariables) {
            this.environmentVariables = environmentVariables;
            return this;
        }

        public ProcessConfiguration configuration() {
            return configuration;
        }

        public ProcessData setConfiguration(ProcessConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }
    }


    public static class ProcessConfiguration {

        private boolean isDetached;
        private int nicePriority;
        private long maxMemory;
        private int maxCpuPercentage;
        private List<String> allowedOperations;
        private SecurityContext securityContext;

        public ProcessConfiguration() {
            this.allowedOperations = new ArrayList<>();
            this.securityContext = new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return null;
                }

                @Override
                public boolean isUserInRole(String s) {
                    return false;
                }

                @Override
                public boolean isSecure() {
                    return false;
                }

                @Override
                public String getAuthenticationScheme() {
                    return "";
                }
            };
        }

        public boolean isDetached() {
            return isDetached;
        }

        public ProcessConfiguration setDetached(boolean detached) {
            isDetached = detached;
            return this;
        }

        public int nicePriority() {
            return nicePriority;
        }

        public ProcessConfiguration setNicePriority(int nicePriority) {
            this.nicePriority = nicePriority;
            return this;
        }

        public long maxMemory() {
            return maxMemory;
        }

        public ProcessConfiguration setMaxMemory(long maxMemory) {
            this.maxMemory = maxMemory;
            return this;
        }

        public int maxCpuPercentage() {
            return maxCpuPercentage;
        }

        public ProcessConfiguration setMaxCpuPercentage(int maxCpuPercentage) {
            this.maxCpuPercentage = maxCpuPercentage;
            return this;
        }

        public List<String> allowedOperations() {
            return allowedOperations;
        }

        public ProcessConfiguration setAllowedOperations(List<String> allowedOperations) {
            this.allowedOperations = allowedOperations;
            return this;
        }

        public SecurityContext securityContext() {
            return securityContext;
        }

        public ProcessConfiguration setSecurityContext(SecurityContext securityContext) {
            this.securityContext = securityContext;
            return this;
        }
    }

    // Clase para manejo de dependencias
    public static class ProcessDependency {

        private final String dependencyProcessId;
        private DependencyType type;
        private DependencyState state;

        public ProcessDependency(String dependencyProcessId) {
            this.dependencyProcessId = dependencyProcessId;
        }

        public enum DependencyType {
            HARD_DEPENDENCY,    // El proceso no puede continuar sin esta dependencia
            SOFT_DEPENDENCY,    // El proceso puede continuar con degradación
            OPTIONAL           // El proceso puede continuar normalmente
        }

        public enum DependencyState {
            PENDING,
            SATISFIED,
            FAILED
        }

        public String dependencyProcessId() {
            return dependencyProcessId;
        }

        public DependencyType type() {
            return type;
        }

        public ProcessDependency setType(DependencyType type) {
            this.type = type;
            return this;
        }

        public DependencyState state() {
            return state;
        }

        public ProcessDependency setState(DependencyState state) {
            this.state = state;
            return this;
        }
    }

    // Métodos del ciclo de vida
    public void initialize() {
        validateStateTransition(ProcessState.INITIALIZED);
        // Inicialización de recursos
        updateState(ProcessState.INITIALIZED, "Process initialization completed");
    }

    public void start() {
        validateStateTransition(ProcessState.RUNNING);
        this.startTime = LocalDateTime.now();
        updateState(ProcessState.RUNNING, "Process started execution");
    }

    public void suspend() {
        validateStateTransition(ProcessState.SUSPENDED);
        updateState(ProcessState.SUSPENDED, "Process suspended");
    }

    public void resume() {
        if (currentState != ProcessState.SUSPENDED) {
            throw new IllegalStateException("Process must be suspended to resume");
        }
        updateState(ProcessState.RUNNING, "Process resumed");
    }

    public void block(String reason) {
        validateStateTransition(ProcessState.BLOCKED);
        updateState(ProcessState.BLOCKED, reason);
    }

    public void terminate() {
        validateStateTransition(ProcessState.TERMINATED);
        this.endTime = LocalDateTime.now();
        updateState(ProcessState.TERMINATED, "Process terminated normally");
        cleanup();
    }

    public void fail(String reason) {
        validateStateTransition(ProcessState.FAILED);
        this.endTime = LocalDateTime.now();
        updateState(ProcessState.FAILED, reason);
        cleanup();
    }

    // Métodos de soporte
    private void validateStateTransition(ProcessState newState) {
        if (!isValidTransition(currentState, newState)) {
            throw new IllegalStateException(
                    String.format("Invalid state transition from %s to %s", currentState, newState)
            );
        }
    }

    private boolean isValidTransition(ProcessState fromState, ProcessState toState) {
        // Definir reglas de transición válidas
        return switch (fromState) {
            case CREATED -> toState == ProcessState.INITIALIZED;
            case INITIALIZED -> toState == ProcessState.READY || toState == ProcessState.FAILED;
            case READY -> toState == ProcessState.RUNNING || toState == ProcessState.FAILED;
            case RUNNING -> toState == ProcessState.SUSPENDED ||
                    toState == ProcessState.BLOCKED ||
                    toState == ProcessState.TERMINATED ||
                    toState == ProcessState.FAILED;
            case SUSPENDED -> toState == ProcessState.RUNNING ||
                    toState == ProcessState.TERMINATED ||
                    toState == ProcessState.FAILED;
            case BLOCKED -> toState == ProcessState.READY ||
                    toState == ProcessState.TERMINATED ||
                    toState == ProcessState.FAILED;
            default -> false;
        };
    }

    private void updateState(ProcessState newState, String reason) {
        ProcessState oldState = this.currentState;
        this.currentState = newState;
        this.lastUpdateTime = LocalDateTime.now();
        addStateTransition(oldState, newState, reason);
        log.info("Process {} state changed from {} to {}: {}",
                processId, oldState, newState, reason);
    }

    private void addStateTransition(ProcessState newState) {
        addStateTransition(null, newState, "Initial state");
    }

    private void addStateTransition(ProcessState fromState, ProcessState toState, String reason) {
        stateHistory.add(new ProcessStateTransition(fromState, toState, reason));
    }

    private void cleanup() {
        // Liberar recursos
        this.cpuUsage = 0;
        this.memoryUsage = 0;
    }

    // Métodos de monitoreo y gestión
    public List<ProcessStateTransition> getStateHistory() {
        return new ArrayList<>(stateHistory);
    }

    public Duration getRunningTime() {
        if (startTime == null) {
            return Duration.ZERO;
        }
        LocalDateTime endPoint = endTime != null ? endTime : LocalDateTime.now();
        return Duration.between(startTime, endPoint);
    }

    public void updateResourceUsage(double cpu, long memory) {
        this.cpuUsage = cpu;
        this.memoryUsage = memory;
        this.lastUpdateTime = LocalDateTime.now();
    }

    public boolean isDependencyMet(String dependencyProcessId) {
        return dependencies.stream()
                .filter(dep -> dep.dependencyProcessId().equals(dependencyProcessId))
                .anyMatch(dep -> dep.state() == ProcessDependency.DependencyState.SATISFIED);
    }

    public void addDependency(String dependencyProcessId, ProcessDependency.DependencyType type) {
        ProcessDependency dependency = new ProcessDependency(dependencyProcessId);
        dependency.setType(type);
        dependency.setState(ProcessDependency.DependencyState.PENDING);
        dependencies.add(dependency);
    }

    protected String processId() {
        return processId;
    }

    protected String name() {
        return name;
    }

    protected String description() {
        return description;
    }

    protected LocalDateTime creationTime() {
        return creationTime;
    }

    protected LocalDateTime startTime() {
        return startTime;
    }

    protected LocalDateTime endTime() {
        return endTime;
    }

    protected LocalDateTime lastUpdateTime() {
        return lastUpdateTime;
    }

    protected ProcessState currentState() {
        return currentState;
    }

    protected List<ProcessStateTransition> stateHistory() {
        return stateHistory;
    }

    protected int priority() {
        return priority;
    }

    protected double cpuUsage() {
        return cpuUsage;
    }

    protected long memoryUsage() {
        return memoryUsage;
    }

    protected String owner() {
        return owner;
    }

    protected ProcessData processData() {
        return processData;
    }

    protected List<ProcessDependency> dependencies() {
        return dependencies;
    }

    protected abstract T start();

    protected abstract T execute();

    protected abstract T suspend();

    protected abstract T resume();

    protected abstract T block();

    protected abstract T terminate();

    protected abstract T fail();


}
