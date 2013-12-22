package org.wisdom.router;

import org.apache.felix.ipojo.annotations.*;
import org.wisdom.api.Controller;
import org.wisdom.api.annotations.Closed;
import org.wisdom.api.annotations.OnMessage;
import org.wisdom.api.annotations.Opened;
import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.http.websockets.Publisher;
import org.wisdom.api.http.websockets.WebSocketDispatcher;
import org.wisdom.api.http.websockets.WebSocketListener;
import org.wisdom.api.router.RouteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Component handling web socket frame routing.
 */
@Component(immediate = true)
@Provides(specifications = Publisher.class)
@Instantiate(name = "WebSocketRouter")
public class WebSocketRouter implements WebSocketListener, Publisher {

    protected static Logger logger = LoggerFactory.getLogger(WebSocketRouter.class);
    @Requires
    private WebSocketDispatcher dispatcher;
    private List<DefaultWebSocketCallback> opens = new ArrayList<>();
    private List<DefaultWebSocketCallback> closes = new ArrayList<>();
    private List<OnMessageWebSocketCallback> listeners = new ArrayList<>();

    @Requires(optional=true)
    private ContentEngine engine;

    @Validate
    public void start() {
        dispatcher.register(this);
    }

    @Invalidate
    public void stop() {
        dispatcher.unregister(this);
    }

    @Bind(aggregate = true)
    public synchronized void bindController(Controller controller) {
        analyze(controller);
    }

    /**
     * Extracts all the annotation from the controller's method
     *
     * @param controller the controller to analyze
     */
    private void analyze(Controller controller) {
        String prefix = RouteUtils.getPath(controller);
        Method[] methods = controller.getClass().getMethods();
        for (Method method : methods) {
            Opened open = method.getAnnotation(Opened.class);
            Closed close = method.getAnnotation(Closed.class);
            OnMessage on = method.getAnnotation(OnMessage.class);
            if (open != null) {

                DefaultWebSocketCallback callback = new DefaultWebSocketCallback(controller, method,
                        RouteUtils.getPrefixedUri(prefix, open.value()));
                if (callback.check()) {
                    opens.add(callback);
                }
            }
            if (close != null) {
                DefaultWebSocketCallback callback = new DefaultWebSocketCallback(controller, method,
                        RouteUtils.getPrefixedUri(prefix, close.value()));
                if (callback.check()) {
                    closes.add(callback);
                }
            }
            if (on != null) {
                OnMessageWebSocketCallback callback = new OnMessageWebSocketCallback(controller, method,
                        RouteUtils.getPrefixedUri(prefix, on.value()));
                if (callback.check()) {
                    listeners.add(callback);
                }
            }
        }

    }

    @Unbind
    public synchronized void unbindController(Controller controller) {
        List<DefaultWebSocketCallback> toRemove = new ArrayList<>();
        for (DefaultWebSocketCallback open : opens) {
            if (open.controller == controller) {
                toRemove.add(open);
            }
        }
        opens.removeAll(toRemove);

        toRemove.clear();
        for (DefaultWebSocketCallback close : closes) {
            if (close.controller == controller) {
                toRemove.add(close);
            }
        }
        closes.removeAll(toRemove);

        toRemove.clear();
        for (DefaultWebSocketCallback callback : listeners) {
            if (callback.controller == controller) {
                toRemove.add(callback);
            }
        }
        listeners.removeAll(toRemove);
    }

    @Override
    public void received(String uri, byte[] content) {
        for (OnMessageWebSocketCallback listener : listeners) {
            if (listener.matches(uri)) {
                try {
                    listener.invoke(uri, content, engine);
                } catch (InvocationTargetException e) { //NOSONAR
                    logger.error("An error occurred in the @OnMessage callback {}#{} : {}",
                            listener.controller.getClass().getName(), listener.method.getName
                            (), e.getTargetException().getMessage(), e.getTargetException());
                } catch (Exception e) {
                    logger.error("An error occurred in the @OnMessage callback {}#{} : {}",
                            listener.controller.getClass().getName(), listener.method.getName(), e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void opened(String uri) {
        for (DefaultWebSocketCallback open : opens) {
            if (open.matches(uri)) {
                try {
                    open.invoke(uri);
                } catch (InvocationTargetException e) { //NOSONAR
                    logger.error("An error occurred in the @Open callback {}#{} : {}",
                            open.controller.getClass().getName(), open.method.getName
                            (), e.getTargetException().getMessage(), e.getTargetException());
                } catch (Exception e) {
                    logger.error("An error occurred in the @Open callback {}#{} : {}",
                            open.controller.getClass().getName(), open.method.getName(), e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void closed(String uri) {
        for (DefaultWebSocketCallback close : closes) {
            if (close.matches(uri)) {
                try {
                    close.invoke(uri);
                } catch (InvocationTargetException e) { //NOSONAR
                    logger.error("An error occurred in the @Close callback {}#{} : {}",
                            close.controller.getClass().getName(), close.method.getName
                            (), e.getTargetException().getMessage(), e.getTargetException());
                } catch (Exception e) {
                    logger.error("An error occurred in the @Close callback {}#{} : {}",
                            close.controller.getClass().getName(), close.method.getName(), e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void publish(String uri, String message) {
        dispatcher.publish(uri, message);
    }

    @Override
    public void publish(String uri, byte[] message) {
        dispatcher.publish(uri, message);
    }
}
