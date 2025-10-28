package org.kevin.ws.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kevin.models.PriceCache;

import java.time.Instant;
import java.util.List;


public class CoinbaseWsClient extends BaseOkHttpWsClient{
    ObjectMapper mapper = new ObjectMapper();
    public CoinbaseWsClient(List<String> productIds, PriceCache cache) {
        super("COINBASE", cache, productIds);
    }

    @Override
    protected String url() {
        return "wss://advanced-trade-ws.coinbase.com";
    }

    @Override
    protected String buildSubscriptionRequest() {
        StringBuilder sb = new StringBuilder("{\"type\":\"subscribe\",\"product_ids\":[");
        for (int i=0; i< symbols.size(); i++){
            if(i>0)sb.append(",");
            sb.append("\"").append(symbols.get(i)).append("\"");
        }
        return sb.append("],\"channel\":\"ticker\"}").toString();
    }

    @Override
    protected void onTextMessage(String msg) {
      // System.out.println(msg);
        try {
            JsonNode root = mapper.readTree(msg);
            JsonNode eventArray = root.get("events");
            for (JsonNode event : eventArray) {
                JsonNode tickers = event.get("tickers");

            if(!tickers.isArray()) continue; {
                for (JsonNode data : tickers) {
                    String productIds = data.get("product_id").asText();
                    String newestPrice = data.get("price").asText();
                    String ts =  root.get("timestamp").asText();
                    long timestamp = Instant.parse(ts).toEpochMilli();
                    update(productIds, newestPrice, timestamp);
                }
            }}
        }catch (Exception ignored){
        }

    }
}
