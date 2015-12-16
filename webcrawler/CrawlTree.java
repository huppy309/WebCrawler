import java.util.*;

public class CrawlTree
{
	private Node root;

	public CrawlTree(Node _root)
	{
		root = _root;
	}

	public Node getRoot()
	{
		return root;
	}

	private int getLevel(Node node)
	{
		int level = 0;
		while(node.getParent() != null)
		{
			level++;
			node = node.getParent();
		}
		return level;
	}

	public synchronized Node findUrl(String url, Node node)
	{
		Stack<Node> nodeStack = new Stack<Node>();
		nodeStack.push(node);

		while(!nodeStack.empty())
		{
			Node poppedNode = nodeStack.pop();
			if(poppedNode.getData().equals(url))
				return poppedNode;
			
			ArrayList<Node> children = poppedNode.getChildren();
			for(Node child : children)
				nodeStack.push(child);
		}
		return null;
	}

	public synchronized void addNewUrl(String parentUrl, String newUrl)
	{
		Node parentNode = findUrl(parentUrl, root);
		if(parentNode != null)
			parentNode.insertChild(newUrl);
	}

	public void print(Node node)
	{
		Stack<Node> nodeStack = new Stack<Node>();
		nodeStack.push(node);

		while(!nodeStack.empty())
		{
			Node poppedNode = nodeStack.pop();
			int level = getLevel(poppedNode);

			for(int i = 0 ; i < level ; i++)
				System.out.print(" ");
			System.out.print(poppedNode.getData() + "\n");

			ArrayList<Node> children = poppedNode.getChildren();
			for(Node child : children)
				nodeStack.push(child);
		} 
	}
}

class Node
{
	private String data;
	private Node parent;
	private ArrayList<Node> children;

	public Node()
	{
		data = null;
		children = new ArrayList<Node>();
		parent = null;
	}

	public Node(String _data, Node _parent)
	{
		data = _data;
		children = new ArrayList<Node>();
		parent = _parent;
	}

	public ArrayList<Node> getChildren()
	{
		return children;
	}

	public String getData()
	{
		return data;
	}

	public void setData(String _data)
	{
		data = _data;
	}

	public Node getParent()
	{
		return parent;
	}

	public void setParent(Node _parent)
	{
		_parent.insertChild(data);
		parent = _parent;
	}

	public void insertChild(String _data)
	{
		Node child = new Node(_data, this);
		children.add(child);
	}

	public boolean isRoot()
	{
		return (parent == null);
	}

}