package org.kevin.ws.clients;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kevin.models.PriceCache;

import java.util.List;

public class OkxWsClient extends BaseOkHttpWsClient {
    private static final ObjectMapper mapper = new ObjectMapper();

    public OkxWsClient(List<String> instId, PriceCache cache) {
        super("OKX", cache, instId);
    }

    @Override
    protected String url() {
        return "wss://ws.okx.com:8443/ws/v5/public";
    }

    @Override
    protected String buildSubscriptionRequest() {
        StringBuilder sb = new StringBuilder("{\"op\":\"subscribe\",\"args\":[");
        for (int i = 0; i< symbols.size(); i++){
            if(i>0){sb.append(",");}
            sb.append("{\"channel\":\"tickers\",\"instId\":\"").append(symbols.get(i)).append("\"}");
        }
        return sb.append("]}").toString();
    }

    @Override
    protected void onTextMessage(String msg) {
 //       System.out.println(msg);
        try {
            JsonNode root = mapper.readTree(msg);
            if (!root.has("data"))return;

            for (JsonNode data : root.get("data")){
                String instId = data.get("instId").asText();
                String newestPrice = data.get("last").asText();
                long timestamp = data.get("ts").asLong(System.currentTimeMillis());
                update(instId, newestPrice, timestamp);
            }
        }catch (Exception ignored){
        }
    }
}
