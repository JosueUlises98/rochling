package org.kopingenieria.util;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Cola is a generic thread-safe queue implementation that supports enqueue, dequeue,
 * and other essential operations commonly used in queue data structures.
 *
 * This class leverages an array for internal storage, with dynamic resizing
 * (expansion and shrinking) based on the number of elements it holds to optimize memory usage.
 * Concurrent access safety is guaranteed with the use of a ReentrantLock.
 *
 * @param <T> The type of elements stored in this queue.
 */
public class Cola<T> {

    /**
     * Array used to store the elements in the queue. This array serves as the
     * internal storage for the queue's operations, such as enqueuing, dequeuing,
     * and resizing (expansion or contraction) when needed.
     *
     * The type of the array is generic, allowing the queue to store elements of
     * any type specified at instantiation. The size of this array is dynamically
     * managed to accommodate changes in the number of stored elements.
     *
     * The array may undergo resizing while maintaining thread safety through
     * locking mechanisms, ensuring consistent operations during concurrent access.
     */
    private T[] cola;
    /**
     * Represents the current index in the queue structure.
     * This variable tracks the position related to the queue's operations
     * and array adjustments, such as expansion or reduction.
     */
    private int index = 0;
    /**
     * Represents the number of insertions performed on the queue.
     * This value is used to track the current count of enqueued elements
     * and may be updated by operations that add elements to the queue.
     */
    private int inserciones;
    /**
     * Represents the current capacity of the queue.
     * It is dynamically adjusted during operations like expansion and contraction
     * to accommodate changes in the number of elements.
     */
    private int size;
    /**
     * A ReentrantLock instance used to ensure thread safety for concurrent access
     * to the queue's operations. This lock helps in synchronizing critical sections
     * to avoid race conditions and maintain data consistency.
     */
    private final ReentrantLock lock = new ReentrantLock(); // Para manejar concurrencia
    /**
     * Constructs a new Cola instance with a specified initial capacity.
     *
     * This constructor initializes the queue with the provided capacity,
     * setting up the internal storage array and other state variables.
     *
     * @param tam The initial size or capacity of the queue. This value
     *            determines the initial size of the internal array used
     *            for storage.
     */
    @SuppressWarnings("unchecked")
    public Cola(int tam) {
        size = tam;
        cola = (T[]) new Object[size];
    }
    /**
     * Initializes a new instance of the Cola class with a default size of 10.
     * The constructor creates an internal array for storing elements of the queue,
     * and initializes it to hold objects of the generic type.
     *
     * Suppresses unchecked warnings due to the array typecasting used for the generic array creation.
     */
    @SuppressWarnings("unchecked")
    public Cola() {
        size = 10;
        cola = (T[]) new Object[size];
    }
    /**
     * Constructs a Cola instance and initializes its internal storage with the specified array.
     * The provided array defines the initial content, capacity, and state of the queue.
     *
     * @param cola An array of type T that represents the initial elements of the queue.
     *             The queue is initialized with this array, and its size and state are
     *             determined based on the length of the array.
     */
    public Cola(T[] cola) {
        this.cola = cola;
        this.size = cola.length;
        this.index = cola.length;
        this.inserciones = cola.length;
    }
    /**
     * Enqueues an element into the queue. If the queue's current capacity is reached,
     * it expands dynamically to accommodate the new element. The method is thread-safe,
     * ensuring proper synchronization when accessed by multiple threads.
     *
     * @param elemento the element to be added to the queue
     * @param s
     */
    public void encolar(T elemento) {
        lock.lock();
        try {
            if (index == size) {
                expansion();
            }
            cola[index] = elemento;
            index++;
            inserciones++;
        } finally {
            lock.unlock();
        }
    }
    /**
     * Removes and returns the first element from the queue. The internal array
     * is resized down if necessary after the operation. Throws an exception if
     * the queue is empty at the time of invocation.
     *
     * @return The element at the front of the queue.
     * @throws IllegalStateException if the queue is empty.
     */
    public T desencolar() {
        lock.lock();
        try {
            if (estaVacia()) {
                throw new IllegalStateException("Cola vacía. No se puede desencolar.");
            }
            T header = cola[0];
            for (int i = 0; i < index - 1; i++) {
                cola[i] = cola[i + 1];
            }
            index--;
            inserciones--;
            encojida();
            return header;
        } finally {
            lock.unlock();
        }
    }
    /**
     * Retrieves the first element of the queue without removing it.
     * If the queue is empty, an IllegalStateException is thrown.
     *
     * @return The first element of the queue.
     * @throws IllegalStateException if the queue is empty.
     */
    public T peek() {
        lock.lock();
        try {
            if (estaVacia()) {
                throw new IllegalStateException("Cola vacía. No se puede obtener el primer elemento.");
            }
            return cola[0];
        } finally {
            lock.unlock();
        }
    }
    /**
     * Verifica si la cola está vacía.
     *
     * @return true si no hay elementos en la cola, false en caso contrario.
     */
    public boolean estaVacia() {
        lock.lock();
        try {
            return inserciones == 0;
        } finally {
            lock.unlock();
        }
    }
    /**
     * Retrieves the current number of elements stored in the queue.
     * The size is updated dynamically as elements are enqueued and dequeued.
     *
     * @return The number of elements currently present in the queue.
     */
    public int size() {
        lock.lock();
        try {
            return index;
        } finally {
            lock.unlock();
        }
    }
    /**
     * Clears the queue by resetting its internal storage and indices.
     *
     * This method removes all elements from the queue by reinitializing
     * the internal array to its default size, effectively clearing all stored elements.
     * The index and insertion count are also reset to zero.
     * Thread safety is ensured by using a lock during the operation.
     */
    @SuppressWarnings("unchecked")
    public void clear() {
        lock.lock();
        try {
            cola = (T[]) new Object[size];// Reiniciar el almacenamiento interno.
            index = 0;
            inserciones = 0;
        } finally {
            lock.unlock();
        }
    }
    /**
     * Expands the internal storage array of the queue to accommodate additional elements
     * when the current capacity is reached. This method dynamically doubles the size of
     * the storage by creating a new array, copying the existing elements into it, and
     * updating the reference to the new array.
     *
     * This process ensures that the queue can continue to grow without running out of
     * storage space, while maintaining thread safety through its usage within locked sections.
     *
     * Key steps of this method:
     * - A new array is allocated with double the current size.
     * - Existing elements are copied from the old array to the new array.
     * - The reference to the internal array is updated to point to the new array.
     * - The size field is updated to reflect the new capacity.
     *
     * The method suppresses type safety warnings associated with generic array creation
     * and casting, given the array's type compatibility within the implementation.
     */
    @SuppressWarnings("unchecked")
    private void expansion() {
        T[] nuevoArray = (T[]) new Object[size * 2];
        if (index >= 0) System.arraycopy(cola, 0, nuevoArray, 0, index);
        System.arraycopy(cola, 0, nuevoArray, 0, cola.length);
        cola = nuevoArray;
        size = size * 2;
    }
    /**
     * Reduces the size of the internal array storing elements of the queue if certain conditions are met.
     * The method halves the size of the array when the number of elements in the queue is equal to
     * or less than half the current size of the array and the resulting size is at least 1.
     *
     * Specifically, if the {@code index} is less than or equal to half the size of the array,
     * and the resulting halved size is greater than or equal to 1, it creates a new array
     * with half the size of the original, copies the contents of the current array to the new
     * one up to the number of elements present, and reassigns the internal array to this newly
     * created one. The size attribute is also updated to reflect the reduced size.
     *
     * This method helps optimize memory usage for the queue by dynamically shrinking its storage
     * when the number of stored elements decreases significantly.
     */
    @SuppressWarnings("unchecked")
    private void encojida() {
        if (index <= size / 2 && size / 2 >= 1) {
            T[] nuevoArray = (T[]) new Object[size / 2];
            System.arraycopy(cola, 0, nuevoArray, 0, index);
            cola = nuevoArray;
            size = size / 2;
        }
    }
    /**
     * Prints all the elements currently stored in the `cola` array up to the specified index.
     *
     * The method acquires a lock before accessing the `cola` array to ensure thread safety
     * when multiple threads are interacting with the same instance of the class.
     * Upon completion or in case of an exception, the lock is released in the `finally` block.
     *
     * This method assumes that `cola` is an array of elements and iterates from the
     * first index up to the last valid index specified by `index`.
     * Each element in this range is printed to the standard output.
     *
     * Thread Safety:
     * - The `lock` ensures that concurrent access to the `cola` array does not cause data inconsistencies.
     * - This method must be used in a multithreaded environment to avoid race conditions with other
     *   methods modifying `cola` or `index`.
     *
     * Implementation Notes:
     * - It is expected that `index` is a valid value representing the count of elements in `cola`.
     * - If `cola` or elements within it are null, the `println` operation may result in null being printed.
     * - Intended for debugging or visual verification of stored elements.
     */
    public void getValues() {
        lock.lock();
        try {
            for (int i = 0; i < index; i++) {
                System.out.println(cola[i]);
            }
        } finally {
            lock.unlock();
        }
    }
}
