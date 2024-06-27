package com.okex.open.api.wsService.publicUrl;

import com.alibaba.fastjson.JSONObject;
import com.okex.open.api.bean.SubscribeReq;
import com.okex.open.api.websocket.OkxWsClient;
import com.okex.open.api.websocket.OkxWssHandler;
import com.okex.open.api.wsService.publicUrl.Impl.pubWsServiceImpl;

import java.util.List;

public class pubDemo {
    private static String Url = "wss://wsaws.okx.com:8443/ws/v5/public";

    private static OkxWsClient client;
    private static final pubWsService wsService = new pubWsServiceImpl();
    public static void main(String[] args) {

        Boolean isLogin = false;
        client = OkxWssHandler.builder()
                .pushUrl(Url)
                .apiKey("")
                .secretKey("")
                .passPhrase("")
                .isLogin(isLogin)
                .listener(response -> {
                    JSONObject json = JSONObject.parseObject(response);
                    //System.out.println("def:" + json);
                    if(isLogin){
                        System.out.println(json);
                    }else{
                        System.out.println(json);
                    }
                    //失败消息的逻辑处理,如:订阅失败
                }).errorListener(response -> {
                    JSONObject json = JSONObject.parseObject(response);
                    System.out.println("error:" + json);
                }).build();
        // ping/pong msg
        getTickers();
//        getTrades();
//        getOrdBooks();
//        getBboTbt();
//        getInstrument();
//        getMarkPrice();
//        getOpenInterest();
//        getMarkPrice();
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Demo finish");
    }

    public static void getTickers() {
        String instId = "FIL-USD-SWAP";

        List<SubscribeReq> args = wsService.getTickers(instId);

        client.subscribe(args);
    }

    public static void getTrades() {
        String instId = "BTC-USDT-xxxx";
        List<SubscribeReq> args = wsService.getTrades(instId);
        client.subscribe(args);
    }

    public static void getOrdBooks() {
        String instId = "BTC-USDT";
        List<SubscribeReq> args = wsService.getOrdBooks(instId);
        client.subscribe(args);
    }

    public static void getBboTbt() {
        String instId = "BTC-USDT";
        List<SubscribeReq> args = wsService.getBboTbt(instId);
        client.subscribe(args);
    }
//
//    public static void getInstrument() {
//        String instType = "FUTURES";
//        String args = wsService.getInstrument(instType);
//        webSocketClient.subscribe(args);
//    }
//
//    public static void getOpenInterest () {
//        String instId = "LTC-USD-SWAP";
//        String args = wsService.getOpenInterest(instId);
//
//        webSocketClient.subscribe(args);
//    }
//
//    public static void getMarkPrice () {
//        String instId = "BTC-USDT-240105";
//        String args = wsService.getMarkPrice(instId);
//
//        webSocketClient.subscribe(args);
//    }

}
