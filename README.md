# Multithreaded Web-Crawler

This is a Multi-threaded Web Crawler implemented in Java. It returns a depth-limited tree that illustrates the set of reachable URLs from the given input URL. There are 10 threads that coordinate with each other while processing a single link. The links are appropriately indented in the output to illustrate which "level" or "depth" of the link tree they are in. For example, a URL x with a depth 2 (indicated by a 2 space indentation) will be reachable from the input URL such that input -> someOtherUrl -> x.

## Usage

Given its implementation using the Socket class, this does not work with many URLs (especially those with https). As an example, you may run the crawler on my old university's website "www.lums.edu.pk" (Do not include the "http://"). You should have Java installed. Navigate to the webcrawler/ directory in a linux shell. Compile and run:

```
	java WebCrawler <YourUrl>
```

As an example:

```
	java WebCrawler www.lums.edu.pk
```

## Bugs

Given the unreliability of the Socket class, the returned HTML responses from websites may be malformed and such issues are currently resolved with appropriate exceptions. I may add more to this in future or change the implementation to work with reliable TCP sockets instead.