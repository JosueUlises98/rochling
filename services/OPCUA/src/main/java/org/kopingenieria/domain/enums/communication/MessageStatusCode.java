package org.kopingenieria.domain.enums.communication;

public enum MessageStatusCode {

    GOOD(0x00000000, "The operation was successful"),
    UNSPECIFIED_ERROR(0x80010000, "An unspecified error occurred"),
    BAD_INTERNAL_ERROR(0x80020000, "An internal error occurred"),
    BAD_OUT_OF_MEMORY(0x80030000, "Not enough memory to complete the operation"),
    BAD_COMMUNICATION_ERROR(0x80050000, "A communication error occurred"),
    BAD_ENCODING_ERROR(0x80060000, "Encoding or decoding operation failed"),
    BAD_DECODING_ERROR(0x80070000, "Decoding of the message failed"),
    BAD_SENDER_CERTIFICATE_INVALID(0x80080000, "Sender certificate is invalid"),
    BAD_REQUEST_TYPE_INVALID(0x800A0000, "The request type is not valid or supported"),
    BAD_RESPONSE_TOO_LARGE(0x800B0000, "The response size exceeds the limits"),
    BAD_SESSION_TIMEOUT(0x800F0000, "The session timed out due to inactivity"),
    BAD_LICENSE_EXPIRED(0x80130000, "The software license has expired"),
    BAD_USER_ACCESS_DENIED(0x801F0000, "User access is denied"),
    BAD_CRC_CHECK_FAILED(0x80200000, "CRC check failed for the message"),
    BAD_TIMEOUT(0x802A0000, "The operation timed out"),
    BAD_CERTIFICATE_REVOKED(0x80320000, "The certificate has been revoked"),
    GOOD_SUBSCRIPTION_TRANSFERRED(0x00B80000, "Subscription successfully transferred"),
    GOOD_RESULTS_MAY_BE_INCOMPLETE(0x00BA0000, "Results may not be complete");

    private final int code;
    private final String description;

    MessageStatusCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static MessageStatusCode fromCode(int code) {
        for (MessageStatusCode statusCode : MessageStatusCode.values()) {
            if (statusCode.code == code) {
                return statusCode;
            }
        }
        throw new IllegalArgumentException("Invalid status code: 0x" + Integer.toHexString(code));
    }
}
