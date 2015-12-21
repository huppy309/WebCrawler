public abstract class StateTag
{
	State state;

	public abstract String parse(IntWrapper count, char[] buffer);
}

class IntWrapper
{
	public int value;

	public IntWrapper(int val)
	{
		value = val;
	}

	public void set(int _value)
	{
		value = _value;
	}
}

class Start extends StateTag
{
	private String redirect;
	private IntWrapper redirectCounter;

	public Start()
	{
		state = State.START;
		redirect = "Location: http://";
		redirectCounter = new IntWrapper(0);
	}

	@Override
	public String parse(IntWrapper count, char[] buffer)
	{
		String tag = "";

		while(count.value < 100)
		{	
			if(buffer[count.value] == redirect.charAt(redirectCounter.value))
				redirectCounter.value++;
			else
				redirectCounter.value = 0;

			if(buffer[count.value] == '<')
			{
				tag = "<";
				count.value++;
				return tag;
			}

			if(redirectCounter.value == redirect.length())
			{
				redirectCounter.set(0);
				tag = "redirect";
				count.value++;
				return tag;
			}
			count.value++;
		}
		
		return tag;
	}
}

class TagStart extends StateTag
{
	private String startA;
	private IntWrapper startACounter;

	private String startComment;
	private IntWrapper startCommentCounter;

	private String startScript;
	private IntWrapper startScriptCounter;

	public TagStart()
	{
		state = State.TAGSTART;
		startA = "a";
		startACounter = new IntWrapper(0);
		startComment = "!--";
		startCommentCounter = new IntWrapper(0);
		startScript = "script";
		startScriptCounter = new IntWrapper(0);
	}

	@Override
	public String parse(IntWrapper count, char[] buffer)
	{
		String tag = "";

		while(count.value < 100)
		{
 			if(buffer[count.value] == startA.charAt(startACounter.value))
				startACounter.value++;
			else
				startACounter.value = 0;


			if(buffer[count.value] == startScript.charAt(startScriptCounter.value))
				startScriptCounter.value++;
			else
				startScriptCounter.value = 0;



			if(buffer[count.value] == startComment.charAt(startCommentCounter.value))
				startCommentCounter.value++;
			else
				startCommentCounter.value = 0;

			if(buffer[count.value] == '<')
			{
				startCommentCounter.value = 0;
				startScriptCounter.value = 0;
				startACounter.value = 0;
				count.value++;
				tag = "<";
				return tag;
			}
			else
			{
				if(startCommentCounter.value == 0 && startScriptCounter.value == 0 && startACounter.value == 0)
				{
					tag = "start";
					return tag;
				}
			}

			if(startCommentCounter.value == 3)
			{
				startCommentCounter.value = 0;
				tag = "<!--";
				count.value++;
				return tag;
			}

			if(startACounter.value == 1)
			{
				startACounter.value = 0;
				tag = "<a";
				count.value++;
				return tag;
			}

			if(startScriptCounter.value == 6)
			{
				startScriptCounter.value = 0;
				tag = "<script";
				count.value++;
				return tag;
			}

			count.value++;
		}
		
		return tag;
	}
}

class Script extends StateTag
{
	private String endScript;
	private IntWrapper endScriptCounter;


	public Script()
	{
		state = State.SCRIPT;
		endScript = "/script>";
		endScriptCounter = new IntWrapper(0);
	}

	@Override
	public String parse(IntWrapper count, char[] buffer)
	{
		String tag = "";

		while(count.value < 100)
		{
			if(buffer[count.value] == endScript.charAt(endScriptCounter.value))
				endScriptCounter.value++;
			else
			{
				endScriptCounter.value = 0;
				if(buffer[count.value] == endScript.charAt(0))
					endScriptCounter.value++;
			}

			if(endScriptCounter.value == 8)
			{
				endScriptCounter.value = 0;
				tag = "/script>";
				count.value++;
				return tag;
			}

			count.value++;
		}
		return tag;
	}
}

class Comment extends StateTag
{
	private String endComment;
	private IntWrapper endCommentCounter;
	
	public Comment()
	{
		state = State.COMMENT;
		endComment = "-->";
		endCommentCounter = new IntWrapper(0);
	}

	@Override
	public String parse(IntWrapper count, char[] buffer)
	{
		String tag = "";

		while(count.value < 100)
		{
			if(buffer[count.value] == endComment.charAt(endCommentCounter.value))
				endCommentCounter.value++;
			else
			{
				endCommentCounter.value = 0;
				if(buffer[count.value] == endComment.charAt(0))
					endCommentCounter.value++;
			}

			if(endCommentCounter.value == 3)
			{
				endCommentCounter.value = 0;
				tag = "-->";
				count.value++;
				return tag;
			}

			count.value++;
		}
		return tag;
	}
}

class A extends StateTag
{
	private String startUrl;
	private IntWrapper startUrlCounter;
	
	public A()
	{
		state = State.ALINK;
		startUrl = "href=" + '"';
		startUrlCounter = new IntWrapper(0);
	}

	@Override
	public String parse(IntWrapper count, char[] buffer)
	{
		String tag = "";

		while(count.value < 100)
		{
			if(buffer[count.value] == '>')
			{
				tag = ">";
				count.value++;
				return tag;
			}

			if(buffer[count.value] == startUrl.charAt(startUrlCounter.value))
				startUrlCounter.value++;
			else
			{
				startUrlCounter.value = 0;
				if(buffer[count.value] == startUrl.charAt(0))
					startUrlCounter.value++;
			}

			if(startUrlCounter.value == 6)
			{
				startUrlCounter.value = 0;
				count.value++;
				return startUrl;
			}

			count.value++;
		}
		return tag;
	}
}

class Href extends StateTag
{
	private String url;

	public Href()
	{
		state = State.HREF;
		url = "";
	}

	@Override
	public String parse(IntWrapper count, char[] buffer)
	{
		String tag = "none";

		while(count.value < 100)
		{
			if(buffer[count.value] == '"')
			{
				count.value++;
				String result = url;
				url = "";
				return result;
			}

			url = url + buffer[count.value];
			count.value++;
		}
		
		return tag;
	}
}