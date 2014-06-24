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
package org.wisdom.api.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import org.junit.Test;
import org.w3c.dom.Document;
import org.wisdom.api.bodies.NoHttpBody;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;


public class ResultsTest {

    @Test
    public void testStatus() throws Exception {
        Result result = Results.status(118);
        assertThat(result.getStatusCode()).isEqualTo(118);
    }

    @Test
    public void testEmptyOk() throws Exception {
        Result result = Results.ok();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(result.getRenderable()).isInstanceOf(NoHttpBody.class);
        assertThat(result.getContentType()).isEqualTo(null);
    }

    @Test
    public void testStringOk() throws Exception {
        Result result = Results.ok("hello");
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(result.getRenderable().content()).isEqualTo("hello");
        assertThat(result.getContentType()).isEqualTo(MimeTypes.TEXT);

        result = Results.ok("<p>hello</p>").html();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(result.getRenderable().content()).isEqualTo("<p>hello</p>");
        assertThat(result.getContentType()).isEqualTo(MimeTypes.HTML);
    }

    @Test
    public void testOkJSON() throws Exception {
        ObjectNode node = new ObjectMapper().createObjectNode();
        node.put("message", "hello");
        Result result = Results.ok(node);
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(result.getRenderable().content()).isEqualTo(node);
        assertThat(result.getContentType()).isEqualTo(MimeTypes.JSON);

    }

    @Test
    public void testOkDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document = factory.newDocumentBuilder().newDocument();
        Result result = Results.ok(document);
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(result.getRenderable().content()).isEqualTo(document);
        assertThat(result.getContentType()).isEqualTo(MimeTypes.XML);
    }

    @Test
    public void testOkWrapper() throws Exception {
        Result r1 = Results.notFound("nope");
        Result result = Results.ok(r1);
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(result.getRenderable().content()).isEqualTo("nope");
        assertThat(result.getContentType()).isEqualTo(MimeTypes.TEXT);
    }


    @Test
    public void testNotFound() throws Exception {
        Result result = Results.notFound();
        assertThat(result.getStatusCode()).isEqualTo(Status.NOT_FOUND);
        assertThat(result.getRenderable()).isInstanceOf(NoHttpBody.class);
        assertThat(result.getContentType()).isEqualTo(null);
    }

    @Test
    public void testNotFoundWithText() throws Exception {
        Result result = Results.notFound("nope");
        assertThat(result.getStatusCode()).isEqualTo(Status.NOT_FOUND);
        assertThat(result.getRenderable().content()).isEqualTo("nope");
        assertThat(result.getContentType()).isEqualTo(MimeTypes.TEXT);
    }

    @Test
    public void testNotFoundWithResult() throws Exception {
        Result r1 = Results.ok("nope");
        Result r2 = Results.notFound(r1);
        assertThat(r2.getStatusCode()).isEqualTo(Status.NOT_FOUND);
        assertThat(r2.getRenderable().content()).isEqualTo("nope");
        assertThat(r2.getContentType()).isEqualTo(MimeTypes.TEXT);
    }

    @Test
    public void testForbiddenWithText() throws Exception {
        Result result = Results.forbidden("nope");
        assertThat(result.getStatusCode()).isEqualTo(Status.FORBIDDEN);
        assertThat(result.getRenderable().content()).isEqualTo("nope");
        assertThat(result.getContentType()).isEqualTo(MimeTypes.TEXT);

    }

    @Test
    public void testForbidden() throws Exception {
        Result result = Results.forbidden();
        assertThat(result.getStatusCode()).isEqualTo(Status.FORBIDDEN);
        assertThat(result.getRenderable()).isInstanceOf(NoHttpBody.class);
        assertThat(result.getContentType()).isEqualTo(null);
    }

    @Test
    public void testForbiddenWitResult() throws Exception {
        Result r1 = Results.ok("nope");
        Result result = Results.forbidden(r1);
        assertThat(result.getStatusCode()).isEqualTo(Status.FORBIDDEN);
        assertThat(result.getRenderable().content()).isEqualTo("nope");
        assertThat(result.getContentType()).isEqualTo(MimeTypes.TEXT);
    }


    @Test
    public void testUnauthorizedWithText() throws Exception {
        Result result = Results.unauthorized("nope");
        assertThat(result.getStatusCode()).isEqualTo(Status.UNAUTHORIZED);
        assertThat(result.getRenderable().content()).isEqualTo("nope");
        assertThat(result.getContentType()).isEqualTo(MimeTypes.TEXT);
    }

    @Test
    public void testUnauthorized() throws Exception {
        Result result = Results.unauthorized();
        assertThat(result.getStatusCode()).isEqualTo(Status.UNAUTHORIZED);
        assertThat(result.getRenderable()).isInstanceOf(NoHttpBody.class);
        assertThat(result.getContentType()).isEqualTo(null);
    }

    @Test
    public void testUnauthorizedWitResult() throws Exception {
        Result r1 = Results.ok("nope");
        Result result = Results.unauthorized(r1);
        assertThat(result.getStatusCode()).isEqualTo(Status.UNAUTHORIZED);
        assertThat(result.getRenderable().content()).isEqualTo("nope");
        assertThat(result.getContentType()).isEqualTo(MimeTypes.TEXT);
    }

    @Test
    public void testBadRequest() throws Exception {
        Result result = Results.badRequest();
        assertThat(result.getStatusCode()).isEqualTo(Status.BAD_REQUEST);
        assertThat(result.getRenderable()).isInstanceOf(NoHttpBody.class);
        assertThat(result.getContentType()).isEqualTo(null);
    }

    @Test
    public void testBadRequestWithResult() throws Exception {
        Result r1 = Results.ok("nope");
        Result result = Results.badRequest(r1);
        assertThat(result.getStatusCode()).isEqualTo(Status.BAD_REQUEST);
        assertThat(result.getRenderable().content()).isEqualTo("nope");
        assertThat(result.getContentType()).isEqualTo(MimeTypes.TEXT);

    }

    @Test
    public void testBadRequestWithText() throws Exception {
        Result result = Results.badRequest("nope");
        assertThat(result.getStatusCode()).isEqualTo(Status.BAD_REQUEST);
        assertThat(result.getRenderable().content()).isEqualTo("nope");
        assertThat(result.getContentType()).isEqualTo(MimeTypes.TEXT);

    }

    @Test
    public void testNoContent() throws Exception {
        Result result = Results.noContent();
        assertThat(result.getRenderable()).isInstanceOf(NoHttpBody.class);
    }

    @Test
    public void testInternalServerError() throws Exception {
        Result result = Results.internalServerError();
        assertThat(result.getStatusCode()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
        assertThat(result.getRenderable()).isInstanceOf(NoHttpBody.class);
        assertThat(result.getContentType()).isEqualTo(null);
    }

    @Test
    public void testInternalServerErrorWithText() throws Exception {
        Result result = Results.internalServerError("nope");
        assertThat(result.getStatusCode()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
        assertThat(result.getRenderable().content()).isEqualTo("nope");
        assertThat(result.getContentType()).isEqualTo(MimeTypes.TEXT);

    }

    @Test
    public void testInternalServerErrorWithException() throws Exception {
        NullPointerException pe = new NullPointerException();
        Result result = Results.internalServerError(pe);
        assertThat(result.getStatusCode()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
        assertThat(result.getContentType()).isEqualTo(MimeTypes.JSON);
    }

    @Test
    public void testRedirect() throws Exception {
        Result result = Results.redirect("/");
        assertThat(result.getStatusCode()).isEqualTo(Status.SEE_OTHER);
        assertThat(result.getRenderable()).isInstanceOf(NoHttpBody.class);
        assertThat(result.getHeaders().get(HeaderNames.LOCATION)).isEqualTo("/");
    }

    @Test
    public void testRedirectTemporary() throws Exception {
        Result result = Results.redirectTemporary("/");
        assertThat(result.getStatusCode()).isEqualTo(Status.TEMPORARY_REDIRECT);
        assertThat(result.getRenderable()).isInstanceOf(NoHttpBody.class);
        assertThat(result.getHeaders().get(HeaderNames.LOCATION)).isEqualTo("/");
    }

    @Test
    public void testHtml() throws Exception {
        Result result = Results.ok("hello").html();
        assertThat(result.getContentType()).isEqualTo(MimeTypes.HTML);
        assertThat(result.getCharset()).isEqualTo(Charsets.UTF_8);
    }

    @Test
    public void testJson() throws Exception {
        Result result = Results.ok("hello").json();
        assertThat(result.getContentType()).isEqualTo(MimeTypes.JSON);
        assertThat(result.getCharset()).isEqualTo(Charsets.UTF_8);

    }

    @Test
    public void testXml() throws Exception {
        Result result = Results.ok("hello").xml();
        assertThat(result.getContentType()).isEqualTo(MimeTypes.XML);
        assertThat(result.getCharset()).isEqualTo(Charsets.UTF_8);
    }

    @Test
    public void testTodo() throws Exception {
        Result result = Results.todo();
        assertThat(result.getStatusCode()).isEqualTo(Status.NOT_IMPLEMENTED);
        assertThat(result.getContentType()).isEqualTo(MimeTypes.JSON);
        assertThat(result.getCharset()).isEqualTo(Charsets.UTF_8);
    }


    @Test
    public void testAsync() throws Exception {
        Result result = Results.async(new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                return Results.ok();
            }
        });
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(AsyncResult.class);
        assertThat(((AsyncResult) result).callable().call().getStatusCode()).isEqualTo(Status.OK);
    }

}