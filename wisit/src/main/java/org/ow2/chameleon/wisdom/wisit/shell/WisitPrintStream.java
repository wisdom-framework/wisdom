package org.ow2.chameleon.wisdom.wisit.shell;

import org.ow2.chameleon.wisdom.api.http.websockets.Publisher;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created with IntelliJ IDEA.
 * User: barjo
 * Date: 11/21/13
 * Time: 1:24 PM
 * To change this template use File | Settings | File Templates.
 *
 * TODO super object! ( cat command etc... )
 */
public class WisitPrintStream extends PrintStream {

    private final Publisher publisher;
    private final String topic;


    public WisitPrintStream(Publisher publisher,String topic) {
        super(new OutputStream() {
            public void write(int i) throws IOException {
                //we only need to handle println!
            }
        },true);

        this.publisher = publisher;
        this.topic=topic;
    }

    @Override
    public void println(String x) {
        synchronized (publisher){
            publisher.publish(topic,x);
        }
    }
}
