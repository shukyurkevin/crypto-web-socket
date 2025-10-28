package org.kevin.models;

import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PriceCache {
    @Getter
    private final ConcurrentMap<String, PriceUpdate> map =  new ConcurrentHashMap<>();

    public void put(PriceUpdate u) {
        map.put(key(u.exchange(),  u.symbol()), u);
    }
    public PriceUpdate get(String exchange, String symbol) {
        return map.get(key(exchange, symbol));
    }

    private static String key(String exchange, String symbol) {
        return exchange +"|"+ symbol;
    }
}
