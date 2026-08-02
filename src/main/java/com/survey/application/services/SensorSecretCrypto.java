package com.survey.application.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class SensorSecretCrypto {
    private static final int KEY_BYTES = 32;
    private static final int NONCE_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final String configuredKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public SensorSecretCrypto(@Value("${sensor.secret-encryption-key:}") String configuredKey) {
        this.configuredKey = configuredKey;
    }

    public EncryptedSecret encrypt(UUID sensorMacId, String name, String hexadecimalValue) {
        byte[] nonce = new byte[NONCE_BYTES];
        secureRandom.nextBytes(nonce);
        byte[] plaintext = HexFormat.of().parseHex(hexadecimalValue);
        return new EncryptedSecret(nonce, crypt(Cipher.ENCRYPT_MODE, sensorMacId, name, nonce, plaintext));
    }

    public String decrypt(UUID sensorMacId, String name, byte[] nonce, byte[] ciphertext) {
        return HexFormat.of().withUpperCase()
                .formatHex(crypt(Cipher.DECRYPT_MODE, sensorMacId, name, nonce, ciphertext));
    }

    private byte[] crypt(int mode, UUID sensorMacId, String name, byte[] nonce, byte[] input) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(mode, new SecretKeySpec(requireKey(), "AES"), new GCMParameterSpec(TAG_BITS, nonce));
            cipher.updateAAD((sensorMacId + ":" + name).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return cipher.doFinal(input);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Sensor secret encryption or authentication failed.", exception);
        }
    }

    private byte[] requireKey() {
        if (configuredKey == null || configuredKey.isBlank()) {
            throw new IllegalStateException(
                    "SENSOR_SECRET_ENCRYPTION_KEY is required for sensor secret operations.");
        }
        try {
            byte[] key = Base64.getDecoder().decode(configuredKey);
            if (key.length != KEY_BYTES) {
                throw new IllegalStateException(
                        "SENSOR_SECRET_ENCRYPTION_KEY must be Base64 encoding of exactly 32 bytes.");
            }
            return key;
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("SENSOR_SECRET_ENCRYPTION_KEY must be valid Base64.", exception);
        }
    }

    public record EncryptedSecret(byte[] nonce, byte[] ciphertext) {}
}
