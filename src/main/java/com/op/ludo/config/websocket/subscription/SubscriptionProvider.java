package com.op.ludo.config.websocket.subscription;

import java.security.Principal;

public interface SubscriptionProvider {

    boolean handles(String destination);

    boolean hasPermission(Principal principal, String destination);
}
