package controllers;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.scheduler.Every;
import org.wisdom.api.scheduler.Scheduled;

// tag::scheduled[]
@Controller
public class ScheduledController extends DefaultController implements Scheduled {

    @Every("1m")
    public void task() {
        System.out.println("Task fired");
    }
}
// end::scheduled[]
