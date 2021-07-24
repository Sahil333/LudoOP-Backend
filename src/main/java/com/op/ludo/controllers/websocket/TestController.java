package com.op.ludo.controllers.websocket;

import java.security.Principal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class TestController {

    @MessageMapping("/test")
    public void test(String msg, Principal principal) {
        log.info(principal.getName());
    }
}
