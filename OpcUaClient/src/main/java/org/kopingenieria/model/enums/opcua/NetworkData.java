package org.kopingenieria.model.enums.opcua;

public enum NetworkData {

    CONSTANT_1(1),
    CONSTANT_2(2),
    CONSTANT_3(3),
    CONSTANT_4(4),
    CONSTANT_5(5);

    private final int value;

    NetworkData(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
