package org.kevin.ws.clients.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.commons.collections4.MapUtils;
import org.kevin.models.PriceCache;
import org.kevin.models.PriceUpdate;
import org.kevin.models.Subscription;
import org.kevin.models.SubscriptionRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@ServerEndpoint("/exchange")
public class ExchangeEndPoint {
    private final Map<Session, Map<Long,Subscription>> clients = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final ObjectMapper mapper = new ObjectMapper();
    private final PriceCache cache = PriceCache.INSTANCE;

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Opened service");
    }
    @OnClose
    public void onClose(Session session) {
        clients.remove(session);
        System.out.println("Client disconnected: " + session.getId());
    }
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Received message: " + message);
        try {
            JsonNode node = mapper.readTree(message);
            String type = node.get("type").asText();
            switch (type) {
                case "subscribe":{
                    SubscriptionRequest request = mapper.convertValue(node.get("params"), SubscriptionRequest.class);
                    subscribe(session, request);
                }
                case "unsubscribe":{
                    unsubscribe(session, node.get("id").asLong());
                }

            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }
    @OnError
    public void onError(Throwable throwable, Session session) {
        System.out.println("Error: " + throwable.getMessage());
    }
    public void subscribe(Session session, SubscriptionRequest request) {
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                ()->{


                    Subscription sub = clients.get(session).get(request.getId());
                    if(sub==null)return;

                    List<PriceUpdate> prices = cache.findBySymbol(sub.symbol());
                    if(prices.isEmpty())return;

                    try {
                        String json = mapper.writeValueAsString(prices);
                        session.getAsyncRemote().sendText(json);

                    }catch (Exception ignored){}
                }
                ,0
                ,request.getTickInterval()
                , TimeUnit.MILLISECONDS
        );
        Subscription sub = new Subscription(
                request.getId(),
                request.getSymbol(),
                session,
                request.getTickInterval(),
                future
        );
        clients.computeIfAbsent(session, s -> new ConcurrentHashMap<>())
                .put(request.getId(), sub);

        System.out.println("Subscribed session: " + session.getId());

    }
    public void unsubscribe(Session session, Long id) {
        Map<Long, Subscription> map = clients.get(session);

        if (MapUtils.isEmpty(map))return;

        Subscription unsub = map.remove(id);
        if (unsub != null){
            unsub.future().cancel(true);
            System.out.println("Unsubscribed session: " + session.getId());
        }
        if (MapUtils.isEmpty(map)){
            clients.remove(session);
        }

    }
    public void unsubscribeAll(){
        if (MapUtils.isEmpty(clients))return;
        clients.values().forEach(s-> s.values().forEach(sub-> sub.future().cancel(true)));
        clients.clear();
    }

}
