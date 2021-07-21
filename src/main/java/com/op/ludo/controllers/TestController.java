package com.op.ludo.controllers;

import com.op.ludo.auth.facade.IAuthenticationFacade;
import com.op.ludo.auth.filter.FirebasePrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@Slf4j
public class TestController {

    @Autowired
    IAuthenticationFacade auth;

    @PostMapping("/v1/lobby/create")
    public ResponseEntity<String> create() {
        FirebasePrincipal user = auth.getPrincipal();
        if(user == null) {
            log.info("No user authenticated");
        } else {
            log.info("authenticated user - {}", user.getUid());
        }
        return ResponseEntity.ok("ok");
    }
}
