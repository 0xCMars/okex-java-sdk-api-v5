package com.okex.open.api.wsService.publicUrl;

import com.okex.open.api.websocket.WebSocketClient;
import com.okex.open.api.wsService.demoWebSocketListener;
import com.okex.open.api.wsService.publicUrl.Impl.pubWsServiceImpl;

public class pubDemo {
    private static final demoWebSocketListener demoListener = new demoWebSocketListener();
    private static String Url = "wss://ws.okx.com:8443/ws/v5/public";
    private static final WebSocketClient webSocketClient = new WebSocketClient(Url, demoListener);

    private static final pubWsServiceImpl wsService = new pubWsServiceImpl();
    public static void main(String[] args) {

        webSocketClient.connect();
        // ping/pong msg
//        webSocketClient.beginTimer();
        getTickers();
//        getTrades();
//        getOrdBooks();
//        getInstrument();
//        getMarkPrice();
//        getOpenInterest();
//        getMarkPrice();
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        demoListener.show();
        webSocketClient.close();
        System.out.println("Demo finish");
    }

    public static void getTickers() {
        String instId = "FIL-USD-SWAP";

        String args = wsService.getTickers(instId);

        webSocketClient.subscribe(args);
    }

    public static void getTrades() {
        String instId = "BTC-USDT-240105";
        String args = wsService.getTrades(instId);
        webSocketClient.subscribe(args);
    }

    public static void getOrdBooks() {
        String chan = "books";
        String instId = "BTC-USDT";
        String args = wsService.getOrdBooks(chan, instId);
        webSocketClient.subscribe(args);
    }

    public static void getInstrument() {
        String instType = "FUTURES";
        String args = wsService.getInstrument(instType);
        webSocketClient.subscribe(args);
    }

    public static void getOpenInterest () {
        String instId = "LTC-USD-SWAP";
        String args = wsService.getOpenInterest(instId);

        webSocketClient.subscribe(args);
    }

    public static void getMarkPrice () {
        String instId = "BTC-USDT-240105";
        String args = wsService.getMarkPrice(instId);

        webSocketClient.subscribe(args);
    }

}
