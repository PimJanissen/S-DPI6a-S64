package messaging.appgateway;

import messaging.messagegateway.MessageReceiverGateway;
import messaging.messagegateway.MessageSenderGateway;

import java.util.Observable;

/**
 * Gateway which handles JMS communication within the application
 */
public abstract class AppGateway extends Observable{
    protected MessageSenderGateway sender;
    protected MessageReceiverGateway receiver;

    public AppGateway(String channelNameSend, String channelNameReceive) {
        if (channelNameSend == null || channelNameSend.trim().isEmpty()) {
            throw new IllegalArgumentException("ChannelNameSend cannot be null nor an empty String.");
        }
        if (channelNameReceive == null || channelNameReceive.trim().isEmpty()) {
            throw new IllegalArgumentException("ChannelNameReceive cannot be null nor an empty String.");
        }
        this.sender = new MessageSenderGateway(channelNameSend);
        this.receiver = new MessageReceiverGateway(channelNameReceive);
    }
}
