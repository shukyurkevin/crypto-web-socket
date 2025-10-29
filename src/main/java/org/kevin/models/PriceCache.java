package org.kevin.models;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class PriceCache {
    public static final PriceCache INSTANCE = new PriceCache();
    private final ConcurrentMap<String, PriceUpdate> priceMap =  new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CopyOnWriteArraySet<PriceUpdate>> mapBySymbol = new ConcurrentHashMap<>();

    public void put(PriceUpdate u) {
        String baseKey = key(u.exchange(), u.symbol());
        priceMap.put(baseKey, u);
        String normKey = normalize(u.symbol());
        mapBySymbol.computeIfAbsent(normKey, __ -> new CopyOnWriteArraySet<>()).add(u);

    }
    public PriceUpdate get(String exchange, String symbol) {
        return priceMap.get(key(exchange, symbol));
    }
    public List<PriceUpdate> findBySymbol(String symbol) {
        String normKey = normalize(symbol);
        return new ArrayList<>(mapBySymbol.get(normKey));
    }

    private static String key(String exchange, String symbol) {
        return exchange +"|"+ symbol;
    }
    private static String normalize(String s){
        return s.replace("_","")
                .replace("-","")
                .replace("/","")
                .toUpperCase();
    }
    public ConcurrentMap<String, PriceUpdate> getPriceMap() {
        return priceMap;
    }

}
