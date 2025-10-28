package org.kevin;

import org.kevin.models.PriceCache;
import org.kevin.ws.ExchangeWsClient;
import org.kevin.ws.OkxWsClient;

import java.util.ArrayList;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws InterruptedException {
        PriceCache priceCache = new PriceCache();
        ExchangeWsClient okx = new OkxWsClient(List.of("BTC-USDT","ETH-USDT"),priceCache);
        okx.start();
        while (true){
            Thread.sleep(2000);
            System.out.println("------OKX-TEST-----");
            priceCache.getMap().forEach((k,v)->{
                System.out.println(k+" : "+v.price());
            });
        }
    }
}