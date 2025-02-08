package org.kopingenieria.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Arrays;
import java.util.Objects;

/**
 * A custom implementation of a string builder that provides methods to build
 * and manipulate a sequence of characters or other objects. This class is designed
 * with a fixed-size array and allows for appending characters, integers, or strings,
 * and building a final string representation of the contents.
 *
 * This class is thread-safe due to the usage of synchronized methods for modifying
 * and accessing the internal state.
 */
public final class StringBuilderClass {

    /**
     * Logger instance for logging messages related to the operations and state of
     * the {@code StringBuilderClass}. It provides logging capabilities and is
     * initialized using the class name to ensure context-specific logging.
     */
    private static final Logger logger = LogManager.getLogger(StringBuilderClass.class);
    /**
     * An array of objects used to store various types of elements, such as characters,
     * integers, or strings, for constructing the internal state of the StringBuilderClass.
     * The array has a fixed size of 30 and supports sequential addition of elements.
     * When the array is filled up to its capacity, further additions will result in
     * an IndexOutOfBoundsException.
     */
    private Object[] chars;
    /**
     * Represents the current position in the array where the next element will be added.
     * Acts as a pointer to track the number of elements inserted in the array.
     * Its value increments each time an element is appended successfully using the `append` methods.
     * If `index` reaches the length of the array, further insertions trigger an IndexOutOfBoundsException.
     */
    private int index = 0;
    /**
     * Represents the current capacity or allocated size of the internal character array
     * used by the StringBuilderClass. It defines the maximum number of characters that
     * can be stored in the array before encountering an IndexOutOfBoundsException.
     *
     * This value directly determines when the character array needs to be resized or
     * operations may throw exceptions if the size limit is exceeded.
     */
    private int size;
    /**
     * Default constructor that initializes the internal array with a size of 30.
     */
    public StringBuilderClass() {
        this.size = 30;
        this.chars = new Object[size];
    }
    /**
     * Constructor that allows specifying the initial size of the internal array.
     *
     * @param capacity the desired initial size for the internal array.
     * @throws IllegalArgumentException if the capacity is less than or equal to zero.
     */
    public StringBuilderClass(int capacity) {
        if (capacity <= 0) {
            IllegalArgumentException exception = new IllegalArgumentException("Capacity must be greater than zero.");
            logger.error("Failed to create StringBuilderClass due to invalid capacity: {}", capacity, exception);
            throw exception;
        }
        this.size = capacity;
        this.chars = new Object[size];
    }
    /**
     * Constructor that initializes the internal array with the characters of the given initial string.
     *
     * @param initialString the string used to initialize the array.
     * @throws NullPointerException if the provided string is null.
     */
    public StringBuilderClass(String initialString) {
        if (initialString == null) {
            NullPointerException exception = new NullPointerException("Initial string cannot be null.");
            logger.error("Failed to create StringBuilderClass due to null initialization string.", exception);
            throw exception;
        }
        this.size = Math.max(30, initialString.length() + 10); // Default size plus buffer space
        this.chars = new Object[size];
        for (int i = 0; i < initialString.length(); i++) {
            this.chars[i] = initialString.charAt(i);
        }
        this.index = initialString.length();
    }
    public synchronized StringBuilderClass append(char c) {
        if (index == chars.length) {
            IndexOutOfBoundsException exception = new IndexOutOfBoundsException("Index: " + index + ", Size: " + chars.length);
            logger.error("Failed to append character: '{}' due to insufficient capacity.", c, exception);
            throw exception;
        }
        chars[index] = c;
        index++;
        return this;
    }
    /**
     * Appends an integer to the internal character array. If the array is full, an
     * IndexOutOfBoundsException is thrown.
     *
     * @param num the integer to be appended
     * @return the current instance of StringBuilderClass for method chaining
     * @throws IndexOutOfBoundsException if the character array is full
     */
    public synchronized StringBuilderClass append(int num) {
        if (index == chars.length) {
            IndexOutOfBoundsException exception = new IndexOutOfBoundsException("Index: " + index + ", Size: " + chars.length);
            logger.error("Failed to append character: '{}' due to insufficient capacity.",num, exception);
            throw exception;
        }
        chars[index] = num;
        index++;
        return this;
    }
    /**
     * Appends the specified string to the internal character array. If the
     * current index exceeds the size of the array, an IndexOutOfBoundsException
     * is thrown.
     *
     * @param str the string to append to the internal character array
     * @return the current instance of StringBuilderClass
     * @throws IndexOutOfBoundsException if the index exceeds the size of the internal array
     */
    public synchronized StringBuilderClass append(String str) {
        if (str == null) {
            NullPointerException exception = new NullPointerException("Attempted to append a null string.");
            logger.warn("Cannot append null string to StringBuilderClass.", exception);
            throw exception;
        } else if (index == chars.length) {
            IndexOutOfBoundsException exception = new IndexOutOfBoundsException("Index: " + index + ", Size: " + chars.length);
            logger.error("Failed to append character: '{}' due to insufficient capacity.", str, exception);
            throw exception;
        }
        for (int i = 0; i < str.length(); i++) {
            chars[index] = str.charAt(i);
            index++;
        }
        return this;
    }
    /**
     * Builds and returns a string representation of the characters stored in the internal array.
     *
     * @return A string representation of the current state of the internal character array.
     */
    public synchronized String build() {
        Object[] resultArray = new Object[index];
        for (int i = 0; i < index; i++) {
                resultArray[i] = chars[i];
        }
        return Arrays.toString(resultArray).replace("[", "").replace("]", "").replace(",", "");
    }
    /**
     * Compares this instance with another object to determine equality.
     * Two instances of StringBuilderClass are considered equal if they have the same index value
     * and their chars arrays are deeply equal.
     *
     * @param o the object to compare with the current instance for equality
     * @return true if the specified object is equal to this instance; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StringBuilderClass that = (StringBuilderClass) o;
        return index == that.index && Objects.deepEquals(chars, that.chars);
    }
    /**
     * Returns the hash code value for this StringBuilderClass instance. The hash code is computed based on
     * the hash code of the chars array and the index field.
     *
     * @return an integer hash code value representing this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(chars), index);
    }
}
