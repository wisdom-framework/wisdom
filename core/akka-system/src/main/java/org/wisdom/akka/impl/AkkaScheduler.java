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
package org.wisdom.akka.impl;

import org.apache.felix.ipojo.annotations.*;
import org.wisdom.akka.AkkaSystemService;
import org.wisdom.api.annotations.scheduler.Every;
import org.wisdom.api.scheduler.Scheduled;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(AkkaScheduler.class);

    @Requires
    AkkaSystemService akka;

    List<Job> jobs = new ArrayList<>();
    
    public static Logger getLogger() {
        return LOGGER;
    }

    @Bind(aggregate = true, optional = true)
    public void bindScheduled(Scheduled scheduled) {
        List<Job> extracted = extractJobsFromScheduled(scheduled);
        for (Job job : extracted) {
            job.submitted(akka.system().scheduler().schedule(job.duration(), job.duration(), job.function(),
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
