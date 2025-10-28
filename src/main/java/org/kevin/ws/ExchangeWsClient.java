package org.kevin.ws;

public interface ExchangeWsClient extends AutoCloseable{
    void start();

    @Override
    void close() throws Exception;
}
