package org.wisdom.wamp.services;

/**
 * Interface used to receive events from WAMP.
 */
public interface WampEventListener<T> {

    public void receive(String topic, T message);

}
