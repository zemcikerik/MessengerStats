package sk.zemco.messengerstats.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

public class Media {

    private String uri;

    @JsonAlias("creation_timestamp")
    private String creationTimestamp;

    @JsonGetter
    public String getUri() {
        return uri;
    }

    @JsonSetter
    public void setUri(String uri) {
        this.uri = uri;
    }

    @JsonGetter
    public String getCreationTimestamp() {
        return creationTimestamp;
    }

    @JsonSetter
    public void setCreationTimestamp(String creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }
}
