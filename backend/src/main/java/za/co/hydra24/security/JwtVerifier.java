package za.co.hydra24.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.RSAPublicKeySpec;
import java.time.Clock;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Verifies the Cognito ID tokens the site sends as bearer tokens. Nothing
 * upstream does: API Gateway has no authorizer.
 *
 * The Lambda has no route to the internet, so it cannot fetch Cognito's JWKS.
 * Terraform fetches the pool's RS256 public keys (iac/jwt_trust.tf) and passes
 * them in {@code auth.jwt.keys} as {@code {kid: {n, e}}}.
 *
 * A token is accepted only when: it has three segments, alg is RS256 with a
 * known kid, the signature verifies, iss is the pool, aud is the site's app
 * client, token_use is "id", it has not expired (one minute of clock skew), and
 * the email is verified.
 */
@ApplicationScoped
@Startup
public class JwtVerifier {

    private static final Logger LOG = Logger.getLogger(JwtVerifier.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Base64.Decoder BASE64URL = Base64.getUrlDecoder();

    static final long LEEWAY_SECONDS = 60;

    private final String issuer;
    private final String audience;
    private final Map<String, PublicKey> keys;
    private final Clock clock;

    @Inject
    public JwtVerifier(@ConfigProperty(name = "auth.jwt.issuer") String issuer,
                       @ConfigProperty(name = "auth.jwt.audience") String audience,
                       @ConfigProperty(name = "auth.jwt.keys") String keysJson) {
        this(issuer, audience, keysJson, Clock.systemUTC());
    }

    JwtVerifier(String issuer, String audience, String keysJson, Clock clock) {
        this.issuer = issuer;
        this.audience = audience;
        this.keys = parseKeys(keysJson);
        this.clock = clock;
    }

    /** The caller when the token is a genuine, current ID token; empty otherwise (reason logged). */
    public Optional<Caller> verify(String token) {
        try {
            Map<String, Object> claims = verifiedClaims(token);
            return Optional.of(new Caller((String) claims.get("sub"), (String) claims.get("email")));
        } catch (InvalidTokenException e) {
            LOG.warnf("Bearer token refused: %s", e.getMessage());
            return Optional.empty();
        }
    }

    private Map<String, Object> verifiedClaims(String token) throws InvalidTokenException {
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3) {
            throw new InvalidTokenException("not a three-part JWT");
        }
        Map<String, Object> header = decodeJson(parts[0]);
        Map<String, Object> claims = decodeJson(parts[1]);

        if (!"RS256".equals(header.get("alg"))) {
            throw new InvalidTokenException("alg must be RS256");
        }
        PublicKey key = keys.get(String.valueOf(header.get("kid")));
        if (key == null) {
            throw new InvalidTokenException("unknown kid");
        }
        if (!signatureValid(parts, key)) {
            throw new InvalidTokenException("bad signature");
        }
        require(issuer.equals(claims.get("iss")), "wrong issuer");
        require(audienceMatches(claims.get("aud")), "wrong audience");
        require("id".equals(claims.get("token_use")), "not an ID token");
        require(notExpired(claims.get("exp")), "expired");
        require(Boolean.TRUE.equals(claims.get("email_verified")), "email not verified");
        require(claims.get("sub") instanceof String && claims.get("email") instanceof String, "no sub or email");
        return claims;
    }

    private boolean audienceMatches(Object aud) {
        return audience.equals(aud) || (aud instanceof Collection<?> list && list.contains(audience));
    }

    private boolean notExpired(Object exp) {
        return exp instanceof Number seconds
                && clock.instant().getEpochSecond() <= seconds.longValue() + LEEWAY_SECONDS;
    }

    private static void require(boolean condition, String reason) throws InvalidTokenException {
        if (!condition) {
            throw new InvalidTokenException(reason);
        }
    }

    private static boolean signatureValid(String[] parts, PublicKey key) throws InvalidTokenException {
        try {
            Signature rs256 = Signature.getInstance("SHA256withRSA");
            rs256.initVerify(key);
            rs256.update((parts[0] + "." + parts[1]).getBytes(StandardCharsets.US_ASCII));
            return rs256.verify(BASE64URL.decode(parts[2]));
        } catch (IllegalArgumentException | GeneralSecurityException e) {
            throw new InvalidTokenException("signature unreadable");
        }
    }

    private static Map<String, Object> decodeJson(String segment) throws InvalidTokenException {
        try {
            return MAPPER.readValue(BASE64URL.decode(segment), new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new InvalidTokenException("segment is not base64url JSON");
        }
    }

    /** Fails startup on bad configuration rather than refusing every sign-in later. */
    private static Map<String, PublicKey> parseKeys(String json) {
        try {
            Map<String, Map<String, String>> jwks = MAPPER.readValue(json, new TypeReference<>() {
            });
            KeyFactory rsa = KeyFactory.getInstance("RSA");
            Map<String, PublicKey> result = new HashMap<>();
            for (Map.Entry<String, Map<String, String>> jwk : jwks.entrySet()) {
                BigInteger n = new BigInteger(1, BASE64URL.decode(jwk.getValue().get("n")));
                BigInteger e = new BigInteger(1, BASE64URL.decode(jwk.getValue().get("e")));
                result.put(jwk.getKey(), rsa.generatePublic(new RSAPublicKeySpec(n, e)));
            }
            if (result.isEmpty()) {
                throw new IllegalArgumentException("no keys");
            }
            return Map.copyOf(result);
        } catch (Exception e) {
            throw new IllegalStateException("auth.jwt.keys is not a usable {kid: {n, e}} map", e);
        }
    }

    private static final class InvalidTokenException extends Exception {
        InvalidTokenException(String reason) {
            super(reason);
        }
    }
}
