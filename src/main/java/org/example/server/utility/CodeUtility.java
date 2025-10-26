package org.example.server.utility;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;


@Component
public class CodeUtility {
    private static final SecureRandom RNG = new SecureRandom();
    public String newNumericCode(int digits) {
        int max = (int) Math.pow(10, digits);
        int n = RNG.nextInt(max);
        return String.format("%0"+digits+"d", n);
    }
    public String newSecret(int bytes) {
        byte[] b = new byte[bytes];
        RNG.nextBytes(b);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }
}
