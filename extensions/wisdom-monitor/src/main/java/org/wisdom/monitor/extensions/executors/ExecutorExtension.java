/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package org.wisdom.monitor.extensions.executors;

import com.codahale.metrics.*;
import com.google.common.collect.ImmutableMap;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.concurrent.ManagedExecutorService;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.security.Authenticated;
import org.wisdom.api.templates.Template;
import org.wisdom.monitor.service.MonitorExtension;

import java.util.*;

/**
 * Monitor extension for executors and schedulers.
 */
@Controller
@Authenticated("Monitor-Authenticator")
public class ExecutorExtension extends DefaultController implements MonitorExtension {

    @Requires
    ManagedExecutorService[] executors;

    @Requires
    ManagedScheduledExecutorService[] schedulers;

    @Requires
    MetricRegistry metrics;

    @View("monitor/executors")
    Template template;

    /**
     * Starts the extension. It registers the different metrics into the metric registry.
     */
    @Validate
    public void start() {
        metrics.register("executors", new MetricSet() {
            @Override
            public Map<String, Metric> getMetrics() {
                return ImmutableMap.<String, Metric>of(
                        "executors.count", new Gauge<Integer>() {
                            @Override
                            public Integer getValue() {
                                return getExecutors().length;
                            }
                        },
                        "schedulers.count", new Gauge<Integer>() {

                            @Override
                            public Integer getValue() {
                                return getSchedulers().length;
                            }
                        },
                        "hung.count", new Gauge<Integer>() {

                            @Override
                            public Integer getValue() {
                                return getHungTasks();
                            }
                        },
                        "completed.count", new Gauge<Integer>() {

                            @Override
                            public Integer getValue() {
                                return getCompletedTasks();
                            }
                        }
                );
            }
        });

        for (ManagedExecutorService service : executors) {
            metrics.register(service.name(), metricsForExecutor(service));
        }

        for (ManagedExecutorService service : schedulers) {
            metrics.register(service.name(), metricsForExecutor(service));
        }
    }

    private MetricSet metricsForExecutor(final ManagedExecutorService executor) {
        return new MetricSet() {
            @Override
            public Map<String, Metric> getMetrics() {
                return ImmutableMap.<String, Metric>builder()
                        .put("queue", new Gauge<Integer>() {
                            @Override
                            public Integer getValue() {
                                return executor.getQueue().size();
                            }
                        })
                        .put("completed", new Counter() {
                            @Override
                            public long getCount() {
                                return executor.getCompletedTaskCount();
                            }
                        })
                        .put("hung", new Gauge<Integer>() {
                            @Override
                            public Integer getValue() {
                                return executor.getHungTasks().size();
                            }
                        })
                        .put("active", new Gauge<Integer>() {
                            @Override
                            public Integer getValue() {
                                return executor.getActiveCount();
                            }
                        })
                        .put("pool", new Gauge<Integer>() {
                            @Override
                            public Integer getValue() {
                                return executor.getPoolSize();
                            }
                        })
                        .put("largest", new Gauge<Integer>() {
                            @Override
                            public Integer getValue() {
                                return executor.getLargestPoolSize();
                            }
                        })
                        .put("max_exec", new Gauge<Long>() {
                            @Override
                            public Long getValue() {
                                return executor.getExecutionTimeStatistics().getMaximumExecutionTime();
                            }
                        })
                        .put("total", new Gauge<Long>() {
                            @Override
                            public Long getValue() {
                                return executor.getExecutionTimeStatistics().getTotalExecutionTime();
                            }
                        })
                        .put("min_exec", new Gauge<Long>() {
                            @Override
                            public Long getValue() {
                                return executor.getExecutionTimeStatistics().getMinimumExecutionTime();
                            }
                        })
                        .put("avg", new Gauge<Double>() {
                            @Override
                            public Double getValue() {
                                return executor.getExecutionTimeStatistics().getAverageExecutionTime();
                            }
                        })
                        .build();
            }
        };
    }

    private ManagedExecutorService[] getExecutors() {
        return executors;
    }

    private ManagedScheduledExecutorService[] getSchedulers() {
        return schedulers;
    }


    /**
     * Gets the extension main view.
     *
     * @return the executors page.
     */
    @Route(method = HttpMethod.GET, uri = "/monitor/executors")
    public Result index() {
        return ok(render(template));
    }

    /**
     * Retrieves the metrics about the executors. This method is intended to be used to handled an AJAX call.
     * @return the metrics as JSON.
     */
    @Route(method = HttpMethod.GET, uri = "/monitor/executors.json")
    public Result data() {
        return ok(ImmutableMap.builder()
                        .put("executors", getExecutorsAsMap(executors))
                        .put("schedulers", getExecutorsAsMap(schedulers))
                        .put("hung", getHungTasks())
                        .put("completed", getCompletedTasks())
                        .build()
        );
    }

    private Map<String, ManagedExecutorService> getExecutorsAsMap(ManagedExecutorService[] exec) {
        Map<String, ManagedExecutorService> map = new LinkedHashMap<>();
        for (ManagedExecutorService svc : exec) {
            map.put(svc.name(), svc);
        }
        return map;
    }

    private int getHungTasks() {
        int count = 0;
        for (ManagedExecutorService svc : executors) {
            count += svc.getHungTasks().size();
        }
        for (ManagedScheduledExecutorService svc : schedulers) {
            count += svc.getHungTasks().size();
        }
        return count;
    }

    private int getCompletedTasks() {
        int count = 0;
        for (ManagedExecutorService svc : executors) {
            count += svc.getCompletedTaskCount();
        }
        for (ManagedScheduledExecutorService svc : schedulers) {
            count += svc.getCompletedTaskCount();
        }
        return count;
    }

    /**
     * @return the label displayed in the menu.
     */
    @Override
    public String label() {
        return "Executors";
    }

    /**
     * @return the url of the extension page.
     */
    @Override
    public String url() {
        return "/monitor/executors";
    }

    /**
     * @return the category of the extension such as "root", "wisdom" or "OSGi".
     */
    @Override
    public String category() {
        return "wisdom";
    }
}
