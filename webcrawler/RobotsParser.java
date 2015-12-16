import java.net.*;
import java.io.*;
import java.util.*;

public class RobotsParser
{
	public RobotsParser(){}

	public HashMap<String, Boolean> parse(String host)
	{
		RedirectHandler redirectHandler = new RedirectHandler();
		String disallow = "Disallow:";
		String allow = "Allow:";
		String redirect = "Location: http://";

		int disallowCounter = 0;
		int allowCounter = 0;
		int redirectCounter = 0;
		boolean enterAllow = false;
		boolean enterDisallow = false;


		HashMap<String, Boolean> result = new HashMap<String, Boolean>();
		try
		{		    
		    Socket socket = new Socket(host, 80);
		    socket.setSoTimeout(10000);
			PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))); 
			
			writer.print("GET /robots.txt HTTP/1.1\r\n");
			writer.print("Host: " + host + "\r\n"); 
			writer.print("\r\n");
			writer.flush(); 
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
		    
		    String line = "";

			while((line = reader.readLine()) != null)
		    {
		    	String entry = "";
		        for(int i = 0 ; i < line.length() ; i++)
		        {
		        	if(disallowCounter == 9)
		        	{
		        		disallowCounter = 0;
		        		enterDisallow = true;
		        	}

		        	if(allowCounter == 6)
		        	{
		        		allowCounter = 0;
		        		enterAllow = true; 
		        	}

		        	if(redirectCounter == redirect.length())
		        	{
		        		redirectCounter = 0;
		        		String newUrl = redirectHandler.getRedirect(host, true);
		        		String correctUrl = "";
		        		for(int j = 0 ; j < newUrl.length() ; j++)
		        		{
		        			if(newUrl.charAt(j) == '/')
		        				break;
		        			correctUrl = correctUrl + newUrl.charAt(j);
		        		}
		        		return parse(correctUrl);
		        	}

		        	if(enterDisallow || enterAllow)
		        	{
		        		if(line.charAt(i) != ' ')
			        		entry = entry + line.charAt(i);
		        	}

		        	if(line.charAt(i) == disallow.charAt(disallowCounter))
		        	{
		        		disallowCounter++;
		        	}
		        	else
		        	{
		        		disallowCounter = 0;
		        	}

		        	if(line.charAt(i) == allow.charAt(allowCounter))
		        	{
		        		allowCounter++;
		        	}
		        	else
		        	{
		        		allowCounter = 0;
		        	}

		        	if(line.charAt(i) == redirect.charAt(redirectCounter))
		        	{
		        		redirectCounter++;
		        	}
		        	else
		        	{
		        		redirectCounter = 0;
		        	}
		        }
		        if(enterAllow)
		        {
		        	enterAllow = false;
		        	result.put(entry, true);
		        }
		        if(enterDisallow)
		        {
		        	enterDisallow = false;
		        	result.put(entry, false);
		        }
		    }
		}
		catch(SocketTimeoutException e)
		{
			// Nothing
		}
		catch(UnknownHostException e)
		{
			return null;
		}
		catch(IOException e)
		{
			result = null;
		}

		return result;
	}
}