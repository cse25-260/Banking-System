package bank.view;

import bank.controller.AccountController;
import bank.controller.LoginController;
import bank.model.BankException;
import bank.model.Customer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.net.URL;

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
        stage.setTitle("Owen National Bank");
        showLogin();
        stage.show();
    }

    private void showLogin() {
        LoginView view = new LoginView(loginController, this::showAccounts);
        stage.setScene(styled(view.getRoot(), 500, 640));
    }

    private void showAccounts(Customer customer) {
        try {
            AccountController controller = new AccountController(customer);
            AccountView view = new AccountView(controller, () -> {
                loginController.logout();
                showLogin();
            });
            stage.setScene(styled(view.getRoot(), 1060, 720));
        } catch (BankException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    /** Creates a scene and applies the shared stylesheet (bank.css). */
    private Scene styled(Parent root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        URL css = getClass().getResource("/bank.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        return scene;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
