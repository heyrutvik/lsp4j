package org.eclipse.lsp4j.websocket.play;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import org.eclipse.lsp4j.jsonrpc.Launcher;

import java.util.Collection;

public abstract class WebSocketActor<T> extends AbstractActor {
    private final ActorRef out;
    private final WebSocketLauncherBuilder<T> builder;

    public WebSocketActor(ActorRef out) {
        this.out = out;
        this.builder = new WebSocketLauncherBuilder<>();
        builder.setOut(this.out);
        configure(this.builder);
        Launcher<T> launcher = builder.create();
        connect(builder.getLocalServices(), launcher.getRemoteProxy());
    }

    @Override
    public Receive createReceive() {
        return builder.createReceive();
    }

    protected abstract void configure(Launcher.Builder<T> builder);

    protected void connect(Collection<Object> localServices, T remoteProxy) {
    }
}
