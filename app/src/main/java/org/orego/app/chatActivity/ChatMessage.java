package org.orego.app.chatActivity;

import java.util.Date;

public class ChatMessage {
    private String messageText;
    private String messageUser;
    private long messageTime;

    ChatMessage(String messageText, String messageUser) {
        this.messageText = messageText;
        this.messageUser = messageUser;

        messageTime = new Date().getTime();
    }

    public ChatMessage() {
    }

    String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }
}
