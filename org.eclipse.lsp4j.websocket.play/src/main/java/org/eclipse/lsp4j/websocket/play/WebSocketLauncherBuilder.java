package org.eclipse.lsp4j.websocket.play;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;

import java.util.Collection;

public class WebSocketLauncherBuilder<T> extends Launcher.Builder<T> {
    private ActorRef out;
    private MessageJsonHandler jsonHandler;
    private RemoteEndpoint remoteEndpoint;

    public WebSocketLauncherBuilder<T> setOut(ActorRef out) {
        this.out = out;
        return this;
    }

    public Collection<Object> getLocalServices() {
        return localServices;
    }

    @Override
    public Launcher<T> create() {
        if (localServices == null) throw new IllegalStateException("Local service must be configured.");
        if (remoteInterfaces == null) throw new IllegalStateException("Remote interface must be configured.");

        jsonHandler = createJsonHandler();
        RemoteEndpoint remoteEndpoint = createRemoteEndpoint(jsonHandler);
        T remoteProxy = createProxy(remoteEndpoint);
        return createLauncher(null, remoteProxy, remoteEndpoint, null);
    }

    @Override
    protected RemoteEndpoint createRemoteEndpoint(MessageJsonHandler jsonHandler) {
        MessageConsumer outgoingMessageStream = new WebSocketMessageConsumer(out, jsonHandler);
        outgoingMessageStream = wrapMessageConsumer(outgoingMessageStream);
        Endpoint localEndpoint = ServiceEndpoints.toEndpoint(localServices);
        if (exceptionHandler == null) remoteEndpoint = new RemoteEndpoint(outgoingMessageStream, localEndpoint);
        else remoteEndpoint = new RemoteEndpoint(outgoingMessageStream, localEndpoint, exceptionHandler);
        jsonHandler.setMethodProvider(remoteEndpoint);
        return remoteEndpoint;
    }

    public AbstractActor.Receive createReceive() {
        return ReceiveBuilder.create().match(String.class, content -> {
            Message message = jsonHandler.parseMessage(content);
            wrapMessageConsumer(remoteEndpoint).consume(message);
        }).build();
    }
}
