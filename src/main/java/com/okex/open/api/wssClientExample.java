package com.okex.open.api;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.okex.open.api.bean.other.OrderBookItem;
import com.okex.open.api.bean.other.SpotOrderBook;
import com.okex.open.api.bean.other.SpotOrderBookDiff;
import com.okex.open.api.bean.other.SpotOrderBookItem;
import com.okex.open.api.enums.CharsetEnum;
import com.okex.open.api.utils.DateUtils;
import lombok.Data;
import net.sf.json.JSONObject;
import okhttp3.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class wssClientExample {

    protected WebSocket webSocket = null;
    protected Boolean loginStatus = false;
    protected Boolean connectStatus = false;
    protected Boolean reConnectStatus = false;
    protected Set<List<Map>> allScriptions = Collections.synchronizedSet(new HashSet<>());    // for reconnect
    protected String sign;
    protected final HashFunction crc32 = Hashing.crc32();
    protected final ObjectReader objectReader = new ObjectMapper().readerFor(OrderBookData.class);

    protected String apiKey;
    protected String secret;
    protected String passphrase;

    private static final String SERVICE_URL = "wss://ws.okx.com:8443/ws/v5/public";
    private Map<String, Optional<SpotOrderBook>> bookMap = new HashMap<>();

    public wssClientExample() {
    }

    public void initConnect() {

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(SERVICE_URL)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            ScheduledExecutorService service;

            @Override
            public void onOpen(final WebSocket webSocket, final Response response) {
                //连接成功后，设置定时器，每隔25s，自动向服务器发送心跳，保持与服务器连接
                connectStatus = true;
                reConnectStatus = false;
                System.out.println(Instant.now().toString() + " Connected to the server success!");
                Runnable runnable = new Runnable() {
                    public void run() {
                        // task to run goes here
                        sendMessage("ping");
                    }
                };
                service = Executors.newSingleThreadScheduledExecutor();
                // 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间
                service.scheduleAtFixedRate(runnable, 25, 25, TimeUnit.SECONDS);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                System.out.println("Connection is about to disconnect！");
                webSocket.close(1000, "Long time no message was sent or received！");
                webSocket = null;
                if (!reConnectStatus) {
                    reConnect();
                }
            }

            @Override
            public void onClosed(final WebSocket webSocket, final int code, final String reason) {
                System.out.println("Connection dropped！");
                if (!reConnectStatus) {
                    reConnect();
                }
            }

            @Override
            public void onFailure(final WebSocket webSocket, final Throwable t, final Response response) {
                System.out.println("Connection failed,Please reconnect!");
                if (Objects.nonNull(service)) {
                    service.shutdown();
                }
                if (!reConnectStatus) {
                    reConnect();
                }
            }

            @Override
            public void onMessage(final WebSocket webSocket, final String byteString) {
                System.out.println("onMsg="+byteString);
                try {
                    JSONObject rst = JSONObject.fromObject(byteString);
                    final boolean hasEvent = rst.has("event");
                    if (hasEvent) {
                        final String eventName = rst.getString("event");
                        if (eventName.equals("login") && rst.getString("code").equals("0")) {
                            loginStatus = true;
                        }
                        System.out.println(DateFormatUtils.format(new Date(), DateUtils.TIME_STYLE_S4) + " Receive: " + byteString);
                    } else if (byteString.contains("\"channel\":\"bbo-tbt\",")) { // 10ms 1st level
                        JSONObject arg = JSONObject.fromObject(rst.get("arg"));
                        final String instId = arg.getString("instId");
                        Object dataObj = rst.get("data");
                        if (dataObj != null) {
                            net.sf.json.JSONArray dataArr = net.sf.json.JSONArray.fromObject(dataObj);
                            JSONObject data0 = JSONObject.fromObject(dataArr.get(0));
                            net.sf.json.JSONArray asksArr = net.sf.json.JSONArray.fromObject(data0.get("asks"));
                            net.sf.json.JSONArray bidsArr = net.sf.json.JSONArray.fromObject(data0.get("bids"));
                            final long timenow = System.currentTimeMillis();
                            final long exchtime = data0.getLong("ts");
                            final double ask = Double.parseDouble(net.sf.json.JSONArray.fromObject(asksArr.get(0)).get(0).toString());
                            final double bid = Double.parseDouble(net.sf.json.JSONArray.fromObject(bidsArr.get(0)).get(0).toString());
                            final double asz = Double.parseDouble(net.sf.json.JSONArray.fromObject(asksArr.get(0)).get(1).toString());
                            final double bsz = Double.parseDouble(net.sf.json.JSONArray.fromObject(bidsArr.get(0)).get(1).toString());

                            System.out.println(new StringBuilder()
                                    .append("okxSpotOB,").append(instId)
                                    .append(",").append(bsz)
                                    .append(",").append(bid)
                                    .append(",").append(ask)
                                    .append(",").append(asz)
                                    .append(",").append(timenow)
                                    .append(",").append(exchtime).toString());
                        }
                    } else if (byteString.contains("\"channel\":\"books50-l2-tbt\",")
                            || byteString.contains("\"channel\":\"books-l2-tbt\",")
                            || byteString.contains("\"channel\":\"books\",")) {
                        // we subscribe to ONLY one of these
                        // these are all incremental. first msg is a snapshot, then other msgs build on top

                        JSONObject arg = JSONObject.fromObject(rst.get("arg"));
                        String instrumentId = arg.get("instId").toString();

                        final String action = rst.getString("action");
                        net.sf.json.JSONArray dataArr = net.sf.json.JSONArray.fromObject(rst.get("data"));
                        JSONObject data = JSONObject.fromObject(dataArr.get(0));
                        String dataStr = data.toString();
                        final long timenow = System.currentTimeMillis();
                        if (action.equals("snapshot")) {
                            Optional<SpotOrderBook> newBook = parse(dataStr);
                            bookMap.put(instrumentId, newBook);
                        } else if (action.equals("update")) {//construct
                            Optional<SpotOrderBook> oldBook = bookMap.get(instrumentId);
                            Optional<SpotOrderBook> bookIncre = parse(dataStr);
                            System.out.println(oldBook.get().getSeqId());
                            System.out.println(bookIncre.get().getPrevSeqId());

                            // increment data pre seq id == old book seqid is enough
                            // seq > prevseq normal / seq = prevseq no new msg / seq < prevseq just reset seq
                            boolean prevSeq = oldBook.get().getSeqId().equals(bookIncre.get().getPrevSeqId());

                            SpotOrderBookDiff bookdiff = oldBook.get().diff(bookIncre.get());
                            System.out.println("name:" + instrumentId + ",merge done! checknum=" + bookdiff.getChecksum() + "newbook=" + bookdiff);
                            String str = getStr(bookdiff.getAsks(), bookdiff.getBids());
                            System.out.println("name:" + instrumentId + ",checksum check str=" + str);

                            int checksum = checksum(bookdiff.getAsks(), bookdiff.getBids());
                            System.out.println("name:" + instrumentId + ",checksum=" + checksum);
                            boolean flag = checksum == bookdiff.getChecksum() ? true : false;

                            if (flag && prevSeq) {
                                System.out.println("name:" + instrumentId + ",checksum res=" + flag);
                                oldBook.get().update(bookdiff.getAsks(), bookdiff.getBids());
                                oldBook.get().setPrevSeqId(bookIncre.get().getPrevSeqId());
                                oldBook.get().setSeqId(bookIncre.get().getSeqId());
//                                final Optional<SpotOrderBook> newBook = Optional.of(oldBook.get().merge(bookIncre.get().getAsks(), bookIncre.get().getBids()));
//                                newBook.get().setPrevSeqId(bookIncre.get().getPrevSeqId());
                                bookMap.put(instrumentId, oldBook);
                            } else {
                                System.out.println("name:" + instrumentId + ",checksum res=" + flag + ",need resub");
                                String channel = rst.get("table").toString();
                                String unSubStr = "{\"op\": \"unsubscribe\", \"args\":[\"" + channel + ":" + instrumentId + "\"]}";
                                System.out.println(DateFormatUtils.format(new Date(), DateUtils.TIME_STYLE_S4) + " Send: " + unSubStr);
                                webSocket.send(unSubStr);
                                String subStr = "{\"op\": \"subscribe\", \"args\":[\"" + channel + ":" + instrumentId + "\"]}";
                                System.out.println(DateFormatUtils.format(new Date(), DateUtils.TIME_STYLE_S4) + " Send: " + subStr);
                                webSocket.send(subStr);
                                System.out.println("name:" + instrumentId + ",resubscribing");
                            }
                        }
                    } else if (byteString.contains("\"channel\":\"trades\",")) { // real-time mkt trades
                        JSONObject arg = JSONObject.fromObject(rst.get("arg"));
                        final String instId = arg.getString("instId");
                        final Object dataObj = rst.get("data");
                        final long timenow = System.currentTimeMillis();
                        net.sf.json.JSONArray dataArr = net.sf.json.JSONArray.fromObject(dataObj);
                        dataArr.forEach(childData -> {
                            final JSONObject childDataObj = JSONObject.fromObject(childData);
                            final long eventtime = childDataObj.getLong("ts");
                            final double price = childDataObj.getDouble("px");
                            final double qty = childDataObj.getDouble("sz");
                        });
                    } else {
                        System.out.println(DateFormatUtils.format(new Date(), DateUtils.TIME_STYLE_S4) + " Receive: " + byteString);
                    }
                }catch(Exception e){
                    System.out.println("onMsg exception="+e);
                    System.out.println("onMsg="+byteString);
                }
            }
        });
    }

    protected void reConnect() {
        reConnectStatus = true;
        connectStatus = false;
        initConnect();
        if(loginStatus) login();
        if (CollectionUtils.isNotEmpty(allScriptions)) {
            for(final List<Map> subscription: allScriptions) {
                subscribe(subscription);
            }
        }
    }

    //获得sign
    protected final String sha256_HMAC(String message, String secret) {
        String hash = "";
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(CharsetEnum.UTF_8.charset()), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] bytes = sha256_HMAC.doFinal(message.getBytes(CharsetEnum.UTF_8.charset()));
            hash = Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            System.out.println("Error HmacSHA256 ===========" + e.getMessage());
        }
        return hash;
    }

    protected final String listToJson(List<Map> list) {
        JSONArray jsonArray = new JSONArray();
        for (Map map : list) {
            //jsonArray.add(JSONObject.fromObject(map));    // this uses net.sf.json.JSONObject;
            jsonArray.add(com.alibaba.fastjson.JSONObject.toJSON(map));
        }
        return jsonArray.toJSONString();
    }

    //登录
    public final void login(String apiKey, String secretkey, String passphrase) {
        this.apiKey = apiKey;
        this.secret = secretkey;
        this.passphrase = passphrase;
        login();
    }

    private final void login(){
        //String timestamp = (Double.parseDouble(DateUtils.getEpochTime()) + 28800) + "";
        String timestamp = Integer.toString((int)(System.currentTimeMillis()/1000));
        String message = timestamp + "GET" + "/users/self/verify";
        sign = sha256_HMAC(message, secret);
        String str = "{\"op\":\"login\",\"args\":[{\"apiKey\":\"" + apiKey + "\",\"passphrase\":\"" + passphrase + "\",\"timestamp\":\"" + timestamp + "\",\"sign\":\"" + sign + "\"}]}";
        sendMessage(str);
    }

    //订阅，参数为频道组成的集合
    public final void subscribe(List<Map> list) {
        allScriptions.add(list);
        String s = listToJson(list);
        String str = "{\"op\": \"subscribe\", \"args\":" + s + "}";
        if (null != webSocket)
            sendMessage(str);
    }

    //取消订阅，参数为频道组成的集合
    public final void unsubscribe(List<Map> list) {
        allScriptions.remove(list);
        String s = listToJson(list);
        String str = "{\"op\": \"unsubscribe\", \"args\":" + s + "}";
        if (null != webSocket)
            sendMessage(str);
    }

    protected final void sendMessage(String str) {
        if (null != webSocket) {
            try {
                Thread.sleep(1300);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //System.out.println(DateFormatUtils.format(new Date(), DateUtils.TIME_STYLE_S4)+" Send a message to the server:" + str);
            webSocket.send(str);
        } else {
            System.out.println("Please establish the connection before you operate it！");
        }
    }

    //断开连接
    public final void closeConnection() {
        if (null != webSocket) {
            webSocket.close(1000, "User actively closes the connection");
        } else {
            System.out.println("Please establish the connection before you operate it！");
        }
    }

    public final boolean getIsLogin() {
        return loginStatus;
    }

    public final boolean getConnectStatus() {
        return connectStatus;
    }

    public final <T extends OrderBookItem> int checksum(List<T> asks, List<T> bids) {
        System.out.println("深度");
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 25; i++) {
            if (i < bids.size()) {
                s.append(bids.get(i).getPrice().toString());
                s.append(":");
                s.append(bids.get(i).getSize());
                s.append(":");
            }
            if (i < asks.size()) {
                s.append(asks.get(i).getPrice().toString());
                s.append(":");
                s.append(asks.get(i).getSize());
                s.append(":");
            }
        }
        final String str;
        if (s.length() > 0) {
            str = s.substring(0, s.length() - 1);
        } else {
            str = "";
        }

        return crc32.hashString(str, StandardCharsets.UTF_8).asInt();
    }

    protected final <T extends OrderBookItem> String getStr(List<T> asks, List<T> bids) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 25; i++) {
            if (i < bids.size()) {
                s.append(bids.get(i).getPrice().toString());
                s.append(":");
                s.append(bids.get(i).getSize());
                s.append(":");
            }
            if (i < asks.size()) {
                s.append(asks.get(i).getPrice().toString());
                s.append(":");
                s.append(asks.get(i).getSize());
                s.append(":");
            }
        }
        final String str;
        if (s.length() > 0) {
            str = s.substring(0, s.length() - 1);
        } else {
            str = "";
        }
        return str;
    }

    public final Optional<SpotOrderBook> parse(String json) {

        try {
            OrderBookData data = objectReader.readValue(json);
            List<SpotOrderBookItem> asks =
                    data.getAsks().stream().map(x -> new SpotOrderBookItem(new String(x.get(0)), x.get(1), x.get(2), x.get(3)))
                            .collect(Collectors.toList());

            List<SpotOrderBookItem> bids =
                    data.getBids().stream().map(x -> new SpotOrderBookItem(new String(x.get(0)), x.get(1), x.get(2), x.get(3)))
                            .collect(Collectors.toList());

            return Optional.of(new SpotOrderBook(asks, bids, data.getTs(),data.getChecksum(), data.getSeqId(), data.getPrevSeqId()));
        } catch (Exception e) {
            System.out.println(e.toString());
            return Optional.empty();
        }
    }

    @Data
    public static final class OrderBookData {
        private List<List<String>> asks;
        private List<List<String>> bids;
        private String ts;
        private int checksum;

        private Long seqId;

        private Long prevSeqId;

    }
}
