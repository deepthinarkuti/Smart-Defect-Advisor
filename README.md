# Project 2 - Suggesting Queries Based on Word Similarity and Query Modification Patterns

This is an implementation of the query-suggestion approach, WebQS, presented in the paper, "Assisting Web Search Using Query Suggestion Based on Word Similarity Measure and Query Modification Patterns," published in the _Journal of World Wide Web_, Volume 17, Number 5, 2014.

## Description
WebQS provides a guide to the users for formulating/completing a keyword query `Q` using suggested keywords (extracted from the AOL query logs) as potential keywords in `Q`. The query-suggestion approach considers _initial and modified queries_ in the AOL query logs, along with _word-similarity measures_, in making query suggestions. WebQS facilitates the formulation of queries in a _trie_ data structure and determines the _rankings_ of suggested keyword queries using distinguished features exhibited in the raw data in the AOL query logs.

## The AOL Query Logs
WebQS relies on the AOL query logs to suggest queries. The logs of AOL, which include 50 million queries that were created by millions of AOL users over a three-month period between March 1, 2006 and May 31, 2006. These logs can be found in [the resource directory](src/main/resources).
An AOL query log includes a number of query sessions, each of which captures a period of sustained user activities on the search engine. Each AOL session differs in length and includes a 
* User ID, 
  * A _user ID_, which is an anonymous identifier of its user who performs the search, determines the boundary of each session (as each user ID is associated with a distinct session).
* The query text, 
  * _Query text_ are keywords in a user query and multiple queries may be created under the same session.
* Date and time of search,
  * The _date and time_ of a search can be used to determine whether two or more queries were created by the same user within 10 minutes, which is the time period that dictates whether two queries should be treated as _related_.
* Optionally clicked documents. 
  * _Clicked documents_ are retrieved documents that the user has clicked on and are ranked by the search engine. 
  
Queries and documents include _stopwords_, which are commonly-occurring keywords, such as prepositions, articles, and pronouns, that carry little meaning and often do not represent the content of a document. Stopwords are not considered by WebQS during the query creation process.
This project implements WebQS by parsing the AOL query logs to extract query keywords while at the same time retains the information of _related_ keywords in the same session, which were submitted by the same user within 10 minutes in the same session, as discussed earlier.

## The Trie Data Structure
Using the extracted keywords, WebQS constructs a trie `T` in which each node is labeled by a _letter_ in an extracted keyword in the given order, and each node in `T` is categorized as either "complete" or "incomplete." A _complete_ node is the last node of a path in`T` representing an (a sequence of, respectively) extracted query keyword (keywords, respectively). If node `c` is a complete node, then `T`<sub>`c`</sub> (the subtree of `T` rooted at a child node of `c`) contains other suggested keyword(s) represented by the nodes in the path(s) leading from, and excluding, `c`. The possible number of suggestions of a (sequence of) keyword(s) `K` rooted at `T`<sub>`c`</sub> is `n`, where `n` is the number of complete nodes in subtrees rooted at `T`<sub>`c`</sub>, and `K` is the (sequence of) keyword(s) extracted from the root of `T`<sub>`c`</sub>. An _incomplete_ node is the last node of a path `P` in `T` such that `P` does not yield a (sequence of) word(s). If `c` is an incomplete node, then all subsequent nodes of `c` up till the first complete node are potential suggestions of keywords represented by the nodes in the path leading from, and including, `c`.

WebQS retains the keywords in query texts in a _trie_ data structure using queries in the AOL query logs. Using the trie, candidate keywords suggested for a query can be found and ranked dynamically. To suggest potential query keywords, WebQS locates a trie branch `b` up till the (letters in the) keywords that have been entered during the query creation process and extracts the subtrees rooted at the child nodes of the last node of `b`. The extracted suggestions are ranked using a set of features.

WebQS ranks suggested query keywords in its trie data structure based on
* The _frequency of occurrence_ (_freq_) of the keywords in the AOL query logs, 
* Their _similarity_ with the keywords submitted by a user based on the word-correlation factors (_WCF_ s)
* The _number of times_ the keywords in user queries were _modified_ (_Mod_) to the keywords in the suggested queries within 10 minutes as shown in the query logs.

This project will show the top eight query suggestions to the user.
"# Smart-Defect-Advisor" 
