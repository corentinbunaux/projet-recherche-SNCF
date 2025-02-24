package org.example;
import java.util.*;

public class ManchetteClustering {
    public static List<Set<String>> clusterManchettes(List<List<String>> manchettes) {
        List<Set<String>> clusters = new ArrayList<>();
        for (List<String> manchette : manchettes) {
            clusters.add(new HashSet<>(manchette));
        }
        return clusters;
    }
}