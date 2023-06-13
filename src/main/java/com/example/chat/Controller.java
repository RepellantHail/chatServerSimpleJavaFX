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
                messageField.clear();
                client.sendMessage(message);
                List<String> messageHistory = client.getServerMessageHistory();
                messageHistory.add(client.getClientName() + ": " + message);
                System.out.println("****New Message****");
                client.clearMessageArea();
                Platform.runLater(() -> {
                    messageArea.selectRange(0, messageArea.getLength());
                    messageArea.cut();
                    for (int i = 0; i < messageHistory.size() - 1; i++) {
                        client.updateMessageArea(messageHistory.get(i));
                        System.out.println(messageHistory.get(i));
                    }
                    if (!messageHistory.isEmpty()) {
                        messageArea.appendText(messageHistory.get(messageHistory.size() - 1));
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