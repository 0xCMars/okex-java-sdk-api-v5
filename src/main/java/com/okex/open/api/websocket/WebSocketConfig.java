package com.okex.open.api.websocket;

public class WebSocketConfig {
    //ws  模拟盘
//    private static final String SERVICE_URL = "wss://ws.okex.com:8443/ws/v5/private?brokerId=9999";

    //ws  实盘
    private static final String SERVICE_URL = "wss://ws.okex.com:8443/ws/v5/private";

    // 实盘api key
    private static final String API_KEY = "99306305-7de4-4c57-bf70-2ef835431182";
    private static final String SECRET_KEY = "AFEC9D50AB0E5EC6CAF88DAA425B257A";
    private static final String PASSPHRASE = "m55AVzi@E9un";

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
