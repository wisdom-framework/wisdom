/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
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
package org.wisdom.maven;

import java.io.File;

/**
 * The interface implemented by objects participating to the <em>watch mode</em>.
 */
public interface Watcher {

    /**
     * Checks whether the given file is managed by the current watcher. Notice that implementation must not check
     * for the existence of the file as this method is also called for deleted files.
     *
     * @param file is the file.
     * @return {@literal true} if the watcher is interested in being notified on an event
     * attached to the given file,
     * {@literal false} otherwise.
     */
    public boolean accept(File file);

    /**
     * Notifies the watcher that a new file is created.
     *
     * @param file is the file.
     * @return {@literal false} if the pipeline processing must be interrupted for this event. Most watchers should
     * return {@literal true} to let other watchers be notified.
     * @throws WatchingException if the watcher failed to process the given file.
     */
    public boolean fileCreated(File file) throws WatchingException;

    /**
     * Notifies the watcher that a file has been modified.
     *
     * @param file is the file.
     * @return {@literal false} if the pipeline processing must be interrupted for this event. Most watchers should
     * returns {@literal true} to let other watchers to be notified.
     * @throws WatchingException if the watcher failed to process the given file.
     */
    public boolean fileUpdated(File file) throws WatchingException;

    /**
     * Notifies the watcher that a file was deleted.
     *
     * @param file the file
     * @return {@literal false} if the pipeline processing must be interrupted for this event. Most watchers should
     * return {@literal true} to let other watchers be notified.
     * @throws WatchingException if the watcher failed to process the given file.
     */
    public boolean fileDeleted(File file) throws WatchingException;

}
