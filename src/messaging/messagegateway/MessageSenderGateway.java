package messaging.messagegateway;

import javax.jms.*;
import java.io.Serializable;

public class MessageSenderGateway extends MessageGateway {
    private MessageProducer producer;

    public MessageSenderGateway(String channelName) {
        super(channelName);
        try {
            this.producer = session.createProducer(this.destination);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new object message.
     * @param body the object to send.
     * @return an object Message
     */
    public Message createObjectMessage(Serializable body) {
        return this.createObjectMessage(body, null);
    }

    /**
     * Creates a new object message.
     * @param body the object to send.
     * @param correlationID the correlation ID, which is the ID of the message this is a reply to.
     * @return an object Message
     */
    public Message createObjectMessage(Serializable body, String correlationID) {
        if (body == null) {
                throw new IllegalArgumentException("Body can not be null.");
        }

        if (correlationID != null && correlationID.trim().isEmpty()) {
            throw new IllegalArgumentException("CorrelationID cannot be an empty String. If you do not wish to enter a correlationID do not add this parameter.");
        }

        try {
            Message message = session.createObjectMessage(body);

            if (correlationID != null) {
                message.setJMSCorrelationID(correlationID);
            }

            return message;
        } catch (JMSException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String send(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null.");
        }

        try {
            this.producer.send(message);
            return message.getJMSMessageID();
        } catch (JMSException e) {
            e.printStackTrace();
            return null;
        }
    }
}
