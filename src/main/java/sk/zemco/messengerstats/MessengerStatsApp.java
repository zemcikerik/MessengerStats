package sk.zemco.messengerstats;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import sk.zemco.messengerstats.models.Message;
import sk.zemco.messengerstats.models.Participant;
import sk.zemco.messengerstats.models.Reaction;
import sk.zemco.messengerstats.models.RootEntry;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class MessengerStatsApp {

    public static void main(String[] args) throws Exception {
        Path jarPath = getJarPath();
        Path jarDirPath = jarPath.getParent();

        ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<String> jsonList = Files.list(jarDirPath)
                .filter(path -> !path.equals(jarPath))
                .filter(Files::isRegularFile)
                .map(path -> wrapTryCatch(() -> Files.readAllBytes(path)))
                .map(MessengerStatsApp::fixFacebookEncoding)
                .collect(Collectors.toUnmodifiableList());

        if (jsonList.size() == 0)
            return;

        RootEntry root = objectMapper.readValue(jsonList.get(0), RootEntry.class);
        ObjectReader updateReader = objectMapper.readerForUpdating(root);

        jsonList.stream().skip(1)
                .forEach(json -> wrapTryCatch(() -> updateReader.readValue(json)));

        List<ChartingFunctionInfo> chartingFunctions = List.of(
                new ChartingFunctionInfo("total_messages.png", MessengerStatsApp::createSentMessagesChart),
                new ChartingFunctionInfo("images.png", MessengerStatsApp::createSentImagesChart),
                new ChartingFunctionInfo("videos.png", MessengerStatsApp::createSentVideosChart),
                new ChartingFunctionInfo("audio_recordings.png", MessengerStatsApp::createSentAudiosChart),
                new ChartingFunctionInfo("given_reactions.png", MessengerStatsApp::createGivenReactionsChart),
                new ChartingFunctionInfo("received_reactions.png", MessengerStatsApp::createReceivedReactionsChart)
        );

        for (ChartingFunctionInfo chartingFunction : chartingFunctions) {
            CategoryChart chart = chartingFunction.getFunction().apply(root);
            BitmapEncoder.saveBitmap(chart, chartingFunction.getOutputName(), BitmapEncoder.BitmapFormat.PNG);
        }
    }

    private static Path getJarPath() {
        URL jarLocation = MessengerStatsApp.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation();

        try {
            return Path.of(jarLocation.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T wrapTryCatch(SupplierWithException<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // https://stackoverflow.com/questions/50008296/facebook-json-badly-encoded
    // https://stackoverflow.com/questions/50799187/encoding-decoding-issue-with-facebook-json-messages-c-sharp-parsing
    // Facebook doesn't encode unicode characters correctly, this is a dirty fix (please don't put this on r/programmerhorror)
    private static String fixFacebookEncoding(byte[] bytes) {
        String str = new String(bytes, StandardCharsets.UTF_8);
        StringBuilder builder = new StringBuilder(str.length());
        StringBuilder unicode = new StringBuilder(4);
        byte[] unicodeBytes = new byte[2];
        boolean shouldBeUnicode = false;

        for (int i = 0; i < str.length(); i++) {
            char character = str.charAt(i);

            if (character != '\\') {
                // ignore for now (not sure what should be done)
                if (shouldBeUnicode)
                    unicode = new StringBuilder(4);

                builder.append(character);
                continue;
            }

            char identifier = str.charAt(++i);

            if (identifier != 'u') {
                builder.append('\\');
                builder.append(identifier);
                continue;
            }

            unicode.append(str.charAt(i + 3));
            unicode.append(str.charAt(i + 4));
            i += 4;
            shouldBeUnicode = true;

            if (unicode.length() == 4) {
                String rawHex = unicode.toString();

                unicodeBytes[0] = (byte) Integer.parseInt(rawHex, 0, 2, 16);
                unicodeBytes[1] = (byte) Integer.parseInt(rawHex, 2, 4, 16);

                String unicodeStr = new String(unicodeBytes, StandardCharsets.UTF_8);
                builder.append(unicodeStr);

                unicode = new StringBuilder(4);
                shouldBeUnicode = false;
            }
        }

        return builder.toString();
    }

    private static CategoryChart createSentMessagesChart(RootEntry rootEntry) {
        PersonToCountFactory factory = root -> root.getMessages().stream()
                .filter(message -> message.getContent() != null)
                .collect(Collectors.groupingBy(Message::getSenderName, Collectors.counting()));

        return createTemplatedIndividualChart(rootEntry, "Total Messages", factory);
    }

    private static CategoryChart createSentImagesChart(RootEntry rootEntry) {
        return createMessageListChart(rootEntry, "Total Images", Message::getPhotos);
    }

    private static CategoryChart createSentVideosChart(RootEntry rootEntry) {
        return createMessageListChart(rootEntry, "Total Videos", Message::getVideos);
    }

    private static CategoryChart createSentAudiosChart(RootEntry rootEntry) {
        return createMessageListChart(rootEntry, "Total Audio Recordings", Message::getAudios);
    }

    private static CategoryChart createGivenReactionsChart(RootEntry rootEntry) {
        PersonToCountFactory factory = root -> root.getMessages().stream()
                .map(Message::getReactions)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(Reaction::getActor)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return createTemplatedIndividualChart(rootEntry, "Given Reactions", factory);
    }

    private static CategoryChart createReceivedReactionsChart(RootEntry rootEntry) {
        return createMessageListChart(rootEntry, "Received Reactions", Message::getReactions);
    }

    private static CategoryChart createMessageListChart(RootEntry rootEntry,
                                                        String description,
                                                        Function<Message, List<?>> listGetter) {
        PersonToCountFactory factory = root -> root.getMessages().stream()
                .filter(message -> listGetter.apply(message) != null)
                .map(message -> new Object(){
                    final String sender = message.getSenderName();
                    final int count = listGetter.apply(message).size();
                }).collect(Collectors.groupingBy(pair -> pair.sender, Collectors.summingLong(pair -> pair.count)));

        return createTemplatedIndividualChart(rootEntry, description, factory);
    }

    private static CategoryChart createTemplatedIndividualChart(RootEntry root,
                                                                String description,
                                                                PersonToCountFactory personCountFactory) {
        Map<String, Long> map = personCountFactory.getPersonToCountMap(root);

        root.getParticipants().stream()
                .map(Participant::getName)
                .forEach(name -> map.computeIfAbsent(name, key -> 0L));

        String title = String.format("%s - %s", root.getTitle(), description);
        return createTemplatedChart(map, title);
    }

    private static CategoryChart createTemplatedChart(Map<String, Long> map, String title) {
        List<Map.Entry<String, Long>> entries = map.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .collect(Collectors.toUnmodifiableList());

        List<String> names = entries.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableList());

        List<Long> values = entries.stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toUnmodifiableList());

        CategoryChart chart = createBarChart(title);
        chart.addSeries("Series", names, values);
        return chart;
    }

    private static CategoryChart createBarChart(String title) {
        CategoryChart chart = new CategoryChartBuilder()
                .width(2560)
                .height(1440)
                .title(title)
                .build();

        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setHasAnnotations(true);
        chart.getStyler().setXAxisLabelRotation(90);

        return chart;
    }

    private MessengerStatsApp() { }

}
