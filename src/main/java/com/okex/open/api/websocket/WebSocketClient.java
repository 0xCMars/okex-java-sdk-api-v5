package com.okex.open.api.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.util.*;

import com.okex.open.api.websocket.WebSocketListener;
import com.okex.open.api.websocket.WebSocketClientHandler;

public class WebSocketClient implements WebSocket {
    private final static HashFunction crc32 = Hashing.crc32();
    private Channel ch;
    private WebSocketListener listener;
    private String URL = "wss://ws.okx.com:8443/ws/v5/private";
    private Timer timer = new HashedWheelTimer(Executors.defaultThreadFactory());

    public WebSocketClient(WebSocketListener listener) {
        this.listener = listener;
    }

    public WebSocketClient(String url, WebSocketListener listener) {
        URL = url;
        this.listener = listener;
    }

    private void init() {
        try {
            URI uri  = new URI(URL);
            String scheme = uri.getScheme() == null? "ws" : uri.getScheme();
            final String host = uri.getHost() == null? "127.0.0.1" : uri.getHost();
            final int port = uri.getPort();

            if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
                this.listener.handleCallbackError(this, new RuntimeException("Only WS(S) is supported."));
                return;
            }

            final boolean ssl = "wss".equalsIgnoreCase(scheme);
            final SslContext sslCtx;
            if (ssl) {
                sslCtx = SslContextBuilder.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            } else {
                sslCtx = null;
            }

            EventLoopGroup group = new NioEventLoopGroup();

            WebSocketClientHandshaker shaker = WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders());
            final WebSocketClientHandler handler = new WebSocketClientHandler(this, shaker, this.listener);

            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            if (sslCtx != null) {
                                p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                            }
                            p.addLast(new HttpClientCodec());
                            p.addLast(new HttpObjectAggregator(8192));
                            p.addLast(WebSocketClientCompressionHandler.INSTANCE);
                            p.addLast(handler);
                            p.addLast(new IdleStateHandler(0, 30, 0));

                        }
                    });

            ch = b.connect(uri.getHost(), port).sync().channel();
            handler.handshakeFuture().sync();

        }catch (Exception e) {
            this.listener.handleCallbackError(this, e);
        }
    }


    @Override
    public void close() {
        ch.close();
    }

    @Override
    public void connect() {
        this.init();
    }

    @Override
    public void login(String apiKey, String apiSecret, String passphrase) {
        // timestamp
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String str = timeStamp + "GET/users/self/verify";
        String hash = "";
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");

            SecretKeySpec secret_key = new SecretKeySpec(apiSecret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            hash = Base64.encodeBase64String(sha256_HMAC.doFinal(str.getBytes()));
        }catch (Throwable t){
            t.printStackTrace();
            this.listener.handleCallbackError(this,t);
        }
        ArrayList<Map> argsList= new ArrayList<>();
        Map argsMap =new HashMap();

        argsMap.put("apiKey", apiKey);
        argsMap.put("passphrase", passphrase);
        argsMap.put("timestamp", timeStamp);
        argsMap.put("sign", hash);
        argsList.add(argsMap);
        String args = formatArgs(argsList);
        this.send("{\"op\": \"login\", \"args\":" + args + "}");
    }

    @Override
    public void subscribe(String args) {
//        String channels = formatArgs(list);
//        System.out.println(channels);
//        this.send("{\"args\":[\"" + channels + "\"],\"op\":\"subscribe\"}");
        this.send("{\"op\": \"subscribe\", \"args\":" + args + "}");
    }

    @Override
    public void unSubscribe(String args) {
//        String channels = formatArgs(list);
//        this.send("{\"args\":[\"" + channels + "\"],\"op\":\"unsubscribe\"}");
        this.send("{\"op\": \"unsubscribe\", \"args\":" + args + "}");

    }

    @Override
    public void sendPing() {
        this.send("ping");
    }

    @Override
    public boolean checkSum(String data) {
        try {
            JSONObject obj = (JSONObject) JSON.parse(data);
            JSONArray bidsArray = null;
            JSONArray asksArray = null;
            int checksum = 0;
            if (obj.get("table") != null && obj.get("action") != null) {
                JSONArray dataObj = (JSONArray) obj.get("data");
                JSONObject targetObj = (JSONObject) dataObj.get(0);
                bidsArray = (JSONArray) targetObj.get("bids");
                asksArray = (JSONArray) targetObj.get("asks");
                checksum = (int)targetObj.get("checksum");

            } else {
                bidsArray = (JSONArray) obj.get("bids");
                asksArray = (JSONArray) obj.get("asks");
                checksum = (int)obj.get("checksum");
            }

            StringBuilder targetStr = new StringBuilder();
            for (int index = 0; index < 25; index++) {
                if (bidsArray != null && index < bidsArray.size()) {
                    JSONArray bidsObject = (JSONArray)bidsArray.get(index);
                    for (int num = 0; num < 2; num++) {
                        targetStr.append(bidsObject.get(num));
                        targetStr.append(":");
                    }
                }
                if (asksArray != null && index < asksArray.size()) {
                    JSONArray asksObject = (JSONArray)asksArray.get(index);
                    for (int num = 0; num < 2; num++) {
                        targetStr.append(asksObject.get(num));
                        targetStr.append(":");
                    }
                }
            }

            final String strs;
            if (targetStr.length() > 0) {
                strs = targetStr.substring(0, targetStr.length() - 1);
            } else {
                strs = "";
            }

            int fianlStr = crc32.hashString(strs, StandardCharsets.UTF_8).asInt();
            return fianlStr == checksum;
        }catch (Exception e) {
            this.listener.handleCallbackError(this, e);
        }
        return false;
    }

    private  void send(String msg){
        WebSocketFrame frame = new TextWebSocketFrame(msg);
        ch.writeAndFlush(frame);
    }

    private String formatArgs(List<Map> list) {
        JSONArray jsonArray = new JSONArray();
        for (Map map : list) {
            jsonArray.add(net.sf.json.JSONObject.fromObject(map));
        }
        return jsonArray.toJSONString();
    }

    public void beginTimer(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                addTask(this);
                timerTask();
            }
        };
        addTask(timerTask);
    }

    private void addTask(TimerTask task){
        this.timer.newTimeout(task, 5000, TimeUnit.MILLISECONDS);
    }

    private void timerTask(){
        this.send("ping");
    }


}
