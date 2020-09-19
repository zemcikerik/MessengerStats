package sk.zemco.messengerstats;

import sk.zemco.messengerstats.models.RootEntry;

import java.util.Map;

@FunctionalInterface
public interface PersonToCountFactory {

    Map<String, Long> getPersonToCountMap(RootEntry root);

}
