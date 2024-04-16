package com.okex.open.api.wsService.accout.Impl;
import com.alibaba.fastjson.JSONArray;
import com.okex.open.api.bean.SubscribeReq;
import com.okex.open.api.wsService.accout.AccountWSService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountWSServiceImpl implements AccountWSService {

    @Override
    public List<SubscribeReq> getBalance() {
        List<SubscribeReq> channelList = new ArrayList<>();
//        Map<String, String> accountMap = new HashMap();
        SubscribeReq subscribeReq = new SubscribeReq("","account");

//        accountMap.put("channel","account");

        channelList.add(subscribeReq);
        return channelList;
    }

    @Override
    public String getBalance(String ccy) {
        ArrayList<Map> channelList = new ArrayList<>(2);
        Map<String, String> accountMap = new HashMap();

        accountMap.put("channel","account");
        accountMap.put("ccy", ccy);
        channelList.add(accountMap);
        return formatArgs(channelList);
    }

    @Override
    public String getPositions(String instType, String instFamily, String instId) {
        ArrayList<Map> channelList = new ArrayList<>(2);
        Map<String, String> positionMap = new HashMap();

        positionMap.put("channel","positions");
        positionMap.put("instType", instType);
        if (instFamily != "") {
            positionMap.put("instFamily", instFamily);
        }
        if (instId != "") {
            positionMap.put("instId", instId);
        }

        channelList.add(positionMap);
        return formatArgs(channelList);
    }

    @Override
    public String getBalanceAndPosition() {
        ArrayList<Map> channelList = new ArrayList<>(2);
        Map<String, String> positionMap = new HashMap();

        positionMap.put("channel","balance_and_position");

        channelList.add(positionMap);
        return formatArgs(channelList);
    }

    @Override
    public String getLiquidationWarning(String instType) {
        ArrayList<Map> channelList = new ArrayList<>(2);
        Map<String, String> positionMap = new HashMap();

        positionMap.put("channel","liquidation-warning");
        positionMap.put("instType", instType);

        channelList.add(positionMap);
        return formatArgs(channelList);
    }

    @Override
    public String getAccountGreeks() {
        ArrayList<Map> channelList = new ArrayList<>(2);
        Map<String, String> positionMap = new HashMap();

        positionMap.put("channel","account-greeks");

        channelList.add(positionMap);
        return formatArgs(channelList);
    }

    @Override
    public String getAccountGreeks(String ccy) {
        ArrayList<Map> channelList = new ArrayList<>(2);
        Map<String, String> positionMap = new HashMap();

        positionMap.put("channel","account-greeks");
        positionMap.put("ccy", ccy);

        channelList.add(positionMap);
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
