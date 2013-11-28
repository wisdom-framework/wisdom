package org.ow2.chameleon.wisdom.akka.impl;

import org.apache.felix.ipojo.annotations.*;
import org.ow2.chameleon.wisdom.akka.AkkaSystemService;
import org.ow2.chameleon.wisdom.api.annotations.scheduler.Every;
import org.ow2.chameleon.wisdom.api.scheduler.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Manage scheduled job using Akka system scheduler.
 */
@Component(immediate = true)
@Instantiate
public class AkkaScheduler {

    public static Logger logger = LoggerFactory.getLogger(AkkaScheduler.class);
    @Requires
    AkkaSystemService akka;
    private List<Job> jobs = new ArrayList<>();

    @Bind
    public void bindScheduled(Scheduled scheduled) {
        List<Job> extracted = extractJobsFromScheduled(scheduled);
        for (Job job : extracted) {
            job.submitted(akka.system().scheduler().schedule(job.duration(), job.duration(), job.getFunction(),
                    akka.system().dispatcher()));
            jobs.add(job);
        }
    }

    @Unbind
    public void unbindScheduled(Scheduled scheduled) {
        for (Job job : jobs.toArray(new Job[jobs.size()])) {
            if (job.scheduled().equals(scheduled)) {
                job.cancellable().cancel();
                jobs.remove(job);
            }
        }
    }

    private List<Job> extractJobsFromScheduled(Scheduled scheduled) {
        Method[] methods = scheduled.getClass().getMethods();
        List<Job> jobs = new ArrayList<>();
        for (Method method : methods) {
            Every every = method.getAnnotation(Every.class);
            if (every != null) {
                try {
                    jobs.add(new Job(scheduled, method, every.value()));
                } catch (IllegalArgumentException e) {
                    logger.error("Cannot parse the period '{}' from scheduled method {}.{}", every.value(),
                            scheduled.getClass().getName(), method.getName(), e);
                }
            }
        }
        return jobs;
    }

}
