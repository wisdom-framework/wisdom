/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wisdom.api.concurrent;

import java.util.concurrent.RunnableScheduledFuture;

/**
 * An interface representing a periodic 'submitted' task. This class extends
 * {@link com.google.common.util.concurrent.ListenableFuture} to observe the completion
 * of the task and also provides management methods such as {@link #isTaskHang()}.
 * <p>
 * To ease the usage of callbacks, it offers:
 * {@link #onSuccess(org.wisdom.api.concurrent.ManagedScheduledFutureTask.SuccessCallback)} and {@link #onFailure(org.wisdom.api.concurrent.ManagedScheduledFutureTask.FailureCallback)}.
 * <p>
 * Depending on which executor the task is submitted, an
 * {@link ExecutionContext} can be applied and removed
 * before and after the task execution.
 *
 * @param <V> the type of result returned by the future. {@link Void} for {@link Runnable}.
 * @since 0.7.1
 */
public interface ManagedScheduledFutureTask<V>
        extends ManagedFutureTask<V>, RunnableScheduledFuture<V> {

}
