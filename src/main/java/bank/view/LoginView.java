package bank.view;

import bank.controller.LoginController;
import bank.model.BankException;
import bank.model.Customer;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
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
    private final Label firstLabel = fieldLabel("First name");
    private final Label surnameLabel = fieldLabel("Surname");
    private final Label companyLabel = fieldLabel("Company name");

    public LoginView(LoginController controller, Consumer<Customer> onLoginSuccess) {
        this.controller = controller;
        this.onLoginSuccess = onLoginSuccess;

        Label title = new Label("Owen National Bank");
        title.getStyleClass().add("app-title");
        Label subtitle = new Label("Secure banking for individuals and companies");
        subtitle.getStyleClass().add("app-subtitle");
        VBox header = new VBox(6, title, subtitle);
        header.getStyleClass().add("app-header");

        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(buildLoginTab(), buildRegisterTab());

        VBox.setVgrow(tabs, Priority.ALWAYS);
        VBox card = new VBox(tabs);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(8, 4, 4, 4));
        BorderPane.setMargin(card, new Insets(22, 28, 28, 28));

        root.setTop(header);
        root.setCenter(card);
    }

    public Parent getRoot() { return root; }

    private Tab buildLoginTab() {
        loginUser.setPromptText("Enter your username");
        loginPass.setPromptText("Enter your password");
        Button loginButton = new Button("Log in");
        loginButton.getStyleClass().add("primary");
        loginButton.setDefaultButton(true);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(e -> doLogin());
        loginPass.setOnAction(e -> doLogin());

        Label welcome = new Label("Welcome back");
        welcome.getStyleClass().add("section-title");
        Label hint = new Label("Log in to view your accounts and make transactions.");
        hint.getStyleClass().add("hint");
        hint.setWrapText(true);

        VBox box = new VBox(8, welcome, hint, spacer(6), fieldLabel("Username"), loginUser,
                fieldLabel("Password"), loginPass, spacer(6), loginButton, loginStatus);
        box.setPadding(new Insets(20, 24, 24, 24));
        return new Tab("Login", box);
    }

    private Tab buildRegisterTab() {
        typeChoice.getItems().addAll("Individual", "Company");
        typeChoice.setValue("Individual");
        typeChoice.setMaxWidth(Double.MAX_VALUE);
        typeChoice.valueProperty().addListener((obs, old, now) -> updateRegistrationFields());
        firstName.setPromptText("e.g. Owen");
        surname.setPromptText("e.g. Koketso");
        companyName.setPromptText("e.g. Acme Ltd");
        address.setPromptText("Street, city");
        phone.setPromptText("e.g. 71234567");
        regUser.setPromptText("Choose a username");
        regPass.setPromptText("At least 6 characters");

        Button register = new Button("Create profile");
        register.getStyleClass().add("primary");
        register.setMaxWidth(Double.MAX_VALUE);
        register.setOnAction(e -> doRegister());

        VBox form = new VBox(6,
                fieldLabel("Customer type"), typeChoice,
                firstLabel, firstName, surnameLabel, surname, companyLabel, companyName,
                fieldLabel("Address"), address,
                fieldLabel("Phone"), phone,
                fieldLabel("Username"), regUser,
                fieldLabel("Password"), regPass,
                spacer(6), register, regStatus);
        form.setPadding(new Insets(16, 24, 20, 24));
        updateRegistrationFields();

        // The registration form is long, so it scrolls when the window is small.
        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return new Tab("Register", scroll);
    }

    private void updateRegistrationFields() {
        boolean individual = "Individual".equals(typeChoice.getValue());
        for (Node n : new Node[]{firstLabel, firstName, surnameLabel, surname}) {
            n.setVisible(individual);
            n.setManaged(individual);
        }
        for (Node n : new Node[]{companyLabel, companyName}) {
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

    private static Label fieldLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return label;
    }

    private static Region2 spacer(double height) {
        return new Region2(height);
    }

    /** Small invisible node used to add vertical space. */
    private static final class Region2 extends javafx.scene.layout.Region {
        Region2(double height) {
            setMinHeight(height);
            setPrefHeight(height);
        }
    }

    private void show(Label label, String message, boolean error) {
        label.setText(message);
        label.setWrapText(true);
        label.getStyleClass().removeAll("status-success", "status-error");
        label.getStyleClass().add(error ? "status-error" : "status-success");
    }
}
