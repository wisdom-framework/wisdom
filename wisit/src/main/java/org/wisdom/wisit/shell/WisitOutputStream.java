package org.wisdom.wisit.shell;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.wisdom.api.http.websockets.Publisher;

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
    
    public enum OutputType { 
        RESULT, 
        ERR
    }

    private final Publisher publisher;
    private final String topic;
    private final OutputType myType;
    private final Object lock = new Object();

    private static final String UTF8 = "UTF-8";

    public WisitOutputStream(final Publisher publisher, final String topic) {
        this(publisher, topic,OutputType.RESULT);
    }

    public WisitOutputStream(final Publisher publisher, final String topic,OutputType outputType) {
        this.publisher = publisher;
        this.topic = topic;
        this.myType = outputType;
    }

    public void write(int i) throws IOException { 
        //Unused
    }

    public void write(byte[] b) throws IOException {
        publish(new String(b, UTF8));
    }

    public void write(byte[] buf, int off, int len) {
        //ignore blank print
        if (len == 1 && buf[off] == 10) { 
            return;
        }

        publish(new String(buf,off,len, Charset.forName(UTF8)));
    }

    private void publish(String buffer){
        CommandResult out = new CommandResult();

        switch(myType){
        case RESULT:
            out.setResult(buffer);
            break;
        case ERR:
            out.setErr(buffer);
            break;
        default:
            break;
        }

        synchronized (lock) {
            publisher.publish(topic, out.toString());
        }
    }
}
