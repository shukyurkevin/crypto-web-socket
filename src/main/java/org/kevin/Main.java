package org.kevin;

import org.kevin.models.PriceCache;
import org.kevin.interfaces.ExchangeWsClient;
import org.kevin.ws.KrakenWsClient;
import org.kevin.ws.OkxWsClient;

import javax.xml.crypto.Data;
import java.sql.Time;
import java.time.Instant;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws InterruptedException {
        PriceCache priceCache = new PriceCache();
//        ExchangeWsClient okx = new OkxWsClient(List.of("BTC-USDT","ETH-USDT"),priceCache);
//        okx.start();
//        while (true){
//            Thread.sleep(2000);
//            System.out.println("------OKX-TEST-----");
//            priceCache.getMap().forEach((k,v)->{
//                System.out.println(k+" : "+v.price());
//            });
//        }
        ExchangeWsClient kraken = new KrakenWsClient(List.of("BTC/USDT","ETH/USDT"),priceCache);
        kraken.start();

        while (true){
            Thread.sleep(4000);
            System.out.println("-----KRAKEN-TEST-----");
            priceCache.getMap().forEach((k,v)->{
                System.out.println(k+" : "+v.price()+" time: " + Instant.ofEpochMilli(v.timestamp()));
            });
        }
    }
}