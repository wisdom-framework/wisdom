package org.ow2.chameleon.wisdom.maven;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Classes implementing by Mojos willing to participate to the <em>Watch mode</em>
 */
public abstract class AbstractWatcherMojo extends AbstractMojo implements Watcher {


    public static final String WATCHERS_PROPERTY = "watchers";
    /**
     * The current build session instance.
     */
    @Component
    private MavenSession session;
    @Component
    private MojoExecution execution;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        addWatcher();
    }

    public void setSession(MavenSession session) {
        getLog().info("Injecting session !");
        this.session = session;
        addWatcher();
    }

    public void addWatcher(Watcher watcher) {
        watchers().add(watcher);
    }

    public void removeWatcher(Watcher watcher) {
        watchers().remove(watcher);
    }

    public boolean containsWatcher(Watcher watcher) {
        return watchers().contains(watcher);
    }

    public void addWatcher() {
        addWatcher(this);
    }

    public MavenSession session() {
        return session;
    }

    public MojoExecution execution() {
        return execution;
    }

    private List<Watcher> watchers() {
        @SuppressWarnings("unchecked")
        List<Watcher> watchers = (List<Watcher>) session.getUserProperties().get(WATCHERS_PROPERTY);

        if (watchers == null) {
            watchers = new ArrayList<>();
            session.getUserProperties().put(WATCHERS_PROPERTY, watchers);
        }

        return watchers;
    }


}
