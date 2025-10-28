package org.kevin.interfaces;

public interface ExchangeWsClient extends AutoCloseable{
    void start();

    @Override
    void close() throws Exception;
}
