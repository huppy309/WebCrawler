import java.util.*;

public class CrawlerThread extends Thread implements Runnable
{
	public static HashMap<String, Integer> visitedHosts;
	public static HashMap<String, Integer> visitedFromOtherDomains;
	public static HashMap<String, Integer> domainCount;
	public static BlockingQueue urlQueue;
	public static HashMap<String, HashMap<String, Boolean>> robotList;
	public static CrawlTree crawlTree;

	public CrawlerThread(HashMap<String, Integer> _visitedHosts, BlockingQueue _urlQueue, HashMap<String, HashMap<String, Boolean>> _robotList, HashMap<String, Integer> _visitedFromOtherDomains, HashMap<String, Integer> _domainCount, CrawlTree _crawlTree)
	{
		visitedHosts = _visitedHosts;
		visitedFromOtherDomains = _visitedFromOtherDomains;
		urlQueue = _urlQueue;
		robotList = _robotList;
		domainCount = _domainCount;
		crawlTree = _crawlTree;
	}

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
			for(int i = 0 ; i < url.length() ; i++)
			{
				if(url.charAt(i) == '/')
					break;
				domain = domain + url.charAt(i);
			}
			return domain;
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
			Integer oldValue = domainCount.get(host);
			Integer newValue = oldValue + one;
			domainCount.remove(host);
			domainCount.put(host, newValue);
		}
	}

	public static boolean isDomainVisited(String host)
	{
		return domainCount.containsKey(host);
	}

	public static synchronized void addVisitedFromOther(String host)
	{
		Integer one = new Integer(1);
		if(!isVisitedFromOther(host))
			visitedFromOtherDomains.put(host, one);
		else
		{
			Integer oldValue = visitedFromOtherDomains.get(host);
			Integer newValue = oldValue + one;
			visitedFromOtherDomains.remove(host);
			visitedFromOtherDomains.put(host, newValue);
		}
	}

	public static boolean isVisitedFromOther(String host)
	{
		return visitedFromOtherDomains.containsKey(host);
	}

	public static synchronized void addRobotsDomain(String domain, HashMap<String, Boolean> map)
	{
		robotList.put(domain, map);
	}

	public static boolean isRobotsVisited(String domain)
	{
		return robotList.containsKey(domain);
	}

	public void run()
	{
		UrlExtractor crawler = new UrlExtractor();
		RobotsParser robotsParser = new RobotsParser();
		RedirectHandler redirectHandler = new RedirectHandler();
		PriorityUrl host = null;
		HashMap<String, Boolean> robotMap = null;
		ArrayList<String> links = null;
		Node node = null;

		while(!Thread.currentThread().isInterrupted())
		{
			if(visitedHosts.size() > 30)
				break;
			try
			{
				host = urlQueue.dequeue();
				if(!isRobotsVisited(getDomain(host.getUrl())))
				{
					robotMap = robotsParser.parse(getDomain(host.getUrl()));
					if(robotMap != null)
						addRobotsDomain(getDomain(host.getUrl()), robotMap);
				}

				robotMap = robotList.get(getDomain(host.getUrl()));
				if(robotMap == null)
				{
					if(!isHostVisited(host.getUrl()))
					{
						links = crawler.extractUrl(host.getUrl());
						if(links != null)
						{
							for(String link : links)
								crawlTree.addNewUrl(host.getUrl(), link);
						}
						addDomainHosts(getDomain(host.getUrl()));
						addVisitedHost(host.getUrl());
					}
				}
				else if(robotMap.containsKey(host.getUrl()))
				{
					boolean decision = robotMap.get(host.getUrl());
					if(decision)
					{
						if(!isHostVisited(host.getUrl()))
						{
							links = crawler.extractUrl(host.getUrl());
							if(links != null)
							{
								for(String link : links)
									crawlTree.addNewUrl(host.getUrl(), link);
							}
							addDomainHosts(getDomain(host.getUrl()));
							addVisitedHost(host.getUrl());
						}
					}
					else
					{
						node = crawlTree.findUrl(host.getUrl(), crawlTree.getRoot());
						String old = node.getData();
						node.setData(old + " - " + "ROBOTS EXCLUSION");
					}
				}
				else
				{
					links = crawler.extractUrl(host.getUrl());
					if(links != null)
					{
						for(String link : links)
							crawlTree.addNewUrl(host.getUrl(), link);
					}
					addDomainHosts(getDomain(host.getUrl()));
					addVisitedHost(host.getUrl());
				}

				if(links != null)
				{
					for(String link : links)
					{
						if(!getDomain(host.getUrl()).equals(getDomain(link)))
							addVisitedFromOther(link);

						if(!isDomainVisited(getDomain(link)))
						{
							double priority = 0;
							if(isVisitedFromOther(link))
								priority = visitedFromOtherDomains.get(link);
							PriorityUrl newUrl = new PriorityUrl(link, priority);
							urlQueue.enqueue(newUrl);
						}
						else
						{
							if(domainCount.get(getDomain(link)) < 5)
							{
								double priority = 0;
								if(isVisitedFromOther(link))
									priority = (visitedFromOtherDomains.get(link)) / (1 + domainCount.get(getDomain(link)));
								PriorityUrl newUrl = new PriorityUrl(link, priority);
								urlQueue.enqueue(newUrl);
							}
							else
							{
								crawlTree.addNewUrl(host.getUrl(), link + " - " + "LINKS PER DOMAIN REACHED");
							}
						}
	
						if(visitedHosts.size() > 100)
						{
							crawlTree.addNewUrl(host.getUrl(), link + " - " + "TOTAL LINKS LIMIT REACHED");
						}
					}
				}
				else
				{
					node = crawlTree.findUrl(host.getUrl(), crawlTree.getRoot());
					if(node != null)
					{
						String old = node.getData();
						node.setData(old + " - " + "ERROR WHILE READING");
					}
				}
			}
			catch(InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}	
		}
	}
}