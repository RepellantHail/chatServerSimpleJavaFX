package com.example.chat;

        import javafx.application.Application;
        import javafx.application.Platform;
        import javafx.scene.Scene;
        import javafx.scene.control.TextArea;
        import javafx.scene.layout.VBox;
        import javafx.stage.Stage;

        import java.rmi.Naming;
        import java.rmi.RemoteException;
        import java.rmi.registry.LocateRegistry;
        import java.rmi.server.UnicastRemoteObject;
        import java.util.ArrayList;
        import java.util.List;

public class Server extends Application implements RemoteServerInterface {
    private TextArea messageArea;
    private List<String> messageHistory;
    private TextArea connectedClientsArea;
    private List<String> chatMessages = new ArrayList<>();
    private List<RemoteClientInterface> clients;

    public Server() throws RemoteException {
        clients = new ArrayList<>();
        messageHistory = new ArrayList<>();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        messageArea = new TextArea();
        messageArea.setEditable(false);

        VBox root = new VBox(messageArea);
        Scene scene = new Scene(root, 400, 300);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Chat Server");
        primaryStage.show();

        startServer();
    }

    private void startServer() {
        try {
            System.out.println("Server is booting....");
            System.setProperty("java.rmi.server.hostname","127.0.0.1");
            LocateRegistry.createRegistry(9000);
            UnicastRemoteObject.exportObject(this, 0);

            Naming.rebind("rmi://localhost:9000/RemoteServer", this);
            connectedClientsArea = new TextArea();
            connectedClientsArea.setEditable(false);
            System.out.println("Server started");
        } catch (Exception e) {
            System.err.println("Server error: " + e.toString());
            e.printStackTrace();
        }
    }

    private void logMessage(String message) {
        System.out.println(message);
        Platform.runLater(() -> {
            if (messageArea != null) {
                messageArea.appendText(message + "\n");
            }
        });
    }

    @Override
    public void receiveMessage(String message) throws RemoteException {
        messageHistory.add(message);
        logMessage("Received message from client: " + message);

        // Send a response back to the client
        String response = "Server: " + message + " - Message received!";
        broadcastMessage(response);

        sendMessageToAllClients("Server: " + message + " - Message received!");
    }

    private void sendMessageToAllClients(String message) {
        for (RemoteClientInterface client : clients) {
            try {
                client.receiveMessage(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMessage(String message) {
        for (RemoteClientInterface client : clients) {
            try {
                client.receiveMessage(message);
            } catch (RemoteException e) {
                // Handle RemoteException if necessary
                e.printStackTrace();
            }
        }
    }

    @Override
    public void registerClient(RemoteClientInterface client) throws RemoteException {
        clients.add(client);
        logMessage("Client registered");

        // Display a message on the server UI when a client connects
        logMessage("Client connected: " + client.toString());
        sendInitialMessageHistory(client);
    }

    @Override
    public void unregisterClient(RemoteClientInterface client) throws RemoteException {
        clients.remove(client);
        logMessage("Client unregistered");
    }

    public TextArea getConnectedClientsArea() {
        return connectedClientsArea;
    }

    @Override
    public List<String> getMessageHistory() throws RemoteException {
        return (messageHistory);
    }

    private void sendInitialMessageHistory(RemoteClientInterface client) {
        StringBuilder historyBuilder = new StringBuilder();
        for (String message : messageHistory) {
            historyBuilder.append(message).append("\n");
        }
        try {
            client.receiveMessage(historyBuilder.toString());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}