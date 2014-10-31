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
package snippets.controllers;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.scheduler.Async;
import org.wisdom.api.bodies.RenderableFile;
import org.wisdom.api.bodies.RenderableStream;
import org.wisdom.api.bodies.RenderableURL;
import org.wisdom.api.http.AsyncResult;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.Callable;

@Controller
public class AsyncExample extends DefaultController {

    // tag::async[]
    @Route(method = HttpMethod.GET, uri = "/async")
    public Result async() {
        return new AsyncResult(new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                // heavy computation here...
                return ok("Computation done");
            }
        });
    }
    // end::async[]

    // tag::async2[]
    @Route(method = HttpMethod.GET, uri = "/async")
    @Async
    public Result regular() {
        return ok("Computation done");
    }
    // end::async2[]


    // tag::hello[]
    public Result hello() {
        return ok("Hello!");
    }
    // end::hello[]


    // tag::file[]
    @Route(method = HttpMethod.GET, uri = "/movie")
    public Result file() {
        return ok(new File("/tmp/myMovie.mkv"));
    }
    // end::file[]

    // tag::file-attachment[]
    @Route(method = HttpMethod.GET, uri = "/pdf")
    public Result fileAsAttachment() {
        return ok(new File("/tmp/wisdom.pdf"), true);
    }
    // end::file-attachment[]

    // tag::url[]
    @Route(method = HttpMethod.GET, uri = "/perdu")
    public Result url() throws MalformedURLException {
        return ok(new URL("http://perdu.com")).html();
    }
    // end::url[]

    // tag::url-manifest[]
    @Route(method = HttpMethod.GET, uri = "/manifest")
    public Result manifest() throws MalformedURLException {
        URL manifest = this.getClass()
                .getClassLoader().getResource("/META-INF/MANIFEST.MF");
        return ok(manifest).as(MimeTypes.TEXT);
    }
    // end::url-manifest[]

    // tag::url-manifest-as-stream[]
    @Route(method = HttpMethod.GET, uri = "/manifest-as-stream")
    public Result manifestAsStream() throws MalformedURLException {
        InputStream is = this.getClass()
                .getClassLoader().getResourceAsStream("/META-INF/MANIFEST.MF");
        return ok(is).as(MimeTypes.TEXT);
    }
    // end::url-manifest-as-stream[]


    public void noChunks() throws MalformedURLException {
        URL url = null;
        InputStream stream = null;
        File file = null;

        // tag::no-chunks[]
        ok(new RenderableFile(file, false));
        ok(new RenderableURL(url, false));
        ok(new RenderableStream(stream, false));
        // end::no-chunks[]
    }

    // tag::live-data[]
    @Route(method = HttpMethod.GET, uri = "/live")
    public Result live() throws IOException {
        PipedInputStream in = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(in);
        ///...
        new Thread(
                new Runnable() {
                    Random random = new Random();

                    public void run() {
                        for (int i = 0; i < 50; i++) {
                            try {
                                String s = random.nextInt() + "\n";
                                out.write(s.getBytes());
                                Thread.sleep(100);
                            } catch (Exception e) { //NOSONAR
                                e.printStackTrace();
                            }
                        }
                        try {
                            out.close();
                        } catch (IOException e) { //NOSONAR
                            // ignore.
                        }
                    }
                }
        ).start();
        return ok(in).as(MimeTypes.TEXT);
    }
    // end::live-data[]

}
