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
package org.wisdom.maven.pipeline;

import org.wisdom.maven.Watcher;
import org.wisdom.maven.WatchingException;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * An implementation of watcher using delegation.
 * The invocation are delegate on an object by reflection. This is necessary because the retrieve watchers are not
 * loaded with the same classloader as the run mojo. This comes from the forked lifecycle done by the run mojo.
 */
public class WatcherDelegate implements Watcher {

    private final Object delegate;
    private final Method fileDeleted;
    private final Method fileCreated;
    private final Method fileUpdated;
    private final Method accept;

    public WatcherDelegate(Object delegate) {
        this.delegate = delegate;
        try {
            this.accept = delegate.getClass().getMethod("accept", File.class);
            this.fileCreated = delegate.getClass().getMethod("fileCreated", File.class);
            this.fileUpdated = delegate.getClass().getMethod("fileUpdated", File.class);
            this.fileDeleted = delegate.getClass().getMethod("fileDeleted", File.class);

            this.accept.setAccessible(true);
            this.fileCreated.setAccessible(true);
            this.fileUpdated.setAccessible(true);
            this.fileDeleted.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the delegate.
     */
    public Object getDelegate() {
        return delegate;
    }

    @Override
    public boolean accept(File file) {
        try {
            return (Boolean) accept.invoke(delegate, file);
        } catch (InvocationTargetException e) { //NOSONAR
            throw new RuntimeException(e.getTargetException());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean fileCreated(File file) throws WatchingException {
        try {
            return (Boolean) fileCreated.invoke(delegate, file);
        } catch (InvocationTargetException e) { //NOSONAR
            throw createWatchingException(e, file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Even {@link org.wisdom.maven.WatchingException} cannot be used directly, so we need to use reflection to
     * recreate the exception with the same content.
     * <p>
     * This method checks if the cause of the {@link java.lang.reflect
     * .InvocationTargetException} is a {@link org.wisdom.maven.WatchingException},
     * in this case if create an exception with the same content. Otherwise it creates a new {@link org.wisdom.maven
     * .WatchingException} from the given exception's cause.
     *
     * @param exception the invocation target exception caught by the delegate
     * @param file      the file having thrown the exception (the processed file).
     * @return a Watching Exception containing the content from the given exception if possible or a new exception
     * from the {@literal exception}'s cause.
     */
    public static WatchingException createWatchingException(InvocationTargetException exception, File file) {
        Throwable cause = exception.getTargetException();
        if (WatchingException.class.getName().equals(exception.getTargetException().getClass().getName())) {
            try {
                Method line = cause.getClass().getMethod("getLine");
                Method character = cause.getClass().getMethod("getCharacter");
                Method source = cause.getClass().getMethod("getFile");
                Method title = cause.getClass().getMethod("getTitle");

                return new WatchingException(
                        (String) title.invoke(cause),
                        cause.getMessage(),
                        (File) source.invoke(cause),
                        (Integer) line.invoke(cause),
                        (Integer) character.invoke(cause),
                        cause.getCause());
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                throw new IllegalArgumentException("Cannot create the watching exception from " + cause);
            }
        } else {
            return new WatchingException(cause.getMessage(), file, cause);
        }
    }

    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        try {
            return (Boolean) fileUpdated.invoke(delegate, file);
        } catch (InvocationTargetException e) { //NOSONAR
            throw createWatchingException(e, file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean fileDeleted(File file) throws WatchingException {
        try {
            return (Boolean) fileDeleted.invoke(delegate, file);
        } catch (InvocationTargetException e) { //NOSONAR
            throw createWatchingException(e, file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
