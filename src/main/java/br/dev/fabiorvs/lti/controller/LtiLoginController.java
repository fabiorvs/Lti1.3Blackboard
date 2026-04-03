package br.dev.fabiorvs.lti.controller;

import br.dev.fabiorvs.lti.config.AppProperties;
import br.dev.fabiorvs.lti.config.LtiPlatformProperties;
import br.dev.fabiorvs.lti.exception.LtiValidationException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/lti")
@RequiredArgsConstructor
public class LtiLoginController {

    private static final Logger log = LoggerFactory.getLogger(LtiLoginController.class);

    private static final String SESSION_STATE = "lti_state";
    private static final String SESSION_NONCE = "lti_nonce";
    private static final String SESSION_ISS = "lti_iss";
    private static final String SESSION_TARGET_LINK_URI = "lti_target_link_uri";

    private final LtiPlatformProperties ltiPlatformProperties;
    private final AppProperties appProperties;

    @Value("${app.base-url}")
    private String baseUrl;

    @GetMapping("/login")
    public void login(
            @RequestParam("iss") String iss,
            @RequestParam("login_hint") String loginHint,
            @RequestParam("target_link_uri") String targetLinkUri,
            @RequestParam(value = "lti_message_hint", required = false) String ltiMessageHint,
            HttpSession session,
            HttpServletResponse response
    ) throws IOException {

        validateIssuer(iss);

        String state = UUID.randomUUID().toString();
        String nonce = UUID.randomUUID().toString();
        String redirectUri = buildRedirectUri();

        saveSessionAttributes(session, state, nonce, iss, targetLinkUri);

        String redirectUrl = buildOidcRedirectUrl(
                loginHint,
                ltiMessageHint,
                state,
                nonce,
                redirectUri
        );

        log.info("LTI login iniciado. iss={}, redirectUri={}", iss, redirectUri);

        response.sendRedirect(redirectUrl);
    }

    private void validateIssuer(String iss) {
        if (iss == null || iss.isBlank()) {
            throw new LtiValidationException("Issuer não informado");
        }

        if (!iss.equals(ltiPlatformProperties.getIssuer())) {
            throw new LtiValidationException("Issuer inválido");
        }
    }

    private void saveSessionAttributes(
            HttpSession session,
            String state,
            String nonce,
            String iss,
            String targetLinkUri
    ) {
        session.setAttribute(SESSION_STATE, state);
        session.setAttribute(SESSION_NONCE, nonce);
        session.setAttribute(SESSION_ISS, iss);
        session.setAttribute(SESSION_TARGET_LINK_URI, targetLinkUri);
    }

    private String buildRedirectUri() {
        return UriComponentsBuilder
                .fromUriString(appProperties.getBaseUrl())
                .path("/lti/launch")
                .toUriString();
    }

    private String buildOidcRedirectUrl(
            String loginHint,
            String ltiMessageHint,
            String state,
            String nonce,
            String redirectUri
    ) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(ltiPlatformProperties.getOidcAuthUrl())
                .queryParam("response_type", "id_token")
                .queryParam("response_mode", "form_post")
                .queryParam("scope", "openid")
                .queryParam("prompt", "none")
                .queryParam("client_id", ltiPlatformProperties.getClientId())
                .queryParam("redirect_uri", redirectUri)
                .queryParam("login_hint", loginHint)
                .queryParam("state", state)
                .queryParam("nonce", nonce);

        if (ltiMessageHint != null && !ltiMessageHint.isBlank()) {
            builder.queryParam("lti_message_hint", ltiMessageHint);
        }

        return builder.toUriString();
    }
}