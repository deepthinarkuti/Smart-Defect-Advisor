package edu.byu.cstaheli.cs453.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cstaheli on 5/11/2017.
 */
public class WordTokenizer
{
    private List<String> tokens;

    public WordTokenizer(String line)
    {
        tokens = new ArrayList<>();
        parseLine(line);
    }

    private void parseLine(String line)
    {
        String[] words = line.split("\\s+");
        for (String word : words)
        {
            if (word.indexOf('-') != -1)
            {
                parseHyphenatedWord(word);
            }
            else
            {
                parseWord(word);
            }
        }
    }

    private void parseWord(String word)
    {
        word = stripExtraCharacters(word);
        //words with numbers shouldn't be added
        if (!wordContainsNumbers(word))
        {
            tokens.add(word.toLowerCase());
        }
    }

    private boolean wordContainsNumbers(String word)
    {
        return word.matches(".*\\d+.*");
    }

    private String stripExtraCharacters(String word)
    {
        return word.replaceAll("[^\\w]+", "");
    }

    private void parseHyphenatedWord(String word)
    {
        String[] hyphenatedWords = word.split("-");
        for (int i = 0; i < hyphenatedWords.length; ++i)
        {
            hyphenatedWords[i] = stripExtraCharacters(hyphenatedWords[i]);
        }
        String concatenatedWord = concatenateHyphenatedWords(hyphenatedWords);
        if (Dictionary.getInstance().wordExists(concatenatedWord))
        {
            parseWord(concatenatedWord);
        }
        else
        {
            for (String hyphenatedWord : hyphenatedWords)
            {
                parseWord(hyphenatedWord);
            }
        }
    }

    private String concatenateHyphenatedWords(String[] hyphenatedWords)
    {
        StringBuilder concatenatedWordBuilder = new StringBuilder();
        for (String hyphenatedWord : hyphenatedWords)
        {
            concatenatedWordBuilder.append(hyphenatedWord);
        }
        return concatenatedWordBuilder.toString();
    }

    public List<String> getWords()
    {
        return tokens;
    }
}
