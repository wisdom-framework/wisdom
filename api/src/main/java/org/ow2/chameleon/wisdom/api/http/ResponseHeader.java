package org.ow2.chameleon.wisdom.api.http;

import java.util.HashMap;

/**
 * Response Header container.
 */
public class ResponseHeader extends HashMap<String, String> {

    private final int status;

    public ResponseHeader(int status) {
        this.status = status;
    }


    public String toString() {
        return status + ", " + super.toString();
    }

}
