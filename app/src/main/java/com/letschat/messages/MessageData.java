package com.letschat.messages;

public class MessageData {
    private String senderName;
    private String message;

    MessageData(String senderName, String message) {
        this.senderName = senderName;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
