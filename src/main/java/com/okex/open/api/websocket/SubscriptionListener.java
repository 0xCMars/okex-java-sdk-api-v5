package com.okex.open.api.websocket;

@FunctionalInterface
public interface SubscriptionListener {
    void onReceive(String data);
}
