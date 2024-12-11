package com.okex.open.api.wsService.orderBook;

import java.util.List;

import com.okex.open.api.bean.SubscribeReq;

public interface OrderBookWSService {

    // Trade
    // Retrieve order information.
    List<SubscribeReq> getOrderChannel(String instType);

    // This channel uses private WebSocket and authentication is required.
    String placeOrder(String side, String instId, String tdMode, String ordType, String sz, String px);

    // This channel uses private WebSocket and authentication is required.
    String cancelOrd(String id, String instId, String ordId);

    String amendOrd(String id, String instId, String ordId, String newSz);

    // Cancel all the MMP pending orders of an instrument family.
    String massCancel(String id, String instId, String instFamily);


    // Market Data
    // This channel uses public WebSocket and authentication is not required.
    String getTicket(String instId);

    //
    String getInstrument(String instType);

    String getTradeChannel();

}
