package br.dev.fabiorvs.lti.service;

import br.dev.fabiorvs.lti.config.LtiPlatformProperties;
import br.dev.fabiorvs.lti.exception.LtiValidationException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LtiTokenValidationService {

    private static final String SESSION_STATE = "lti_state";
    private static final String SESSION_NONCE = "lti_nonce";
    private static final String CLAIM_NONCE = "nonce";

    private final LtiPlatformProperties ltiPlatformProperties;

    public JWTClaimsSet validateLaunch(String idToken, String state, HttpSession session)
            throws ParseException, BadJOSEException, JOSEException, MalformedURLException {

        validateState(state, session);

        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        JWSAlgorithm expectedAlg = JWSAlgorithm.parse(ltiPlatformProperties.getSignatureAlg());

        RemoteJWKSet<SecurityContext> jwkSource = new RemoteJWKSet<>(
                new URL(ltiPlatformProperties.getJwksUrl()),
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
            throw new LtiValidationException("State inválido");
        }
    }

    private void validateIssuer(JWTClaimsSet claims) {
        String issuer = claims.getIssuer();

        if (issuer == null || !issuer.equals(ltiPlatformProperties.getIssuer())) {
            throw new LtiValidationException("Issuer inválido");
        }
    }

    private void validateAudience(JWTClaimsSet claims) {
        List<String> audience = claims.getAudience();

        if (audience == null || audience.isEmpty() || !audience.contains(ltiPlatformProperties.getClientId())) {
            throw new LtiValidationException("Audience inválida");
        }
    }

    private void validateNonce(JWTClaimsSet claims, HttpSession session) {
        String expectedNonce = (String) session.getAttribute(SESSION_NONCE);
        Object tokenNonce = claims.getClaim(CLAIM_NONCE);

        if (expectedNonce == null || tokenNonce == null || !expectedNonce.equals(String.valueOf(tokenNonce))) {
            throw new LtiValidationException("Nonce inválido");
        }
    }

    private void validateTimes(JWTClaimsSet claims) {
        Date now = new Date();

        if (claims.getIssueTime() == null) {
            throw new LtiValidationException("Token sem iat");
        }

        if (claims.getExpirationTime() == null) {
            throw new LtiValidationException("Token sem exp");
        }

        if (claims.getExpirationTime().before(now)) {
            throw new LtiValidationException("Token expirado");
        }
    }
}