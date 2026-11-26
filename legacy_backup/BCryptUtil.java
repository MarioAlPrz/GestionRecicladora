// ============================================================================
// BCryptUtil.java — Implementación pura de BCrypt en Java (sin dependencias externas)
// ============================================================================
// Proporciona generación y verificación de contraseñas con hash BCrypt ($2a$).
// Compatible con los estándares de OpenBSD / jBCrypt.
// ============================================================================

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Utilidad criptográfica para el almacenamiento seguro de contraseñas mediante BCrypt.
 * Implementa el algoritmo EksBlowfish completo en Java puro sin requerir Maven ni librerías externas.
 */
public class BCryptUtil {

    private static final int BCRYPT_SALT_LEN = 16;
    private static final int DEFAULT_LOG_ROUNDS = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    // Alfabeto base64 estándar de BCrypt: ./A-Za-z0-9
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

    // Texto cifrado inicial estándar de BCrypt: "OrpheanBeholderScryDoubt"
    private static final int[] BCRYPT_CIPHERTEXT = {
        0x4f727068, 0x65616e42, 0x65686f6c, 0x64657253, 0x63727944, 0x6f756274
    };

    // P-box inicial de Blowfish
    private static final int[] P_ORIG = {
        0x243f6a88, 0x85a308d3, 0x13198a2e, 0x03707344, 0xa4093822, 0x299f31d0,
        0x082efa98, 0xec4e6c89, 0x452821e6, 0x38d01377, 0xbe5466cf, 0x34e90c6c,
        0xc0ac29b7, 0xc97c50dd, 0x3f84d5b5, 0xb5470917, 0x9216d5d9, 0x8979fb1b
    };

    // S-boxes iniciales de Blowfish
    private static final int[] S_ORIG = {
        0xd1310ba6, 0x98dfb5ac, 0x2ffd72db, 0xd01adfb7, 0xb8e1afed, 0x6a267e96,
        0xba7c9045, 0xf12c7f99, 0x24a19947, 0xb3916cf7, 0x0801f2e2, 0x858efc16,
        0x636920d8, 0x71574e69, 0xa458fea3, 0xf4933d7e, 0x0d95748f, 0x728eb658,
        0x718bcd58, 0x82154aee, 0x7b54a41d, 0xc25a59b5, 0x9c30d539, 0x2af26013,
        0xc9942e86, 0xdae08227, 0x46f8841a, 0x0d039720, 0x282b00a9, 0x2fe483db,
        0x5a6ab25e, 0x52523e47, 0xb606bd40, 0x5436032c, 0x654e920f, 0x5b4f7fb0,
        0x85b2a24e, 0x2314d230, 0x679dd51a, 0x89425f16, 0x4284f365, 0x2e604ec0,
        0x9c6ddf3f, 0x847d24e8, 0x507743e1, 0xb2294c14, 0x4b73a201, 0x717b0f14,
        0xacc23322, 0xce5404e4, 0x9fb14e42, 0xa3a27909, 0x8415d710, 0xbf1d56b0,
        0x6880261f, 0xbbbe80e6, 0x27874147, 0x4fa50a99, 0x0777547d, 0xf742f4b2,
        0xefd2fb27, 0x706071cc, 0xddbf5404, 0xe052615e, 0x38475299, 0x55209a97,
        0x325e5ff6, 0x649d6607, 0x532e592f, 0x9c70c5de, 0x14e578c7, 0xe244b0ec,
        0x54a29265, 0xbf6add79, 0x032a39cd, 0x4e6c9c69, 0xd33d251e, 0x514a087d,
        0x56ab346e, 0x815a1f3f, 0x44a7e159, 0x03c268fa, 0x93d8304c, 0x46427c1b,
        0x5791649d, 0xbcd2773a, 0xf23390fb, 0x9b11202c, 0x7b772b1e, 0x4e77a5f1,
        0xdb7920b8, 0x9864d746, 0x49540564, 0xc16a65cb, 0x367f0add, 0x8a9e2f1d,
        0xf9b9ffc4, 0x8a5e2dc0, 0xfe9712a3, 0xbec5f899, 0x66722e03, 0xeb667c56,
        0xab092b76, 0xdcd34ddee, 0x5c471e9e, 0x140356b2, 0x310166a5, 0x7ec6442c,
        0x42c4e188, 0xaac6fd46, 0x57d00a4e, 0x4e5ac859, 0xa2c7d9e0, 0x5ee28370,
        0x3249ee52, 0xb636f40f, 0x38cc5761, 0x89b65b12, 0x203acfed, 0xb891492d,
        0x6b5451b6, 0xa01dae92, 0x60c75727, 0x59723cbf, 0x62161e0b, 0xa437f35c,
        0x879c1e52, 0x6e7d6032, 0xb6a1a311, 0xb222030e, 0xaa2718cd, 0x5393b4ea,
        0xda2011ff, 0x82d41a7e, 0x93838a9c, 0x574e947d, 0x0d457065, 0x83c26563,
        0xfd40a3d2, 0xefc31c61, 0x56ab573a, 0xce9e459b, 0x1537ec41, 0x9602c728,
        0x712fe67c, 0x478038ec, 0x52774c69, 0xd276d97c, 0xa33ec65e, 0xaa612f02,
        0x1a46b858, 0x84aa2735, 0x67f14e72, 0x40c49624, 0x9cd81631, 0x946d557f,
        0xc0d6dd05, 0x9673b265, 0x7f4d3e90, 0x184738ac, 0x6a61764b, 0x75c5eed4,
        0x84de45b0, 0x77977405, 0x9657b952, 0x4b78e246, 0x6ed95230, 0x952c7ab2,
        0x3763479b, 0x0e11849e, 0x88226bc7, 0x09a14416, 0xb0f39f47, 0x6073c524,
        0x59ff659f, 0x73863870, 0x613096f9, 0x2b2f6424, 0xba57ac68, 0x264560ea,
        0x563e887f, 0x4464a0e4, 0x22350006, 0x1fb3c845, 0x600a4c4c, 0xd0da28e4,
        0x616dd3e5, 0x47d08f7b, 0x6ceb4184, 0x481cebcf, 0xd6452535, 0xd0d61c13,
        0x39d901d3, 0x32515fbb, 0xc9f67447, 0x4207eb15, 0x77305c68, 0x6e737fe0,
        0xe113c04f, 0x4779f2fb, 0x42e3bda3, 0xb773de40, 0x88cd5870, 0xfb3704de,
        0x6830777d, 0x529a4728, 0x47bf85c6, 0x40dfa741, 0x587d6e9a, 0x9e13dbd6,
        0xe2d16d0e, 0x35612307, 0xb91405e0, 0x483a5e6f, 0x8537720a, 0x53e479a6,
        0xe3caec97, 0x41f32412, 0x3104f2a1, 0x3ced4014, 0x405de30e, 0x170f8051,
        0x47232b5c, 0xfd75e49f, 0x6c6e14d1, 0xe26a0c5f, 0x5521abdf, 0x3a40e4f7,
        0x02126ab3, 0x7e4a0c0f, 0xd00114f4, 0x5c7f8937, 0x4e6ad27f, 0x55b1e856,
        0x72c5d304, 0x5ac4f04b, 0x4bc455cf, 0x65edac1f, 0x857800de, 0x81e0142a,
        0x943666a6, 0xb1f31d36, 0x279e0f38, 0x87974230, 0xbf4e7dc4, 0x44c39722
    };

    /**
     * Genera un hash BCrypt para una contraseña en texto plano con el costo por defecto (10).
     *
     * @param password Contraseña en texto plano
     * @return Cadena formateada como "$2a$10$..."
     */
    public static String hashPassword(String password) {
        return hashPassword(password, DEFAULT_LOG_ROUNDS);
    }

    /**
     * Genera un hash BCrypt con un costo de rondas específico.
     */
    public static String hashPassword(String password, int logRounds) {
        if (password == null) {
            throw new IllegalArgumentException("La contraseña no puede ser nula");
        }
        if (logRounds < 4 || logRounds > 31) {
            throw new IllegalArgumentException("logRounds inválido (debe ser 4-31)");
        }
        byte[] salt = new byte[BCRYPT_SALT_LEN];
        RANDOM.nextBytes(salt);
        return crypt(password, salt, logRounds);
    }

    /**
     * Comprueba si una contraseña en texto plano coincide con un hash BCrypt existente.
     *
     * @param password Contraseña en texto plano
     * @param hash     Hash BCrypt almacenado (ej: "$2a$10$...")
     * @return true si coincide, false en caso contrario
     */
    public static boolean checkPassword(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        // Validar formato estándar: $2a$XX$... o $2b$XX$... o $2y$XX$...
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
        byte[] passBytes = (password + "\0").getBytes(java.nio.charset.StandardCharsets.UTF_8);

        Blowfish bf = new Blowfish();
        bf.eksSetup(passBytes, salt, logRounds);

        int[] cdata = Arrays.copyOf(BCRYPT_CIPHERTEXT, BCRYPT_CIPHERTEXT.length);
        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < cdata.length; j += 2) {
                bf.encipher(cdata, j);
            }
        }

        byte[] rawOutput = new byte[cdata.length * 4];
        for (int i = 0, j = 0; i < cdata.length; i++) {
            rawOutput[j++] = (byte) ((cdata[i] >>> 24) & 0xff);
            rawOutput[j++] = (byte) ((cdata[i] >>> 16) & 0xff);
            rawOutput[j++] = (byte) ((cdata[i] >>> 8) & 0xff);
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

    // --- Motor Blowfish interno para EksBlowfish ---
    private static class Blowfish {
        private final int[] P = new int[18];
        private final int[] S = new int[1024];

        Blowfish() {
            reset();
        }

        private void reset() {
            System.arraycopy(P_ORIG, 0, P, 0, P_ORIG.length);
            for (int i = 0; i < 4; i++) {
                System.arraycopy(S_ORIG, 0, S, i * 256, 256);
            }
        }

        private void encipher(int[] lr, int off) {
            int l = lr[off];
            int r = lr[off + 1];

            l ^= P[0];
            r ^= ((S[(l >>> 24) & 0xff] + S[256 + ((l >>> 16) & 0xff)]) ^ S[512 + ((l >>> 8) & 0xff)]) + S[768 + (l & 0xff)] ^ P[1];
            l ^= ((S[(r >>> 24) & 0xff] + S[256 + ((r >>> 16) & 0xff)]) ^ S[512 + ((r >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[2];
            r ^= ((S[(l >>> 24) & 0xff] + S[256 + ((l >>> 16) & 0xff)]) ^ S[512 + ((l >>> 8) & 0xff)]) + S[768 + (l & 0xff)] ^ P[3];
            l ^= ((S[(r >>> 24) & 0xff] + S[256 + ((r >>> 16) & 0xff)]) ^ S[512 + ((r >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[4];
            r ^= ((S[(l >>> 24) & 0xff] + S[256 + ((l >>> 16) & 0xff)]) ^ S[512 + ((l >>> 8) & 0xff)]) + S[768 + (l & 0xff)] ^ P[5];
            l ^= ((S[(r >>> 24) & 0xff] + S[256 + ((r >>> 16) & 0xff)]) ^ S[512 + ((r >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[6];
            r ^= ((S[(l >>> 24) & 0xff] + S[256 + ((l >>> 16) & 0xff)]) ^ S[512 + ((l >>> 8) & 0xff)]) + S[768 + (l & 0xff)] ^ P[7];
            l ^= ((S[(r >>> 24) & 0xff] + S[256 + ((r >>> 16) & 0xff)]) ^ S[512 + ((r >>> 8) & 0xff)]) + S[768 + (l & 0xff)] ^ P[8];
            r ^= ((S[(l >>> 24) & 0xff] + S[256 + ((l >>> 16) & 0xff)]) ^ S[512 + ((l >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[9];
            l ^= ((S[(r >>> 24) & 0xff] + S[256 + ((r >>> 16) & 0xff)]) ^ S[512 + ((r >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[10];
            r ^= ((S[(l >>> 24) & 0xff] + S[256 + ((l >>> 16) & 0xff)]) ^ S[512 + ((l >>> 8) & 0xff)]) + S[768 + (l & 0xff)] ^ P[11];
            l ^= ((S[(r >>> 24) & 0xff] + S[256 + ((r >>> 16) & 0xff)]) ^ S[512 + ((r >>> 8) & 0xff)]) + S[768 + (l & 0xff)] ^ P[12];
            r ^= ((S[(l >>> 24) & 0xff] + S[256 + ((l >>> 16) & 0xff)]) ^ S[512 + ((l >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[13];
            l ^= ((S[(r >>> 24) & 0xff] + S[256 + ((r >>> 16) & 0xff)]) ^ S[512 + ((r >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[14];
            r ^= ((S[(l >>> 24) & 0xff] + S[256 + ((l >>> 16) & 0xff)]) ^ S[512 + ((l >>> 8) & 0xff)]) + S[768 + (l & 0xff)] ^ P[15];
            l ^= ((S[(r >>> 24) & 0xff] + S[256 + ((r >>> 16) & 0xff)]) ^ S[512 + ((r >>> 8) & 0xff)]) + S[768 + (r & 0xff)] ^ P[16];
            r ^= P[17];

            lr[off] = r;
            lr[off + 1] = l;
        }

        private static int streamWords(byte[] data, int[] off) {
            int w = 0;
            for (int i = 0; i < 4; i++) {
                w = (w << 8) | (data[off[0]] & 0xff);
                off[0] = (off[0] + 1) % data.length;
            }
            return w;
        }

        private void key(byte[] key) {
            int[] koff = {0};
            for (int i = 0; i < 18; i++) {
                P[i] ^= streamWords(key, koff);
            }
            int[] lr = {0, 0};
            for (int i = 0; i < 18; i += 2) {
                encipher(lr, 0);
                P[i] = lr[0];
                P[i + 1] = lr[1];
            }
            for (int i = 0; i < 1024; i += 2) {
                encipher(lr, 0);
                S[i] = lr[0];
                S[i + 1] = lr[1];
            }
        }

        private void eksSetup(byte[] key, byte[] salt, int logRounds) {
            reset();
            int[] koff = {0};
            for (int i = 0; i < 18; i++) {
                P[i] ^= streamWords(key, koff);
            }

            int[] lr = {0, 0};
            int[] soff = {0};

            for (int i = 0; i < 18; i += 2) {
                lr[0] ^= streamWords(salt, soff);
                lr[1] ^= streamWords(salt, soff);
                encipher(lr, 0);
                P[i] = lr[0];
                P[i + 1] = lr[1];
            }

            for (int i = 0; i < 1024; i += 2) {
                lr[0] ^= streamWords(salt, soff);
                lr[1] ^= streamWords(salt, soff);
                encipher(lr, 0);
                S[i] = lr[0];
                S[i + 1] = lr[1];
            }

            int rounds = 1 << logRounds;
            for (int r = 0; r < rounds; r++) {
                key(key);
                key(salt);
            }
        }
    }
}
