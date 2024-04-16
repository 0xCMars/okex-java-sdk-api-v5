package com.okex.open.api.wsService;

import com.okex.open.api.websocket.OkxWsClient;
import com.okex.open.api.websocket.WebSocketListener;

import java.util.ArrayList;

public class demoWebSocketListener implements WebSocketListener {

    private long sendTime;
    private ArrayList<Long> timeTable;

    public void setSendTime(Long sendTime) {
        this.sendTime = sendTime;
        this.timeTable = new ArrayList<Long>();
    }

    public void onTextMessage(OkxWsClient ws, String text) {
        this.timeTable.add(System.currentTimeMillis() - this.sendTime);
        this.sendTime = System.currentTimeMillis();
        System.out.println("Received msg is:" + text);
//        System.out.println("Time: " + System.currentTimeMillis());
    };

    public void onWebsocketOpen(OkxWsClient ws) {
            System.out.println("Use in websocket open");
        }

    public void handleCallbackError(OkxWsClient websocket, Throwable cause) {
            System.out.println("This is a Error!!!!!");
            System.out.println(cause);
    }
    public  void onWebsocketClose(OkxWsClient ws, int code) {
            System.out.println("This no more websocket connect");
    }
    public void onWebsocketPong(OkxWsClient ws) {
            System.out.println("The Heart is just beaten");
    }

    public void show() {
        int size = timeTable.size();
        Long avg = 0L;
        for (int i = 0; i < size; i++) {
            System.out.printf("The %d msg received Time Delay is %d ms\n", i+1, timeTable.get(i));
            avg += timeTable.get(i);
        }
        System.out.printf("Avg msg received Time Delay is %d ms\n", avg / size);

    }

}
