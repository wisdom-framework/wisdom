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
package org.wisdom.test.assertions;

import org.assertj.core.api.AbstractIntegerAssert;
import org.wisdom.api.http.Status;

/**
 * Specific AssertJ assertion for {@link org.wisdom.api.http.Status}.
 */
public class StatusAssert extends AbstractIntegerAssert<StatusAssert> {

    /**
     * Creates a {@link StatusAssert}.
     *
     * @param actual the status.
     */
    protected StatusAssert(Integer actual) {
        super(actual, StatusAssert.class);
    }

    /**
     * Checks that the {@link StatusAssert} has the given status.
     *
     * @param actual the expected status.
     * @return the current {@link StatusAssert}
     */
    public static StatusAssert assertThat(Integer actual) {
        return new StatusAssert(actual);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isOk() {
        return isEqualTo(Status.OK);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isBadRequest() {
        return isEqualTo(Status.BAD_REQUEST);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isAccepted() {
        return isEqualTo(Status.ACCEPTED);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isBadGateway() {
        return isEqualTo(Status.BAD_GATEWAY);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isConflict() {
        return isEqualTo(Status.CONFLICT);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isContinue() {
        return isEqualTo(Status.CONTINUE);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isCreated() {
        return isEqualTo(Status.CREATED);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isExpectationFailed() {
        return isEqualTo(Status.EXPECTATION_FAILED);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isForbidden() {
        return isEqualTo(Status.FORBIDDEN);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isFound() {
        return isEqualTo(Status.FOUND);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isGatewayTimeout() {
        return isEqualTo(Status.GATEWAY_TIMEOUT);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isGone() {
        return isEqualTo(Status.GONE);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isHttpVersionNotSupported() {
        return isEqualTo(Status.HTTP_VERSION_NOT_SUPPORTED);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isInternalServerError() {
        return isEqualTo(Status.INTERNAL_SERVER_ERROR);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isLengthRequired() {
        return isEqualTo(Status.LENGTH_REQUIRED);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isMethodNotAllowed() {
        return isEqualTo(Status.METHOD_NOT_ALLOWED);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isMovedPermanently() {
        return isEqualTo(Status.MOVED_PERMANENTLY);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isMultipleChoices() {
        return isEqualTo(Status.MULTIPLE_CHOICES);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isNoContent() {
        return isEqualTo(Status.NO_CONTENT);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isNonAuthoritativeInformation() {
        return isEqualTo(Status.NON_AUTHORITATIVE_INFORMATION);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isNotAcceptable() {
        return isEqualTo(Status.NOT_ACCEPTABLE);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isNotFound() {
        return isEqualTo(Status.NOT_FOUND);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isNotImplemented() {
        return isEqualTo(Status.NOT_IMPLEMENTED);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isNotModified() {
        return isEqualTo(Status.NOT_MODIFIED);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isPartialContent() {
        return isEqualTo(Status.PARTIAL_CONTENT);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isPaymentRequired() {
        return isEqualTo(Status.PAYMENT_REQUIRED);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isPreconditionFailed() {
        return isEqualTo(Status.PRECONDITION_FAILED);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isProxyAuthenticationRequired() {
        return isEqualTo(Status.PROXY_AUTHENTICATION_REQUIRED);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isRequestEntityTooLarge() {
        return isEqualTo(Status.REQUEST_ENTITY_TOO_LARGE);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isRequestTimeout() {
        return isEqualTo(Status.REQUEST_TIMEOUT);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isRequestUriTooLong() {
        return isEqualTo(Status.REQUEST_URI_TOO_LONG);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isRequestedRangeNotSatisfiable() {
        return isEqualTo(Status.REQUESTED_RANGE_NOT_SATISFIABLE);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isResetContent() {
        return isEqualTo(Status.RESET_CONTENT);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isSeeOther() {
        return isEqualTo(Status.SEE_OTHER);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isServiceUnavailable() {
        return isEqualTo(Status.SERVICE_UNAVAILABLE);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isSwitchingProtocol() {
        return isEqualTo(Status.SWITCHING_PROTOCOLS);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isTemporaryRedirect() {
        return isEqualTo(Status.TEMPORARY_REDIRECT);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isUnauthorized() {
        return isEqualTo(Status.UNAUTHORIZED);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isUnsupportedMediaType() {
        return isEqualTo(Status.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Checks that the status is the expected status. The expected status is in the method name.
     *
     * @return the current {@link StatusAssert}
     */
    public StatusAssert isUseProxy() {
        return isEqualTo(Status.USE_PROXY);
    }
}
