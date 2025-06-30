package com.mychatserver;

import java.io.PrintWriter;

public class ClientConnectionDetails {
    private final String userID;
    private final PrintWriter out;

    public ClientConnectionDetails(String userID, PrintWriter out){
        this.userID = userID;
        this.out = out;
    }
    public String getUserID(){
        return userID;
    }
    public PrintWriter getPrintWriter(){
        return out;
    }

}
