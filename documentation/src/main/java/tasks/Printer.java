package tasks;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.ow2.chameleon.wisdom.api.annotations.scheduler.Every;
import org.ow2.chameleon.wisdom.api.scheduler.Scheduled;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A component with scheduled tasks.
 */
// tag::scheduled[]
@Component
@Provides
@Instantiate
public class Printer implements Scheduled {

    @Every("1m")
    public void print() {
        System.out.println(new SimpleDateFormat().format(new Date()) + " - Hello ...");
    }
}
// end::scheduled[]
