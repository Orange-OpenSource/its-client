package com.orange.iot3mobility.message.cam.v230.model;

/**
 * CamAsn1Payload v2.3.0
 * <p>
 * Base64-encoded ASN.1 payload alternative.
 *
 * @param version ASN.1 PDU version
 * @param payload Base64-encoded ASN.1 binary payload
 */
public record CamAsn1Payload(
        String version,
        String payload) implements CamMessage230 {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String version;
        private String payload;

        private Builder() {}

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public CamAsn1Payload build() {
            return new CamAsn1Payload(
                    requireNonNull(version, "version"),
                    requireNonNull(payload, "payload"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
