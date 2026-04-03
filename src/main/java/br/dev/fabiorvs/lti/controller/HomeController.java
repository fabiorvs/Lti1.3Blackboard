package br.dev.fabiorvs.lti.controller;

import br.dev.fabiorvs.lti.model.LtiUserResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Aplicação LTI rodando";
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        LtiUserResponse user = (LtiUserResponse) session.getAttribute("lti_user");

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Usuário não autenticado via LTI"
            ));
        }

        return ResponseEntity.ok(user);
    }

    @GetMapping("/debug/lti")
    public ResponseEntity<?> debugLti(HttpSession session) {
        Object user = session.getAttribute("lti_user");
        Object claims = session.getAttribute("lti_claims");
        Object token = session.getAttribute("lti_raw_token");

        if (claims == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Nenhum launch LTI encontrado na sessão"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "user", user,
                "claims", claims,
                "rawToken", token
        ));
    }
}