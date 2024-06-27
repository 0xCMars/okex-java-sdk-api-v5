package com.okex.open.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class demo {
    public static void main(String[] args){
        wssClientExample wssClientExample = new wssClientExample();
        wssClientExample.initConnect();

        ArrayList<Map> channelList = new ArrayList<>(); // add everything in one go

        final Map firstLevelBBO_map = new HashMap();
        firstLevelBBO_map.put("channel", "bbo-tbt");
        final Map nLevels_map = new HashMap();
        nLevels_map.put("channel", "books");    // use this to test
        final Map mktTrades_map = new HashMap();
        mktTrades_map.put("channel", "trades");

        firstLevelBBO_map.put("instId", "ETH-USDT");
        nLevels_map.put("instId", "BTC-USDT");
        mktTrades_map.put("instId", "BTC-USDT");


        channelList.add(firstLevelBBO_map);
        channelList.add(nLevels_map);
        channelList.add(mktTrades_map);

        wssClientExample.subscribe(channelList);
    }
}
