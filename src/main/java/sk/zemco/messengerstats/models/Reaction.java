package sk.zemco.messengerstats.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

public class Reaction {

    private String actor;

    @JsonGetter
    public String getActor() {
        return actor;
    }

    @JsonSetter
    public void setActor(String actor) {
        this.actor = actor;
    }

}
