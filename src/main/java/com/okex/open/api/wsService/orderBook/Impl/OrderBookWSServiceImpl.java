package com.okex.open.api.wsService.orderBook.Impl;
import com.alibaba.fastjson.JSONArray;
import com.okex.open.api.bean.OrderReq;
import com.okex.open.api.bean.SubscribeReq;
import com.okex.open.api.wsService.orderBook.OrderBookWSService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderBookWSServiceImpl implements OrderBookWSService {

    @Override
    public String placeOrder(String side, String instId, String tdMode, String ordType, String sz, String px) {
        ArrayList<Map> channelList = new ArrayList<>(6);
        Map<String, String> paramMap = new HashMap();

        paramMap.put("side",side);
        paramMap.put("instId",instId);
        paramMap.put("tdMode",tdMode);
        paramMap.put("ordType",ordType);
        paramMap.put("sz",sz);
        paramMap.put("px",px);

        channelList.add(paramMap);
        return formatArgs(channelList);
    }

    // This channel uses private WebSocket and authentication is required.
    public String cancelOrd(String id, String instId, String ordId){
        ArrayList<Map> channelList = new ArrayList<>(2);
        Map<String, String> paramMap = new HashMap();

        paramMap.put("instId",instId);
        if (!id.equals("")) paramMap.put("clOrdId",id);
        else if (!ordId.equals("")) paramMap.put("ordId",ordId);
        else return "";

        channelList.add(paramMap);
        return formatArgs(channelList);
    }

    public String amendOrd(String id, String instId, String ordId, String newSz) {
        return "";
    }

    // Cancel all the MMP pending orders of an instrument family.
    public String massCancel(String id, String instId, String instFamily) {
        return "";
    }


    // Market Data
    // This channel uses public WebSocket and authentication is not required.
    public String getTicket(String instId) {
        return "";
    }

    //
    public String getInstrument(String instType) {
        return "";
    }

    public String getTradeChannel() {
        return "";
    }

    // Trade
    // Retrieve order information.
    public List<SubscribeReq> getOrderChannel(String instType) {
        List<SubscribeReq> channelList = new ArrayList<>(2);

        OrderReq subscribeReq = new OrderReq(instType);
        channelList.add(subscribeReq);
        return channelList;
    }

    private String formatArgs(List<Map> list) {
        JSONArray jsonArray = new JSONArray();
        for (Map map : list) {
            jsonArray.add(net.sf.json.JSONObject.fromObject(map));
        }
        return jsonArray.toJSONString();
    }
}
