package messaging.messagegateway;

import javax.jms.*;

public class MessageReceiverGateway extends MessageGateway {
    private MessageConsumer consumer;

    public MessageReceiverGateway(String channelName) {
        super(channelName);

        try {
            this.consumer = this.session.createConsumer(this.destination);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void setListener(MessageListener listener) {
        try {
            this.connection.start();
            this.consumer.setMessageListener(listener);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
