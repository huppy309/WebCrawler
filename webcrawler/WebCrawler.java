import java.net.*;
import java.io.*;
import java.util.*;

public class WebCrawler
{
	public static HashMap<String, Integer> visitedHosts = new HashMap<String, Integer>();
	public static HashMap<String, Integer> visitedFromOtherDomains = new HashMap<String, Integer>();
	public static HashMap<String, Integer> domainCount = new HashMap<String, Integer>();
	public static BlockingQueue urlQueue = new BlockingQueue();
	public static HashMap<String, HashMap<String, Boolean>> robotList = new HashMap<String, HashMap<String, Boolean>>();
	public static CrawlTree crawlTree = null;

	public static String getDomain(String url)
	{
		String domain = "";
		if(url.startsWith("http://"))
		{
			for(int i = 7 ; i < url.length() ; i++)
			{
				if(url.charAt(i) == '/')
					break;
				domain = domain + url.charAt(i);
			}
			return domain;
		}
		else
		{
			return null;
		}
	}

	public static synchronized void addVisitedHost(String host)
	{
		Integer one = new Integer(1);
		if(!isHostVisited(host))
			visitedHosts.put(host, one);
		else
		{
			Integer oldValue = visitedHosts.get(host);
			Integer newValue = oldValue + one;
			visitedHosts.remove(host);
			visitedHosts.put(host, newValue);
		}
	}

	public static boolean isHostVisited(String host)
	{
		return visitedHosts.containsKey(host);
	}

	public static synchronized void addDomainHosts(String host)
	{
		Integer one = new Integer(1);
		if(!isDomainVisited(host))
			domainCount.put(host, one);
		else
		{
			Integer oldValue = visitedHosts.get(host);
			Integer newValue = oldValue + one;
			domainCount.remove(host);
			domainCount.put(host, newValue);
		}
	}

	public static boolean isDomainVisited(String host)
	{
		return domainCount.containsKey(host);
	}

	public static void main(String[] args)
	{
		String inputUrl;
		if(args.length == 0)
		{
			System.out.println("Please provide an input host..");
			System.exit(0);
		}

		inputUrl = args[0];
		PriorityUrl host = new PriorityUrl(inputUrl, 0);
		urlQueue.enqueue(host);

		addVisitedHost(host.getUrl());
		addDomainHosts(getDomain(host.getUrl()));
		Node root = new Node(host.getUrl(), null);
		crawlTree = new CrawlTree(root);

		CrawlerThread[] threadList = new CrawlerThread[10];
		for(int i = 0 ; i < threadList.length ; i++)
		{
			threadList[i] = new CrawlerThread(visitedHosts, urlQueue, robotList, visitedFromOtherDomains, domainCount, crawlTree);
			threadList[i].start();
		}

		while(true)
		{
			int counter = 0;
			for(CrawlerThread thread : threadList)
				if(thread.getState() == Thread.State.WAITING || thread.getState() == Thread.State.TERMINATED)
					counter++;
			if(counter == 10)
				break;
		}


		for(CrawlerThread thread : threadList)
		{
			thread.interrupt();
		}

		crawlTree.print(crawlTree.getRoot());
	}
}