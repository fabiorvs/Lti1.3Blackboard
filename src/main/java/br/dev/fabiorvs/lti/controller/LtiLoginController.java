package br.dev.fabiorvs.lti.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/lti")
@RequiredArgsConstructor
public class LtiLoginController {

    @Value("${lti.platform.oidc-auth-url}")
    private String authUrl;

    @Value("${lti.platform.client-id}")
    private String clientId;

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

        String state = UUID.randomUUID().toString();
        String nonce = UUID.randomUUID().toString();

        // 🔐 salva na sessão
        session.setAttribute("lti_state", state);
        session.setAttribute("lti_nonce", nonce);
        session.setAttribute("lti_iss", iss);
        session.setAttribute("lti_target_link_uri", targetLinkUri);

        String redirectUri = baseUrl + "/lti/launch";

        String url = authUrl
                + "?response_type=id_token"
                + "&response_mode=form_post"
                + "&scope=openid"
                + "&prompt=none"
                + "&client_id=" + enc(clientId)
                + "&redirect_uri=" + enc(redirectUri)
                + "&login_hint=" + enc(loginHint)
                + "&state=" + enc(state)
                + "&nonce=" + enc(nonce);

        if (ltiMessageHint != null && !ltiMessageHint.isBlank()) {
            url += "&lti_message_hint=" + enc(ltiMessageHint);
        }

        response.sendRedirect(url);
    }

    private String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}