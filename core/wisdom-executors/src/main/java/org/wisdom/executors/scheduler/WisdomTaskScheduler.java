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
package org.wisdom.executors.scheduler;

import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.annotations.scheduler.Every;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;
import org.wisdom.api.concurrent.ManagedScheduledFutureTask;
import org.wisdom.api.scheduler.Scheduled;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Manage scheduled job using the system scheduler.
 */
@Component(immediate = true)
@Instantiate
public class WisdomTaskScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WisdomTaskScheduler.class);

    @Requires(filter = "(name=" + ManagedScheduledExecutorService.SYSTEM + ")", proxy = false)
    ManagedScheduledExecutorService scheduler;

    List<Job> jobs = new ArrayList<>();

    /**
     * @return the logger.
     */
    protected static Logger getLogger() {
        return LOGGER;
    }

    /**
     * Binds a new {@link Scheduled} object. All jobs defined in this objects are submitted to the system scheduler.
     *
     * @param scheduled the scheduled object
     */
    @Bind(aggregate = true, optional = true)
    public synchronized void bindScheduled(Scheduled scheduled) {
        LOGGER.info("Scheduled service bound ({}) - analyzing jobs", scheduled);
        List<Job> extracted = extractJobsFromScheduled(scheduled);
        for (Job job : extracted) {
            LOGGER.info("Job extracted from {} : {}", scheduled, job.method().getName());
            ManagedScheduledFutureTask task = scheduler.scheduleAtFixedRate(job.function(),
                    job.period(), job.period(), job.unit());
            job.submitted(task);
        }
        jobs.addAll(extracted);
    }

    /**
     * Invalidate method.
     * The system dispatcher has been shutdown, cancelling all submitted tasks.
     */
    @Invalidate
    public synchronized void invalidate() {
        for (Job job : jobs) {
            LOGGER.info("Cancelling periodic task {}#{} on invalidation", job.scheduled().getClass().getName(),
                    job.method().getName());
            job.task().cancel(true);
            job.submitted(null);
        }
    }

    /**
     * Validate method.
     * Re-submit all jobs, if there are not submitted yet.
     */
    @Validate
    public synchronized void validate() {
        for (Job job : jobs) {
            if (job.task() == null) {
                ManagedScheduledFutureTask task = scheduler.scheduleAtFixedRate(job.function(),
                        job.period(), job.period(), job.unit());
                job.submitted(task);
            }
        }
    }

    /**
     * Unbinds a scheduled service. All jobs created from this scheduled object are cancelled.
     *
     * @param scheduled the scheduled service
     */
    @Unbind
    public synchronized void unbindScheduled(Scheduled scheduled) {
        for (Job job : jobs.toArray(new Job[jobs.size()])) {
            if (job.scheduled().equals(scheduled)) {
                LOGGER.info("Cancelling periodic task {}#{}", job.scheduled().getClass().getName(),
                        job.method().getName());
                job.task().cancel(true);
                jobs.remove(job);
            }
        }
    }

    /**
     * Extracts the {@link Job} from a {@link Scheduled} service. If creates an instance of {@link Job} for each
     * method annotated with {@link Every} contained in the {@link Scheduled} class.
     *
     * @param scheduled the scheduled object
     * @return the list of job
     */
    public static List<Job> extractJobsFromScheduled(Scheduled scheduled) {
        Method[] methods = scheduled.getClass().getMethods();
        List<Job> listOfJobs = new ArrayList<>();
        for (Method method : methods) {
            Every every = method.getAnnotation(Every.class);
            if (every != null) {
                try {
                    listOfJobs.add(new Job(scheduled, method, every));
                } catch (IllegalArgumentException e) {
                    LOGGER.error("Cannot parse the period '{}' from scheduled method {}.{}", every.value(),
                            scheduled.getClass().getName(), method.getName(), e);
                }
            }
        }
        return listOfJobs;
    }

}
