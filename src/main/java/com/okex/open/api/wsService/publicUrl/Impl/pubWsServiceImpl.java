package com.okex.open.api.wsService.publicUrl.Impl;
import com.alibaba.fastjson.JSONArray;
import com.okex.open.api.bean.SubscribeReq;
import com.okex.open.api.wsService.publicUrl.pubWsService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class pubWsServiceImpl implements pubWsService {

    @Override
    public List<SubscribeReq> getTickers(String instId) {
        List<SubscribeReq> channelList = new ArrayList<>();
        SubscribeReq subscribeReq = new SubscribeReq(instId, "tickers");
//        Map<String, String> argsMap = new HashMap();
//
//        argsMap.put("channel","tickers");
//        argsMap.put("instId", instId);
        channelList.add(subscribeReq);
        return channelList;
    }

    @Override
    public String getTrades(String instId) {
        ArrayList<Map> channelList = new ArrayList<>();
        Map<String, String> argsMap = new HashMap();

        argsMap.put("channel","trades");
        argsMap.put("instId", instId);
        channelList.add(argsMap);
        return formatArgs(channelList);
    }

    @Override
    public String getOrdBooks(String channel, String instId) {
        ArrayList<Map> channelList = new ArrayList<>();
        Map<String, String> argsMap = new HashMap();

        argsMap.put("channel", channel);
        argsMap.put("instId", instId);
        channelList.add(argsMap);
        return formatArgs(channelList);
    }

    @Override
    public String getInstrument(String instType) {
        ArrayList<Map> channelList = new ArrayList<>();
        Map<String, String> argsMap = new HashMap();

        argsMap.put("channel", "instruments");
        argsMap.put("instType", instType);
        channelList.add(argsMap);
        return formatArgs(channelList);
    }

    @Override
    public String getOpenInterest(String instId) {
        ArrayList<Map> channelList = new ArrayList<>();
        Map<String, String> argsMap = new HashMap();

        argsMap.put("channel", "open-interest");
        argsMap.put("instId", instId);
        channelList.add(argsMap);
        return formatArgs(channelList);
    }

    @Override
    public String getMarkPrice(String instId) {
        ArrayList<Map> channelList = new ArrayList<>();
        Map<String, String> argsMap = new HashMap();

        argsMap.put("channel", "mark-price");
        argsMap.put("instId", instId);
        channelList.add(argsMap);
        return formatArgs(channelList);
    }

    private String formatArgs(List<Map> list) {
        JSONArray jsonArray = new JSONArray();
        for (Map map : list) {
            jsonArray.add(net.sf.json.JSONObject.fromObject(map));
        }
        return jsonArray.toJSONString();
    }

}
