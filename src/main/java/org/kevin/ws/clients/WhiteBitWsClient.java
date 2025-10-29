package org.kevin.ws.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kevin.models.PriceCache;

import java.util.List;

public class WhiteBitWsClient extends BaseOkHttpWsClient{
    ObjectMapper mapper = new ObjectMapper();
    public WhiteBitWsClient(List<String> symbol, PriceCache cache) {
        super("WhiteBit", cache, symbol);
    }

    @Override
    protected String url() {
        return "wss://api.whitebit.com/ws";
    }

    @Override
    protected String buildSubscriptionRequest() {
        StringBuilder sb = new StringBuilder("{\"id\": 1,\"method\":\"market_subscribe\",\"params\":[");
        for (int i=0; i<symbols.size(); i++) {
            if (i>0) sb.append(",");
            sb.append("\"").append(symbols.get(i)).append("\"");
        }
        return sb.append("]}").toString();
    }

    @Override
    protected void onTextMessage(String msg) {
        try {
            JsonNode root = mapper.readTree(msg);
            if (!root.has("params"))return;

            JsonNode params = root.path("params");
            if(!params.isArray())return;

            String symbol = params.get(0).asText();
            JsonNode data = params.get(1);
            String price = data.get("last").asText();
            update(symbol, price, System.currentTimeMillis());
        }catch (Exception ignored){}
    }
}
