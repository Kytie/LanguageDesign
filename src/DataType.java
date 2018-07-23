import org.antlr.v4.runtime.tree.*;

public class DataType {
	
	final Object data;
	
	public DataType(Object data)
	{
		this.data = data;
	}
	
	public Object getObject()
	{
		return data;
	}	
	
	public Integer INT()
	{
		String sData = data.toString();
		if(sData.contains("."))
		{
			int i = data.toString().indexOf(".");
			sData = data.toString().substring(0, i);
		}
		return Integer.valueOf(sData);
	}
	
	public Double DOUBLE()
	{
		return Double.valueOf(data.toString());
	}
	
	public Integer BOOL()
	{
		String sData = data.toString();
		if(sData.contains("."))
		{
			int i = data.toString().indexOf(".");
			sData = data.toString().substring(0, i);
		}
		
		int value = 0;
		if(Integer.valueOf(sData) != 0)
		{
			value = 1;
		}
		
		return value;
	}
	
	public String makeString()
	{
		return data.toString();
	}
	
	public ParseTree asTree()
	{
		return (ParseTree) data;
	}
}
