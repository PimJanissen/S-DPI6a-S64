package messaging.appgateway;

import messaging.requestreply.RequestReply;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import model.loan.LoanReply;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

public class BankBrokerAppGateway extends AppGateway {
    // Might want to refactor this to having a single entity holding all request/reply keypairs.
    private LoanBrokerAppGateway loanBrokerAppGateway;

    public BankBrokerAppGateway() {
        super("LoanReply", "InterestReply");

        this.receiver.setListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                String messageIDInterest = null;
                BankInterestReply bankInterestReply = null;

                try {
                    messageIDInterest = message.getJMSCorrelationID();
                    bankInterestReply = (BankInterestReply) ((ObjectMessage) message).getObject();
                } catch (JMSException e) {
                    e.printStackTrace();
                }

                String messageIDLoan = loanBrokerAppGateway.getLoanInterestMessageIDS().get(messageIDInterest);
                BankInterestRequest bankInterestRequest = loanBrokerAppGateway.getBankInterestRequestMessageIDs().get(messageIDInterest);
                LoanReply loanReply = new LoanReply(bankInterestReply.getInterest(), bankInterestReply.getQuoteId());

                RequestReply<BankInterestRequest, LoanReply> requestReply = loanBrokerAppGateway.getRequestReplies().stream().filter(rr -> rr.getRequest().equals(bankInterestRequest)).findFirst().get();
                requestReply.setReply(loanReply);

                sendLoanReply(loanReply, messageIDLoan);

                setChanged();
                notifyObservers();
            }
        });
    }

    public void setLoanBrokerAppGateway(LoanBrokerAppGateway loanBrokerAppGateway) {
        if (loanBrokerAppGateway == null) {
            throw new IllegalArgumentException("BankBrokerAppGateway cannot be null.");
        }

        this.loanBrokerAppGateway = loanBrokerAppGateway;
    }

    public void sendLoanReply(LoanReply loanReply, String messageIDLoan) {
        if (loanReply == null) {
            throw new IllegalArgumentException("LoanReply can not be null.");
        }

        if (messageIDLoan == null || messageIDLoan.trim().isEmpty()) {
            throw new IllegalArgumentException("MessageIDLoan can not be null nor an empty String (spaces do not count).");
        }

        Message msg = sender.createObjectMessage(loanReply, messageIDLoan);
        sender.send(msg);
    }
}
