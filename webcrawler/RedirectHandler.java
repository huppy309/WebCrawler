import java.net.*;
import java.io.*;
import java.util.*;

public class RedirectHandler
{
	RedirectHandler() {}

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

	public static String getPath(String url)
	{
		String path = "";
		boolean enter = false;
		if(url.startsWith("http://"))
		{
			for(int i = 7 ; i < url.length() ; i++)
			{
				if(enter)
					path = path + url.charAt(i);
				if(url.charAt(i) == '/')
					enter = true;
			}
			return path;
		}
		else
		{
			for(int i = 0 ; i < url.length() ; i++)
			{
				if(enter)
					path = path + url.charAt(i);
				if(url.charAt(i) == '/')
					enter = true;
			}
			return path;
		}
	} 

	public String getRedirect(String host, boolean isRobots)
	{
		String result = null;
		try
		{
			Socket socket = new Socket(getDomain(host), 80);
			PrintWriter writer = new PrintWriter(socket.getOutputStream());
			
			if(!isRobots)
			{
				writer.print("GET " + getPath(host) + " HTTP/1.1\r\n");
				writer.print("Host: " + getDomain(host) + "\r\n"); 
				writer.print("\r\n");
				writer.flush(); 
			}
			else
			{
				writer.print("GET /robots.txt HTTP/1.1\r\n");
				writer.print("Host: " + host + "\r\n"); 
				writer.print("\r\n");
				writer.flush(); 
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String line = "";

			String target = "Location: http://";
			int targetCount = 0;
			boolean startNotingRedirectHost = false;
			String redirectHost = "";

			while((line = reader.readLine()) != null)
			{
				for(int i = 0 ; i < line.length() ; i++)
				{
					if(targetCount == target.length())
					{
						targetCount = 0;
						startNotingRedirectHost = true;
					}

					if(startNotingRedirectHost)
					{
						if(line.charAt(i) != '\n')
							redirectHost = redirectHost + line.charAt(i);
					}

					if(line.charAt(i) == target.charAt(targetCount))
					{
						targetCount++;
					}
					else
					{
						targetCount = 0;
					}
				}
				if(startNotingRedirectHost)
				{
					startNotingRedirectHost = false;
					return redirectHost;
				}
			}
			reader.close();
			socket.close(); 
		}
		catch(IOException e)
		{
			return null;
		}

		return result;
	}
}