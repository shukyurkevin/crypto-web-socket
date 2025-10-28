package org.kevin;

import org.kevin.models.PriceCache;
import org.kevin.ws.clients.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Starter {
    public static void main(String[] args) throws Exception {
        PriceCache priceCache = new PriceCache();
        List<BaseOkHttpWsClient> exchangeWsClients = new ArrayList<>();
        exchangeWsClients.add(new OkxWsClient(List.of("BTC-USDT","ETH-USDT"),priceCache));
        exchangeWsClients.add(new KrakenWsClient(List.of("BTC/USDT","ETH/USDT"),priceCache));
        exchangeWsClients.add(new CoinbaseWsClient(List.of("BTC-USDT","ETH-USDT"),priceCache));
        exchangeWsClients.add(new WhiteBitWsClient(List.of("BTC_USDT","ETH_USDT"),priceCache));
        exchangeWsClients.add(new ByBitWsClient(List.of("tickers.BTCUSDT","tickers.ETHUSDT"),priceCache));
        for (BaseOkHttpWsClient exchangeWsClient : exchangeWsClients) {
            exchangeWsClient.start();
        }

        while (true){
            Thread.sleep(4000);
            System.out.println("-----GigaTest-----");
            priceCache.getMap().forEach((k,v)->{
                System.out.println(k+" : "+v.price() +"    data: "+ Instant.ofEpochMilli(v.timestamp()));
            });
        }

    }

}