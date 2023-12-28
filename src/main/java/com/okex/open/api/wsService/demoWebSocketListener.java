package com.okex.open.api.wsService;

import com.okex.open.api.websocket.WebSocket;
import com.okex.open.api.websocket.WebSocketListener;

public class demoWebSocketListener implements WebSocketListener {

    public void onTextMessage(WebSocket ws, String text) {
            System.out.println("Received msg is:" + text);
        };

    public void onWebsocketOpen(WebSocket ws) {
            System.out.println("Use in websocket open");
        }

    public void handleCallbackError(WebSocket websocket, Throwable cause) {
            System.out.println("This is a Error!!!!!");
            System.out.println(cause);
    }
    public  void onWebsocketClose(WebSocket ws, int code) {
            System.out.println("This no more websocket connect");

    }
    public void onWebsocketPong(WebSocket ws) {
            System.out.println("The Heart is just beaten");
    }

}
