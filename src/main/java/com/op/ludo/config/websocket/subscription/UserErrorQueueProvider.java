package com.op.ludo.config.websocket.subscription;

import java.security.Principal;
import java.util.regex.Pattern;

public class UserErrorQueueProvider implements SubscriptionProvider {

    private static final String userErrorQueuePattern = "/user/queue/errors";

    @Override
    public boolean handles(String destination) {
        return Pattern.matches(userErrorQueuePattern, destination);
    }

    @Override
    public boolean hasPermission(Principal principal, String destination) {
        return handles(destination) && principal != null;
    }
}
