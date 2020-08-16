package edu.byu.cstaheli.cs453.suggesting_similar_queries.util;

import edu.byu.cstaheli.cs453.common.util.PorterStemmer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * Created by cstaheli on 6/2/2017.
 */
public class WordCorrelationEvaluator
{
    /**
     * Calculates the word correlation factor (WCF) of the two given words.
     *
     * @param word1 the first word
     * @param word2 the second word
     * @return the WCF of the two words. A return value of -1 for two stemmed words indicates that there
     * is no WCF value for the two words, and the WCF value of the two words should be treated as zero.
     */
    public static double getWordCorrelationFactor(String word1, String word2)
    {
        try
        {
            PorterStemmer stemmer = new PorterStemmer();
            word1 = stemmer.stem(word1);
            word2 = stemmer.stem(word2);

            String correlationUrl = "http://peacock.cs.byu.edu/CS453Proj2/?word1=" + word1 + "&word2=" + word2;
            Document pageDoc = Jsoup.connect(correlationUrl).get();
            String htmlContent = pageDoc.html();
            Document contentDoc = Jsoup.parse(htmlContent);
            String contentValue = contentDoc.body().text();

            return Double.parseDouble(contentValue);
        }
        catch (SocketTimeoutException e)
        {
            System.out.println("Unable to connect to peacock.cs.byu.edu. Probably the VPN crapped out");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return -1;
    }
}
