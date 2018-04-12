package bank.abnamro;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import messaging.MessageSessionManager;
import messaging.requestreply.RequestReply;
import model.bank.BankInterestReply;
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

public class Controller {

    private MessageSessionManager messageSessionManager = MessageSessionManager.getInstance();

    private Map<RequestReply<BankInterestRequest, BankInterestReply>, String> bankInterestRequestIDS = new HashMap<>();
    /**
     * Maps loan requests and bank interest replies
     */
    private List<RequestReply<BankInterestRequest, BankInterestReply>> requestReplies = new ArrayList<>();

    @FXML
    private ListView lvRequests;

    @FXML
    private TextField txtReply;

    @FXML
    private Button btnSendReply;

    @FXML
    protected void handleBtnSendReplyAction(ActionEvent event) {
        RequestReply<BankInterestRequest, BankInterestReply> requestReply = (RequestReply<BankInterestRequest, BankInterestReply>)lvRequests.getSelectionModel().getSelectedItems().get(0);

        BankInterestReply bankInterestReply = new BankInterestReply(Integer.parseInt(txtReply.getText()), "ABNAMRO");
        String messageID = bankInterestRequestIDS.get(requestReply);
        requestReply.setReply(bankInterestReply);
        this.syncListView();
        sendMessage(bankInterestReply, messageID);
    }

    public Controller() {
        this.receiveMessages();
    }

    private void sendMessage(BankInterestReply reply, String messageID) {
        this.messageSessionManager.sendMessage(reply, "InterestReply", messageID);
    }

    private void receiveMessages() {
        this.messageSessionManager.startListening("InterestRequest", new MessageListener() {
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

                syncListView();
            }
        });
    }

    private void syncListView() {
        lvRequests.getItems().clear();
        lvRequests.getItems().addAll(this.requestReplies);
    }
}
