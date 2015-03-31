# A database of words and their frequencies.

This project takes in any number of root pages from a text file and parses the words on each. These words are stored in
a persistent Btree with the words as keys and a hashtable for values. The hashtable takes website strings as keys and a
frequency as values.

A byte buffer is used to read in the text file instead of a buffered reader, etc...

There is also a hash-based persistent cache for websites that checks to see whether or not the websites have been updated every
two minutes. This is currently a work in progress.
