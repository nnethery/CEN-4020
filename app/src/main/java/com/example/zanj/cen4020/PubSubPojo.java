package com.example.zanj.cen4020;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class PubSubPojo { //converts messages so they are easy to manipulate
    private final String sender;
    private final String message;
    private final String timestamp;
    private final String message_id;
    private final String upvotes;
    private final String upvoted;

    public PubSubPojo(@JsonProperty("message_id") String message_id, @JsonProperty("sender") String sender, @JsonProperty("message") String message, @JsonProperty("timestamp") String timestamp, @JsonProperty("upvotes") String upvotes) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
        this.message_id = message_id;
        this.upvoted = upvotes;
        this.upvotes = this.getUpvotes();
    }
    //get functions for getting info about a message
    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getMessage_id() { return message_id; }

    public String getUpvotes()
    {
        if (upvoted.equals("null"))
        {
            return "null";
        }
        int count = 0;
        for (char c : upvoted.toCharArray()) {
            if (c == ' ') {
                count++;
            }
        }
        return Integer.toString(count);
    }

    public String getUpvoted() { return upvoted; }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final PubSubPojo other = (PubSubPojo) obj;

        return Objects.equal(this.sender, other.sender)
                && Objects.equal(this.message, other.message)
                && Objects.equal(this.timestamp, other.timestamp)
                && Objects.equal(this.message_id, other.message_id)
                && Objects.equal(this.upvotes, other.upvotes);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sender, message, timestamp, message_id, upvotes);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(PubSubPojo.class)
                .add("sender", sender)
                .add("message", message)
                .add("timestamp", timestamp)
                .add("message_id", message_id)
                .add("upvotes", upvoted)
                .toString();
    }
}
