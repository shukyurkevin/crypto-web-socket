package org.kevin.ws.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kevin.models.PriceCache;

import java.util.List;

public class ByBitWsClient extends BaseOkHttpWsClient{
    ObjectMapper mapper = new ObjectMapper();
    public ByBitWsClient(List<String> symbol, PriceCache cache) {
        super("ByBit", cache, symbol);
    }
    @Override
    protected String url() {
        return "wss://stream.bybit.com/v5/public/spot";
    }

    @Override
    protected String buildSubscriptionRequest() {
        StringBuilder sb = new StringBuilder("{\"op\":\"subscribe\",\"args\":[");
        for (int i=0; i < symbols.size(); i++){
            if(i>0)sb.append(",");
            sb.append("\"").append(symbols.get(i)).append("\"");
        }
        return sb.append("]}").toString();
    }

    @Override
    protected void onTextMessage(String msg) {
        //System.out.println(msg);
        try {
            JsonNode root = mapper.readTree(msg);
            if (!root.has("data")) return;
            long ts = root.get("ts").asLong(System.currentTimeMillis());
            JsonNode data = root.get("data");
            String symbol = data.get("symbol").asText();
            String price = data.get("lastPrice").asText();
            update(symbol,price,ts);
        }catch (Exception ignored){}
    }
}
