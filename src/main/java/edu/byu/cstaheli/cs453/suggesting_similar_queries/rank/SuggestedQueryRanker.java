package edu.byu.cstaheli.cs453.suggesting_similar_queries.rank;

import edu.byu.cstaheli.cs453.suggesting_similar_queries.util.WordCorrelationEvaluator;

/**
 * Created by cstaheli on 6/2/2017.
 */
public class SuggestedQueryRanker
{
    private final double rank;
    private final String originalQuery;
    private final String suggestedQuery;
    private final QueryTrie trie;

    public SuggestedQueryRanker(String query, String suggestedQuery, QueryTrie trie)
    {
        this.originalQuery = query;
        this.suggestedQuery = suggestedQuery;
        this.trie = trie;
        rank = rankQuery();
    }

    /**
     * Returns the minimum of three given values.
     *
     * @param a the first value.
     * @param b the second value.
     * @param c the third value.
     * @return the minimum of the three given values.
     */
    private static double min(double a, double b, double c)
    {
        return Math.min(Math.min(a, b), c);
    }

    /**
     * Ranks the suggested query based on the suggested query keywords in the trie data structure based on
     * <ol>
     * <li>the frequency of occurrence (<code>freq</code>) of the keywords in the AOL query logs,</li>
     * <li>their similarity with the keywords submitted by a user based on the word-correlation factors
     * (<code>WCFs</code>), and </li>
     * <li>the number of times the keywords in user queries were modified (<code>Mod</code) to the keywords
     * in the suggested queries within 10 minutes as shown in the query logs.</li>
     * </ol>
     *
     * @return the ranking of the suggested query with respect to the original query.
     */
    private double rankQuery()
    {
        // originalQuery = Q, suggestedQuery = SQ. All values are normalized.

        // freqSq - the frequency of occurrence of SQ in the AOL query logs
        double freqSq = getOccurrenceOfSuggestedQuery();

        // wcf - the word correction factor of the last word in Q and the next
        //       suggested word in SQ
        double wcf = getWordCorrelationFactorOfQueries();
        wcf = (wcf == -1) ? 0 : wcf;

        // mod - the number of times Q is modified to SQ in the same session in
        //       the AOL query logs within 10 minutes.
        double mod = getNumberOfDirectModifications();

        // All should be bounded between [0,1]
        assert (0 <= freqSq) && (freqSq <= 1);
        assert (0 <= wcf) && (wcf <= 1);
        assert (0 <= mod) && (mod <= 1);
        assert (freqSq != 1) && (wcf != 1) && (mod != 1);

        //SuggRank(S,Q) = (freq(SQ) + WCF(Q,SQ) + Mod(S,SQ)/(1-Min{freq(SQ),WCF(Q,SQ),Mod(S,SQ))
        return (freqSq + wcf + mod) / (1 - min(freqSq, wcf, mod));
    }

    /**
     * <code>freq(SQ)</code> in <code>SuggRank(Q, SQ)</code>
     * <p>
     * <code>freq(SQ)</code> is the frequency of occurrence of <code>SQ</code> in the AOL query logs.<br>
     * <p>
     * Given a query <code>Q</code>, the frequency of a suggested query <code>SQ</code> of <code>Q</code>, denoted
     * <code>freq(SQ)</code>, is the normalized frequency of occurrence of <code>SQ</code> in the AOL query logs. It is
     * normalized relative to the most frequent query in the AOL query logs.
     *
     * @return the normalized occurrence of the suggested query.
     */
    private double getOccurrenceOfSuggestedQuery()
    {
        return (double) trie.frequency(suggestedQuery) / (double) trie.getMostCommonQueryFrequency();
    }

    /**
     * <code>WCF(Q, SQ)</code> in <code>SuggRank(Q, SQ)</code>.
     * <p>
     * Given a query <code>Q</code> with <code>m (≥ 1) </code> words and a suggestion SQ with <code>m+i</code> words,
     * consider the last word in <code>Q</code> as <code>word1</code>, and the first suggested word in <code>SQ</code>,
     * i.e., the <code>m+1</code><sup><code>th</code></sup> word in <code>SQ</code> as <code>word2</code>,
     * <code>WCF(word1, word2)</code> is the <code>WCF(Q, SQ)</code>. For example, if <code>Q</code> is “tropical
     * fish” and <code>SQ</code> is “tropical fish pond”, <code>WCF(“fish”, “pond”)</code> is the computed value of
     * <code>WCF(Q, SQ)</code>.
     *
     * @return the word correlation factor of the original query and the suggested query.
     */
    private double getWordCorrelationFactorOfQueries()
    {
        //SQ.length must be at least Q.length+1
        String[] origQuery = originalQuery.split(" ");
        String[] suggQuery = suggestedQuery.split(" ");
        assert origQuery.length < suggQuery.length;

        int lastWordInOriginalIndex = origQuery.length - 1;
        String lastWordInOriginalQuery = origQuery[lastWordInOriginalIndex];
        String nextWordInSuggestedQuery = suggQuery[lastWordInOriginalIndex + 1];
        return WordCorrelationEvaluator.getWordCorrelationFactor(lastWordInOriginalQuery, nextWordInSuggestedQuery);
        //return -1;
    }

    /**
     * <code>Mod(Q, SQ)</code> in <code>SuggRank(Q, SQ)</code>
     * <p>
     * <code>Mod(Q, SQ)</code> is the number of times <code>Q</code> is modified to <Code>SQ</Code> in the same
     * session in the AOL query logs within 10 minutes. This is normalized with respect to the most frequent query
     * in the logs.
     *
     * @return the normalized number of modifications of the original query to the suggested query.
     */
    private double getNumberOfDirectModifications()
    {
        return (double) trie.getFrequencyOfAdjacency(originalQuery, suggestedQuery) / (double) trie.getMostCommonQueryFrequency();
    }

    public double getRank()
    {
        return rank;
    }
}
