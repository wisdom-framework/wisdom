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

    private final Object lock = new Object();

    private final String topic;

    private final Publisher publisher;

    private final WisitSession session;


    public WisitPrintStream(WisitSession session,final Publisher publisher,final String topic) {
        super(new OutputStream() {
            public void write(int i) throws IOException {
                //we only need to handle println!
            }
        },true);

        this.session=session;
        this.publisher=publisher;
        this.topic=topic;
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        byte[] out = new byte[len];
        int j = 0;

        for(int i = off;j<len;i++,j++){
            out[j] = buf[i];
        }

        //TODO check max length supported by the websocket
        synchronized (lock){
            publisher.publish(topic,new String(out));
        }
    }

    /*@Override
    public void println(String x) {
        synchronized (lock){
            publisher.publish(topic,x);
        }
    }*/
}
