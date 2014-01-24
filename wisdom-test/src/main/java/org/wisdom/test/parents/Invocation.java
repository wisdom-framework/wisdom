package org.wisdom.test.parents;

import org.wisdom.api.http.Result;

/**
 *
 */
public interface Invocation {

    Result invoke() throws Throwable;
}
