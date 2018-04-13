package messaging;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Deprecated
public class MessageSessionManager {
    private static MessageSessionManager ourInstance = new MessageSessionManager();

    public static MessageSessionManager getInstance() {
        return ourInstance;
    }

    private Connection connection; // connection to ActiveMQ
    private Session session; // session with ActiveMQ. A session is required for creating messages and producers.
    private Context jndiContext;

    private Map<String, Destination> destinations = new HashMap<>();

    private MessageSessionManager() {
        System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES","*");

        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory"); // Set this property
        props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616"); // Set this property

        // Connect to the Destination called myFirstChannel
        // queue or topic: "queue.myFirstDestination" or "topic.myFirstDestination"
        props.put(("queue.LoanRequest"), "LoanRequest");
        props.put(("queue.LoanReply"), "LoanReply");
        props.put(("queue.InterestRequest"), "InterestRequest");
        props.put(("queue.InterestReply"), "InterestReply");

        try {
            jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }

    public String sendMessage(Serializable body, String destinationÎD) {
        return sendMessage(body, destinationÎD, null);
    }

    public String sendMessage(Serializable body, String destinationID, String messageID) {
        Destination sendDestination = this.destinations.get(destinationID);
        MessageProducer producer;

        if (sendDestination == null) {
            try {
                //connect to the sender destination
                sendDestination = (Destination) jndiContext.lookup(destinationID);
                this.destinations.put(destinationID, sendDestination);
            } catch (NamingException e) {
                e.printStackTrace();
                return null;
            }
        }

        try {
            producer = session.createProducer(sendDestination);

            // Create an object message
            Message msg = session.createObjectMessage(body);

            if (messageID != null) {
                msg.setJMSCorrelationID(messageID);
            }

            // Send the message
            producer.send(msg);
            return msg.getJMSMessageID();
        } catch (JMSException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void startListening(String destinationID, MessageListener messageListener) {
        Destination receiveDestination = this.destinations.get(destinationID);
        MessageConsumer consumer;

        if (receiveDestination == null) {
            try {
                // Connect to the receiver destination
                receiveDestination = (Destination) jndiContext.lookup(destinationID);
                this.destinations.put(destinationID, receiveDestination);
            } catch (NamingException e) {
                e.printStackTrace();
                return;
            }
        }

        try {
            consumer = session.createConsumer(receiveDestination);

            // This is required to start receiving messages
            connection.start();
        } catch (JMSException e) {
            e.printStackTrace();
            return;
        }

        try {
            consumer.setMessageListener(messageListener);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void stopListening(String sender) {
        //TODO
    }
}
