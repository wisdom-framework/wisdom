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

    protected StatusAssert(Integer actual) {
        super(actual, StatusAssert.class);
    }

    public static StatusAssert assertThat(Integer actual){
        return new StatusAssert(actual);
    }

    public StatusAssert isOK(){
        return isEqualTo(Status.OK);
    }

    public StatusAssert isBAD_REQUEST(){
        return isEqualTo(Status.BAD_REQUEST);
    }

    public StatusAssert isACCEPTED(){
        return isEqualTo(Status.ACCEPTED);
    }

    public StatusAssert isBAD_GATEWAY(){
        return isEqualTo(Status.BAD_GATEWAY);
    }

    public StatusAssert isCONFLICT(){
        return isEqualTo(Status.CONFLICT);
    }

    public StatusAssert isCONTINUE(){
        return isEqualTo(Status.CONTINUE);
    }

    public StatusAssert isCREATED(){
        return isEqualTo(Status.CREATED);
    }

    public StatusAssert isEXPECTATION_FAILED(){
        return isEqualTo(Status.EXPECTATION_FAILED);
    }

    public StatusAssert isFORBIDDEN(){
        return isEqualTo(Status.FORBIDDEN);
    }

    public StatusAssert isFOUND(){
        return isEqualTo(Status.FOUND);
    }

    public StatusAssert isGATEWAY_TIMEOUT(){
        return isEqualTo(Status.GATEWAY_TIMEOUT);
    }

    public StatusAssert isGONE(){
        return isEqualTo(Status.GONE);
    }

    public StatusAssert isHTTP_VERSION_NOT_SUPPORTED(){
        return isEqualTo(Status.HTTP_VERSION_NOT_SUPPORTED);
    }

    public StatusAssert isINTERNAL_SERVER_ERROR(){
        return isEqualTo(Status.INTERNAL_SERVER_ERROR);
    }

    public StatusAssert isLENGTH_REQUIRED(){
        return isEqualTo(Status.LENGTH_REQUIRED);
    }

    public StatusAssert isMETHOD_NOT_ALLOWED(){
        return isEqualTo(Status.METHOD_NOT_ALLOWED);
    }

    public StatusAssert isMOVED_PERMANENTLY(){
        return isEqualTo(Status.MOVED_PERMANENTLY);
    }

    public StatusAssert isMULTIPLE_CHOICES(){
        return isEqualTo(Status.MULTIPLE_CHOICES);
    }

    public StatusAssert isNO_CONTENT(){
        return isEqualTo(Status.NO_CONTENT);
    }

    public StatusAssert isNON_AUTHORITATIVE_INFORMATION(){
        return isEqualTo(Status.NON_AUTHORITATIVE_INFORMATION);
    }

    public StatusAssert isNOT_ACCEPTABLE(){
        return isEqualTo(Status.NOT_ACCEPTABLE);
    }

    public StatusAssert isNOT_FOUND(){
        return isEqualTo(Status.NOT_FOUND);
    }

    public StatusAssert isNOT_IMPLEMENTED(){
        return isEqualTo(Status.NOT_IMPLEMENTED);
    }

    public StatusAssert isNOT_MODIFIED(){
        return isEqualTo(Status.NOT_MODIFIED);
    }

    public StatusAssert isPARTIAL_CONTENT(){
        return isEqualTo(Status.PARTIAL_CONTENT);
    }

    public StatusAssert isPAYMENT_REQUIRED(){
        return isEqualTo(Status.PAYMENT_REQUIRED);
    }

    public StatusAssert isPRECONDITION_FAILED(){
        return isEqualTo(Status.PRECONDITION_FAILED);
    }

    public StatusAssert isPROXY_AUTHENTICATION_REQUIRED(){
        return isEqualTo(Status.PROXY_AUTHENTICATION_REQUIRED);
    }

    public StatusAssert isREQUEST_ENTITY_TOO_LARGE(){
        return isEqualTo(Status.REQUEST_ENTITY_TOO_LARGE);
    }

    public StatusAssert isREQUEST_TIMEOUT(){
        return isEqualTo(Status.REQUEST_TIMEOUT);
    }

    public StatusAssert isREQUEST_URI_TOO_LONG(){
        return isEqualTo(Status.REQUEST_URI_TOO_LONG);
    }

    public StatusAssert isREQUESTED_RANGE_NOT_SATISFIABLE(){
        return isEqualTo(Status.REQUESTED_RANGE_NOT_SATISFIABLE);
    }

    public StatusAssert isRESET_CONTENT(){
        return isEqualTo(Status.RESET_CONTENT);
    }

    public StatusAssert isSEE_OTHER(){
        return isEqualTo(Status.SEE_OTHER);
    }

    public StatusAssert isSERVICE_UNAVAILABLE(){
        return isEqualTo(Status.SERVICE_UNAVAILABLE);
    }

    public StatusAssert isSWITCHING_PROTOCOLS(){
        return isEqualTo(Status.SWITCHING_PROTOCOLS);
    }

    public StatusAssert isTEMPORARY_REDIRECT(){
        return isEqualTo(Status.TEMPORARY_REDIRECT);
    }

    public StatusAssert isUNAUTHORIZED(){
        return isEqualTo(Status.UNAUTHORIZED);
    }

    public StatusAssert isUNSUPPORTED_MEDIA_TYPE(){
        return isEqualTo(Status.UNSUPPORTED_MEDIA_TYPE);
    }

    public StatusAssert isUSE_PROXY(){
        return isEqualTo(Status.USE_PROXY);
    }
}
