package messaging.appgateway;

import messaging.requestreply.RequestReply;
import model.bank.BankInterestRequest;
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

public class LoanBrokerAppGateway extends AppGateway {

    /**
     * Maps interest requests' message id's and the loan requests' message id's together.
     */
    private Map<String, String> loanInterestMessageIDS = new HashMap<>();

    /**
     * Maps message id to loan request
     */
    private Map<String, LoanRequest> loanRequestMessageIDs = new HashMap<>();

    /**
     * Maps message id to bank interest request.
     */
    private Map<String, BankInterestRequest> bankInterestRequestMessageIDs = new HashMap<>();

    /**
     * Maps loan requests and bank interest replies
     */
    private List<RequestReply<BankInterestRequest, LoanReply>> requestReplies = new ArrayList<>();

    public Map<String, String> getLoanInterestMessageIDS() {
        return this.loanInterestMessageIDS;
    }

    public Map<String, BankInterestRequest> getBankInterestRequestMessageIDs() {
        return bankInterestRequestMessageIDs;
    }

    public List<RequestReply<BankInterestRequest, LoanReply>> getRequestReplies() {
        return this.requestReplies;
    }

    public LoanBrokerAppGateway() {
        super("InterestRequest", "LoanRequest");

        this.receiver.setListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                LoanRequest loanRequest = null;
                String loanMessageID = null;
                try {
                    loanRequest = (LoanRequest)((ObjectMessage) message).getObject();
                    loanMessageID = message.getJMSMessageID();
                    loanRequestMessageIDs.put(loanMessageID, loanRequest);
                } catch (JMSException e) {
                    e.printStackTrace();
                    return;
                }

                BankInterestRequest bankInterestRequest = new BankInterestRequest(loanRequest.getAmount(), loanRequest.getTime());

                sendBankRequest(bankInterestRequest, loanMessageID);
            }
        });
    }

    //TODO: Change so message ID doesn't have to be added as a param.
    public void sendBankRequest(BankInterestRequest request, String loanMessageID) {
        // Combine bankInterestRequest with empty remply
        RequestReply<BankInterestRequest, LoanReply> requestReply = new RequestReply<>(request, null);
        requestReplies.add(requestReply);
        setChanged();
        notifyObservers();

        Message message = sender.createObjectMessage(request);
        String interestMessageID = sender.send(message);

        if (interestMessageID != null) {
            bankInterestRequestMessageIDs.put(interestMessageID, request);
        }

        loanInterestMessageIDS.put(interestMessageID, loanMessageID);
    }
}
