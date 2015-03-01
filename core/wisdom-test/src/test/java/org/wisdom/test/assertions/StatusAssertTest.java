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
package org.wisdom.test.assertions;

import org.junit.Test;
import org.wisdom.api.http.Status;

/**
 * Checks status assertion.
 */
public class StatusAssertTest {

    @Test
    public void testStatus() {
        StatusAssert.assertThat(Status.ACCEPTED).isAccepted();
        StatusAssert.assertThat(Status.BAD_GATEWAY).isBadGateway();
        StatusAssert.assertThat(Status.BAD_REQUEST).isBadRequest();
        StatusAssert.assertThat(Status.CONFLICT).isConflict();
        StatusAssert.assertThat(Status.CONTINUE).isContinue();
        StatusAssert.assertThat(Status.CREATED).isCreated();
        StatusAssert.assertThat(Status.EXPECTATION_FAILED).isExpectationFailed();
        StatusAssert.assertThat(Status.FORBIDDEN).isForbidden();
        StatusAssert.assertThat(Status.FOUND).isFound();
        StatusAssert.assertThat(Status.GATEWAY_TIMEOUT).isGatewayTimeout();
        StatusAssert.assertThat(Status.GONE).isGone();
        StatusAssert.assertThat(Status.HTTP_VERSION_NOT_SUPPORTED).isHttpVersionNotSupported();
        StatusAssert.assertThat(Status.INTERNAL_SERVER_ERROR).isInternalServerError();
        StatusAssert.assertThat(Status.LENGTH_REQUIRED).isLengthRequired();
        StatusAssert.assertThat(Status.METHOD_NOT_ALLOWED).isMethodNotAllowed();
        StatusAssert.assertThat(Status.MOVED_PERMANENTLY).isMovedPermanently();
        StatusAssert.assertThat(Status.MULTIPLE_CHOICES).isMultipleChoices();
        StatusAssert.assertThat(Status.NO_CONTENT).isNoContent();
        StatusAssert.assertThat(Status.NON_AUTHORITATIVE_INFORMATION).isNonAuthoritativeInformation();
        StatusAssert.assertThat(Status.NOT_ACCEPTABLE).isNotAcceptable();
        StatusAssert.assertThat(Status.NOT_FOUND).isNotFound();
        StatusAssert.assertThat(Status.NOT_IMPLEMENTED).isNotImplemented();
        StatusAssert.assertThat(Status.NOT_MODIFIED).isNotModified();
        StatusAssert.assertThat(Status.OK).isOk();
        StatusAssert.assertThat(Status.PARTIAL_CONTENT).isPartialContent();
        StatusAssert.assertThat(Status.PAYMENT_REQUIRED).isPaymentRequired();
        StatusAssert.assertThat(Status.PRECONDITION_FAILED).isPreconditionFailed();
        StatusAssert.assertThat(Status.PROXY_AUTHENTICATION_REQUIRED).isProxyAuthenticationRequired();
        StatusAssert.assertThat(Status.REQUEST_ENTITY_TOO_LARGE).isRequestEntityTooLarge();
        StatusAssert.assertThat(Status.REQUEST_TIMEOUT).isRequestTimeout();
        StatusAssert.assertThat(Status.REQUEST_URI_TOO_LONG).isRequestUriTooLong();
        StatusAssert.assertThat(Status.REQUESTED_RANGE_NOT_SATISFIABLE).isRequestedRangeNotSatisfiable();
        StatusAssert.assertThat(Status.RESET_CONTENT).isResetContent();
        StatusAssert.assertThat(Status.SEE_OTHER).isSeeOther();
        StatusAssert.assertThat(Status.SERVICE_UNAVAILABLE).isServiceUnavailable();
        StatusAssert.assertThat(Status.SWITCHING_PROTOCOLS).isSwitchingProtocol();
        StatusAssert.assertThat(Status.TEMPORARY_REDIRECT).isTemporaryRedirect();
        StatusAssert.assertThat(Status.UNAUTHORIZED).isUnauthorized();
        StatusAssert.assertThat(Status.UNSUPPORTED_MEDIA_TYPE).isUnsupportedMediaType();
        StatusAssert.assertThat(Status.USE_PROXY).isUseProxy();
    }

}