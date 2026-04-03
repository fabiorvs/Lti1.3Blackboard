package br.dev.fabiorvs.lti.controller;

import br.dev.fabiorvs.lti.model.LtiUserResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/user")
    public ResponseEntity<?> user(HttpSession session) {
        LtiUserResponse user = (LtiUserResponse) session.getAttribute("lti_user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuário não autenticado via LTI"));
        }

        return ResponseEntity.ok(user);
    }
}