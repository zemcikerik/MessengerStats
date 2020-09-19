package sk.zemco.messengerstats.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.List;

public class RootEntry {

    private String title;
    private List<Participant> participants;

    @JsonMerge
    private List<Message> messages;

    @JsonGetter
    public String getTitle() {
        return title;
    }

    @JsonSetter
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonGetter
    public List<Participant> getParticipants() {
        return participants;
    }

    @JsonSetter
    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    @JsonGetter
    public List<Message> getMessages() {
        return messages;
    }

    @JsonSetter
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
