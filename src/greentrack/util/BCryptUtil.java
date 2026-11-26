package greentrack.util;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Utilidad criptográfica pura para el almacenamiento y verificación de contraseñas
 * mediante el algoritmo OpenBSD / jBCrypt ($2a$).
 * Implementa EksBlowfish completo sin librerías externas.
 */
public class BCryptUtil {

    private static final int BCRYPT_SALT_LEN = 16;
    private static final int DEFAULT_LOG_ROUNDS = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final int[] BCRYPT_CIPHERTEXT = {
        0x4f727068, 0x65616e42, 0x65686f6c,
        0x64657253, 0x63727944, 0x6f756274
    };

    private static final char[] BCRYPT_BASE64_CODE = {
        '.', '/', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
        'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
        'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9'
    };

    private static final byte[] BCRYPT_INDEX = new byte[128];
    static {
        Arrays.fill(BCRYPT_INDEX, (byte) -1);
        for (int i = 0; i < BCRYPT_BASE64_CODE.length; i++) {
            BCRYPT_INDEX[BCRYPT_BASE64_CODE[i]] = (byte) i;
        }
    }


    /**
     * Genera un hash BCrypt para una contraseña en texto plano con el costo por defecto (10).
     */
    public static String hashPassword(String password) {
        return hashPassword(password, DEFAULT_LOG_ROUNDS);
    }

    /**
     * Genera un hash BCrypt con costo de rondas específico (4 a 31).
     */
    public static String hashPassword(String password, int logRounds) {
        if (password == null) {
            throw new IllegalArgumentException("La contraseña no puede ser nula");
        }
        if (logRounds < 4 || logRounds > 31) {
            throw new IllegalArgumentException("logRounds inválido (debe ser entre 4 y 31)");
        }
        byte[] salt = new byte[BCRYPT_SALT_LEN];
        RANDOM.nextBytes(salt);
        return crypt(password, salt, logRounds);
    }

    /**
     * Comprueba si una contraseña en texto plano coincide con un hash BCrypt.
     */
    public static boolean checkPassword(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        if (hash.length() != 60 || hash.charAt(0) != '$' || hash.charAt(3) != '$' || hash.charAt(6) != '$') {
            return false;
        }
        try {
            int logRounds = Integer.parseInt(hash.substring(4, 6));
            if (logRounds < 4 || logRounds > 31) {
                return false;
            }
            String saltStr = hash.substring(7, 29);
            byte[] salt = decodeBase64(saltStr, BCRYPT_SALT_LEN);
            String calculated = crypt(password, salt, logRounds);
            return slowEquals(calculated, hash);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean slowEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }

    private static String crypt(String password, byte[] salt, int logRounds) {
        byte[] passBytes = (password + "\0").getBytes(StandardCharsets.UTF_8);

        int[] P = new int[18];
        int[] S = new int[1024];

        System.arraycopy(BCryptConstants.P_ORIG, 0, P, 0, 18);
        System.arraycopy(BCryptConstants.S_ORIG, 0, S, 0, 1024);

        ekskey(P, S, salt, passBytes);

        int rounds = 1 << logRounds;
        for (int i = 0; i < rounds; i++) {
            key(P, S, passBytes);
            key(P, S, salt);
        }

        int[] cdata = Arrays.copyOf(BCRYPT_CIPHERTEXT, BCRYPT_CIPHERTEXT.length);
        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < (cdata.length >> 1); j++) {
                encipher(P, S, cdata, j << 1);
            }
        }

        byte[] rawOutput = new byte[cdata.length * 4];
        for (int i = 0, j = 0; i < cdata.length; i++) {
            rawOutput[j++] = (byte) ((cdata[i] >> 24) & 0xff);
            rawOutput[j++] = (byte) ((cdata[i] >> 16) & 0xff);
            rawOutput[j++] = (byte) ((cdata[i] >> 8) & 0xff);
            rawOutput[j++] = (byte) (cdata[i] & 0xff);
        }

        StringBuilder sb = new StringBuilder(60);
        sb.append("$2a$");
        if (logRounds < 10) sb.append('0');
        sb.append(logRounds);
        sb.append('$');
        sb.append(encodeBase64(salt, BCRYPT_SALT_LEN));
        sb.append(encodeBase64(rawOutput, 23));
        return sb.toString();
    }

    private static void encipher(int[] P, int[] S, int lr[], int off) {
        int l = lr[off];
        int r = lr[off + 1];

        l ^= P[0];
        r ^= ((S[(l >>> 24) & 0xff] + S[256 + ((l >>> 16) & 0xff)]) ^ S[512 + ((l >>> 8) & 0xff)]) + S[768 + (l & 0xff)] ^ P[1];
        l ^= ((S[(r >>> 24) & 0xff] + S[256 + ((r >>> 16) & 0xff)]) ^ S[512 + ((r >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[2];
        r ^= ((S[(l >>> 24) & 0xff] + S[256 + ((l >>> 16) & 0xff)]) ^ S[512 + ((l >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[3];
        l ^= ((S[(r >>> 24) & 0xff] + S[256 + ((r >>> 16) & 0xff)]) ^ S[512 + ((r >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[4];
        r ^= ((S[(l >>> 24) & 0xff] + S[256 + ((l >>> 16) & 0xff)]) ^ S[512 + ((l >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[5];
        l ^= ((S[(r >>> 24) & 0xff] + S[256 + ((r >>> 16) & 0xff)]) ^ S[512 + ((r >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[6];
        r ^= ((S[(l >>> 24) & 0xff] + S[256 + ((l >>> 16) & 0xff)]) ^ S[512 + ((r >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[7];
        l ^= ((S[(r >>> 24) & 0xff] + S[256 + ((r >>> 16) & 0xff)]) ^ S[512 + ((r >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[8];
        r ^= ((S[(l >>> 24) & 0xff] + S[256 + ((l >>> 16) & 0xff)]) ^ S[512 + ((l >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[9];
        l ^= ((S[(r >>> 24) & 0xff] + S[256 + ((r >>> 16) & 0xff)]) ^ S[512 + ((r >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[10];
        r ^= ((S[(l >>> 24) & 0xff] + S[256 + ((l >>> 16) & 0xff)]) ^ S[512 + ((l >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[11];
        l ^= ((S[(r >>> 24) & 0xff] + S[256 + ((r >>> 16) & 0xff)]) ^ S[512 + ((r >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[12];
        r ^= ((S[(l >>> 24) & 0xff] + S[256 + ((l >>> 16) & 0xff)]) ^ S[512 + ((r >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[13];
        l ^= ((S[(r >>> 24) & 0xff] + S[256 + ((r >>> 16) & 0xff)]) ^ S[512 + ((r >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[14];
        r ^= ((S[(l >>> 24) & 0xff] + S[256 + ((l >>> 16) & 0xff)]) ^ S[512 + ((l >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[15];
        l ^= ((S[(r >>> 24) & 0xff] + S[256 + ((r >>> 16) & 0xff)]) ^ S[512 + ((r >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[16];
        r ^= P[17];

        lr[off] = r;
        lr[off + 1] = l;
    }

    private static void key(int[] P, int[] S, byte[] key) {
        int[] koffp = { 0 };
        int[] lr = { 0, 0 };
        for (int i = 0; i < P.length; i++) {
            P[i] ^= streamtoword(key, koffp);
        }
        for (int i = 0; i < P.length; i += 2) {
            encipher(P, S, lr, 0);
            P[i] = lr[0];
            P[i + 1] = lr[1];
        }
        for (int i = 0; i < S.length; i += 2) {
            encipher(P, S, lr, 0);
            S[i] = lr[0];
            S[i + 1] = lr[1];
        }
    }

    private static void ekskey(int[] P, int[] S, byte[] data, byte[] key) {
        int[] koffp = { 0 };
        int[] doffp = { 0 };
        int[] lr = { 0, 0 };
        for (int i = 0; i < P.length; i++) {
            P[i] ^= streamtoword(key, koffp);
        }
        for (int i = 0; i < P.length; i += 2) {
            lr[0] ^= streamtoword(data, doffp);
            lr[1] ^= streamtoword(data, doffp);
            encipher(P, S, lr, 0);
            P[i] = lr[0];
            P[i + 1] = lr[1];
        }
        for (int i = 0; i < S.length; i += 2) {
            lr[0] ^= streamtoword(data, doffp);
            lr[1] ^= streamtoword(data, doffp);
            encipher(P, S, lr, 0);
            S[i] = lr[0];
            S[i + 1] = lr[1];
        }
    }

    private static int streamtoword(byte data[], int offp[]) {
        int word = 0;
        int off = offp[0];
        for (int i = 0; i < 4; i++) {
            word = (word << 8) | (data[off] & 0xff);
            off = (off + 1) % data.length;
        }
        offp[0] = off;
        return word;
    }

    private static String encodeBase64(byte[] d, int len) {
        StringBuilder rs = new StringBuilder();
        int c1, c2, off = 0;
        while (off < len) {
            c1 = d[off++] & 0xff;
            rs.append(BCRYPT_BASE64_CODE[(c1 >> 2) & 0x3f]);
            c1 = (c1 & 0x03) << 4;
            if (off >= len) {
                rs.append(BCRYPT_BASE64_CODE[c1 & 0x3f]);
                break;
            }
            c2 = d[off++] & 0xff;
            c1 |= (c2 >> 4) & 0x0f;
            rs.append(BCRYPT_BASE64_CODE[c1 & 0x3f]);
            c1 = (c2 & 0x0f) << 2;
            if (off >= len) {
                rs.append(BCRYPT_BASE64_CODE[c1 & 0x3f]);
                break;
            }
            c2 = d[off++] & 0xff;
            c1 |= (c2 >> 6) & 0x03;
            rs.append(BCRYPT_BASE64_CODE[c1 & 0x3f]);
            rs.append(BCRYPT_BASE64_CODE[c2 & 0x3f]);
        }
        return rs.toString();
    }

    private static byte[] decodeBase64(String s, int maxLen) {
        byte[] rs = new byte[maxLen];
        int off = 0, slen = s.length(), olen = 0;
        int c1, c2, c3, c4, o;

        while (off < slen - 1 && olen < maxLen) {
            c1 = BCRYPT_INDEX[s.charAt(off++) & 0x7f];
            c2 = BCRYPT_INDEX[s.charAt(off++) & 0x7f];
            if (c1 == -1 || c2 == -1) break;
            o = (c1 << 2) | ((c2 & 0x30) >> 4);
            rs[olen++] = (byte) o;
            if (olen >= maxLen || off >= slen) break;
            c3 = BCRYPT_INDEX[s.charAt(off++) & 0x7f];
            if (c3 == -1) break;
            o = ((c2 & 0x0f) << 4) | ((c3 & 0x3c) >> 2);
            rs[olen++] = (byte) o;
            if (olen >= maxLen || off >= slen) break;
            c4 = BCRYPT_INDEX[s.charAt(off++) & 0x7f];
            if (c4 == -1) break;
            o = ((c3 & 0x03) << 6) | c4;
            rs[olen++] = (byte) o;
        }
        return rs;
    }
}
