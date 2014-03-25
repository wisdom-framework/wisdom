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
 * loaded with the same classloader as the run mojo. This comes form the forked lifecycle done by the run mojo.
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
            this.accept = delegate.getClass().getMethod("accept", new Class[] {File.class});
            this.fileCreated = delegate.getClass().getMethod("fileCreated", new Class[] {File.class});
            this.fileUpdated = delegate.getClass().getMethod("fileUpdated", new Class[] {File.class});
            this.fileDeleted = delegate.getClass().getMethod("fileDeleted", new Class[] {File.class});
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
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
            throw new WatchingException(e.getTargetException().getMessage(), file, e.getTargetException());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        try {
            return (Boolean) fileUpdated.invoke(delegate, file);
        } catch (InvocationTargetException e) { //NOSONAR
            throw new WatchingException(e.getTargetException().getMessage(), file, e.getTargetException());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean fileDeleted(File file) throws WatchingException {
        try {
            return (Boolean) fileDeleted.invoke(delegate, file);
        } catch (InvocationTargetException e) { //NOSONAR
            throw new WatchingException(e.getTargetException().getMessage(), file, e.getTargetException());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
