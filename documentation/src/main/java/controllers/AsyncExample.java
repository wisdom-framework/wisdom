package controllers;

import org.apache.commons.io.input.ReaderInputStream;
import org.ow2.chameleon.wisdom.api.DefaultController;
import org.ow2.chameleon.wisdom.api.annotations.Controller;
import org.ow2.chameleon.wisdom.api.annotations.Route;
import org.ow2.chameleon.wisdom.api.bodies.RenderableFile;
import org.ow2.chameleon.wisdom.api.bodies.RenderableStream;
import org.ow2.chameleon.wisdom.api.bodies.RenderableURL;
import org.ow2.chameleon.wisdom.api.http.AsyncResult;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.MimeTypes;
import org.ow2.chameleon.wisdom.api.http.Result;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.Callable;

@Controller
public class AsyncExample extends DefaultController {

    // tag::async[]
    @Route(method= HttpMethod.GET, uri = "/async")
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
                new Runnable(){
                    Random random = new Random();
                    public void run(){
                        for (int i = 0; i < 50; i++) {
                            try {
                                String s = random.nextInt() + "\n";
                                System.out.println("..." + s);
                                out.write(s.getBytes());
                                Thread.sleep(100);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            // ignore.
                        }
                    }
                }
        ).start();
        return ok(in).as(MimeTypes.TEXT);
    }
    // end::live-data[]

}
