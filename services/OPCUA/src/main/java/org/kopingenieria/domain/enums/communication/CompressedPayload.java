package org.kopingenieria.domain.enums.communication;


public enum CompressedPayload {

    VALID,
    INVALID,
    PARTIALLY_COMPRESSED;

    private byte[] data;
    private CompressionAlgorithm algorithm;
    private int originalSize;
    private int compressedSize;

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setAlgorithm(CompressionAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public void setOriginalSize(int originalSize) {
        this.originalSize = originalSize;
    }

    public void setCompressedSize(int compressedSize) {
        this.compressedSize = compressedSize;
    }

    public double getCompressionRatio() {
        if (compressedSize == 0 || originalSize == 0) {
            return 0.0;
        }
        return (double) compressedSize / originalSize;
    }

    public boolean isValidPayload() {
        return data != null && data.length > 0 && originalSize > 0 && compressedSize > 0;
    }
}
