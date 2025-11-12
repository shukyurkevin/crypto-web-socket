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
        exchangeWsClients.add(new OkxWsClient(List.of("BTC-USDT","ETH-USDT"),priceCache));
        exchangeWsClients.add(new KrakenWsClient(List.of("BTC/USDT","ETH/USDT"),priceCache));
        exchangeWsClients.add(new CoinbaseWsClient(List.of("BTC-USDT","ETH-USDT"),priceCache));
        exchangeWsClients.add(new WhiteBitWsClient(List.of("BTC_USDT","ETH_USDT"),priceCache));
        exchangeWsClients.add(new ByBitWsClient(List.of("tickers.BTCUSDT","tickers.ETHUSDT"),priceCache));


        boolean running = false;
        while (true) {
            System.out.println("\n1 = start , 2 = stop , 0 = exit");
            int action = scanner.nextInt();

            switch (action) {

                case 1 -> {
                    if (!running) {
                        exchangeWsClients.forEach(BaseOkHttpWsClient::start);
                        running = true;
                    }

                    while (running) {
                        Thread.sleep(4000);

                        System.out.println("-----GigaTest-----");
                        priceCache.getPriceMap().forEach((k, v) ->
                                System.out.println(k + " : " + v.price() +
                                        "    data: " + Instant.ofEpochMilli(v.timestamp()))
                        );

                        System.out.println("\n2 = stop , 0 = exit");
                        int subAction = scanner.nextInt();

                        if (subAction == 2) {
                            exchangeWsClients.forEach(BaseOkHttpWsClient::close);
                            running = false;
                        } else if (subAction == 0) {
                            exchangeWsClients.forEach(BaseOkHttpWsClient::close);
                            System.exit(0);
                        }
                    }
                }

                case 2 -> {
                    if (running) {
                        exchangeWsClients.forEach(BaseOkHttpWsClient::close);
                        running = false;
                        System.out.println("Stopped.");
                    } else {
                        System.out.println("Already stopped.");
                    }
                }

                case 0 -> {
                    exchangeWsClients.forEach(BaseOkHttpWsClient::close);
                    System.exit(0);
                }

                default -> System.out.println("Unknown cmd");
            }
        }

    }

}