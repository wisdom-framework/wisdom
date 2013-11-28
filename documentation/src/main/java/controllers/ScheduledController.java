package controllers;

import org.ow2.chameleon.wisdom.api.DefaultController;
import org.ow2.chameleon.wisdom.api.annotations.Controller;
import org.ow2.chameleon.wisdom.api.annotations.scheduler.Every;
import org.ow2.chameleon.wisdom.api.scheduler.Scheduled;

// tag::scheduled[]
@Controller
public class ScheduledController extends DefaultController implements Scheduled {

    @Every("1m")
    public void task() {
        System.out.println("Task fired");
    }
}
// end::scheduled[]
