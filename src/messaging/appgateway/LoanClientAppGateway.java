package messaging.appgateway;

import messaging.requestreply.RequestReply;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoanClientAppGateway extends AppGateway {

    private Map<String, RequestReply<LoanRequest, LoanReply>> requestReplies = new HashMap<>();

    public LoanClientAppGateway() {
        super("LoanRequest", "LoanReply");

        this.receiver.setListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {

                LoanReply loanReply = null;

                try {
                    loanReply = (LoanReply) ((ObjectMessage) message).getObject();

                    // Correlation ID needs to be the same as the message ID from the request
                    String correlationID = message.getJMSCorrelationID();

                    RequestReply<LoanRequest, LoanReply> requestReply = requestReplies.get(correlationID);

                    if (requestReply != null) {
                        requestReply.setReply(loanReply);
                        setChanged();
                        notifyObservers();
                    }
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public List<RequestReply<LoanRequest, LoanReply>> getRequestReplies() {
        return new ArrayList<>((this.requestReplies.values()));
    }

    public void applyForLoan(LoanRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null.");
        }

        RequestReply<LoanRequest, LoanReply> requestReply = new RequestReply<>(request, null);

        Message message = this.sender.createObjectMessage(request);
        String messageID = this.sender.send(message);

        if (messageID != null) {
            requestReplies.put(messageID, requestReply);
        }

        this.setChanged();
        this.notifyObservers();
    }
}
