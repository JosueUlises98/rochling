package org.kopingenieria.domain.enums.client.network.communication;

public enum CompressionAlgorithm {
    ZIP("ZIP", ".zip"),
    GZIP("GZIP", ".gz"),
    LZMA("LZMA", ".xz"),
    BROTLI("BROTLI", ".br");

    private final String algorithmName;
    private final String fileExtension;

    CompressionAlgorithm(String algorithmName, String fileExtension) {
        this.algorithmName = algorithmName;
        this.fileExtension = fileExtension;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public static CompressionAlgorithm getByAlgorithmName(String name) {
        for (CompressionAlgorithm algorithm : values()) {
            if (algorithm.getAlgorithmName().equalsIgnoreCase(name)) {
                return algorithm;
            }
        }
        throw new IllegalArgumentException("Invalid compression algorithm name: " + name);
    }

    @Override
    public String toString() {
        return algorithmName + " (" + fileExtension + ")";
    }
}
