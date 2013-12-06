package org.wisdom.wisit.shell;

import org.wisdom.api.http.websockets.Publisher;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: barjo
 * Date: 11/21/13
 * Time: 1:24 PM
 * To change this template use File | Settings | File Templates.
 * <p/>
 * TODO super object! ( cat command etc... )
 */
public class WisitOutputStream extends OutputStream {

    private final Publisher publisher;
    private final String topic;
    private final OutputType myType;
    private final Object lock = new Object();

    public WisitOutputStream(final Publisher publisher, final String topic) {
        this(publisher, topic,OutputType.result);
    }

    public WisitOutputStream(final Publisher publisher, final String topic,OutputType outputType) {
        this.publisher = publisher;
        this.topic = topic;
        this.myType = outputType;
    }

    public void write(int i) throws IOException { }

    public void write(byte[] b) throws IOException {
        publish(new String(b));
    }

    public void write(byte[] buf, int off, int len) {
        if (len == 1 && buf[off] == 10) { //ignore blank print
            return;
        }

        publish(new String(buf,off,len));
    }

    private void publish(String buffer){
        CommandResult out = new CommandResult();

        switch(myType){
            case result:
                out.result=buffer;
            break;
            case err:
                out.err=buffer;
            break;
        }

        synchronized (lock) {
            publisher.publish(topic, out.toString());
        }
    }

    public enum OutputType { result, err};
}
