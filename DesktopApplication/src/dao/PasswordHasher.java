package dao;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;

public final class PasswordHasher {
    private static final int ITERATIONS = 310_000;
    private static final int KEY_BITS = 256;

    private PasswordHasher() { }

    public static String newSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return toHex(salt);
    }

    public static String hash(char[] password, String saltHex) {
        PBEKeySpec specification = new PBEKeySpec(password, fromHex(saltHex), ITERATIONS, KEY_BITS);
        try {
            byte[] result = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(specification).getEncoded();
            return toHex(result);
        } catch (Exception exception) {
            throw new IllegalStateException("Password hashing is unavailable", exception);
        } finally {
            specification.clearPassword();
        }
    }

    public static boolean matches(char[] password, String salt, String expectedHash) {
        byte[] actual = fromHex(hash(password, salt));
        byte[] expected = fromHex(expectedHash);
        return MessageDigest.isEqual(actual, expected);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) result.append(String.format(Locale.ROOT, "%02x", value & 0xff));
        return result.toString();
    }

    private static byte[] fromHex(String value) {
        byte[] result = new byte[value.length() / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) Integer.parseInt(value.substring(i * 2, i * 2 + 2), 16);
        }
        return result;
    }
}
