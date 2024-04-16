package com.okex.open.api.websocket.old;

public class WebSocketConfig {
    //ws  模拟盘
//    private static final String SERVICE_URL = "wss://ws.okex.com:8443/ws/v5/private?brokerId=9999";

    //ws  实盘
    private static final String SERVICE_URL = "wss://ws.okex.com:8443/ws/v5/private";

    // 实盘api key
    private static final String API_KEY = "xxxx";
    private static final String SECRET_KEY = "xxxx";
    private static final String PASSPHRASE = "xxxx";

    public static String getApiKey() {
        return API_KEY;
    }

    public static String getSecretKey() {
        return SECRET_KEY;
    }

    public static String getPassphrase() {
        return PASSPHRASE;
    }

}
