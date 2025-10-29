package org.kevin.models;

import jakarta.websocket.Session;

import java.util.concurrent.ScheduledFuture;
public record Subscription(
        long id,
        String symbol,
        Session session,
        long tickInterval,
        ScheduledFuture<?> future
) {}