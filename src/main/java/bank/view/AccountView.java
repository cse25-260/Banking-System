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
import javafx.scene.control.TableCell;
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
    private interface AccountAction { String run(Account account) throws BankException; }
    private interface Getter<T> { String get(T item); }

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

        // ---- top bar ----
        Label welcome = new Label("Welcome, " + controller.getCustomer().getDisplayName());
        welcome.getStyleClass().add("welcome-label");
        Label welcomeSub = new Label(controller.getCustomer().getCustomerType().equals("COMPANY")
                ? "Company account holder" : "Personal account holder");
        welcomeSub.getStyleClass().add("welcome-sub");
        VBox welcomeBox = new VBox(2, welcome, welcomeSub);

        Label chipCaption = new Label("TOTAL BALANCE");
        chipCaption.getStyleClass().add("chip-caption");
        totalLabel.getStyleClass().add("chip-amount");
        VBox balanceChip = new VBox(2, chipCaption, totalLabel);
        balanceChip.getStyleClass().add("balance-chip");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button logout = new Button("Log out");
        logout.getStyleClass().add("ghost");
        logout.setOnAction(e -> onLogout.run());
        HBox top = new HBox(18, welcomeBox, spacer, balanceChip, logout);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getStyleClass().add("top-bar");

        // ---- centre: tables ----
        buildAccountTable();
        buildTransactionTable();
        Label accountsTitle = new Label("Your accounts");
        accountsTitle.getStyleClass().add("section-title");
        Label historyTitle = new Label("Transaction history");
        historyTitle.getStyleClass().add("section-title");
        Label historyHint = new Label("Click an account above to see its transactions");
        historyHint.getStyleClass().add("hint");
        HBox historyHeader = new HBox(10, historyTitle, historyHint);
        historyHeader.setAlignment(Pos.BASELINE_LEFT);
        VBox.setVgrow(accountTable, Priority.ALWAYS);
        VBox.setVgrow(transactionTable, Priority.ALWAYS);
        VBox center = new VBox(8, accountsTitle, accountTable, historyHeader, transactionTable);
        center.setPadding(new Insets(20, 12, 16, 24));

        // ---- bottom status bar ----
        statusLabel.getStyleClass().add("status-info");
        statusLabel.setWrapText(true);
        HBox statusBar = new HBox(statusLabel);
        statusBar.getStyleClass().add("status-bar");

        root.setTop(top);
        root.setCenter(center);
        root.setRight(buildActions());
        root.setBottom(statusBar);

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
        accountTable.setPlaceholder(new Label("No accounts yet. Open one using the panel on the right."));
    }

    private void buildTransactionTable() {
        transactionTable.getColumns().add(txColumn("Date", 150, t -> t.getTimestamp().format(DATE_FORMAT)));
        TableColumn<Transaction, String> typeCol = txColumn("Type", 110, t -> t.getType());
        typeCol.setCellFactory(col -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("tx-deposit", "tx-withdrawal", "tx-interest");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    getStyleClass().add("tx-" + item.toLowerCase());
                }
            }
        });
        transactionTable.getColumns().add(typeCol);
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
        deposit.getStyleClass().add("primary");
        Button withdraw = new Button("Withdraw");
        withdraw.getStyleClass().add("secondary");
        deposit.setMaxWidth(Double.MAX_VALUE);
        withdraw.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(deposit, Priority.ALWAYS);
        HBox.setHgrow(withdraw, Priority.ALWAYS);
        deposit.setOnAction(e -> withSelectedAccount(acc -> "Deposit successful. New balance: BWP "
                + String.format("%,.2f", controller.deposit(acc.getAccountNumber(), amountField.getText()))));
        withdraw.setOnAction(e -> withSelectedAccount(acc -> "Withdrawal successful. New balance: BWP "
                + String.format("%,.2f", controller.withdraw(acc.getAccountNumber(), amountField.getText()))));
        TitledPane moneyPane = card("Deposit / Withdraw",
                new VBox(10, amountField, new HBox(8, deposit, withdraw)));

        // Open account
        typeChoice.getItems().addAll("Savings", "Investment", "Cheque");
        typeChoice.setValue("Savings");
        typeChoice.setMaxWidth(Double.MAX_VALUE);
        openingDeposit.setPromptText("Opening deposit (BWP)");
        employerName.setPromptText("Employer name");
        employerAddress.setPromptText("Employer address");
        Label employerLabel = new Label("Employer details (Cheque only)");
        employerLabel.getStyleClass().add("field-label");
        employerBox.getChildren().addAll(employerLabel, employerName, employerAddress);
        employerBox.setVisible(false);
        employerBox.setManaged(false);
        typeChoice.valueProperty().addListener((obs, old, now) -> {
            boolean cheque = "Cheque".equals(now);
            employerBox.setVisible(cheque);
            employerBox.setManaged(cheque);
        });
        Button open = new Button("Open account");
        open.getStyleClass().add("primary");
        open.setMaxWidth(Double.MAX_VALUE);
        open.setOnAction(e -> run(() -> {
            Account a = controller.openAccount(typeChoice.getValue(), openingDeposit.getText(),
                    employerName.getText(), employerAddress.getText());
            openingDeposit.clear();
            return a.getTypeLabel() + " account " + a.getAccountNumber() + " opened.";
        }));
        TitledPane openPane = card("Open a new account",
                new VBox(10, typeChoice, openingDeposit, employerBox, open));

        // Interest
        Label rates = new Label("Investment earns 5% and Savings 0.05% per month.");
        rates.getStyleClass().add("hint");
        rates.setWrapText(true);
        Button interest = new Button("Pay monthly interest");
        interest.getStyleClass().add("accent");
        interest.setMaxWidth(Double.MAX_VALUE);
        interest.setOnAction(e -> run(() -> {
            double paid = controller.payInterestOnAllAccounts();
            return String.format("Interest paid: BWP %,.2f in total.", paid);
        }));
        TitledPane interestPane = card("Interest", new VBox(10, rates, interest));

        VBox box = new VBox(14, moneyPane, openPane, interestPane);
        box.setPadding(new Insets(20, 24, 16, 12));
        box.setPrefWidth(320);
        box.setMinWidth(320);
        return box;
    }

    private TitledPane card(String title, VBox content) {
        TitledPane pane = new TitledPane(title, content);
        pane.setCollapsible(false);
        pane.getStyleClass().add("card");
        return pane;
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
        totalLabel.setText(String.format("BWP %,.2f", controller.getTotalBalance()));
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
        statusLabel.getStyleClass().removeAll("status-success", "status-error", "status-info");
        statusLabel.getStyleClass().add(error ? "status-error" : "status-success");
    }

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
