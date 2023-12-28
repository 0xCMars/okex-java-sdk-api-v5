//package com.okex.open.api.websocket;
//
//import java.util.*;
//
//public class demo {
//
//    private static final demoWebSocketListener demoListener = new demoWebSocketListener();
//    private static final WebSocketClient webSocketClient = new WebSocketClient(demoListener);
//
//    public static void main(String[] args) {
//
//        webSocketClient.connect();
//        // ping/pong msg
//        webSocketClient.beginTimer();
//
////        webSocketClient.login(WebSocketConfig.getApiKey(), WebSocketConfig.getSecretKey(), WebSocketConfig.getPassphrase());
//
//        tickersChannel();
//        try {
//            Thread.sleep(10000000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        webSocketClient.close();
//        System.out.println("Demo finish");
//    }
//
//    public static void privateAccountChannel() {
//        //添加订阅频道
//        ArrayList<Map> channelList= new ArrayList<>();
//        Map accountMap =new HashMap();
//
//        accountMap.put("channel","account");
//        accountMap.put("ccy","USDT");
//
//        channelList.add(accountMap);
//
//        //调用订阅方法
//        webSocketClient.subscribe(channelList);
//        //为保证测试方法不停，需要让线程延迟
//        try {
//            Thread.sleep(10000000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void tickersChannel() {
//        //添加订阅频道
//        ArrayList<Map> channelList= new ArrayList<>();
//
//
//        Map spotTickerMap = new HashMap();
//        spotTickerMap.put("channel","tickers");
//        spotTickerMap.put("instId","FIL-USD-SWAP");
//
//
//        channelList.add(spotTickerMap);
//
//
//        //调用订阅方法
//        webSocketClient.subscribe(channelList);
//        //为保证测试方法不停，需要让线程延迟
//        try {
//            Thread.sleep(100000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        webSocketClient.unSubscribe(channelList);
//    }
//}
