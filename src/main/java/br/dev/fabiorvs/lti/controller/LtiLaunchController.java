package br.dev.fabiorvs.lti.controller;

import br.dev.fabiorvs.lti.model.LtiUserResponse;
import br.dev.fabiorvs.lti.service.LtiTokenValidationService;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/lti")
@RequiredArgsConstructor
public class LtiLaunchController {

    private static final String LTI_USER = "lti_user";
    private static final String LTI_CLAIMS = "lti_claims";
    private static final String LTI_RAW_TOKEN = "lti_raw_token";
    private static final String ROLES_CLAIM = "https://purl.imsglobal.org/spec/lti/claim/roles";

    private final LtiTokenValidationService tokenValidationService;

    @PostMapping("/launch")
    public void launch(
            @RequestParam("id_token") String idToken,
            @RequestParam(value = "state", required = false) String state,
            HttpSession session,
            HttpServletResponse response
    ) throws Exception {

        JWTClaimsSet claims = tokenValidationService.validateLaunch(idToken, state, session);

        LtiUserResponse user = new LtiUserResponse();
        user.setName((String) claims.getClaim("name"));
        user.setEmail((String) claims.getClaim("email"));
        user.setRoles(extractRoles(claims.getClaim(ROLES_CLAIM)));

        session.setAttribute(LTI_USER, user);
        session.setAttribute(LTI_CLAIMS, claims.getClaims());
        session.setAttribute(LTI_RAW_TOKEN, idToken);

        response.sendRedirect("/me");
    }

    @GetMapping("/claims")
    public ResponseEntity<?> claims(HttpSession session) {
        Object claims = session.getAttribute(LTI_CLAIMS);

        if (claims == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Nenhum launch LTI encontrado na sessão"));
        }

        return ResponseEntity.ok(claims);
    }

    @GetMapping("/token")
    public ResponseEntity<?> token(HttpSession session) {
        Object token = session.getAttribute(LTI_RAW_TOKEN);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Nenhum token LTI encontrado na sessão"));
        }

        return ResponseEntity.ok(Map.of("id_token", token));
    }

    private List<String> extractRoles(Object rolesClaim) {
        if (rolesClaim instanceof List<?> rawList) {
            return rawList.stream().map(String::valueOf).toList();
        }

        if (rolesClaim != null) {
            return List.of(String.valueOf(rolesClaim));
        }

        return List.of();
    }
}