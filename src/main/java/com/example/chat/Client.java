package com.example.chat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Client extends Application implements RemoteClientInterface {
    private Controller controller;
    private TextArea messageArea;
    private String clientName;
    private List<String> messageHistory;
    private TextField inputField;
    private Button sendButton;
    private RemoteServerInterface server;
    private StringProperty chatMessages = new SimpleStringProperty();
    public static void main(String[] args) {
        launch(args);
    }
    public Client() {
        messageHistory = new ArrayList<>();
    }

    public Client(RemoteServerInterface server) throws RemoteException {
        this.server = server;
        UnicastRemoteObject.exportObject(this, 0);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("client.fxml"));
            Parent root = fxmlLoader.load();
            controller = fxmlLoader.getController();
            controller.initialize(this);
            Scene scene = new Scene(root, 400, 300);
            primaryStage.setTitle("Chat Client");
            primaryStage.setScene(scene);
            primaryStage.show();
            connectToServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void connectToServer() {
        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 9000);
            RemoteServerInterface remoteInterface = (RemoteServerInterface) registry.lookup("RemoteServer");
            server = remoteInterface; // Assign the remote interface to the server field
            server.registerClient(this);

            Platform.runLater(() -> {
                messageArea.setText(""); // Clear the message area initially
            });

            List<String> receivedMessageHistory = server.getMessageHistory();

            for (String message : receivedMessageHistory) {
                System.out.println(message);
            }



            logMessage("Connected to the server.");

            // Display message history on the client
            StringBuilder historyBuilder = new StringBuilder();
            for (String message : receivedMessageHistory) {
                historyBuilder.append(message).append("\n");
            }

            Platform.runLater(() -> {
                messageArea.setText(historyBuilder.toString());
            });
        } catch (Exception e) {
            System.out.println("Error in client side: " + e);
        }
    }


    public void sendMessage(String message) throws RemoteException {
        if (server != null) {
            server.receiveMessage(clientName + ": " + message);
        } else {
            logMessage("Error: Server is not connected.");
        }
    }

    public void addMessageToArea(String message) {
        logMessage(clientName + ": " + message);
        try {
            // Update the message area in the UI
            updateMessageArea(clientName + ": " + message);
            // Send the message to the server
            sendMessage(message);
        } catch (RemoteException e) {
            logMessage("Error sending message: " + e.getMessage());
        }
    }


    public void setMessageArea(TextArea messageArea) {
        this.messageArea = messageArea;
        messageArea.textProperty().bind(chatMessages);
    }

    private void logMessage(String message) {
        System.out.println(message);
        Platform.runLater(() -> {
            if (messageArea != null) {
                messageArea.appendText(message + "\n");
            }
        });
    }

    public void updateMessageArea(String message) {
        chatMessages.set(chatMessages.get() + message + "\n");
    }

    public void clearMessageArea(){
        messageArea.clear();
        chatMessages.set("");
    }

    public List<String> getServerMessageHistory() throws RemoteException {
        return server.getMessageHistory();
    }

    @Override
    public void receiveMessage(String message) throws RemoteException {
        logMessage(message);

        // Update the message area on the UI thread
        Platform.runLater(() -> {
            System.out.println(message); // Display the message in the console
        });
    }

    public StringProperty chatMessagesProperty() {
        return chatMessages;
    }

    public void setChatMessagesProperty(StringProperty chatMessages) {
        this.chatMessages = chatMessages;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
}