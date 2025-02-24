package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManchetteMerger {
        public static List<List<String>> mergeManchettes(List<List<String>> manchettes) {
        List<List<String>> merged = new ArrayList<>();
        
        for (List<String> manchette : manchettes) {
            boolean mergedFlag = false;
            for (List<String> existing : merged) {
                if (!Collections.disjoint(existing, manchette)) { // Vérifie si elles ont des gares en commun
                    existing.addAll(manchette);
                    Collections.sort(existing);
                    mergedFlag = true;
                    break;
                }
            }
            if (!mergedFlag) {
                merged.add(new ArrayList<>(manchette));
            }
        }
        return merged;
    }
    
}
