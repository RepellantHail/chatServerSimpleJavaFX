package com.example.chat;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.List;

public class Controller {
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;
    @FXML
    private TextArea messageArea;
    private Client client;
    public void initialize(Client _client) {
        messageArea.setText("Client Started");
        this.client = _client;
        client.setClientName("Client 1");
        client.setController(this);
        client.setMessageArea(messageArea);
        messageArea.textProperty().bind(client.chatMessagesProperty());
        client.connectToServer();
        sendButton.setOnAction(this::handleSendButton);
    }

    public void handleSendButton(ActionEvent event) {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            client.setClientName("Client 1");
            try {
                //client.addMessageToArea(message);
                messageField.clear();
                client.updateMessageArea(client.getClientName() + ": " + message);
                client.sendMessage(message);


                // Retrieve the full message history from the server and update the text area
                List<String> messageHistory = client.getServerMessageHistory();
                for (String msg : messageHistory) {
                    System.out.println(msg);
                    client.updateMessageArea(msg);
                }
                messageHistory.add(client.getClientName() + ": " + message);
                StringBuilder historyBuilder = new StringBuilder();
                for (String msg : messageHistory) {
                    historyBuilder.append(msg).append("\n");
                }


                Platform.runLater(() -> {
                    //messageArea.setText(historyBuilder.toString());
                    for (String msg : messageHistory) {
                        messageArea.appendText(msg);
                    }
                });
            }  catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public TextArea getMessageArea() {
        return messageArea;
    }
}