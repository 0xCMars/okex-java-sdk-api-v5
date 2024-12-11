package com.okex.open.api;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.okex.open.api.websocket.OkxWsClient;
import com.okex.open.api.websocket.OkxWssHandler;
import com.okex.open.api.wsService.orderBook.Impl.OrderBookWSServiceImpl;
import com.okex.open.api.wsService.orderBook.OrderBookWSService;

public class SendOrderWs {
  private static final OrderBookWSService wsService= new OrderBookWSServiceImpl();
  private static OkxWsClient client;
  private static int id = 1;
  public static long placeOrderStart = 0;
  private static long cancelOrderStart = 0;
  public static String lastOrdId = "";
  public static Metric m1 = new Metric("send order");
  public static Metric m2 = new Metric("cancel order");

  public static void placeOrder(String side, String instId, String tdMode, String ordType, String sz, String px) {

    String args = wsService.placeOrder(side, instId, tdMode, ordType, sz, px);
    StringBuilder sb = new StringBuilder();
    sb.append("{\"id\":\"");
    sb.append(id++);
    sb.append("\",\"op\":\"order\",\"args\":");
    sb.append(args);
    sb.append("}");

    client.sendMessage(sb.toString());
  }

  public static void cancelOrder(String instId, String ordId) {

    String args = wsService.cancelOrd("",instId,ordId);
    StringBuilder sb = new StringBuilder();
    sb.append("{\"id\":\"");
    sb.append(id++);
    sb.append("\",\"op\":\"cancel-order\",\"args\":");
    sb.append(args);
    sb.append("}");

    client.sendMessage(sb.toString());
  }
    
    public static void main(String[] args) throws InterruptedException{
      String privateUrl = "wss://colows3.okx.com/ws/v5/private";

      client  = OkxWssHandler.builder()
                .pushUrl(privateUrl)
                .apiKey("7349788e-3776-4649-9151-4cc8d16afd9b")
                .secretKey("DFB0CC0AF519D142DA4B1671515BAE13")
                .passPhrase("Qweasdzxc11!")
                .isLogin(true)
                .listener(response -> {
                    JSONObject json = JSONObject.parseObject(response);
                    if (json.containsKey("op") && json.getString("op").equals("order")){
                      JSONArray data = json.getJSONArray("data");
                      if (data.size() < 1) {
                          System.out.println("failed to place order");
                          return;
                      }
                      lastOrdId = data.getJSONObject(0).getString("ordId");
                      long inTime = json.getLong("inTime");
                      m1.pushSampe(inTime-placeOrderStart);
                      placeOrderStart = 0;
                      System.out.println("hi1");
                    }
                    if (json.containsKey("op") && json.getString("op").equals("cancel-order")){
                      JSONArray data = json.getJSONArray("data");
                      if (data.size() < 1) {
                          System.out.println("failed to cancel order");
                          return;
                      }
                      long inTime = json.getLong("inTime");
                      m2.pushSampe(inTime-cancelOrderStart);
                      cancelOrderStart = 0;
                      System.out.println("hi2");
                    }

                    System.out.println(json);
                }).errorListener(response -> {
                    JSONObject json = JSONObject.parseObject(response);
                    System.out.println("error:" + json);
                }).build();
      
      
      client.subscribe(wsService.getOrderChannel("SWAP"));

      
      m1 = new Metric("send order");
      m2 = new Metric("cancel order");
      for (int i = 0; i < 100; i++){
        //Long start = ChronoUnit.MICROS.between(Instant.EPOCH, Instant.now());
        if (placeOrderStart != 0) {
          System.out.println("last order not finished");
          break;
        }
        placeOrderStart = ChronoUnit.MICROS.between(Instant.EPOCH, Instant.now());
        placeOrder("buy", "TRX-USDT-SWAP", "cross", "limit", "1", "0.23");
        Thread.sleep(500);
        if (cancelOrderStart != 0) {
          System.out.println("last cancel not finished");
          break;
        }
        cancelOrderStart = ChronoUnit.MICROS.between(Instant.EPOCH, Instant.now());
        if (lastOrdId.equals("")) {
          System.out.println("last order not finished");
          break;
        }
        cancelOrder("TRX-USDT-SWAP", lastOrdId);
        lastOrdId = "";
        Thread.sleep(1000);
      }
      m1.reportAll();
      m1.reportAvg();
      m1.clear();
      m2.reportAll();
      m2.reportAvg();
      m2.clear();
      return;
    }
  
}
