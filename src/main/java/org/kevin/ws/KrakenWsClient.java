package org.kevin.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kevin.models.PriceCache;

import java.util.List;

public class KrakenWsClient extends BaseOkHttpWsClient {
    ObjectMapper mapper = new ObjectMapper();
    public KrakenWsClient(List<String> symbol, PriceCache cache) {
        super("KRAKEN", cache, symbol);
    }

    @Override
    protected String url() {
        return "wss://ws.kraken.com/v2";
    }

    @Override
    protected String buildSubscriptionRequest() {
        StringBuilder sb = new StringBuilder("{\"method\":\"subscribe\",\"params\":{\"channel\":\"ticker\",\"symbol\":[");
        for (int i = 0; i < symbols.size(); i++) {
            if (i > 0){sb.append(",");}
            sb.append("\"").append(symbols.get(i)).append("\"");
        }
        return sb.append("]}}").toString();
    }

    @Override
    protected void onTextMessage(String msg) {
        try {
            JsonNode root = mapper.readTree(msg);
            if (!root.has("data"))return;
            JsonNode dataArray = root.get("data");
            if (dataArray.isArray()) {
            for (JsonNode data : dataArray) {
                String instId = data.get("symbol").asText();
                String newestPrice = data.get("last").asText();
                update(instId, newestPrice, System.currentTimeMillis());
            }
            }
        }catch (Exception ignored){
        }

    }
}
