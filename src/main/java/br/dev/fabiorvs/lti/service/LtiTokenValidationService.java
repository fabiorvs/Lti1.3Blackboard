package br.dev.fabiorvs.lti.service;

import br.dev.fabiorvs.lti.config.LtiPlatformProperties;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LtiTokenValidationService {

    private static final String SESSION_STATE = "lti_state";
    private static final String SESSION_NONCE = "lti_nonce";
    private static final String CLAIM_NONCE = "nonce";

    private final LtiPlatformProperties properties;

    public JWTClaimsSet validateLaunch(String idToken, String state, HttpSession session)
            throws ParseException, BadJOSEException, JOSEException, MalformedURLException {

        validateState(state, session);
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        JWSAlgorithm expectedAlg = JWSAlgorithm.parse(properties.getSignatureAlg());
        RemoteJWKSet<SecurityContext> jwkSource = new RemoteJWKSet<>(
                new URL(properties.getJwksUrl()),
                new DefaultResourceRetriever(3000, 3000, 1024 * 1024)
        );
        JWSKeySelector<SecurityContext> keySelector =
                new JWSVerificationKeySelector<>(expectedAlg, jwkSource);
        jwtProcessor.setJWSKeySelector(keySelector);
        JWTClaimsSet claims = jwtProcessor.process(idToken, null);

        validateIssuer(claims);
        validateAudience(claims);
        validateNonce(claims, session);
        validateTimes(claims);

        return claims;
    }

    private void validateState(String state, HttpSession session) {
        String expectedState = (String) session.getAttribute(SESSION_STATE);

        if (expectedState == null || state == null || !expectedState.equals(state)) {
            throw new IllegalArgumentException("State inválido");
        }
    }

    private void validateIssuer(JWTClaimsSet claims) {
        String iss = claims.getIssuer();

        if (iss == null || !iss.equals(properties.getIssuer())) {
            throw new IllegalArgumentException("Issuer inválido");
        }
    }

    private void validateAudience(JWTClaimsSet claims) {
        List<String> audience = claims.getAudience();

        if (audience == null || audience.isEmpty() || !audience.contains(properties.getClientId())) {
            throw new IllegalArgumentException("Audience inválida");
        }
    }

    private void validateNonce(JWTClaimsSet claims, HttpSession session) {
        String expectedNonce = (String) session.getAttribute(SESSION_NONCE);
        Object tokenNonce = claims.getClaim(CLAIM_NONCE);

        if (expectedNonce == null || tokenNonce == null || !expectedNonce.equals(String.valueOf(tokenNonce))) {
            throw new IllegalArgumentException("Nonce inválido");
        }
    }

    private void validateTimes(JWTClaimsSet claims) {
        if (claims.getExpirationTime() == null) {
            throw new IllegalArgumentException("Token sem exp");
        }

        if (claims.getExpirationTime().before(new java.util.Date())) {
            throw new IllegalArgumentException("Token expirado");
        }
    }
}