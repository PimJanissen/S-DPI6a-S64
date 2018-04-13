package messaging.appgateway;

import messaging.requestreply.RequestReply;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankClientAppGateway extends AppGateway {
    private Map<RequestReply<BankInterestRequest, BankInterestReply>, String> bankInterestRequestIDS = new HashMap<>();

    /**
     * Maps loan requests and bank interest replies
     */
    private List<RequestReply<BankInterestRequest, BankInterestReply>> requestReplies = new ArrayList<>();

    public List<RequestReply<BankInterestRequest, BankInterestReply>> getRequestReplies() {
        return this.requestReplies;
    }

    public BankClientAppGateway() {
        super("InterestReply", "InterestRequest");

        this.receiver.setListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                BankInterestRequest bankInterestRequest = null;
                String messageID = null;

                try {
                    bankInterestRequest = (BankInterestRequest) ((ObjectMessage) message).getObject();
                    messageID = message.getJMSMessageID();
                } catch (JMSException e) {
                    e.printStackTrace();
                }

                RequestReply<BankInterestRequest, BankInterestReply> requestReply = new RequestReply<>(bankInterestRequest, null);

                requestReplies.add(requestReply);

                bankInterestRequestIDS.put(requestReply, messageID);

                setChanged();
                notifyObservers();
            }
        });
    }

    public void sendReply(RequestReply<BankInterestRequest, BankInterestReply> requestReply) {
        String messageID = bankInterestRequestIDS.get(requestReply);

        Message message = this.sender.createObjectMessage(requestReply.getReply(), messageID);
        this.sender.send(message);

        setChanged();
        notifyObservers();
    }
}
