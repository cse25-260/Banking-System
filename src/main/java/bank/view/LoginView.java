package bank.view;

import bank.controller.LoginController;
import bank.model.BankException;
import bank.model.Customer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/** Boundary class: login and registration screen. It only shows data and passes input to the controller. */
public class LoginView {
    private final LoginController controller;
    private final Consumer<Customer> onLoginSuccess;
    private final BorderPane root = new BorderPane();
    private final TabPane tabs = new TabPane();

    // login widgets
    private final TextField loginUser = new TextField();
    private final PasswordField loginPass = new PasswordField();
    private final Label loginStatus = new Label();

    // registration widgets
    private final ChoiceBox<String> typeChoice = new ChoiceBox<>();
    private final TextField firstName = new TextField();
    private final TextField surname = new TextField();
    private final TextField companyName = new TextField();
    private final TextField address = new TextField();
    private final TextField phone = new TextField();
    private final TextField regUser = new TextField();
    private final PasswordField regPass = new PasswordField();
    private final Label regStatus = new Label();
    private final Label firstLabel = new Label("First name");
    private final Label surnameLabel = new Label("Surname");
    private final Label companyLabel = new Label("Company name");

    public LoginView(LoginController controller, Consumer<Customer> onLoginSuccess) {
        this.controller = controller;
        this.onLoginSuccess = onLoginSuccess;

        Label title = new Label("Banking System");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1f3864;");
        Label subtitle = new Label("Sign in or create a customer profile");
        VBox header = new VBox(4, title, subtitle);
        header.setPadding(new Insets(24, 24, 8, 24));

        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(buildLoginTab(), buildRegisterTab());
        root.setTop(header);
        root.setCenter(tabs);
    }

    public Parent getRoot() { return root; }

    private Tab buildLoginTab() {
        loginUser.setPromptText("Username");
        loginPass.setPromptText("Password");
        Button loginButton = new Button("Log in");
        loginButton.setDefaultButton(true);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(e -> doLogin());
        loginPass.setOnAction(e -> doLogin());

        VBox box = new VBox(10, new Label("Username"), loginUser, new Label("Password"), loginPass,
                loginButton, loginStatus);
        box.setPadding(new Insets(24));
        return new Tab("Login", box);
    }

    private Tab buildRegisterTab() {
        typeChoice.getItems().addAll("Individual", "Company");
        typeChoice.setValue("Individual");
        typeChoice.valueProperty().addListener((obs, old, now) -> updateRegistrationFields());
        address.setPromptText("Street, city");
        phone.setPromptText("e.g. 71234567");
        regUser.setPromptText("Choose a username");
        regPass.setPromptText("At least 6 characters");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(24));
        int r = 0;
        grid.addRow(r++, new Label("Customer type"), typeChoice);
        grid.addRow(r++, firstLabel, firstName);
        grid.addRow(r++, surnameLabel, surname);
        grid.addRow(r++, companyLabel, companyName);
        grid.addRow(r++, new Label("Address"), address);
        grid.addRow(r++, new Label("Phone"), phone);
        grid.addRow(r++, new Label("Username"), regUser);
        grid.addRow(r++, new Label("Password"), regPass);

        Button register = new Button("Register");
        register.setOnAction(e -> doRegister());
        VBox box = new VBox(12, grid, register, regStatus);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(0, 24, 24, 0));
        updateRegistrationFields();
        return new Tab("Register", box);
    }

    private void updateRegistrationFields() {
        boolean individual = "Individual".equals(typeChoice.getValue());
        for (javafx.scene.Node n : new javafx.scene.Node[]{firstLabel, firstName, surnameLabel, surname}) {
            n.setVisible(individual);
            n.setManaged(individual);
        }
        for (javafx.scene.Node n : new javafx.scene.Node[]{companyLabel, companyName}) {
            n.setVisible(!individual);
            n.setManaged(!individual);
        }
    }

    private void doLogin() {
        try {
            Customer customer = controller.login(loginUser.getText(), loginPass.getText());
            loginPass.clear();
            onLoginSuccess.accept(customer);
        } catch (BankException ex) {
            show(loginStatus, ex.getMessage(), true);
        }
    }

    private void doRegister() {
        try {
            if ("Individual".equals(typeChoice.getValue())) {
                controller.registerIndividual(firstName.getText(), surname.getText(), address.getText(),
                        phone.getText(), regUser.getText(), regPass.getText());
            } else {
                controller.registerCompany(companyName.getText(), address.getText(),
                        phone.getText(), regUser.getText(), regPass.getText());
            }
            show(regStatus, "Registration successful. You can now log in.", false);
            loginUser.setText(regUser.getText());
            regPass.clear();
            tabs.getSelectionModel().select(0);
            show(loginStatus, "Registration successful. Please log in.", false);
        } catch (BankException ex) {
            show(regStatus, ex.getMessage(), true);
        }
    }

    private void show(Label label, String message, boolean error) {
        label.setText(message);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: " + (error ? "#b00020" : "#1b7a2f") + ";");
    }
}
