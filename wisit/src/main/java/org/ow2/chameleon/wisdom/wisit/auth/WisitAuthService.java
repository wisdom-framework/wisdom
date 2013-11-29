package org.ow2.chameleon.wisdom.wisit.auth;

/**
 * Wisit Authentication service.
 *
 * @author Jonathan M. Bardin
 */
public interface WisitAuthService {

    /**
     * Wisit username property (in application.conf)
     */
    String WISIT_USER = "wisit.user";

    /**
     * Wisit password property (in application.conf)
     */
    String WISIT_PASS = "wisit.pass";

    /**
     * @return true if the logged user is authorised to use the wisdom terminal.
     */
    boolean isAuthorised();
}
