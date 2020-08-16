package edu.byu.cstaheli.cs453.suggesting_similar_queries;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import edu.byu.cstaheli.cs453.common.util.StopWordsRemover;
import edu.byu.cstaheli.cs453.common.util.WordTokenizer;
import edu.byu.cstaheli.cs453.suggesting_similar_queries.process.AolQueryLogsProcessor;
import edu.byu.cstaheli.cs453.suggesting_similar_queries.rank.QueryLog;
import edu.byu.cstaheli.cs453.suggesting_similar_queries.rank.QueryTrie;
import edu.byu.cstaheli.cs453.suggesting_similar_queries.rank.SuggestedQueryRanker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by cstaheli on 5/19/2017.
 */
public class Driver
{
    private QueryTrie queryTrie;

    public Driver()
    {
        queryTrie = new QueryTrie();
    }

    public static void main(String[] args)
    {
        String resourcesDirectory = "src/main/resources";
        Driver driver = new Driver();
        driver.readInAolQueries(resourcesDirectory);
        printOutPrompt();
        Scanner scanner = new Scanner(System.in);
        String queryInput = scanner.nextLine();
        while (!"done".equals(queryInput) && !"".equals(queryInput))
        {
            List<String> suggestedQueries = driver.processQuery(queryInput);
            driver.outputSuggestedQueries(suggestedQueries);
            printOutPrompt();
            queryInput = scanner.nextLine();
        }
    }

    private static void printOutPrompt()
    {
        System.out.println("\nEnter a query. Type \"done\" to exit");
    }

    private void outputSuggestedQueries(List<String> suggestedQueries)
    {
        if (suggestedQueries.size() == 0)
        {
            System.out.println("No suggestions found. Possible reasons:");
            System.out.println("The original query doesn't appear in the AOL query logs");
            System.out.println("The original query was never expanded in the AOL query logs");
            System.out.println("Something went really horribly wrong with the code (Oh please, oh please not)");
        }
        else
        {
            System.out.println("Suggested Queries:");
            for (String query : suggestedQueries)
            {
                System.out.println(query);
            }
        }
        System.out.println();
    }

    private List<String> processQuery(String query)
    {
        String sanitizedQuery = getSanitizedQuery(query);
        List<String> suggestions = queryTrie.getUniqueQuerySuggestionsFromQuery(sanitizedQuery);

        return getBestSuggestions(suggestions, sanitizedQuery);
    }

    private String getSanitizedQuery(String query)
    {
        List<String> queryWords = new WordTokenizer(query.toLowerCase()).getWords();
        //queryWords = removeLeadingStopwords(queryWords);
        return String.join(" ", queryWords);
    }

    private List<String> removeLeadingStopwords(List<String> words)
    {
        while (StopWordsRemover.getInstance().contains(words.get(0)))
        {
            if (words.size() > 1)
            {
                words = words.subList(1, words.size());
            }
            else
            {
                throw new RuntimeException("Your query contains only stopwords. This will not currently work for query expansion.");
            }
        }
        return words;
    }

    private List<String> getBestSuggestions(List<String> suggestions, String sanitizedQuery)
    {
        suggestions = sortAndRankSuggestedQueries(suggestions, sanitizedQuery);
        return (suggestions.size() <= 8) ? suggestions : suggestions.subList(0, 8);
    }

    private List<String> sortAndRankSuggestedQueries(List<String> suggestedQueries, String originalQuery)
    {
        // Put the values into a map from the query to their ranking score so that it can later get a sorted
        Map<String, Double> queryRanks = new HashMap<>(suggestedQueries.size());
        for (String suggestedQuery : suggestedQueries)
        {
            double rank = new SuggestedQueryRanker(originalQuery, suggestedQuery, queryTrie).getRank();
            queryRanks.put(suggestedQuery, rank);
        }
        // return a list that is sorted by the values in the Map in descending order
        return queryRanks
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private void readInAolQueries(String directory)
    {
        try (Stream<Path> paths = Files.walk(Paths.get(directory)))
        {
            Multimap<String, QueryLog> multimap = HashMultimap.create();
            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".txt"))
                    .filter(path -> path.toString().contains("Clean-Data-"))
                    .forEach(path ->
                    {
                        String fileName = path.toString();
                        List<QueryLog> queryLogs = new AolQueryLogsProcessor(fileName).getQueryLogs();
                        // Read logs into a multimap to preserve duplicates
                        multimap.putAll(Multimaps.index(queryLogs, QueryLog::getQuery));
                    });
            //Put the multimap into the trie. It now also has duplicates.
            queryTrie.addAll(multimap.asMap());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
