package com.example.chat;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteServerInterface extends Remote {
    void broadcastMessage(String message) throws RemoteException;
    void registerClient(RemoteClientInterface client) throws RemoteException;
    void unregisterClient(RemoteClientInterface client) throws RemoteException;

    List<String> getMessageHistory() throws RemoteException;
    void receiveMessage(String message) throws RemoteException;
}

