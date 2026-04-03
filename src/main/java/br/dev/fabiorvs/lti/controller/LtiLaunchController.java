package br.dev.fabiorvs.lti.controller;

import br.dev.fabiorvs.lti.model.LtiUserResponse;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/lti")
public class LtiLaunchController {

    @PostMapping("/launch")
    public void launch(
            @RequestParam("id_token") String idToken,
            @RequestParam(value = "state", required = false) String state,
            HttpSession session,
            HttpServletResponse response) throws Exception {

        SignedJWT jwt = SignedJWT.parse(idToken);
        JWTClaimsSet claims = jwt.getJWTClaimsSet();

        Map<String, Object> allClaims = claims.getClaims();

        Object rolesClaim = claims.getClaim("https://purl.imsglobal.org/spec/lti/claim/roles");

        List<String> roles;
        if (rolesClaim instanceof List<?> rawList) {
            roles = rawList.stream().map(String::valueOf).toList();
        } else if (rolesClaim != null) {
            roles = List.of(String.valueOf(rolesClaim));
        } else {
            roles = List.of();
        }

        LtiUserResponse user = new LtiUserResponse();
        user.setName((String) claims.getClaim("name"));
        user.setEmail((String) claims.getClaim("email"));
        user.setRoles(roles);

        session.setAttribute("lti_user", user);
        session.setAttribute("lti_claims", allClaims);
        session.setAttribute("lti_raw_token", idToken);

        System.out.println("=== LTI USER ===");
        System.out.println(user);

        System.out.println("=== LTI CLAIMS ===");
        System.out.println(allClaims);

        response.sendRedirect("/me");
    }

    @GetMapping("/claims")
    public ResponseEntity<?> claims(HttpSession session) {
        Object claims = session.getAttribute("lti_claims");

        if (claims == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Nenhum launch LTI encontrado na sessão"
            ));
        }

        return ResponseEntity.ok(claims);
    }

    @GetMapping("/token")
    public ResponseEntity<?> token(HttpSession session) {
        Object token = session.getAttribute("lti_raw_token");

        if (token == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Nenhum token LTI encontrado na sessão"
            ));
        }

        return ResponseEntity.ok(Map.of("id_token", token));
    }
}