package za.co.hydra24.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** JwtVerifier on its own, no Quarkus: genuine ID tokens pass, forged or misused ones do not. */
class JwtVerifierTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Base64.Encoder BASE64URL = Base64.getUrlEncoder().withoutPadding();
    private static final String ISSUER = "https://cognito-idp.test.invalid/pool";
    private static final String AUDIENCE = "web-client";
    private static final String KID = "key-1";
    private static final long NOW = 1_800_000_000L;

    private static KeyPair trustedKeys;
    private static KeyPair otherKeys;
    private static JwtVerifier verifier;

    @BeforeAll
    static void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        trustedKeys = generator.generateKeyPair();
        otherKeys = generator.generateKeyPair();
        verifier = new JwtVerifier(ISSUER, AUDIENCE, keysJson((RSAPublicKey) trustedKeys.getPublic()),
                Clock.fixed(Instant.ofEpochSecond(NOW), ZoneOffset.UTC));
    }

    @Test
    void genuineTokenGivesTheCaller() throws Exception {
        Optional<Caller> caller = verifier.verify(sign(claims(), trustedKeys));
        assertEquals(Optional.of(new Caller("sub-1", "Ben@Example.com")), caller);
    }

    @Test
    void tokenSignedByAnotherKeyIsRefused() throws Exception {
        assertRefused(sign(claims(), otherKeys));
    }

    @Test
    void editedPayloadIsRefused() throws Exception {
        String[] parts = sign(claims(), trustedKeys).split("\\.");
        Map<String, Object> forged = claims();
        forged.put("email", "someone.else@example.com");
        assertRefused(parts[0] + "." + encode(forged) + "." + parts[2]);
    }

    @Test
    void unsignedTokenIsRefused() throws Exception {
        assertRefused(encode(Map.of("alg", "none", "kid", KID)) + "." + encode(claims()) + ".");
    }

    @Test
    void wrongIssuerAudienceOrTokenUseIsRefused() throws Exception {
        assertRefused(sign(claims("iss", "https://cognito-idp.test.invalid/other-pool"), trustedKeys));
        assertRefused(sign(claims("aud", "another-client"), trustedKeys));
        assertRefused(sign(claims("token_use", "access"), trustedKeys));
    }

    @Test
    void expiredTokenIsRefusedAfterTheLeeway() throws Exception {
        assertTrue(verifier.verify(sign(claims("exp", NOW - JwtVerifier.LEEWAY_SECONDS), trustedKeys)).isPresent());
        assertRefused(sign(claims("exp", NOW - JwtVerifier.LEEWAY_SECONDS - 1), trustedKeys));
    }

    @Test
    void unverifiedEmailIsRefused() throws Exception {
        assertRefused(sign(claims("email_verified", false), trustedKeys));
    }

    @Test
    void garbageIsRefused() {
        assertRefused("not-a-jwt");
        assertRefused("a.b.c");
    }

    @Test
    void missingKeysFailStartup() {
        assertThrows(IllegalStateException.class, () -> new JwtVerifier(ISSUER, AUDIENCE, "{}"));
    }

    private static void assertRefused(String token) {
        assertTrue(verifier.verify(token).isEmpty());
    }

    private static Map<String, Object> claims(String name, Object value) {
        Map<String, Object> claims = claims();
        claims.put(name, value);
        return claims;
    }

    private static Map<String, Object> claims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", ISSUER);
        claims.put("aud", AUDIENCE);
        claims.put("token_use", "id");
        claims.put("exp", NOW + 3600);
        claims.put("sub", "sub-1");
        claims.put("email", "Ben@Example.com");
        claims.put("email_verified", true);
        return claims;
    }

    private static String sign(Map<String, Object> claims, KeyPair keys) throws Exception {
        String signingInput = encode(Map.of("alg", "RS256", "kid", KID)) + "." + encode(claims);
        Signature rs256 = Signature.getInstance("SHA256withRSA");
        rs256.initSign(keys.getPrivate());
        rs256.update(signingInput.getBytes(StandardCharsets.US_ASCII));
        return signingInput + "." + BASE64URL.encodeToString(rs256.sign());
    }

    private static String encode(Map<String, Object> json) throws Exception {
        return BASE64URL.encodeToString(MAPPER.writeValueAsBytes(json));
    }

    private static String keysJson(RSAPublicKey key) throws Exception {
        return MAPPER.writeValueAsString(Map.of(KID, Map.of(
                "n", BASE64URL.encodeToString(key.getModulus().toByteArray()),
                "e", BASE64URL.encodeToString(key.getPublicExponent().toByteArray()))));
    }
}
