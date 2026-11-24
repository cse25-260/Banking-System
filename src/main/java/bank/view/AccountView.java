package bank.view;

import bank.controller.AccountController;
import bank.model.Account;
import bank.model.BankException;
import bank.model.Transaction;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

/**
 * Boundary class: the customer's accounts screen. It contains NO business logic:
 * every action is handed to AccountController and the result is displayed.
 */
public class AccountView {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private interface Action { String run() throws BankException; }

    private final AccountController controller;
    private final BorderPane root = new BorderPane();
    private final ObservableList<Account> accountItems = FXCollections.observableArrayList();
    private final ObservableList<Transaction> transactionItems = FXCollections.observableArrayList();
    private final TableView<Account> accountTable = new TableView<>(accountItems);
    private final TableView<Transaction> transactionTable = new TableView<>(transactionItems);
    private final Label totalLabel = new Label();
    private final Label statusLabel = new Label("Select an account to get started.");

    private final TextField amountField = new TextField();
    private final ChoiceBox<String> typeChoice = new ChoiceBox<>();
    private final TextField openingDeposit = new TextField();
    private final TextField employerName = new TextField();
    private final TextField employerAddress = new TextField();
    private final VBox employerBox = new VBox(6);

    public AccountView(AccountController controller, Runnable onLogout) {
        this.controller = controller;

        Label welcome = new Label("Welcome, " + controller.getCustomer().getDisplayName());
        welcome.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f3864;");
        totalLabel.setStyle("-fx-font-size: 15px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button logout = new Button("Log out");
        logout.setOnAction(e -> onLogout.run());
        HBox top = new HBox(16, welcome, spacer, totalLabel, logout);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(14, 16, 14, 16));
        top.setStyle("-fx-background-color: #eef2f9;");

        buildAccountTable();
        buildTransactionTable();
        Label accountsTitle = new Label("Your accounts");
        accountsTitle.setStyle("-fx-font-weight: bold;");
        Label historyTitle = new Label("Transaction history (selected account)");
        historyTitle.setStyle("-fx-font-weight: bold;");
        VBox.setVgrow(accountTable, Priority.ALWAYS);
        VBox.setVgrow(transactionTable, Priority.ALWAYS);
        VBox center = new VBox(6, accountsTitle, accountTable, historyTitle, transactionTable);
        center.setPadding(new Insets(12));

        statusLabel.setPadding(new Insets(8, 16, 8, 16));
        statusLabel.setWrapText(true);

        root.setTop(top);
        root.setCenter(center);
        root.setRight(buildActions());
        root.setBottom(statusLabel);

        accountTable.getSelectionModel().selectedItemProperty().addListener((obs, old, now) -> loadHistory());
        refreshAll();
    }

    public Parent getRoot() { return root; }

    // ------------------------------------------------------------------ layout

    private void buildAccountTable() {
        accountTable.getColumns().add(column("Account No.", 130, a -> a.getAccountNumber()));
        accountTable.getColumns().add(column("Type", 100, a -> a.getTypeLabel()));
        accountTable.getColumns().add(column("Branch", 130, a -> a.getBranch()));
        accountTable.getColumns().add(column("Balance", 130, a -> String.format("BWP %,.2f", a.getBalance())));
        accountTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        accountTable.setPlaceholder(new Label("No accounts yet. Open one on the right."));
    }

    private void buildTransactionTable() {
        transactionTable.getColumns().add(txColumn("Date", 150, t -> t.getTimestamp().format(DATE_FORMAT)));
        transactionTable.getColumns().add(txColumn("Type", 100, t -> t.getType()));
        transactionTable.getColumns().add(txColumn("Amount", 110, t -> String.format("BWP %,.2f", t.getAmount())));
        transactionTable.getColumns().add(txColumn("Balance after", 120, t -> String.format("BWP %,.2f", t.getBalanceAfter())));
        transactionTable.getColumns().add(txColumn("Description", 200, t -> t.getDescription()));
        transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        transactionTable.setPlaceholder(new Label("No transactions to show."));
    }

    private VBox buildActions() {
        // Deposit / withdraw
        amountField.setPromptText("Amount (BWP)");
        Button deposit = new Button("Deposit");
        Button withdraw = new Button("Withdraw");
        deposit.setOnAction(e -> withSelectedAccount(acc -> "Deposit successful. New balance: BWP "
                + String.format("%,.2f", controller.deposit(acc.getAccountNumber(), amountField.getText()))));
        withdraw.setOnAction(e -> withSelectedAccount(acc -> "Withdrawal successful. New balance: BWP "
                + String.format("%,.2f", controller.withdraw(acc.getAccountNumber(), amountField.getText()))));
        TitledPane moneyPane = new TitledPane("Deposit / Withdraw",
                new VBox(8, amountField, new HBox(8, deposit, withdraw)));
        moneyPane.setCollapsible(false);

        // Open account
        typeChoice.getItems().addAll("Savings", "Investment", "Cheque");
        typeChoice.setValue("Savings");
        openingDeposit.setPromptText("Opening deposit (BWP)");
        employerName.setPromptText("Employer name");
        employerAddress.setPromptText("Employer address");
        employerBox.getChildren().addAll(new Label("Employer details (Cheque only)"), employerName, employerAddress);
        employerBox.setVisible(false);
        employerBox.setManaged(false);
        typeChoice.valueProperty().addListener((obs, old, now) -> {
            boolean cheque = "Cheque".equals(now);
            employerBox.setVisible(cheque);
            employerBox.setManaged(cheque);
        });
        Button open = new Button("Open account");
        open.setOnAction(e -> run(() -> {
            Account a = controller.openAccount(typeChoice.getValue(), openingDeposit.getText(),
                    employerName.getText(), employerAddress.getText());
            openingDeposit.clear();
            return a.getTypeLabel() + " account " + a.getAccountNumber() + " opened.";
        }));
        TitledPane openPane = new TitledPane("Open a new account",
                new VBox(8, typeChoice, openingDeposit, employerBox, open));
        openPane.setCollapsible(false);

        // Interest
        Button interest = new Button("Pay monthly interest");
        interest.setOnAction(e -> run(() -> {
            double paid = controller.payInterestOnAllAccounts();
            return String.format("Interest paid: BWP %,.2f in total.", paid);
        }));
        TitledPane interestPane = new TitledPane("Interest", new VBox(8,
                new Label("Investment 5% and Savings 0.05% per month"), interest));
        interestPane.setCollapsible(false);

        VBox box = new VBox(12, moneyPane, openPane, interestPane);
        box.setPadding(new Insets(12));
        box.setPrefWidth(300);
        return box;
    }

    // ----------------------------------------------------------------- helpers

    private void withSelectedAccount(AccountAction action) {
        Account selected = accountTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Please select an account first.", true);
            return;
        }
        run(() -> action.run(selected));
    }

    private interface AccountAction { String run(Account account) throws BankException; }

    private void run(Action action) {
        try {
            showStatus(action.run(), false);
        } catch (BankException ex) {
            showStatus(ex.getMessage(), true);
        }
        refreshAll();
    }

    private void refreshAll() {
        Account selected = accountTable.getSelectionModel().getSelectedItem();
        String selectedNumber = selected == null ? null : selected.getAccountNumber();
        accountItems.setAll(controller.getAccounts());
        totalLabel.setText(String.format("Total balance: BWP %,.2f", controller.getTotalBalance()));
        if (selectedNumber != null) {
            for (Account a : accountItems) {
                if (a.getAccountNumber().equals(selectedNumber)) {
                    accountTable.getSelectionModel().select(a);
                    break;
                }
            }
        }
        loadHistory();
    }

    private void loadHistory() {
        Account selected = accountTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            transactionItems.clear();
            return;
        }
        try {
            transactionItems.setAll(controller.getTransactions(selected.getAccountNumber()));
        } catch (BankException ex) {
            showStatus(ex.getMessage(), true);
        }
    }

    private void showStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + (error ? "#b00020" : "#1b7a2f") + ";");
    }

    private interface Getter<T> { String get(T item); }

    private TableColumn<Account, String> column(String title, double width, Getter<Account> getter) {
        TableColumn<Account, String> col = new TableColumn<>(title);
        col.setPrefWidth(width);
        col.setCellValueFactory(cell -> new ReadOnlyStringWrapper(getter.get(cell.getValue())));
        return col;
    }

    private TableColumn<Transaction, String> txColumn(String title, double width, Getter<Transaction> getter) {
        TableColumn<Transaction, String> col = new TableColumn<>(title);
        col.setPrefWidth(width);
        col.setCellValueFactory(cell -> new ReadOnlyStringWrapper(getter.get(cell.getValue())));
        return col;
    }
}
