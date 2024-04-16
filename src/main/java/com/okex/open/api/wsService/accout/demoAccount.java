//package com.okex.open.api.wsService.accout;
//
//import com.okex.open.api.websocket.old.OkxWsClientClient;
//import com.okex.open.api.wsService.accout.Impl.AccountWSServiceImpl;
//import com.okex.open.api.wsService.demoWebSocketListener;
//import com.okex.open.api.websocket.old.WebSocketConfig;
//public class demoAccount {
//
//    private static final demoWebSocketListener demoListener = new demoWebSocketListener();
//    private static final OkxWsClientClient webSocketClient = new OkxWsClientClient(demoListener);
//
//    private static final AccountWSServiceImpl wsService= new AccountWSServiceImpl();
//    public static void main(String[] args) {
//
//        webSocketClient.connect();
//        // ping/pong msg
//        webSocketClient.beginTimer();
//
//        webSocketClient.login(WebSocketConfig.getApiKey(), WebSocketConfig.getSecretKey(), WebSocketConfig.getPassphrase());
//
//        // wait for login successfully
//        try {
//            Thread.sleep(1000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
////        getBalance();
////        getPositions();
////        getBalAndPos();
////        getLiquidation();
//        getAccountGreek();
//        try {
//            Thread.sleep(1000000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        webSocketClient.close();
//        System.out.println("Demo finish");
//    }
//
//    public static void getBalance() {
//        String ccy = "BTC";
////        String args = wsService.getBalance();
//
//        String args = wsService.getBalance(ccy);
//
//        webSocketClient.subscribe(args);
//    }
//
//    public static void getPositions() {
//        String args = wsService.getPositions("FUTURES", "BTC-USD", "");
//        webSocketClient.subscribe(args);
//    }
//
//    public static void getBalAndPos() {
//        String args = wsService.getBalanceAndPosition();
//        webSocketClient.subscribe(args);
//    }
//
//    public static void getLiquidation() {
//        String args = wsService.getLiquidationWarning("ANY");
//        webSocketClient.subscribe(args);
//    }
//
//    public static void getAccountGreek () {
//        String args = wsService.getAccountGreeks();
//
////        String ccy = "BTC";
////        String args = wsService.getAccountGreeks(ccy);
//
//        webSocketClient.subscribe(args);
//    }
//}
