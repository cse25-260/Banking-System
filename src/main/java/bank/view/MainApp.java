package bank.view;

import bank.controller.AccountController;
import bank.controller.LoginController;
import bank.model.BankException;
import bank.model.Customer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/** Starts the JavaFX application and switches between the login and account screens. */
public class MainApp extends Application {
    private Stage stage;
    private final LoginController loginController = new LoginController();

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        try {
            loginController.initialiseSystem();
        } catch (BankException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            Platform.exit();
            return;
        }
        stage.setTitle("Banking System");
        showLogin();
        stage.show();
    }

    private void showLogin() {
        LoginView view = new LoginView(loginController, this::showAccounts);
        stage.setScene(new Scene(view.getRoot(), 480, 560));
    }

    private void showAccounts(Customer customer) {
        try {
            AccountController controller = new AccountController(customer);
            AccountView view = new AccountView(controller, () -> {
                loginController.logout();
                showLogin();
            });
            stage.setScene(new Scene(view.getRoot(), 1000, 680));
        } catch (BankException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
