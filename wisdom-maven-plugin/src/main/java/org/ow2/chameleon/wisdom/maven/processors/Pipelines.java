package org.ow2.chameleon.wisdom.maven.processors;

import org.apache.maven.execution.MavenSession;
import org.ow2.chameleon.wisdom.maven.AbstractWatcherMojo;
import org.ow2.chameleon.wisdom.maven.Watcher;
import sun.plugin2.main.server.ModalitySupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Pipeline bootstrap
 */
public class Pipelines {

//    public static Pipeline watcher() {
//        return new Pipeline()
//                .addLast(new CopyConfigurationProcessor())
//                .addLast(new CopyExternalAssetProcessor())
//                .addLast(new CopyExternalTemplateProcessor())
//                .addLast(new CopyInternalResourcesProcessor())
//                .addLast(new CoffeeScriptProcessor())
//                .addLast(new LessProcessor())
//                .addLast(new SourceCompilerProcessor())
//                .addLast(new BundlePackagerProcessor())
//                .addLast(new BundleDeployerProcessor());
//    }
//
//    public static Pipeline resourceProcessing() {
//        return new Pipeline()
//                .addLast(new CopyConfigurationProcessor())
//                .addLast(new CopyExternalAssetProcessor())
//                .addLast(new CopyExternalTemplateProcessor())
//                .addLast(new CopyInternalResourcesProcessor())
//                .addLast(new CoffeeScriptProcessor())
//                .addLast(new LessProcessor());
//    }

    public static Pipeline fromSession(MavenSession session) {
        @SuppressWarnings("unchecked")
        List<Watcher> watchers = (List<Watcher>) session.getUserProperties().get(AbstractWatcherMojo.WATCHERS_PROPERTY);
        if (watchers == null) {
            watchers = new ArrayList<>();
        }
        return new Pipeline(watchers);
    }
}
