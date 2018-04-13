package bank.abnamro;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import messaging.appgateway.BankClientAppGateway;
import messaging.requestreply.RequestReply;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;

import java.util.*;

public class Controller implements Observer {

    private BankClientAppGateway bankClientAppGateway;

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
        requestReply.setReply(bankInterestReply);

        this.bankClientAppGateway.sendReply(requestReply);
    }

    public Controller() {
        this.bankClientAppGateway = new BankClientAppGateway();
        this.bankClientAppGateway.addObserver(this);
    }

    private void syncListView() {
        lvRequests.getItems().clear();
        lvRequests.getItems().addAll(this.bankClientAppGateway.getRequestReplies());
    }

    @Override
    public void update(Observable o, Object arg) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                syncListView();
            }
        });
    }
}
