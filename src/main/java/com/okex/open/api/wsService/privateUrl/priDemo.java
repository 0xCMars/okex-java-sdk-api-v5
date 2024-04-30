package com.okex.open.api.wsService.privateUrl;

import com.alibaba.fastjson.JSONObject;
import com.okex.open.api.bean.SubscribeReq;
import com.okex.open.api.websocket.OkxWsClient;
import com.okex.open.api.websocket.OkxWssHandler;
import com.okex.open.api.websocket.WebSocketConfig;
import com.okex.open.api.wsService.accout.Impl.AccountWSServiceImpl;

import java.util.List;

public class priDemo {
    private static String privateUrl = "wss://ws.okx.com:8443/ws/v5/private";
    private static OkxWsClient client;
    private static final AccountWSServiceImpl wsService= new AccountWSServiceImpl();
    public static void main(String[] args) {

        Boolean isLogin = true;

        client = OkxWssHandler.builder()
                .pushUrl(privateUrl)
                .apiKey(WebSocketConfig.getApiKey())
                .secretKey(WebSocketConfig.getSecretKey())
                .passPhrase(WebSocketConfig.getPassphrase())
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

        // wait for login successfully
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        getBalance();
        getPositions();
//        getBalAndPos();
//        getLiquidation();
//        getAccountGreek();
        try {
            Thread.sleep(1000000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Demo finish");
    }

    public static void getBalance() {
        String ccy = "USDT";
//        String args = wsService.getBalance();

        List<SubscribeReq> args = wsService.getBalance(ccy);

        client.subscribe(args);
    }

    public static void getPositions() {
        List<SubscribeReq> args = wsService.getPositions("FUTURES", "BTC-USD", "");
        client.subscribe(args);
    }

//    public static void getBalAndPos() {
//        List<SubscribeReq> args = wsService.getBalanceAndPosition();
//        client.subscribe(args);
//    }
//
//    public static void getLiquidation() {
//        String args = wsService.getLiquidationWarning("ANY");
//        client.subscribe(args);
//    }
//
    public static void getAccountGreek () {
        List<SubscribeReq> args = wsService.getAccountGreeks();

        client.subscribe(args);
    }
}
