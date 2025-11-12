package org.kevin;

import org.glassfish.tyrus.server.Server;
import org.kevin.models.PriceCache;
import org.kevin.ws.clients.*;
import org.kevin.ws.clients.endpoints.ExchangeEndPoint;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Starter {
    public static void main(String[] args) throws Exception {
        Server server = new Server(
                "localhost",
                8082,
                "/ws",
                null,
                ExchangeEndPoint.class
        );
        Scanner scanner = new Scanner(System.in);

        server.start();


        PriceCache priceCache = PriceCache.INSTANCE;
        List<BaseOkHttpWsClient> exchangeWsClients = new ArrayList<>();
        exchangeWsClients.add(new OkxWsClient(List.of("BTC-USDT", "ETH-USDT"), priceCache));
        exchangeWsClients.add(new KrakenWsClient(List.of("BTC/USDT", "ETH/USDT"), priceCache));
        exchangeWsClients.add(new CoinbaseWsClient(List.of("BTC-USDT", "ETH-USDT"), priceCache));
        exchangeWsClients.add(new WhiteBitWsClient(List.of("BTC_USDT", "ETH_USDT"), priceCache));
        exchangeWsClients.add(new ByBitWsClient(List.of("tickers.BTCUSDT", "tickers.ETHUSDT"), priceCache));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down gracefully...");
            exchangeWsClients.forEach(BaseOkHttpWsClient::close);
            System.out.println("All WebSocket clients closed.");
        }));

        exchangeWsClients.forEach(BaseOkHttpWsClient::start);

        while (true) {
            Thread.sleep(4000);

            System.out.println("-----GigaTest-----");
            priceCache.getPriceMap().forEach((k, v) ->
                    System.out.println(k + " : " + v.price() +
                            "    data: " + Instant.ofEpochMilli(v.timestamp()))
            );

        }
    }
}