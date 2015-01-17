package org.wisdom.api.concurrent;

import java.util.concurrent.ScheduledExecutorService;

/**
 * The interface exposed a thread pools supporting scheduling
 */
public interface ManagedScheduledExecutorService extends ManagedExecutorService, ScheduledExecutorService {

}
