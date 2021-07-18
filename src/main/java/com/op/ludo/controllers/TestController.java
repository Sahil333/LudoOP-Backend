package com.op.ludo.controllers;

import com.op.ludo.auth.filter.FirebasePrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@Slf4j
public class TestController {

    @PostMapping("/api/client/create/")
    public ResponseEntity<String> create() {
        FirebasePrincipal user = ((FirebasePrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        log.info("{} {}", user.getUsername(), user.getUid());
        return ResponseEntity.ok("ok");
    }
}
