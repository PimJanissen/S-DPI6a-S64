package loanbroker;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import messaging.MessageSessionManager;
import messaging.requestreply.RequestReply;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.*;

public class Controller {

    private MessageSessionManager messageSessionManager = MessageSessionManager.getInstance();

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

    @FXML
    private ListView lvRequests;

    public Controller() {
        receiveLoanMessages();
        receiveBankInterestMessages();
    }

    /**
     * 1. Get a loan request
     * 2. Store loan request with it's message id
     * 3. Store requestreply for loan request + bank interest reply
     * 4. Sync listview
     * 5. Create bank interest request
     * 6. Send bank interest request
     * 7. Store bank interest request + message id
     * 8. Map loan request message id with bank interest request message id
     */
    public void receiveLoanMessages() {
       messageSessionManager.startListening("LoanRequest", new MessageListener() {
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

               RequestReply<BankInterestRequest, LoanReply> requestReply = new RequestReply<>(bankInterestRequest, null);
               requestReplies.add(requestReply);
               syncListView();

               String interestMessageID = messageSessionManager.sendMessage(bankInterestRequest, "InterestRequest");

               if (interestMessageID != null) {
                   bankInterestRequestMessageIDs.put(interestMessageID, bankInterestRequest);
               }

                loanInterestMessageIDS.put(interestMessageID, loanMessageID);
           }
       });
    }

    /**
     * 1. Get correlation message id
     * 2. Find correlation message id for loan
     * 3. Send Loan Reply with loan correlation message id
     * 4. Store Loan Reply in requestreplies
     * 5. Update listview?
     */
    public void receiveBankInterestMessages() {
        messageSessionManager.startListening("InterestReply", new MessageListener() {
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

                String messageIDLoan = loanInterestMessageIDS.get(messageIDInterest);
                BankInterestRequest bankInterestRequest = bankInterestRequestMessageIDs.get(messageIDInterest);
                LoanReply loanReply = new LoanReply(bankInterestReply.getInterest(), bankInterestReply.getQuoteId());

                RequestReply<BankInterestRequest, LoanReply> requestReply = requestReplies.stream().filter(rr -> rr.getRequest().equals(bankInterestRequest)).findFirst().get();
                requestReply.setReply(loanReply);

                messageSessionManager.sendMessage(loanReply, "LoanReply", messageIDLoan);

                syncListView();
            }
        });
    }

    private void syncListView() {
        lvRequests.getItems().clear();
        lvRequests.getItems().addAll(this.requestReplies);
    }
}
