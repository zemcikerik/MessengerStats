package sk.zemco.messengerstats.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.List;

public class Message {

    @JsonAlias("sender_name")
    private String senderName;

    private String content;
    private List<Reaction> reactions;
    private List<Media> photos;
    private List<Media> videos;

    @JsonAlias("audio_files")
    private List<Media> audios;

    @JsonGetter
    public String getSenderName() {
        return senderName;
    }

    @JsonSetter
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    @JsonGetter
    public String getContent() {
        return content;
    }

    @JsonSetter
    public void setContent(String content) {
        this.content = content;
    }

    @JsonGetter
    public List<Reaction> getReactions() {
        return reactions;
    }

    @JsonSetter
    public void setReactions(List<Reaction> reactions) {
        this.reactions = reactions;
    }

    @JsonGetter
    public List<Media> getPhotos() {
        return photos;
    }

    @JsonSetter
    public void setPhotos(List<Media> photos) {
        this.photos = photos;
    }

    @JsonGetter
    public List<Media> getVideos() {
        return videos;
    }

    @JsonSetter
    public void setVideos(List<Media> videos) {
        this.videos = videos;
    }

    @JsonGetter
    public List<Media> getAudios() {
        return audios;
    }

    @JsonSetter
    public void setAudios(List<Media> audios) {
        this.audios = audios;
    }
}
