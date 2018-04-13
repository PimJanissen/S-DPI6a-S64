package loanbroker;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import messaging.appgateway.BankBrokerAppGateway;
import messaging.appgateway.LoanBrokerAppGateway;

import java.util.*;

public class Controller implements Observer {

    private LoanBrokerAppGateway loanBrokerAppGateway;
    private BankBrokerAppGateway bankBrokerAppGateway;

    @FXML
    private ListView lvRequests;

    public Controller() {
        this.loanBrokerAppGateway = new LoanBrokerAppGateway();
        this.bankBrokerAppGateway = new BankBrokerAppGateway();

        this.bankBrokerAppGateway.setLoanBrokerAppGateway(this.loanBrokerAppGateway);

        this.loanBrokerAppGateway.addObserver(this);
        this.bankBrokerAppGateway.addObserver(this);
    }

    private void syncListView() {
        lvRequests.getItems().clear();
        lvRequests.getItems().addAll(this.loanBrokerAppGateway.getRequestReplies());
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
