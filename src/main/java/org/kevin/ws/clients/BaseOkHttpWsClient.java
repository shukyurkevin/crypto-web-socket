package org.kevin.ws.clients;


import okhttp3.*;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kevin.interfaces.ExchangeWsClient;
import org.kevin.models.PriceCache;
import org.kevin.models.PriceUpdate;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseOkHttpWsClient implements ExchangeWsClient{
    protected final OkHttpClient httpClient =  new OkHttpClient();
    protected final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    protected volatile WebSocket ws;
    protected volatile boolean closed = false;
    protected final PriceCache priceCache;
    protected final List<String> symbols;
    protected final String name;
    protected final int MAX_RECONNECT_ATTEMPTS = 5;
    protected final long RECONNECT_DELAY_MS = 2000;
    protected final long MAX_RECONNECT_DELAY_MS = 10000;
    protected final AtomicInteger reconnectAttempts = new AtomicInteger(0);

    protected BaseOkHttpWsClient(String name, PriceCache priceCache, List<String> symbols) {
        this.name = name;
        this.priceCache = priceCache;
        this.symbols = symbols;
    }
    protected abstract String url();
    protected abstract String buildSubscriptionRequest();
    protected abstract void onTextMessage(String msg);
    @Override
    public void start() {
        connectInternal();
    }
    public synchronized void connectInternal(){
        if (ws != null){
            ws.close(1000, "reconnecting");
        } ws = null;
        Request request = new Request.Builder().url(url()).build();
        ws = httpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                webSocket.send(buildSubscriptionRequest());
                System.out.println("subscribed :" + name);
                reconnectAttempts.set(0);
                super.onOpen(webSocket, response);
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                onTextMessage(text);
                super.onMessage(webSocket, text);
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                onTextMessage(bytes.utf8());
                super.onMessage(webSocket, bytes);
            }

            @Override
            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                webSocket.close(1000, reason);
                super.onClosing(webSocket, code, reason);
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                System.out.println("Connection failed : trying to reconnect");
                reconnect();
                super.onFailure(webSocket, t, response);
            }
        });

    }
    public void reconnect(){
        if (closed) return;
        int attempts = reconnectAttempts.incrementAndGet();
        if (attempts > MAX_RECONNECT_ATTEMPTS){
            System.out.println("reconnection failed");
            close();
            return;
        }
        if (attempts == 1){
            System.out.println("start reconnecting");
        }
        long delay = Math.min(RECONNECT_DELAY_MS, MAX_RECONNECT_DELAY_MS*(1L << (attempts-1)));
        scheduler.schedule(() ->{
            try {

                System.out.println("reconnecting attempt: " + attempts);
                connectInternal();
            }catch (Exception e){
                System.out.println("reconnecting failed " + e);
                reconnect();
            }
        },delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close(){
        closed = true;
        try {
            if (ws != null) {
                ws.close(1000, "shutting down");
            }
        }catch (Exception ignore){
        }
        finally {
            scheduler.shutdownNow();
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }
    }

    public void update(String symbol, String price, long timestamp) {
        priceCache.put(new PriceUpdate(name,symbol,price,timestamp));
    }
}
