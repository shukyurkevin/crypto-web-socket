package org.kevin.models;

public record PriceUpdate(String exchange, String symbol, String price, long timestamp) {

}
