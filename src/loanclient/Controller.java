package loanclient;

import javafx.fxml.FXML;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import messaging.MessageSessionManager;
import messaging.requestreply.RequestReply;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import java.io.Serializable;
import java.util.*;

public class Controller {

    private MessageSessionManager messageSessionManager = MessageSessionManager.getInstance();
    private Map<String, RequestReply<LoanRequest, LoanReply>> requestReplies = new HashMap<>();

    @FXML
    private TextField txtSSN;

    @FXML
    private TextField txtAmount;

    @FXML
    private TextField txtTime;

    @FXML
    private Button btnSendRequest;

    @FXML
    private ListView lvRequests;

    @FXML
    protected void handleBtnSendRequestAction(ActionEvent event) {
        LoanRequest loanRequest = new LoanRequest(Integer.parseInt(txtSSN.getText()), Integer.parseInt(txtAmount.getText()), Integer.parseInt(txtTime.getText()));
        sendMessage(loanRequest);
    }

    public Controller() {
        this.receiveMessages();
    }

    public void sendMessage(LoanRequest loanRequest) {
        RequestReply<LoanRequest, LoanReply> requestReply = new RequestReply<>(loanRequest, null);

        String messageID = messageSessionManager.sendMessage(loanRequest, "LoanRequest");
        if (messageID != null) {
            requestReplies.put(messageID, requestReply);
            this.syncListView();
        }
    }

    public void receiveMessages() {
        this.messageSessionManager.startListening("LoanReply", new MessageListener() {
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
                        syncListView();
                    }
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void syncListView() {
        lvRequests.getItems().clear();
        lvRequests.getItems().addAll(this.requestReplies);
    }
}
