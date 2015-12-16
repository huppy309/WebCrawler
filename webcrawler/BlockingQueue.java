import java.util.*;

public class BlockingQueue
{
	private ArrayList<PriorityUrl> urlPriorityList;

	public BlockingQueue()
	{
		urlPriorityList = new ArrayList<PriorityUrl>();
	}

	public synchronized int getSize()
	{
		return urlPriorityList.size();
	}

	public void insert(PriorityUrl urlNew)
	{
		for(int i = 0 ; i < urlPriorityList.size() ; i++)
		{
			if(urlNew.getPriority() < urlPriorityList.get(i).getPriority())
				continue;
			urlPriorityList.add(i, urlNew);
			return;
		}
		urlPriorityList.add(urlNew);
	}

	public synchronized void enqueue(PriorityUrl urlNew)
	{
		insert(urlNew);
	}	

	public synchronized PriorityUrl dequeue() throws InterruptedException
	{
		while(urlPriorityList.size() == 0)
			wait();
		if(urlPriorityList.size() > 0)
			notify();
		return urlPriorityList.remove(urlPriorityList.size() - 1);
	}
}

class PriorityUrl
{
	private String url;
	private double priority;

	public PriorityUrl()
	{
		url = "";
		priority = 0;
	}

	public PriorityUrl(String _url, double _priority)
	{
		url = _url;
		priority = _priority;
	}

	public double getPriority()
	{
		return priority;
	}

	public String getUrl()
	{
		return url;
	}
}