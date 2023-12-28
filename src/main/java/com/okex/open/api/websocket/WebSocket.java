package com.okex.open.api.websocket;

import java.util.List;
import java.util.Map;

public interface WebSocket {

    void connect();

    void close();

    void login(String apiKey, String apiSecret, String passphrase);

    void subscribe(String args);

    void unSubscribe(String args);

    void sendPing();

    boolean checkSum(String data);
}
