package loanclient;

import javafx.application.Platform;
import javafx.fxml.FXML;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import messaging.appgateway.LoanClientAppGateway;
import messaging.requestreply.RequestReply;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import java.util.*;

public class Controller implements Observer{

    private LoanClientAppGateway loanClientGateway;

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

        this.loanClientGateway.applyForLoan(loanRequest);
    }

    public Controller() {
        this.loanClientGateway = new LoanClientAppGateway();
        this.loanClientGateway.addObserver(this);
    }

    private void syncListView() {
        lvRequests.getItems().clear();
        lvRequests.getItems().addAll(this.loanClientGateway.getRequestReplies());
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
