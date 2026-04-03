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
    private static final String CLAIM_DEPLOYMENT_ID =
            "https://purl.imsglobal.org/spec/lti/claim/deployment_id";
    private static final String CLAIM_MESSAGE_TYPE =
            "https://purl.imsglobal.org/spec/lti/claim/message_type";
    private static final String CLAIM_VERSION =
            "https://purl.imsglobal.org/spec/lti/claim/version";

    private static final String EXPECTED_MESSAGE_TYPE = "LtiResourceLinkRequest";
    private static final String EXPECTED_VERSION = "1.3.0";

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
        validateDeploymentId(claims);
        validateMessageType(claims);
        validateVersion(claims);

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

    private void validateDeploymentId(JWTClaimsSet claims) {
        Object deploymentId = claims.getClaim(CLAIM_DEPLOYMENT_ID);

        if (deploymentId == null || String.valueOf(deploymentId).isBlank()) {
            throw new LtiValidationException("Deployment ID não informado");
        }

        if (!String.valueOf(deploymentId).equals(ltiPlatformProperties.getDeploymentId())) {
            throw new LtiValidationException("Deployment ID inválido");
        }
    }

    private void validateMessageType(JWTClaimsSet claims) {
        Object messageType = claims.getClaim(CLAIM_MESSAGE_TYPE);

        if (messageType == null || String.valueOf(messageType).isBlank()) {
            throw new LtiValidationException("Message type não informado");
        }

        if (!EXPECTED_MESSAGE_TYPE.equals(String.valueOf(messageType))) {
            throw new LtiValidationException("Message type não suportado");
        }
    }

    private void validateVersion(JWTClaimsSet claims) {
        Object version = claims.getClaim(CLAIM_VERSION);

        if (version == null || String.valueOf(version).isBlank()) {
            throw new LtiValidationException("Versão LTI não informada");
        }

        if (!EXPECTED_VERSION.equals(String.valueOf(version))) {
            throw new LtiValidationException("Versão LTI inválida");
        }
    }
}