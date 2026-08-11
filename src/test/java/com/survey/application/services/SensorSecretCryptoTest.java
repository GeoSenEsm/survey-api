package com.survey.application.services;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SensorSecretCryptoTest {
    private static final String SECRET = "00112233445566778899AABBCCDDEEFF";
    private static final String KEY = Base64.getEncoder()
            .encodeToString("0123456789ABCDEF0123456789ABCDEF".getBytes(StandardCharsets.UTF_8));

    @Test
    void encrypt_usesAuthenticatedDeviceScopedEncryption() {
        SensorSecretCrypto crypto = new SensorSecretCrypto(KEY);
        UUID sensorId = UUID.randomUUID();

        SensorSecretCrypto.EncryptedSecret encrypted = crypto.encrypt(sensorId, "bind_key", SECRET);

        assertThat(encrypted.nonce()).hasSize(12);
        assertThat(encrypted.ciphertext()).isNotEqualTo(SECRET.getBytes(StandardCharsets.UTF_8));
        assertThat(crypto.decrypt(sensorId, "bind_key", encrypted.nonce(), encrypted.ciphertext()))
                .isEqualTo(SECRET);
        assertThatThrownBy(() -> crypto.decrypt(
                UUID.randomUUID(), "bind_key", encrypted.nonce(), encrypted.ciphertext()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void encrypt_rejectsNonHexadecimalValueAsBadRequest() {
        SensorSecretCrypto crypto = new SensorSecretCrypto(KEY);
        UUID sensorId = UUID.randomUUID();

        assertThatThrownBy(() -> crypto.encrypt(sensorId, "bind_key", "not-hexadecimal"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hexadecimal");
    }

    @Test
    void operations_failClosedWhenEnvironmentKeyIsMissingOrInvalid() {
        UUID sensorId = UUID.randomUUID();

        assertThatThrownBy(() -> new SensorSecretCrypto("").encrypt(sensorId, "bind_key", SECRET))
                .hasMessageContaining("SENSOR_SECRET_ENCRYPTION_KEY is required");
        assertThatThrownBy(() -> new SensorSecretCrypto("not-base64").encrypt(sensorId, "bind_key", SECRET))
                .hasMessageContaining("valid Base64");
    }
}
