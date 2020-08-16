package edu.byu.cstaheli.cs453.suggesting_similar_queries.process;

import edu.byu.cstaheli.cs453.suggesting_similar_queries.rank.QueryLog;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by cstaheli on 5/19/2017.
 */
public class AolQueryLogsProcessor
{
    private List<QueryLog> queryLogs;

    public AolQueryLogsProcessor(String fileName)
    {
        try
        {
            List<String[]> lines = readFile(fileName)
                    .stream()
                    .filter(line -> !line.isEmpty())
                    .map(line -> line.split("\t"))
                    .collect(Collectors.toList());

            queryLogs = new ArrayList<>(lines.size() - 1);
            //Dates looks like this 2006-03-28 20:39:58
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            //The first line is the header. Skip it.
            for (int i = 1; i < lines.size(); ++i)
            {
                String anonId = lines.get(i)[0];
                String query = lines.get(i)[1];
                LocalDateTime timeStamp = LocalDateTime.parse(lines.get(i)[2], formatter);
                QueryLog log = new QueryLog(anonId, query, timeStamp);
                queryLogs.add(log);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private List<String> readFile(String fileName) throws IOException
    {
        //A lot of the files aren't encoded with UTF-8
        return Files.readAllLines(Paths.get(fileName), Charset.forName("UTF-8"));
    }

    public List<QueryLog> getQueryLogs()
    {
        return queryLogs;
    }
}
