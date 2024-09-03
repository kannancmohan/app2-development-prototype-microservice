package com.kcm.msp.dev.app2.development.prototype.microservice;

import static com.nimbusds.jose.JWSAlgorithm.RS256;

import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.Date;
import java.time.Instant;
import java.util.Map;

public final class JWTUtil {

  public static final String KEY_ID = "a283028048930930785";

  private JWTUtil() {
    throw new UnsupportedOperationException();
  }

  private static final KeyPair KEY_PAIR;

  static {
    try {
      final var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(2048);
      KEY_PAIR = keyPairGenerator.generateKeyPair();
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate RSA key pair", e);
    }
  }

  public static String createJWT(final String subject, final Map<String, Object> claims)
      throws Exception {
    final var privateKey = (RSAPrivateKey) KEY_PAIR.getPrivate();
    final var signer = new RSASSASigner(privateKey);
    final var now = Instant.now();
    final var claimsBuilder =
        new JWTClaimsSet.Builder()
            .subject(subject)
            .issueTime(Date.from(now))
            .issuer("https://localhost")
            .expirationTime(Date.from(Instant.now().plusSeconds(60)));
    claims.forEach(claimsBuilder::claim);
    final var signedJWT =
        new SignedJWT(
            new com.nimbusds.jose.JWSHeader.Builder(RS256).keyID(KEY_ID).build(),
            claimsBuilder.build());
    signedJWT.sign(signer);
    return signedJWT.serialize();
  }

  public static String createJWKS() {
    final var publicKey = (RSAPublicKey) KEY_PAIR.getPublic();
    final var jwk = new RSAKey.Builder(publicKey).keyID(KEY_ID).build();
    return new JWKSet(jwk).toJSONObject().toString();
  }
}
