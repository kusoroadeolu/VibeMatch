package com.victor.VibeMatch;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Test {

    public float calculateJaccardSimilarity_givenEqualSets(Set<Character> a, Set<Character> b){
        //Jaccard similarity: Overlapping items/Total Unique Items

        int overlap = 0;

        //Assuming both sets are equal in size
        for(Character c: a){
            if(b.contains(c)){
                overlap++;
                System.out.println("Overlap: " + c);
            }
        }

        int total = a.size() + b.size() - overlap;
        System.out.println("Total unique: " + total);

        return (float) overlap /total;
    }

    public float calculateJaccardSimilarity_givenUnequalSets(Set<Character> a, Set<Character> b){
        //Jaccard similarity: Overlapping items/Total Unique Items

        int overlap = 0;
        int aSize = a.size();
        int bSize = b.size();

        //Assuming both sets are not equal in size
        int largest = Math.max(aSize, bSize);
        System.out.println("Largest: " + largest);

        if(largest == aSize){
            for(Character c: a){
                if(b.contains(c)){
                    overlap++;
                    System.out.println("Overlap: " + c);
                }
            }

        }else{

            for(Character c: b){
                if(a.contains(c)){
                    overlap++;
                    System.out.println("Overlap: " + c);
                }
            }

        }

        int total = a.size() + b.size() - overlap;

        return (float) overlap /total;
    }

    public float weightedAverage(List<Integer> weights){
        int size = weights.size();
        int total = weights.stream().reduce(0, Integer::sum);
        return (float) total/size;
    }

    public double calculateManhattanDistance(Obj a, Obj b){
        double discovery = Math.abs(a.getDiscoveryRate() - b.getDiscoveryRate());
        double mainstream = Math.abs(a.getMainstream() - b.getMainstream());
        System.out.println("Discovery: " + discovery);
        System.out.println("Mainstream: " + mainstream);
        return discovery + mainstream;

    }


}

@Getter
@Setter
@AllArgsConstructor

class Obj{
    private double mainstream;
    private double discoveryRate;
}
