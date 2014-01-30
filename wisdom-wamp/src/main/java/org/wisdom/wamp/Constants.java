package org.wisdom.wamp;

/**
 * Constants used in the WAMP support.
 */
public interface Constants {

    /**
     * The WAMP protocol version.
     */
    public static final int WAMP_PROTOCOL_VERSION = 1;

    /**
     * The WAMP server id.
     */
    public static final String WAMP_SERVER_VERSION = "Wisdom/Wamp 1.0";

    /**
     * The route to access WAMP.
     */
    public static final String WAMP_ROUTE = "/wamp";

    /**
     * The error url prefix to append to the regular prefix.
     */
    public static final String WAMP_ERROR = "/error";
}
