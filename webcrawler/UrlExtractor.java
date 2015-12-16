import java.net.*;
import java.io.*;
import java.util.*;

public class UrlExtractor
{
	private Start start;
	private TagStart tagStart;
	private Script script;
	private Comment comment; 
	private A a;
	private Href href ;


	public UrlExtractor()
	{
		start = new Start();
		tagStart = new TagStart();
		script = new Script();
		comment = new Comment();
		a = new A();
		href = new Href();
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

	public ArrayList<String> extractUrl(String inputUrl)
	{
		RedirectHandler handler = new RedirectHandler();
		ArrayList<String> urlList = null;
		try
		{
			Socket socket = new Socket(getDomain(inputUrl), 80);
			socket.setSoTimeout(3000);

			PrintWriter writer = new PrintWriter(socket.getOutputStream());
			writer.print("GET /" + getPath(inputUrl) + " HTTP/1.1\r\n");
			writer.print("Host: " + getDomain(inputUrl) + "\r\n"); 
			writer.print("\r\n");
			writer.flush();

			int end = 0;
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			int bufferSize = 100;
			char[] buffer;
			buffer = new char[bufferSize];
			urlList = new ArrayList<String>();
			StateTag current = start;
			
			String result;

			while((end = reader.read(buffer, 0, bufferSize)) != -1)
			{
				IntWrapper count = new IntWrapper(0);
				while(count.value < 100)
				{
					switch(current.state)
					{
						case START:
							result = current.parse(count, buffer);
							if(result.equals("<"))
								current = tagStart;
							else if(result.equals("redirect"))
							{
								current = start;
								String correctUrl = handler.getRedirect(inputUrl, false);
								return extractUrl(correctUrl);
							}

							break;

						case TAGSTART:
							result = current.parse(count, buffer);
							if(result.equals("start"))
								current = start;
							else if(result.equals("<"))
								current = tagStart;
							else if(result.equals("<a"))
								current = a;
							else if(result.equals("<script"))
								current = script;
							else if(result.equals("<!--"))
								current = comment;
							else
								current = tagStart;

							break;

						case SCRIPT:
							result = current.parse(count, buffer);
							if(result.equals("/script>"))
								current = start;

							break;

						case COMMENT:
							result = current.parse(count, buffer);
							if(result.equals("-->"))
								current = start;

							break;

						case ALINK:
							result = current.parse(count, buffer);
							if(result.equals("href=" + '"'))
							{
								current = href;
							}
							else if(result.equals(">"))
								current = start;

							break;

						case HREF:
							result = current.parse(count, buffer);
							if(result.length() != 0 && !result.equals("none"))
							{
								boolean found = false;
								for(String link : urlList)
								{	
									if(link.equals(result))
									{
										found = true;
										break;
									}
								}

								if(found == false)
									urlList.add(result);
								current = a;
							}	
							break;
					}
				}
			}

			reader.close();
			socket.close();

			int i = 0;
			while(i < urlList.size())
			{
				if(urlList.get(i).equals("#"))
					urlList.remove(i);
				else if(urlList.get(i).charAt(0) == '?')
					urlList.remove(i);
				else if(urlList.get(i).charAt(0) == '/')
				{
					String newUrl = "http://" + getDomain(inputUrl) + urlList.get(i);
					urlList.remove(i);
					urlList.add(newUrl);
				}
				else 
					i++;
			}
		}
		catch(SocketTimeoutException e)
		{
			return urlList;
		}
		catch(UnknownHostException e)
		{
			return null;
		}
		catch(IOException e)
		{
			return null;
		}
		

		return urlList;
	} 
}