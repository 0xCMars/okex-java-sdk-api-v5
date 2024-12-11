package com.okex.open.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.okex.open.api.bean.trade.param.CancelOrder;
import com.okex.open.api.bean.trade.param.PlaceOrder;
import com.okex.open.api.config.APIConfiguration;
import com.okex.open.api.enums.I18nEnum;
import com.okex.open.api.service.trade.TradeAPIService;
import com.okex.open.api.service.trade.impl.TradeAPIServiceImpl;
import java.time.temporal.ChronoUnit;
import java.time.Instant;

public class SendOrder {
    
    public static void main(String[] args) throws InterruptedException{
      APIConfiguration config = new APIConfiguration();

      config.setEndpoint("https://coloapi3.okx.com/");


      config.setApiKey("7349788e-3776-4649-9151-4cc8d16afd9b");
      config.setSecretKey("DFB0CC0AF519D142DA4B1671515BAE13");
      config.setPassphrase("Qweasdzxc11!");


      config.setPrint(true);
     /* config.setI18n(I18nEnum.SIMPLIFIED_CHINESE);*/
      config.setI18n(I18nEnum.ENGLISH);

      TradeAPIService tradeAPIService = new TradeAPIServiceImpl(config);


      Metric m1 = new Metric("send order");
      Metric m2 = new Metric("cancel order");
      for (int i = 0; i < 100; i++){
        PlaceOrder placeOrder =new PlaceOrder();
        placeOrder.setInstId("TRX-USDT-SWAP");
        placeOrder.setTdMode("cross");
        placeOrder.setSide("buy");
        placeOrder.setPosSide("net");
        placeOrder.setOrdType("limit");
        placeOrder.setPx("0.23");
        placeOrder.setSz("1");
        Long start = ChronoUnit.MICROS.between(Instant.EPOCH, Instant.now());
        //m1.startSample();
        JSONObject result = tradeAPIService.placeOrder(placeOrder);
        //m1.stopSampe();
        //Long end = ChronoUnit.MICROS.between(Instant.EPOCH, Instant.now());
        //Long newOrderTime = end - start;
        JSONArray data = result.getJSONArray("data");
        if (data.size() < 1) {
            System.out.println("failed to place order");
            return;
        }
        Long end = result.getLong("inTime");
        m1.pushSampe(end - start);
        String ordId = data.getJSONObject(0).getString("ordId");
        CancelOrder cancelOrder = new CancelOrder();
        cancelOrder.setInstId("TRX-USDT-SWAP");
        cancelOrder.setOrdId(ordId);
        cancelOrder.setClOrdId("");
        //m2.startSample();
        Thread.sleep(500);
        start = ChronoUnit.MICROS.between(Instant.EPOCH, Instant.now());
        result = tradeAPIService.cancelOrder(cancelOrder);
        //m2.stopSampe();
        //end = ChronoUnit.MICROS.between(Instant.EPOCH, Instant.now());
        data = result.getJSONArray("data");
        if (data.size() < 1) {
            System.out.println("failed to cancel order "+ordId);
            return;
        }
        end = result.getLong("inTime");
        m2.pushSampe(end - start);
        Thread.sleep(1000);
        //Long cancelOrderTime = end - start;
        //System.out.println(String.format("newOrderTime:%d", newOrderTime));
        //System.out.println(String.format("cancelOrderTime:%d", cancelOrderTime));
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
