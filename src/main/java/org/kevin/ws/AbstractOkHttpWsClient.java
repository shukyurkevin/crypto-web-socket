package org.kevin.ws;


import okhttp3.*;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kevin.models.PriceCache;
import org.kevin.models.PriceUpdate;

import java.util.List;

public abstract class AbstractOkHttpWsClient implements ExchangeWsClient{
    protected final OkHttpClient httpClient =  new OkHttpClient();
    protected volatile WebSocket ws;
    protected final PriceCache priceCache;
    protected final List<String> symbols;
    protected final String name;

    protected AbstractOkHttpWsClient(String name, PriceCache priceCache, List<String> symbols) {
        this.name = name;
        this.priceCache = priceCache;
        this.symbols = symbols;
    }
    protected abstract String url();
    protected abstract String buildSubscriptionRequest();
    protected abstract void onTextMessage(String msg);
    @Override
    public void start() {
        Request request = new Request.Builder().url(url()).build();
       ws = httpClient.newWebSocket(request, new WebSocketListener() {
           @Override
           public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
               webSocket.send(buildSubscriptionRequest());
               System.out.println("subscribed :" + name);
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
               System.out.println("failed :" + name);;
               super.onFailure(webSocket, t, response);
           }
       });

    }

    @Override
    public void close() throws Exception {
        try {
            if (ws != null) {
                ws.close(1000, null);
            }
        }catch (Exception ignored){}
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
    }

    public void update(String symbol, String price, long timestamp) {
        priceCache.put(new PriceUpdate(name,symbol,price,timestamp));
    }
}
