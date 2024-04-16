package com.okex.open.api.websocket;

import com.okex.open.api.bean.SubscribeReq;
import com.okex.open.api.bean.WsBaseReq;

import java.util.List;

public interface OkxWsClient {

    void sendMessage(WsBaseReq req);

    void sendMessage(String message);

    void unsubscribe(List<SubscribeReq> str);

    void subscribe(List<SubscribeReq> list);

    void subscribe(List<SubscribeReq> list, SubscriptionListener listener);

    void login();
}
