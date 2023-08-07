package org.eclipse.lsp4j.websocket.play;

import akka.actor.ActorRef;
import akka.actor.ActorRef$;
import org.eclipse.lsp4j.jsonrpc.JsonRpcException;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.MessageIssueException;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.messages.Message;

public class WebSocketMessageConsumer implements MessageConsumer {

    private final ActorRef out;
    private final MessageJsonHandler jsonHandler;

    public WebSocketMessageConsumer(ActorRef out, MessageJsonHandler jsonHandler) {
        this.out = out;
        this.jsonHandler = jsonHandler;
    }

    @Override
    public void consume(Message message) throws MessageIssueException, JsonRpcException {
        String content = jsonHandler.serialize(message);
        out.$bang(content, ActorRef$.MODULE$.noSender());
    }
}
