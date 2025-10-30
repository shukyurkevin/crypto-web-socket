package org.kevin.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@Getter
public class SubscriptionRequest {
    private final long id;
    private final String symbol;
    private final long tickInterval;

@JsonCreator
public SubscriptionRequest(JsonNode node) {
    this.id = node.get("id").asLong();
    this.symbol = node.get("symbol").asText();
    this.tickInterval = node.get("tick-interval").asLong();
}
}
