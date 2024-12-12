package com.okex.open.api.websocket;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.okex.open.api.bean.SubscribeReq;
import com.okex.open.api.bean.WsBaseReq;
import com.okex.open.api.utils.DateUtils;
import com.okex.open.api.utils.SignTypeEnum;
import net.openhft.affinity.AffinityLock;
import net.openhft.affinity.AffinityThreadFactory;
import okhttp3.*;
import okhttp3.WebSocketListener;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;
import java.util.concurrent.*;

import static net.openhft.affinity.AffinityStrategies.SAME_CORE;

public class OkxWssAffHandler implements OkxWsClient {
    public static final String WS_OP_LOGIN = "login";
    public static final String WS_OP_SUBSCRIBE = "subscribe";
    public static final String WS_OP_UNSUBSCRIBE = "unsubscribe";
    private WebSocket webSocket;

    private volatile boolean loginStatus = false;
    private volatile boolean connectStatus = false;
    private volatile boolean reconnectStatus = false;

    private OkxWssAffHandler.OkxClientBuilder builder;

    private final Map<SubscribeReq, SubscriptionListener> scribeMap = new ConcurrentHashMap<>();

    private AffinityThreadFactory factory;
    private ExecutorService executorService;

    private Set<SubscribeReq> allSuribe = Collections.synchronizedSet(new HashSet<>());

    private OkxWssAffHandler(OkxWssAffHandler.OkxClientBuilder builder) {
        this.builder = builder;
        factory = new AffinityThreadFactory(builder.affThreadName, SAME_CORE);
        executorService = Executors.newFixedThreadPool(builder.threadNum, factory);

        // dont need to receive the websocket from initClient,
        // in initClient, webSocket have been update at line67
        initClient();
    }

    private static void printLog(String msg, String type) {
        System.out.println("[" + DateUtils.getUnixTime() + "] [" + type.toUpperCase() + "] " + msg);
    }

    private WebSocket initClient() {
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(builder.threadNum, factory);
        }
        Dispatcher dispatcher = new Dispatcher(executorService);

        OkHttpClient client = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(builder.pushUrl)
                .build();

        this.webSocket = client.newWebSocket(request, new OkxWssAffHandler.OkxWsListener(this));

        if (builder.isLogin) {
            login();
        }
        printLog(" start connect ....", "info");
        while (!connectStatus) {
        }

        return webSocket;
    }

    public static OkxWssAffHandler.OkxClientBuilder builder() {
        return new OkxWssAffHandler.OkxClientBuilder();
    }

    public void sendMessage(WsBaseReq req) {
        sendMessage(JSONObject.toJSONString(req));
    }

    @Override
    public void sendMessage(String message) {
        printLog(" start send message:" + message, "INFO");
        webSocket.send(message);
    }

    @Override
    public void unsubscribe(List<SubscribeReq> channels) {
        allSuribe.removeAll(channels);
        channels.forEach(channel -> {
            scribeMap.remove(channel);
//            sendMessage("{\"op\": \"unsubscribe\", \"args\":" + channels + "}");
        });
        sendMessage(new WsBaseReq(WS_OP_UNSUBSCRIBE, channels));

    }

    @Override
    public void subscribe(List<SubscribeReq> channels) {
        allSuribe.addAll(channels);
        sendMessage(new WsBaseReq(WS_OP_SUBSCRIBE, channels));
    }

    @Override
    public void subscribe(List<SubscribeReq> channels, SubscriptionListener listener) {
        channels.forEach(channel -> {
            scribeMap.put(channel, listener);
        });
        subscribe(channels);
    }

    @Override
    public void login() {
        Validate.notNull(builder.apiKey, "apiKey is null");
        Validate.notNull(builder.secretKey, "secretKey is null");
        Validate.notNull(builder.passPhrase, "passphrase is null");

        // timestamp
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String str = timeStamp + "GET/users/self/verify";
        String hash = "";
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");

            SecretKeySpec secret_key = new SecretKeySpec(builder.secretKey.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            hash = Base64.encodeBase64String(sha256_HMAC.doFinal(str.getBytes()));
        }catch (Throwable t){
            t.printStackTrace();
        }
        ArrayList<Map> argsList= new ArrayList<>();
        Map argsMap =new HashMap();

        argsMap.put("apiKey", builder.apiKey);
        argsMap.put("passphrase", builder.passPhrase);
        argsMap.put("timestamp", timeStamp);
        argsMap.put("sign", hash);
        argsList.add(argsMap);
        String args = formatArgs(argsList);
        this.sendMessage("{\"op\": \"login\", \"args\":" + args + "}");
    }

    private String formatArgs(List<Map> list) {
        JSONArray jsonArray = new JSONArray();
        for (Map map : list) {
            jsonArray.add(net.sf.json.JSONObject.fromObject(map));
        }
        return jsonArray.toJSONString();
    }

    private final class OkxWsListener extends WebSocketListener {

        ScheduledExecutorService service;
        private OkxWsClient okxWsClient;

        public OkxWsListener(OkxWsClient okxWsClient) {
            this.okxWsClient = okxWsClient;
        }

        @Override
        public void onOpen(final WebSocket webSocket, final Response response) {
            connectStatus = true;
            reconnectStatus = false;
            //连接成功后，设置定时器，每隔25s，自动向服务器发送心跳，保持与服务器连接
            Runnable runnable = () -> {
                // task to run goes here
                okxWsClient.sendMessage("ping");
            };

            service = Executors.newSingleThreadScheduledExecutor();
            // 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间
            service.scheduleAtFixedRate(runnable, 25, 25, TimeUnit.SECONDS);
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            String message = "{\"msg\":\"onClosing\"}";
            if (Objects.nonNull(builder.errorListener)) {
                builder.errorListener.onReceive(message);
            }
            if(reason!=null){
                System.out.println("Connection is about to disconnect！"+reason);
            }else{
                System.out.println("Connection is about to disconnect！reason=null");
            }
            close();
            if (!reconnectStatus) {
                reConnect();
            }

        }

        @Override
        public void onClosed(final WebSocket webSocket, final int code, final String reason) {
            String message = "{\"msg\":\"onClosed\"}";
            if (Objects.nonNull(builder.errorListener)) {
                builder.errorListener.onReceive(message);
            }
            if(reason!=null) {
                System.out.println("Connection dropped！" + code+","+reason);
            }else{
                System.out.println("Connection dropped！" + code+",reason=null");
            }
            close();
            if (!reconnectStatus) {
                reConnect();
            }
        }

        @Override
        public void onFailure(final WebSocket webSocket, final Throwable t, final Response response) {
            String message = "{\"msg\":\"onFailure\", \"apikey5\":\"" + builder.apiKey5 + "\"}";
            if (Objects.nonNull(builder.errorListener)) {
                builder.errorListener.onReceive(message);
            }
            if(response!=null){
                System.out.println("onFailure response code is: " + response.code() + "response body" + response.body());
            }else{
                System.out.println("onFailure response null");
            }
            t.printStackTrace();
            close();
            if (!reconnectStatus) {
                reConnect();
            }
        }

//        @Override
//        public void onMessage(final WebSocket webSocket, final ByteString bytes) {
//            final String s = uncompress(bytes.toByteArray());
//            onMessage(webSocket,s);
//        }

        @Override
        public void onMessage(final WebSocket webSocket, final String message) {
            try {
                if (message.equals("pong")) {
                    printLog(" Keep connected:" + message, "info");
                    return;
                }
                JSONObject jsonObject = JSONObject.parseObject(message);


                if (jsonObject.containsKey("event") && jsonObject.get("event").equals("login")) {
                    loginStatus = true;
                    return;
                }
                SubscriptionListener listener = null;
                if (jsonObject.containsKey("data")) {
                    listener = getListener(jsonObject);

                    //check sum
                    boolean checkSumFlag = checkSum(jsonObject);
                    if (!checkSumFlag) {
                        return;
                    }

                    if (Objects.nonNull(listener)) {
                        listener.onReceive(message);
                        return;
                    }
                    if (Objects.nonNull(builder.listener)) {
                        builder.listener.onReceive(message);
                        return;
                    }
                }
            } catch (Exception e) {
                printLog(builder.apiKey5+" receive error msg:" + message, "error");
            }
        }

        private boolean checkSum(JSONObject jsonObject) {
            try {
                if (!jsonObject.containsKey("arg")) {
                    return true;
                }
                String arg = jsonObject.get("arg").toString();
                SubscribeReq subscribeReq = JSONObject.parseObject(arg, SubscribeReq.class);

                if (!StringUtils.equalsIgnoreCase(subscribeReq.getChannel(), "books")) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            return true;
        }

        private SubscriptionListener getListener(JSONObject jsonObject) {
            try {
                if (jsonObject.containsKey("arg")) {
                    SubscribeReq subscribeReq = JSONObject.parseObject(jsonObject.get("arg").toString(), SubscribeReq.class);
                    return scribeMap.get(subscribeReq);
                }
            } catch (Exception e) {

            }
            return null;

        }

        private void close() {
            loginStatus = false;
            connectStatus = false;
            webSocket.close(1000, "Long time no message was sent or received！");
            webSocket = null;
        }

        private void reConnect() {
            reconnectStatus = true;
            // initClient() have a while loop to check the connectStatus
            // when onOpen is called, connectStatus will be set to true and finish initClient
            // when reconnecting, it should set to false and wait until onOpen is called.
            connectStatus = false;
            printLog("Dump affinity locks ", "INFO");
            printLog(AffinityLock.dumpLocks(), "INFO");
            executorService.shutdown();
            printLog(" start reconnection ...", "info");
            // dont need to assign to the websocket two time
            initClient();
            if (CollectionUtils.isNotEmpty(allSuribe)) {
                subscribe(new ArrayList<>(allSuribe));
            }
        }

    }

    public static class OkxClientBuilder {
        private String pushUrl;
        private boolean isLogin;
        private String apiKey;
        private String secretKey;
        private String passPhrase;
        private String apiKey5;

        private Integer threadNum = 1;

        private String affThreadName = "affOkx";

        private SignTypeEnum signType = SignTypeEnum.SHA256;

        private SubscriptionListener listener;
        private SubscriptionListener errorListener;

        public OkxWssAffHandler.OkxClientBuilder listener(SubscriptionListener listener) {
            this.listener = listener;
            return this;
        }

        public OkxWssAffHandler.OkxClientBuilder errorListener(SubscriptionListener errorListener) {
            this.errorListener = errorListener;
            return this;
        }

        public OkxWssAffHandler.OkxClientBuilder pushUrl(String pushUrl) {
            this.pushUrl = pushUrl;
            return this;
        }

        public OkxWssAffHandler.OkxClientBuilder isLogin(boolean isLogin) {
            this.isLogin = isLogin;
            return this;
        }

        public OkxWssAffHandler.OkxClientBuilder apiKey(String apiKey) {
            this.apiKey = apiKey;
            if(apiKey!=null && apiKey.length()>=5) this.apiKey5 = apiKey.substring(0,5);
            return this;
        }

        public OkxWssAffHandler.OkxClientBuilder secretKey(String secretKey) {
            this.secretKey = secretKey;
            return this;
        }

        public OkxWssAffHandler.OkxClientBuilder passPhrase(String passPhrase) {
            this.passPhrase = passPhrase;
            return this;
        }

        public OkxWssAffHandler.OkxClientBuilder signType(SignTypeEnum signType) {
            this.signType = signType;
            return this;
        }

        public OkxWssAffHandler.OkxClientBuilder threadNum(Integer threadNum) {
            this.threadNum = threadNum;
            return this;
        }

        public OkxWssAffHandler.OkxClientBuilder affThreadName(String affThreadName) {
            this.affThreadName = affThreadName;
            return this;
        }

        public OkxWssAffHandler build() {
            return new OkxWssAffHandler(this);
        }

    }
}
