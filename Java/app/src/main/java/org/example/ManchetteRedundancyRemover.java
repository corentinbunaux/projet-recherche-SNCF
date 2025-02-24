package org.example;
import java.util.*;

public class ManchetteRedundancyRemover {
    public static List<List<String>> removeRedundancies(List<List<String>> manchettes) {
        List<List<String>> cleaned = new ArrayList<>();
        Set<String> seenStations = new HashSet<>();

        for (List<String> manchette : manchettes) {
            List<String> reduced = new ArrayList<>();
            for (String station : manchette) {
                if (!seenStations.contains(station)) {
                    reduced.add(station);
                    seenStations.add(station);
                }
            }
            if (!reduced.isEmpty()) {
                cleaned.add(reduced);
            }
        }
        return cleaned;
    }
}