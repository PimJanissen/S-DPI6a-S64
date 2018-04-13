package messaging.messagegateway;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public abstract class MessageGateway {
    protected Connection connection;
    protected Session session;
    protected Context jndiContext;
    protected Destination destination;

    public MessageGateway(String channelName) {
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
            this.jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
            this.connection = connectionFactory.createConnection();
            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            this.destination = (Destination) jndiContext.lookup(channelName);
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }
}
