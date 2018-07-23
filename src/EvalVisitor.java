import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.antlr.v4.runtime.tree.*;

public class EvalVisitor extends GrammarBaseVisitor<DataType>
{	
	HashMap<String, DataType> function = new HashMap<String, DataType>();
	HashMap<String, HashMap<String, List<String>>> funcTypesStack = new HashMap<String, HashMap<String, List<String>>>();
	HashMap<String, HashMap<String, List<DataType>>> variableStack = new HashMap<String, HashMap<String, List<DataType>>>();
	HashMap<String, HashMap<String, String>> typeStack = new HashMap<String, HashMap<String, String>>();
	HashMap<String, HashMap<String, String>> constStack = new HashMap<String, HashMap<String, String>>();
	int scopeLevel = 1;
	int scopeLimit = 1;
	
	
	public EvalVisitor()
	{
		variableStack.put(Integer.toString(scopeLevel), new HashMap<String, List<DataType>>());
		typeStack.put(Integer.toString(scopeLevel), new HashMap<String, String>());
		constStack.put(Integer.toString(scopeLevel), new HashMap<String, String>());
	}
	
	public DataType visitPrintFunc(GrammarParser.PrintFuncContext ctx)
	{
		String newLine = System.getProperty("line.separator");
		String outputString = "";
		
		if(ctx.getChildCount() > 3)
		{
			for(int i = 0; i < ctx.expr().size(); i++)
			{
				DataType data = visit(ctx.expr(i));
				int level = scopeLevel;
				boolean isVar = false;
				
				while(level > scopeLimit-1)
				{
					if(variableStack.containsKey(Integer.toString(level)))
					{
						if(variableStack.get(Integer.toString(level)).containsKey(data.makeString()))
						{
							if(typeStack.get(Integer.toString(level)).get(data.makeString()).contains("List"))
							{
								for(int k = 0; k < variableStack.get(Integer.toString(level)).get(data.makeString()).size(); k++)
								{
									outputString += variableStack.get(Integer.toString(level)).get(data.makeString()).get(k).makeString();
									if(k != variableStack.get(Integer.toString(level)).get(data.makeString()).size()-1)
									{
										outputString += ",";
									}
									isVar = true;
								}
							}
							else
							{
								for(int k = 0; k < variableStack.get(Integer.toString(level)).get(data.makeString()).size(); k++)
								{
									outputString += variableStack.get(Integer.toString(level)).get(data.makeString()).get(k).makeString();
								}
								level = scopeLimit;
								isVar = true;
							}
						}
					}
					level--;
				}
				
				if(!isVar)
				{
					if(data.makeString().contains("\""))
					{
						int index = data.makeString().lastIndexOf("\"");
						data = new DataType(data.makeString().substring(1, index));
						outputString += (data.makeString());
					}
					else
					{
						outputString += (data.makeString());
					}
				}
			}
		}
		
		System.out.println(newLine + outputString);
		
		return new DataType(0);
	}

	public DataType visitInt(GrammarParser.IntContext ctx)
	{
		return new DataType(Integer.valueOf(ctx.getText()));
	}
	
	public DataType visitDouble(GrammarParser.DoubleContext ctx)
	{
		return new DataType(Double.valueOf(ctx.getText()));
	}
	
	public DataType visitBool(GrammarParser.BoolContext ctx)
	{
		return new DataType(ctx.getText());
	}
	
	public DataType visitString(GrammarParser.StringContext ctx)
	{
		return new DataType(ctx.getText());
	}
	
	public DataType visitVariable(GrammarParser.VariableContext ctx)
	{
		return new DataType(ctx.ID().getText());
	}
	
	public DataType visitOperator(GrammarParser.OperatorContext ctx)
	{
		return new DataType(ctx.getChild(0).getText());
	}
	
	//VARIABLES-----------------------------------------------------------------------
	public DataType visitInitialise(GrammarParser.InitialiseContext ctx)
	{
		String type = ctx.type().getText();
		String actualType = "";
		List<DataType> data = new ArrayList<DataType>();
		data.add(visit(ctx.expr()));
		String scope = Integer.toString(scopeLevel);
		int level = scopeLevel;
		boolean varFound = false;
		
		while(level > (scopeLimit -1))
		{
			if(variableStack.containsKey(Integer.toString(level)))
			{
				if(variableStack.get(Integer.toString(level)).containsKey(data.get(0).makeString()))
				{	
					data.set(0, variableStack.get(Integer.toString(level)).get(data.get(0).makeString()).get(0));
					level = scopeLimit;
					varFound = true;
				}
			}
			level--;
		}
		
		if(data.get(0).getObject() instanceof Integer)
		{
			if(type.equals("bool"))
			{
				if(data.get(0).makeString().equals("0") || data.get(0).makeString().equals("1"))
				{
					actualType = "bool";
				}
			}
			else if(type.equals("int"))
			{
				actualType = "int";
			}
		}
		else if(data.get(0).getObject() instanceof Double)
		{
			actualType = "double";
		}
		else if(data.get(0).makeString().contains("\""))
		{
			actualType = "string";
			int index = data.get(0).makeString().lastIndexOf("\"");
			data.set(0, new DataType(data.get(0).makeString().substring(1, index)));
		}
		else
		{
			if(data.get(0).makeString().equals("true"))
			{
				if(type.equals("bool"))
				{
					actualType = "bool";
					data.set(0, new DataType(1));
				}
			}
			else if(data.get(0).makeString().equals("false"))
			{
				if(type.equals("bool"))
				{
					actualType = "bool";
					data.set(0, new DataType(0));
				}
			}
			else if(data.get(0).getObject() instanceof String)
			{
				if(varFound)
				{
					actualType = "string";
				}
				else
				{
					if(type.equals("string"))
					{
						System.out.println("When adding data to a string it must be formatted with the \"\" marks unless it is data from a variable");
					}
				}
			}
		}
		
		if(type.equals(actualType))
		{
			if(variableStack.containsKey(scope) && typeStack.containsKey(scope))
			{
				if(variableStack.get(scope).containsKey(ctx.ID().getText()))
				{
					System.out.println("Variable " + ctx.ID().getText() + " already exists");
				}
				else
				{
					variableStack.get(scope).put(ctx.ID().getText(), data);
					typeStack.get(scope).put(ctx.ID().getText(), actualType);
				}
			}
		}
		else
		{
			System.out.println("Cannot initialise variable with data of a different type");
		}
		
		return new DataType(ctx.ID().getText());
	}
	
	public DataType visitConstInitialise(GrammarParser.ConstInitialiseContext ctx)
	{
		String type = ctx.type().getText();
		String actualType = "";
		List<DataType> data = new ArrayList<DataType>();
		data.add(visit(ctx.expr()));
		String scope = Integer.toString(scopeLevel);
		int level = scopeLevel;
		boolean varFound = false;
		
		while(level > (scopeLimit -1))
		{
			if(variableStack.containsKey(Integer.toString(level)))
			{
				if(variableStack.get(Integer.toString(level)).containsKey(data.get(0).makeString()))
				{	
					data.set(0, variableStack.get(Integer.toString(level)).get(data.get(0).makeString()).get(0));
					level = scopeLimit;
					varFound = true;
				}
			}
			level--;
		}
		
		if(data.get(0).getObject() instanceof Integer)
		{
			if(type.equals("bool"))
			{
				if(data.get(0).makeString().equals("0") || data.get(0).makeString().equals("1"))
				{
					actualType = "bool";
				}
			}
			else if(type.equals("int"))
			{
				actualType = "int";
			}
		}
		else if(data.get(0).getObject() instanceof Double)
		{
			actualType = "double";
		}
		else if(data.get(0).makeString().contains("\""))
		{
			actualType = "string";
			int index = data.get(0).makeString().lastIndexOf("\"");
			data.set(0, new DataType(data.get(0).makeString().substring(1, index)));
		}
		else
		{
			if(data.get(0).makeString().equals("true"))
			{
				if(type.equals("bool"))
				{
					actualType = "bool";
					data.set(0, new DataType(1));
				}
			}
			else if(data.get(0).makeString().equals("false"))
			{
				if(type.equals("bool"))
				{
					actualType = "bool";
					data.set(0, new DataType(0));
				}
			}
			else if(data.get(0).getObject() instanceof String)
			{
				if(varFound)
				{
					actualType = "string";
				}
				else
				{
					if(type.equals("string"))
					{
						System.out.println("When adding data to a string it must be formatted with the \"\" marks unless it is data from a variable");
					}
				}
			}
		}
		
		if(type.equals(actualType))
		{
			if(variableStack.containsKey(scope) && typeStack.containsKey(scope) && constStack.containsKey(scope))
			{
				if(variableStack.get(scope).containsKey(ctx.ID().getText()))
				{
					System.out.println("Variable " + ctx.ID().getText() + " already exists");
				}
				else
				{
					variableStack.get(scope).put(ctx.ID().getText(), data);
					typeStack.get(scope).put(ctx.ID().getText(), actualType);
					constStack.get(scope).put(ctx.ID().getText(), "const");
				}
			}
		}
		else
		{
			System.out.println("Your variable type and data do not match");
		}
		
		return new DataType(0);
	}
	
	
	
	public DataType visitAssignFromVariable(GrammarParser.AssignFromVariableContext ctx)
	{
		String left =  ctx.ID(0).getText();
		String right =  ctx.ID(1).getText();
		int level = scopeLevel;
		boolean constant = false;
		int leftLevel = 0;
		int rightLevel = 0;
		List<DataType> data = new ArrayList<DataType>();
		while(level > (scopeLimit -1))
		{
			if(constStack.containsKey(Integer.toString(level)))
			{
				if(constStack.get(Integer.toString(level)).containsKey(left))
				{	
					System.out.println("Cannot reassign a constant");
					level = scopeLimit;
					constant = true;
				}
			}
			level--;
		}
		if(!constant)
		{
			level = scopeLevel;
			while(level > (scopeLimit -1))
			{
				if(variableStack.containsKey(Integer.toString(level)))
				{
					if(variableStack.get(Integer.toString(level)).containsKey(left))
					{	
						if(leftLevel == 0)
						{
							leftLevel = level;
						}
					}
				}
				if(variableStack.containsKey(Integer.toString(level)))
				{
					if(variableStack.get(Integer.toString(level)).containsKey(right))
					{
						if(rightLevel == 0)
						{
							rightLevel = level;
						}
					}
				}
				level--;
			}
			
			if(leftLevel > 0 && rightLevel > 0)
			{
				if(!typeStack.get(Integer.toString(leftLevel)).get(left).equals("string") && typeStack.get(Integer.toString(rightLevel)).get(right).equals("string"))
				{
					System.out.println("Cannot convert string to a different type");
				}
				else
				{
					if(typeStack.get(Integer.toString(leftLevel)).get(left).equals("int"))
					{
						data.add(new DataType(variableStack.get(Integer.toString(rightLevel)).get(right).get(0).INT()));
						variableStack.get(Integer.toString(leftLevel)).put(left, data);
					}
					else if(typeStack.get(Integer.toString(leftLevel)).get(left).equals("double"))
					{
						data.add(new DataType(variableStack.get(Integer.toString(rightLevel)).get(right).get(0).DOUBLE()));
						variableStack.get(Integer.toString(leftLevel)).put(left, data);
					}
					else if(typeStack.get(Integer.toString(leftLevel)).get(left).equals("bool"))
					{
						data.add(new DataType(variableStack.get(Integer.toString(rightLevel)).get(right).get(0).BOOL()));
						variableStack.get(Integer.toString(leftLevel)).put(left, data);
					}
					else if(typeStack.get(Integer.toString(leftLevel)).get(left).equals("string"))
					{
						data.add(new DataType(variableStack.get(Integer.toString(rightLevel)).get(right).get(0).makeString()));
						variableStack.get(Integer.toString(leftLevel)).put(left, data);
					}
					else if(typeStack.get(Integer.toString(leftLevel)).get(left).equals("List<int>") && typeStack.get(Integer.toString(rightLevel)).get(right).equals("List<int>"))
					{
						variableStack.get(Integer.toString(leftLevel)).put(left, variableStack.get(Integer.toString(rightLevel)).get(right));
					}
					else if(typeStack.get(Integer.toString(leftLevel)).get(left).equals("List<double>") && typeStack.get(Integer.toString(rightLevel)).get(right).equals("List<double>"))
					{
						variableStack.get(Integer.toString(leftLevel)).put(left, variableStack.get(Integer.toString(rightLevel)).get(right));
					}
					else if(typeStack.get(Integer.toString(leftLevel)).get(left).equals("List<bool>") && typeStack.get(Integer.toString(rightLevel)).get(right).equals("List<bool>"))
					{
						variableStack.get(Integer.toString(leftLevel)).put(left, variableStack.get(Integer.toString(rightLevel)).get(right));
					}
					else if(typeStack.get(Integer.toString(leftLevel)).get(left).equals("List<string>") && typeStack.get(Integer.toString(rightLevel)).get(right).equals("List<string>"))
					{
						variableStack.get(Integer.toString(leftLevel)).put(left, variableStack.get(Integer.toString(rightLevel)).get(right));
					}
					else
					{
						System.out.println("could not assign " + left + " to " + right);
					}
				}
			}
			else
			{
				if(leftLevel < 1)
				{
					System.out.println("Left hand side variable not found");
				}
				
				if(rightLevel < 1)
				{
					System.out.println("Right hand side variable not found");
				}
			}
		}
		return new DataType(ctx.ID(0).getText());
	}
	
	public DataType visitAssignFromExpression(GrammarParser.AssignFromExpressionContext ctx)
	{
		boolean constant = false;
		int level = scopeLevel;
		String left =  ctx.ID().getText();
		DataType data = visit(ctx.expr());
		boolean leftFound = false;
		while(level > (scopeLimit -1))
		{
			if(constStack.containsKey(Integer.toString(level)))
			{
				if(constStack.get(Integer.toString(level)).containsKey(left))
				{	
					System.out.println("Cannot reassign a constant");
					level = scopeLimit;
					constant = true;
				}
			}
			level--;
		}
		
		if(!constant)
		{
			level = scopeLevel;
			
			while(level > (scopeLimit -1))
			{
				if(variableStack.containsKey(Integer.toString(level)))
				{
					if(variableStack.get(Integer.toString(level)).containsKey(left))
					{
						leftFound = true;
						boolean rightFound = false;
						int levelRight = scopeLevel;
						//checks for the returnedData variable returned from a function call
						while(levelRight > (scopeLimit -1))
						{
							if(variableStack.containsKey(Integer.toString(levelRight)))
							{
								if(variableStack.get(Integer.toString(levelRight)).containsKey(data.makeString()))
								{
									variableStack.get(Integer.toString(level)).put(left, variableStack.get(Integer.toString(levelRight)).get(data.makeString()));
									levelRight = scopeLimit;
									rightFound = true;
								}
							}
							levelRight--;
						}
						
						if(!rightFound)
						{	
							if(!typeStack.get(Integer.toString(level)).get(left).equals("string") && data.makeString().contains("\""))
							{
								System.out.println("Cannot convert string to a different type");
							}
							else
							{
								if(variableStack.get(Integer.toString(level)).get(left).get(0).getObject() instanceof Integer)
								{
									if(typeStack.get(Integer.toString(level)).get(left).equals("bool"))
									{						
										if(data.makeString().equals("true"))
										{
											List<DataType> newList = new ArrayList<DataType>();
											newList.add(new DataType(1));
											variableStack.get(Integer.toString(level)).put(left, newList);
											level = scopeLimit;
										}
										else if(data.makeString().equals("false"))
										{
											List<DataType> newList = new ArrayList<DataType>();
											newList.add(new DataType(0));
											variableStack.get(Integer.toString(level)).put(left, newList);
											level = scopeLimit;
										}
										else
										{
											List<DataType> newList = new ArrayList<DataType>();
											newList.add(new DataType(data.BOOL()));
											variableStack.get(Integer.toString(level)).put(left, newList);
											level = scopeLimit;
										}
									}
									else
									{
										List<DataType> newList = new ArrayList<DataType>();
										newList.add(new DataType(data.INT()));
										variableStack.get(Integer.toString(level)).put(left, newList);
										level = scopeLimit;
									}
								}
								else if(variableStack.get(Integer.toString(level)).get(left).get(0).getObject() instanceof Double)
								{
									List<DataType> newList = new ArrayList<DataType>();
									newList.add(new DataType(data.DOUBLE()));
									variableStack.get(Integer.toString(level)).put(left, newList);
									level = scopeLimit;
								}
								else if(variableStack.get(Integer.toString(level)).get(left).get(0).getObject() instanceof String)
								{
									if(data.makeString().contains("\""))
									{
										int index = data.makeString().lastIndexOf("\"");
										List<DataType> newList = new ArrayList<DataType>();
										newList.add(new DataType(data.makeString().substring(1, index)));
										variableStack.get(Integer.toString(level)).put(left, newList);
										level = scopeLimit;
									}
									else
									{
										System.out.println("When adding data to a string it must be formatted with the \"\" marks unless it is data from a variable");
									}
								}
							}
						}
					}
				}
				else
				{
					System.out.println("Stack Level not found, stack issue");
				}
				level--;
			}
		}
		if(!leftFound)
		{
			if(!constant)
			{
				System.out.println("Variable on left does not exist");
			}
		}
		
		return new DataType(ctx.ID().getText());
	}
	
	public DataType visitInitialiseList(GrammarParser.InitialiseListContext ctx)
	{
		String varName = ctx.ID().getText();
		if(variableStack.containsKey(Integer.toString(scopeLevel)) && typeStack.containsKey(Integer.toString(scopeLevel)))
		{
			if(variableStack.get(Integer.toString(scopeLevel)).containsKey(varName))
			{
				System.out.println("Variable " + ctx.ID().getText() + " already exists");
			}
			else
			{
				variableStack.get(Integer.toString(scopeLevel)).put(varName, new ArrayList<DataType>());
				String fullType = "List<" + ctx.type().getText() + ">";
				typeStack.get(Integer.toString(scopeLevel)).put(varName, fullType);
			}
		}
		return new DataType(0);
	}
	
	public DataType visitInitialiseListWithData(GrammarParser.InitialiseListWithDataContext ctx)
	{
		String varName = ctx.ID().getText();
		String type = ctx.type().getText();
		List<DataType> newList = new ArrayList<DataType>();
		boolean failed = false;
		int level = 0;
		
		if(variableStack.get(Integer.toString(scopeLevel)).containsKey(varName))
		{
			System.out.println("Variable " + ctx.ID().getText() + " already exists");
			failed = true;
		}
		else
		{
			for(int i = 0; i < ctx.expr().size(); i++)
			{
				DataType data = visit(ctx.expr(i));
				boolean found = false;
				level = scopeLevel;
				
				while(level > scopeLimit -1)
				{
					if(variableStack.containsKey(Integer.toString(level)))
					{
						if(variableStack.get(Integer.toString(level)).containsKey(data.makeString()) && typeStack.get(Integer.toString(level)).containsKey(data.makeString()))
						{
							if(typeStack.get(Integer.toString(level)).get(data.makeString()).contains(type))
							{
								if(variableStack.get(Integer.toString(level)).get(data.makeString()).size() > 1)
								{
									if(ctx.expr().size() == 1)
									{
										newList = new ArrayList<DataType>(variableStack.get(Integer.toString(level)).get(data.makeString()));
										level = scopeLimit;
										found = true;
									}
									else
									{
										System.out.println("You cannot initialise a list with multiple Lists");
										i = ctx.expr().size();
										failed = true;
									}
								}
								else
								{
									newList.add(variableStack.get(Integer.toString(level)).get(data.makeString()).get(0));
									level = scopeLimit;
									found = true;
								}
								
							}
							else if(type.contains("bool") && typeStack.get(Integer.toString(level)).get(data.makeString()).contains("int"))
							{
								if(variableStack.get(Integer.toString(level)).get(data.makeString()).get(0).makeString().equals("0") || variableStack.get(Integer.toString(level)).get(data.makeString()).get(0).makeString().equals("1"))
								{
									newList.add(variableStack.get(Integer.toString(level)).get(data.makeString()).get(0));
									level = scopeLimit;
									found = true;
								}
								else
								{
									System.out.println("You cannot initialise a list with values that do not match the data type");
									i = ctx.expr().size();
									failed = true;
								}
							}
							else
							{
								System.out.println("You cannot initialise a list with values that do not match the data type");
								i = ctx.expr().size();
								failed = true;
							}
						}
					}
					level--;
				}
				
				if(!found)
				{
					if(data.getObject() instanceof Integer)
					{
						if(type.equals("bool"))
						{
							if(data.makeString().equals("0") || data.makeString().equals("1"))
							{
								newList.add(data);
							}
							else
							{
								System.out.println("You cannot initialise a list of type bool with integer values bigger than 1 or smaller than 0");
								i = ctx.expr().size();
								failed = true;
							}
						}
						else if(type.equals("int"))
						{
							newList.add(data);
						}
						else
						{
							System.out.println("You cannot initialise a list with values that do not match the data type");
							i = ctx.expr().size();
							failed = true;
						}
					}
					else if(data.getObject() instanceof Double && type.equals("double"))
					{
						newList.add(data);
					}
					else if(type.equals("bool"))
					{
						if(data.makeString().equals("true"))
						{
							newList.add(new DataType(1));
						}
						else if(data.makeString().equals("false"))
						{
							newList.add(new DataType(0));
						}
						else
						{
							failed = true;
						}
					}
					else if(data.makeString().contains("\"") && type.equals("string"))
					{
						int index = data.makeString().lastIndexOf("\"");
						newList.add(new DataType(data.makeString().substring(1, index)));
					}
					else
					{
						if(found)
						{
							if(data.getObject() instanceof String)
							{
							
								newList.add(data);
							}
							else
							{
								System.out.println("When adding data to a string it must be formatted with the \"\" marks unless it is data from a variable");
								i = ctx.expr().size();
								failed = true;
							}
						}
						else
						{
							System.out.println("You cannot initialise a list with values that do not match the data type");
							i = ctx.expr().size();
							failed = true;
						}
					}
				}
			}
		}
		if(!failed)
		{
			if(variableStack.containsKey(Integer.toString(scopeLevel)) && typeStack.containsKey(Integer.toString(scopeLevel)))
			{
				variableStack.get(Integer.toString(scopeLevel)).put(varName, newList);
				String fullType = "List<" + ctx.type().getText() + ">";
				typeStack.get(Integer.toString(scopeLevel)).put(varName, fullType);
			}
		}
		return new DataType(0);
	}
	
		
	
	
	//FUNCTIONS-----------------------------------------------------------------------
	public DataType visitAddFunc(GrammarParser.AddFuncContext ctx)
	{
		DataType left = visit(ctx.expr(0));
		DataType right = visit(ctx.expr(1));
		
		boolean foundLeft = false;
		boolean foundRight = false;
		
		int level = scopeLevel;
		
		while(level > scopeLimit -1)
		{
			if(variableStack.containsKey(Integer.toString(level)))
			{
				if(variableStack.get(Integer.toString(level)).containsKey(left.makeString()))
				{
					if(foundLeft == false)
					{
						foundLeft = true;
						left = variableStack.get(Integer.toString(level)).get(left.makeString()).get(0);
					}
				}
				
				if(variableStack.get(Integer.toString(level)).containsKey(right.makeString()))
				{
					if(foundRight == false)
					{
						foundRight = true;
						right = variableStack.get(Integer.toString(level)).get(right.makeString()).get(0);
					}
				}
			}
			level--;
		}
		
		DataType result = new DataType(0);
		if(left.getObject() instanceof String || right.getObject() instanceof String)
		{
			System.out.println("Cannot use strings in ADD function");
		}
		else
		{
			result = new DataType(left.DOUBLE() + right.DOUBLE());
		}
		return result;	
    }
	
	public DataType visitSubFunc(GrammarParser.SubFuncContext ctx)
	{
		DataType left = visit(ctx.expr(0));
		DataType right = visit(ctx.expr(1));
		
		boolean foundLeft = false;
		boolean foundRight = false;
		
		int level = scopeLevel;
		
		while(level > scopeLimit -1)
		{
			if(variableStack.containsKey(Integer.toString(level)))
			{
				if(variableStack.get(Integer.toString(level)).containsKey(left.makeString()))
				{
					if(foundLeft == false)
					{
						foundLeft = true;
						left = variableStack.get(Integer.toString(level)).get(left.makeString()).get(0);
					}
				}
				
				if(variableStack.get(Integer.toString(level)).containsKey(right.makeString()))
				{
					if(foundRight == false)
					{
						foundRight = true;
						right = variableStack.get(Integer.toString(level)).get(right.makeString()).get(0);
					}
				}
			}
			level--;
		}
		
		DataType result = new DataType(0);
		if(left.getObject() instanceof String || right.getObject() instanceof String)
		{
			System.out.println("Cannot use strings in SUB function");
		}
		else
		{
			result = new DataType(left.DOUBLE() - right.DOUBLE());
		}
		return result;	
    }
	
	public DataType visitMulFunc(GrammarParser.MulFuncContext ctx)
	{
		DataType left = visit(ctx.expr(0));
		DataType right = visit(ctx.expr(1));
		
		boolean foundLeft = false;
		boolean foundRight = false;
		
		int level = scopeLevel;
		
		while(level > scopeLimit -1)
		{
			if(variableStack.containsKey(Integer.toString(level)))
			{
				if(variableStack.get(Integer.toString(level)).containsKey(left.makeString()))
				{
					if(foundLeft == false)
					{
						foundLeft = true;
						left = variableStack.get(Integer.toString(level)).get(left.makeString()).get(0);
					}
				}
				
				if(variableStack.get(Integer.toString(level)).containsKey(right.makeString()))
				{
					if(foundRight == false)
					{
						foundRight = true;
						right = variableStack.get(Integer.toString(level)).get(right.makeString()).get(0);
					}
				}
			}
			level--;
		}
		
		DataType result = new DataType(0);
		if(left.getObject() instanceof String || right.getObject() instanceof String)
		{
			System.out.println("Cannot use strings in MUL function");
		}
		else
		{
			result = new DataType(left.DOUBLE() * right.DOUBLE());
		}
		return result;		
    }
	
	public DataType visitDivFunc(GrammarParser.DivFuncContext ctx)
	{
		DataType left = visit(ctx.expr(0));
		DataType right = visit(ctx.expr(1));
		
		boolean foundLeft = false;
		boolean foundRight = false;
		
		int level = scopeLevel;
		
		while(level > scopeLimit -1)
		{
			if(variableStack.containsKey(Integer.toString(level)))
			{
				if(variableStack.get(Integer.toString(level)).containsKey(left.makeString()))
				{
					if(foundLeft == false)
					{
						foundLeft = true;
						left = variableStack.get(Integer.toString(level)).get(left.makeString()).get(0);
					}
				}
				
				if(variableStack.get(Integer.toString(level)).containsKey(right.makeString()))
				{
					if(foundRight == false)
					{
						foundRight = true;
						right = variableStack.get(Integer.toString(level)).get(right.makeString()).get(0);
					}
				}
			}
			level--;
		}
		
		DataType result = new DataType(0);
		if(left.getObject() instanceof String || right.getObject() instanceof String)
		{
			System.out.println("Cannot use strings in DIV function");
		}
		else
		{
			if(right.DOUBLE() != 0.0)
			{
				result = new DataType(left.DOUBLE() / right.DOUBLE());
				return result;	
			}
			else
			{
				System.out.println("Cannot devide by 0");		
			}
		}
		
		return new DataType(0);
		
    }
	
	//LOOP---------------------------------------------------------------------------------------
	public DataType visitForLoop(GrammarParser.ForLoopContext ctx)
	{
		scopeLevel++;
		variableStack.put(Integer.toString(scopeLevel), new HashMap<String, List<DataType>>());
		typeStack.put(Integer.toString(scopeLevel), new HashMap<String, String>());
		constStack.put(Integer.toString(scopeLevel), new HashMap<String, String>());
		
		DataType loopVar = visit(ctx.var());
		DataType limit = visit(ctx.expr(0));
		DataType increment = visit(ctx.expr(1));
		DataType operator = visit(ctx.operator());
		
		String loopVarName = "";
		String limitVarName = "";
		String incrementVarName = "";
		
		int loopVarLevel = 0;
		int limitVarLevel = 0;
		int incrementVarLevel = 0;
		
		int level = scopeLevel;
		
		while(level > scopeLimit -1)
		{
			if(variableStack.containsKey(Integer.toString(level)))
			{
				if(variableStack.get(Integer.toString(level)).containsKey(loopVar.makeString()))
				{
					if(loopVarName.equals(""))
					{
						loopVarName = loopVar.makeString();
						loopVar = variableStack.get(Integer.toString(level)).get(loopVarName).get(0);
						loopVarLevel = level;
					}
				}
				
				if(variableStack.get(Integer.toString(level)).containsKey(limit.makeString()))
				{
					if(limitVarName.equals(""))
					{
						limitVarName = limit.makeString();
						limit = variableStack.get(Integer.toString(level)).get(limit.makeString()).get(0);
						limitVarLevel = level;
					}
				}
				
				if(variableStack.get(Integer.toString(level)).containsKey(increment.makeString()))
				{
					if(incrementVarName.equals(""))
					{
						incrementVarName = increment.makeString();
						increment = variableStack.get(Integer.toString(level)).get(increment.makeString()).get(0);
						incrementVarLevel = level;
					}
				}				
				
			}
			level--;
		}
		
		if(loopVar.getObject() instanceof Integer)
		{
			if(typeStack.get(Integer.toString(loopVarLevel)).get(loopVarName).equals("bool"))
			{
				System.out.println("Cannot use variable of type bool for a FOR loop's interator variable");
			}
			else
			{
				if(increment.getObject() instanceof Integer)
				{
					if(operator.makeString().equals("<"))
					{
						for(int i = loopVar.INT(); i < limit.DOUBLE(); i = i + increment.INT())
						{
							List<DataType> data = new ArrayList<DataType>();
							data.add(new DataType(i));
							variableStack.get(Integer.toString(loopVarLevel)).put(loopVarName, data);
							visit(ctx.program());
							if(!limitVarName.isEmpty())
							{
								limit = variableStack.get(Integer.toString(limitVarLevel)).get(limitVarName).get(0);
							}
							if(!incrementVarName.isEmpty())
							{
								increment = variableStack.get(Integer.toString(incrementVarLevel)).get(incrementVarName).get(0);
							}
						}
					}
					else if(operator.makeString().equals(">"))
					{
						for(int i = loopVar.INT(); i > limit.DOUBLE(); i = i + increment.INT())
						{
							List<DataType> data = new ArrayList<DataType>();
							data.add(new DataType(i));
							variableStack.get(Integer.toString(loopVarLevel)).put(loopVarName, data);
							visit(ctx.program());
							if(!limitVarName.isEmpty())
							{
								limit = variableStack.get(Integer.toString(limitVarLevel)).get(limitVarName).get(0);
							}
							if(!incrementVarName.isEmpty())
							{
								increment = variableStack.get(Integer.toString(incrementVarLevel)).get(incrementVarName).get(0);
							}
						}
					}
					else if(operator.makeString().equals("=="))
					{
						for(int i = loopVar.INT(); i == limit.DOUBLE(); i = i + increment.INT())
						{
							List<DataType> data = new ArrayList<DataType>();
							data.add(new DataType(i));
							variableStack.get(Integer.toString(loopVarLevel)).put(loopVarName, data);
							visit(ctx.program());
							if(!limitVarName.isEmpty())
							{
								limit = variableStack.get(Integer.toString(limitVarLevel)).get(limitVarName).get(0);
							}
							if(!incrementVarName.isEmpty())
							{
								increment = variableStack.get(Integer.toString(incrementVarLevel)).get(incrementVarName).get(0);
							}
						}
					}
					else if(operator.makeString().equals("!="))
					{
						for(int i = loopVar.INT(); i != limit.DOUBLE(); i = i + increment.INT())
						{
							List<DataType> data = new ArrayList<DataType>();
							data.add(new DataType(i));
							variableStack.get(Integer.toString(loopVarLevel)).put(loopVarName, data);
							visit(ctx.program());
							if(!limitVarName.isEmpty())
							{
								limit = variableStack.get(Integer.toString(limitVarLevel)).get(limitVarName).get(0);
							}
							if(!incrementVarName.isEmpty())
							{
								increment = variableStack.get(Integer.toString(incrementVarLevel)).get(incrementVarName).get(0);
							}
						}
					}
					else if(operator.makeString().equals(">="))
					{
						for(int i = loopVar.INT(); i >= limit.DOUBLE(); i = i + increment.INT())
						{
							List<DataType> data = new ArrayList<DataType>();
							data.add(new DataType(i));
							variableStack.get(Integer.toString(loopVarLevel)).put(loopVarName, data);
							visit(ctx.program());
							if(!limitVarName.isEmpty())
							{
								limit = variableStack.get(Integer.toString(limitVarLevel)).get(limitVarName).get(0);
							}
							if(!incrementVarName.isEmpty())
							{
								increment = variableStack.get(Integer.toString(incrementVarLevel)).get(incrementVarName).get(0);
							}
						}
					}
					else if(operator.makeString().equals("<="))
					{
						for(int i = loopVar.INT(); i <= limit.DOUBLE(); i = i + increment.INT())
						{
							List<DataType> data = new ArrayList<DataType>();
							data.add(new DataType(i));
							variableStack.get(Integer.toString(loopVarLevel)).put(loopVarName, data);
							visit(ctx.program());
							if(!limitVarName.isEmpty())
							{
								limit = variableStack.get(Integer.toString(limitVarLevel)).get(limitVarName).get(0);
							}
							if(!incrementVarName.isEmpty())
							{
								increment = variableStack.get(Integer.toString(incrementVarLevel)).get(incrementVarName).get(0);
							}
						}
					}
				}
				else
				{
					System.out.println("Cannot increment an int with type double");
				}
			}
		}
		else
		{
			if(operator.makeString().equals("<"))
			{
				for(double i = loopVar.DOUBLE(); i < limit.DOUBLE(); i = i + increment.DOUBLE())
				{
					List<DataType> data = new ArrayList<DataType>();
					data.add(new DataType(i));
					variableStack.get(Integer.toString(loopVarLevel)).put(loopVarName, data);
					visit(ctx.program());
					if(!limitVarName.isEmpty())
					{
						limit = variableStack.get(Integer.toString(limitVarLevel)).get(limitVarName).get(0);
					}
					if(!incrementVarName.isEmpty())
					{
						increment = variableStack.get(Integer.toString(incrementVarLevel)).get(incrementVarName).get(0);
					}
				}
			}			
			else if(operator.makeString().equals(">"))
			{
				for(double i = loopVar.DOUBLE(); i > limit.DOUBLE(); i = i + increment.DOUBLE())
				{
					List<DataType> data = new ArrayList<DataType>();
					data.add(new DataType(i));
					variableStack.get(Integer.toString(loopVarLevel)).put(loopVarName, data);
					visit(ctx.program());
					if(!limitVarName.isEmpty())
					{
						limit = variableStack.get(Integer.toString(limitVarLevel)).get(limitVarName).get(0);
					}
					if(!incrementVarName.isEmpty())
					{
						increment = variableStack.get(Integer.toString(incrementVarLevel)).get(incrementVarName).get(0);
					}
				}
			}
			else if(operator.makeString().equals("=="))
			{
				for(double i = loopVar.DOUBLE(); i == limit.DOUBLE(); i = i + increment.DOUBLE())
				{
					List<DataType> data = new ArrayList<DataType>();
					data.add(new DataType(i));
					variableStack.get(Integer.toString(loopVarLevel)).put(loopVarName, data);
					visit(ctx.program());
					if(!limitVarName.isEmpty())
					{
						limit = variableStack.get(Integer.toString(limitVarLevel)).get(limitVarName).get(0);
					}
					if(!incrementVarName.isEmpty())
					{
						increment = variableStack.get(Integer.toString(incrementVarLevel)).get(incrementVarName).get(0);
					}
				}
			}
			else if(operator.makeString().equals("!="))
			{
				for(double i = loopVar.DOUBLE(); i != limit.DOUBLE(); i = i + increment.DOUBLE())
				{
					List<DataType> data = new ArrayList<DataType>();
					data.add(new DataType(i));
					variableStack.get(Integer.toString(loopVarLevel)).put(loopVarName, data);
					visit(ctx.program());
					if(!limitVarName.isEmpty())
					{
						limit = variableStack.get(Integer.toString(limitVarLevel)).get(limitVarName).get(0);
					}
					if(!incrementVarName.isEmpty())
					{
						increment = variableStack.get(Integer.toString(incrementVarLevel)).get(incrementVarName).get(0);
					}
				}
			}
			else if(operator.makeString().equals(">="))
			{
				for(double i = loopVar.DOUBLE(); i >= limit.DOUBLE(); i = i + increment.DOUBLE())
				{
					List<DataType> data = new ArrayList<DataType>();
					data.add(new DataType(i));
					variableStack.get(Integer.toString(loopVarLevel)).put(loopVarName, data);
					visit(ctx.program());
					if(!limitVarName.isEmpty())
					{
						limit = variableStack.get(Integer.toString(limitVarLevel)).get(limitVarName).get(0);
					}
					if(!incrementVarName.isEmpty())
					{
						increment = variableStack.get(Integer.toString(incrementVarLevel)).get(incrementVarName).get(0);
					}
				}
			}
			else if(operator.makeString().equals("<="))
			{
				for(double i = loopVar.DOUBLE(); i <= limit.DOUBLE(); i = i + increment.DOUBLE())
				{
					List<DataType> data = new ArrayList<DataType>();
					data.add(new DataType(i));
					variableStack.get(Integer.toString(loopVarLevel)).put(loopVarName, data);
					visit(ctx.program());
					if(!limitVarName.isEmpty())
					{
						limit = variableStack.get(Integer.toString(limitVarLevel)).get(limitVarName).get(0);
					}
					if(!incrementVarName.isEmpty())
					{
						increment = variableStack.get(Integer.toString(incrementVarLevel)).get(incrementVarName).get(0);
					}
				}
			}
		}
		
		variableStack.remove(Integer.toString(scopeLevel));
		typeStack.remove(Integer.toString(scopeLevel));
		constStack.remove(Integer.toString(scopeLevel));
		scopeLevel--;
		return new DataType(0);
	}
	
	public DataType visitWhileLoop(GrammarParser.WhileLoopContext ctx)
	{
		DataType var = visit(ctx.expr(0));
		DataType data = visit(ctx.expr(1));
		DataType operator = visit(ctx.operator());
		
		String varName = "";
		String dataVarName = "";
		
		int varLevel = 0;
		int dataLevel = 0;
		
		
		int level = scopeLevel;
		
		while(level > scopeLimit -1)
		{
			if(variableStack.containsKey(Integer.toString(level)))
			{
				if(variableStack.get(Integer.toString(level)).containsKey(var.makeString()))
				{
					if(varName.equals(""))
					{
						varName = var.makeString();
						var = variableStack.get(Integer.toString(level)).get(varName).get(0);
						varLevel = level;
					}
				}
				
				if(variableStack.get(Integer.toString(level)).containsKey(data.makeString()))
				{
					if(dataVarName.equals(""))
					{
						dataVarName = data.makeString();
						data = variableStack.get(Integer.toString(level)).get(data.makeString()).get(0);
						dataLevel = level;
					}
				}
			}
			level--;
		}
		
		
		
		scopeLevel++;
		variableStack.put(Integer.toString(scopeLevel), new HashMap<String, List<DataType>>());
		typeStack.put(Integer.toString(scopeLevel), new HashMap<String, String>());
		constStack.put(Integer.toString(scopeLevel), new HashMap<String, String>());
		if(!operator.makeString().equals("==") && !operator.makeString().equals("!="))
		{	
			if(var.getObject() instanceof String || data.getObject() instanceof String)
			{
				System.out.println("Strings can only be used with the == operator in IF statements");
			}
			else
			{
				//converting everything to double and them comparing is a simple method of allowing doubles and integers to be compared with each other
				//without needing to make lots of if statements depending on variable types.
				if(data.makeString().equals("false"))
				{
					data = new DataType(0);
				}
				else if(data.makeString().equals("true"))
				{
					data = new DataType(1);
				}
				if(var.makeString().equals("false"))
				{
					var = new DataType(0);
				}
				else if(var.makeString().equals("true"))
				{
					var = new DataType(1);
				}
				
				double convertData1 = var.DOUBLE();
				double convertData2 = data.DOUBLE();
				
				if(operator.makeString().equals("<"))
				{
					while(convertData1 < convertData2)
					{
						visit(ctx.program());
						if(!varName.isEmpty())
						{
							var = variableStack.get(Integer.toString(varLevel)).get(varName).get(0);
							convertData1 = var.DOUBLE();
						}
						if(!dataVarName.isEmpty())
						{
							data = variableStack.get(Integer.toString(dataLevel)).get(dataVarName).get(0);
							convertData2 = data.DOUBLE();
						}
					}
				}
				else if(operator.makeString().equals(">"))
				{
					while(convertData1 > convertData2)
					{
						visit(ctx.program());
						if(!varName.isEmpty())
						{
							var = variableStack.get(Integer.toString(varLevel)).get(varName).get(0);
							convertData1 = var.DOUBLE();
						}
						if(!dataVarName.isEmpty())
						{
							data = variableStack.get(Integer.toString(dataLevel)).get(dataVarName).get(0);
							convertData2 = data.DOUBLE();
						}
					}
				}
				else if(operator.makeString().equals(">="))
				{
					while(convertData1 >= convertData2)
					{
						visit(ctx.program());
						if(!varName.isEmpty())
						{
							var = variableStack.get(Integer.toString(varLevel)).get(varName).get(0);
							convertData1 = var.DOUBLE();
						}
						if(!dataVarName.isEmpty())
						{
							data = variableStack.get(Integer.toString(dataLevel)).get(dataVarName).get(0);
							convertData2 = data.DOUBLE();
						}
					}
				}
				else if(operator.makeString().equals("<="))
				{
					while(convertData1 <= convertData2)
					{
						visit(ctx.program());
						if(!varName.isEmpty())
						{
							var = variableStack.get(Integer.toString(varLevel)).get(varName).get(0);
							convertData1 = var.DOUBLE();
						}
						if(!dataVarName.isEmpty())
						{
							data = variableStack.get(Integer.toString(dataLevel)).get(dataVarName).get(0);
							convertData2 = data.DOUBLE();
						}
					}
				}
			}
		}
		else
		{
			if(var.getObject() instanceof String && data.getObject() instanceof String)
			{
				if(var.makeString().contains("\""))
				{
					int index = var.makeString().lastIndexOf("\"");
					var = new DataType(var.makeString().substring(1, index));
				}
				if(data.makeString().contains("\""))
				{
					int index = data.makeString().lastIndexOf("\"");
					data = new DataType(data.makeString().substring(1, index));
				}
				
				if(operator.makeString().equals("=="))
				{
					while(var.makeString().equals(data.makeString()))
					{
						visit(ctx.program());
						if(!varName.isEmpty())
						{
							var = variableStack.get(Integer.toString(varLevel)).get(varName).get(0);
						}
						if(!dataVarName.isEmpty())
						{
							data = variableStack.get(Integer.toString(dataLevel)).get(dataVarName).get(0);
						}
					}
				}
				else if(operator.makeString().equals("!="))
				{
					while(!var.makeString().equals(data.makeString()))
					{
						visit(ctx.program());
						if(!varName.isEmpty())
						{
							var = variableStack.get(Integer.toString(varLevel)).get(varName).get(0);
						}
						if(!dataVarName.isEmpty())
						{
							data = variableStack.get(Integer.toString(dataLevel)).get(dataVarName).get(0);
						}
					}
				}
			}
			else if(!(var.getObject() instanceof String) && !(data.getObject() instanceof String))
			{
				double convertData1 = var.DOUBLE();
				double convertData2 = data.DOUBLE();
				
				if(operator.makeString().equals("=="))
				{
					while(convertData1 == convertData2)
					{
						visit(ctx.program());
						if(!varName.isEmpty())
						{
							var = variableStack.get(Integer.toString(varLevel)).get(varName).get(0);
							convertData1 = var.DOUBLE();
						}
						if(!dataVarName.isEmpty())
						{
							data = variableStack.get(Integer.toString(dataLevel)).get(dataVarName).get(0);
							convertData2 = data.DOUBLE();
						}
					}
				}
				else if(operator.makeString().equals("!="))
				{
					while(convertData1 != convertData2)
					{
						visit(ctx.program());
						if(!varName.isEmpty())
						{
							var = variableStack.get(Integer.toString(varLevel)).get(varName).get(0);
							convertData1 = var.DOUBLE();
						}
						if(!dataVarName.isEmpty())
						{
							data = variableStack.get(Integer.toString(dataLevel)).get(dataVarName).get(0);
							convertData2 = data.DOUBLE();
						}
					}
				}
			}
			else
			{
				boolean stringForBoolean = false;
				if(data.makeString().equals("false"))
				{
					data = new DataType(0);
					stringForBoolean = true;
				}
				else if(data.makeString().equals("true"))
				{
					data = new DataType(1);
					stringForBoolean = true;
				}
				if(var.makeString().equals("false"))
				{
					var = new DataType(0);
					stringForBoolean = true;
				}
				else if(var.makeString().equals("true"))
				{
					var = new DataType(1);
					stringForBoolean = true;
				}
				
				if(stringForBoolean)
				{
					double convertData1 = var.DOUBLE();
					double convertData2 = data.DOUBLE();
					
					if(operator.makeString().equals("=="))
					{
						while(convertData1 == convertData2)
						{
							visit(ctx.program());
							if(!varName.isEmpty())
							{
								var = variableStack.get(Integer.toString(varLevel)).get(varName).get(0);
								convertData1 = var.DOUBLE();
							}
							if(!dataVarName.isEmpty())
							{
								data = variableStack.get(Integer.toString(dataLevel)).get(dataVarName).get(0);
								convertData2 = data.DOUBLE();
							}
						}
					}
					else if(operator.makeString().equals("!="))
					{
						while(convertData1 != convertData2)
						{
							visit(ctx.program());
							if(!varName.isEmpty())
							{
								var = variableStack.get(Integer.toString(varLevel)).get(varName).get(0);
								convertData1 = var.DOUBLE();
							}
							if(!dataVarName.isEmpty())
							{
								data = variableStack.get(Integer.toString(dataLevel)).get(dataVarName).get(0);
								convertData2 = data.DOUBLE();
							}
						}
					}
				}
				else
				{
					System.out.println("Can only compare a string with another string");
				}
			}
		}
		
		variableStack.remove(Integer.toString(scopeLevel));
		typeStack.remove(Integer.toString(scopeLevel));
		constStack.remove(Integer.toString(scopeLevel));
		scopeLevel--;
		return new DataType(0);		
	}
	
	//CONTROL-----------------------------------------------------------------------
	public DataType visitIFStatement(GrammarParser.IFStatementContext ctx)
	{
		Boolean solved = false;
		Boolean failed = false;
		DataType data1 = visit(ctx.expr(0));
		DataType data2 = visit(ctx.expr(1));
		int level = scopeLevel;
		DataType operator = visit(ctx.operator());
		
		while(level > scopeLimit - 1)
		{
			if(variableStack.containsKey(Integer.toString(level)))
			{
				if(variableStack.get(Integer.toString(level)).containsKey(data1.makeString()))
				{
					data1 = variableStack.get(Integer.toString(level)).get(data1.makeString()).get(0);
					level = scopeLimit;
				}
			}
			level--;
		}
		
		level = scopeLevel;
		while(level > scopeLimit - 1)
		{
			if(variableStack.containsKey(Integer.toString(level)))
			{
				if(variableStack.get(Integer.toString(level)).containsKey(data2.makeString()))
				{
					data2 = variableStack.get(Integer.toString(level)).get(data2.makeString()).get(0);
					level = scopeLimit;
				}
			}
			level--;
		}
		
		scopeLevel++;
		variableStack.put(Integer.toString(scopeLevel), new HashMap<String, List<DataType>>());
		typeStack.put(Integer.toString(scopeLevel), new HashMap<String, String>());
		constStack.put(Integer.toString(scopeLevel), new HashMap<String, String>());
		if(!operator.makeString().equals("==") && !operator.makeString().equals("!="))
		{	
			if(data1.getObject() instanceof String || data2.getObject() instanceof String)
			{
				System.out.println("Strings can only be used with the == operator in IF statements");
			}
			else
			{
				//converting everything to double and them comparing is a simple method of allowing doubles and integers to be compared with each other
				//without needing to make lots of if statements depending on variable types.
				if(data2.makeString().equals("false"))
				{
					data2 = new DataType(0);
				}
				else if(data2.makeString().equals("true"))
				{
					data2 = new DataType(1);
				}
				
				if(data1.makeString().equals("false"))
				{
					data1 = new DataType(0);
				}
				else if(data1.makeString().equals("true"))
				{
					data1 = new DataType(1);
				}
				
				double convertData1 = data1.DOUBLE();
				double convertData2 = data2.DOUBLE();
				
				if(operator.makeString().equals("<"))
				{
					if(convertData1 < convertData2)
					{
						visit(ctx.program(0));
						solved = true;
					}
				}
				else if(operator.makeString().equals(">"))
				{
					if(convertData1 > convertData2)
					{
						visit(ctx.program(0));
						solved = true;
					}
				}
				else if(operator.makeString().equals("<="))
				{
					if(convertData1 <= convertData2)
					{
						visit(ctx.program(0));
						solved = true;
					}
				}
				else if(operator.makeString().equals(">="))
				{
					if(convertData1 >= convertData2)
					{
						visit(ctx.program(0));
						solved = true;
					}
				}
			}
		}
		else
		{
			if(data1.getObject() instanceof String && data2.getObject() instanceof String)
			{
				if(data1.makeString().contains("\""))
				{
					int index = data1.makeString().lastIndexOf("\"");
					data1 = new DataType(data1.makeString().substring(1, index));
				}
				if(data2.makeString().contains("\""))
				{
					int index = data2.makeString().lastIndexOf("\"");
					data2 = new DataType(data2.makeString().substring(1, index));
				}
				
				if(operator.makeString().equals("=="))
				{
					if(data1.makeString().equals(data2.makeString()))
					{
						visit(ctx.program(0));
						solved = true;
					}
				}
				else if(operator.makeString().equals("!="))
				{
					if(!data1.makeString().equals(data2.makeString()))
					{
						visit(ctx.program(0));
						solved = true;
					}
				}
			}
			else if(!(data1.getObject() instanceof String) && !(data2.getObject() instanceof String))
			{
				double convertData1 = data1.DOUBLE();
				double convertData2 = data2.DOUBLE();
				
				if(operator.makeString().equals("=="))
				{
					if(convertData1 == convertData2)
					{
						visit(ctx.program(0));
						solved = true;
					}
				}				
				else if(operator.makeString().equals("!="))
				{
					if(convertData1 != convertData2)
					{
						visit(ctx.program(0));
						solved = true;
					}
				}
			}
			else
			{
				boolean stringForBoolean = false;
				if(data2.makeString().equals("false"))
				{
					data2 = new DataType(0);
					stringForBoolean = true;
				}
				else if(data2.makeString().equals("true"))
				{
					data2 = new DataType(1);
					stringForBoolean = true;
				}
				
				if(data1.makeString().equals("false"))
				{
					data1 = new DataType(0);
					stringForBoolean = true;
				}
				else if(data1.makeString().equals("true"))
				{
					data1 = new DataType(1);
					stringForBoolean = true;
				}
				
				if(stringForBoolean)
				{
					double convertData1 = data1.DOUBLE();
					double convertData2 = data2.DOUBLE();
					
					if(operator.makeString().equals("=="))
					{
						if(convertData1 == convertData2)
						{
							visit(ctx.program(0));
							solved = true;
						}
					}				
					else if(operator.makeString().equals("!="))
					{
						if(convertData1 != convertData2)
						{
							visit(ctx.program(0));
							solved = true;
						}
					}
				}
				else
				{
					System.out.println("Can only compare a string with another string");
				}
			}
		}
		
		if(!solved)
		{
			if(!failed)
			{
				for(int j = 0; j < ctx.getChildCount(); j++)
				{
					DataType control = new DataType(ctx.getChild(j).getText());
					if(control.makeString().equals("ELSE"))
					{
						visit(ctx.program(1));
						
					}
				}
			}
		}
		
		variableStack.remove(Integer.toString(scopeLevel));
		typeStack.remove(Integer.toString(scopeLevel));
		constStack.remove(Integer.toString(scopeLevel));
		scopeLevel--;
		
		return new DataType(0);		
	}
	
	//FUNCTION CALLS----------------------------------------------------------------------------------------
	public DataType visitDeclareFunc(GrammarParser.DeclareFuncContext ctx)
	{
		String functionName = ctx.ID().getText();
		HashMap<String, List<String>> record = new HashMap<String, List<String>>(); 
		
		if(function.containsKey(functionName))
		{
			System.out.println("Function " + functionName + " already exists");
		}
		else
		{
			List<String> data = new ArrayList<String>();
			data.add(ctx.getChild(0).getText());
			record.put("returnType", new ArrayList<String>(data));
			data.clear();
			for(int i = 0; i < ctx.param().size(); i++)
			{
				switch(ctx.param(i).getChild(0).getText())
				{
					case "int":
						data.add("int");
						data.add(ctx.param(i).getChild(1).getText());
						record.put(Integer.toString(i), new ArrayList<String>(data));
						data.clear();
						break;
					case "double":
						data.add("double");
						data.add(ctx.param(i).getChild(1).getText());
						record.put(Integer.toString(i), new ArrayList<String>(data));
						data.clear();
						break;
					case "bool":
						data.add("bool");
						data.add(ctx.param(i).getChild(1).getText());
						record.put(Integer.toString(i), new ArrayList<String>(data));
						data.clear();
						break;
					case "string":
						data.add("string");
						data.add(ctx.param(i).getChild(1).getText());
						record.put(Integer.toString(i), new ArrayList<String>(data));
						data.clear();
						break;
					case "List<":
						data.add("List");
						data.add("List<" + ctx.param(i).getChild(1).getText() + ">");
						data.add(ctx.param(i).getChild(3).getText());
						record.put(Integer.toString(i), new ArrayList<String>(data));
						data.clear();
						break;
					case "const":
						data.add("const");
						data.add(ctx.param(i).getChild(1).getText());
						data.add(ctx.param(i).getChild(2).getText());
						record.put(Integer.toString(i), new ArrayList<String>(data));
						data.clear();
						break;
				}
			}
			if(ctx.getText().contains("RETURN"))
			{
				if(ctx.getChild(0).getText().contains("void"))
				{
					System.out.println("A function with return type void cannot return a value");
				}
				else
				{
					data.add("return");
					record.put("return", data);
					data.clear();
					function.put(functionName, new DataType((ParseTree)ctx.program()));
					funcTypesStack.put(functionName, record);
				}
			}
			else if(!ctx.getChild(0).getText().equals("void"))
			{
				System.out.println("A RETURN statement has not been declared for a function with a return type");
			}
			else
			{
				function.put(functionName, new DataType((ParseTree)ctx.program()));
				funcTypesStack.put(functionName, record);
			}
		}
		return new DataType(0);
	}
	
	public DataType visitFuncCall(GrammarParser.FuncCallContext ctx)
	{
		String functionName = ctx.ID().getText();
		if(function.containsKey(functionName) && funcTypesStack.containsKey(functionName))
		{	
			int passedParameterCount = 0;
			int functionParameterCount = 0;
			
			if(ctx.getChildCount() > 3)
			{
				passedParameterCount = ctx.expr().size();
			}
			
			if(funcTypesStack.get(functionName).containsKey("return"))
			{
				functionParameterCount = funcTypesStack.get(functionName).size() - 2;
			}
			else
			{
				functionParameterCount = funcTypesStack.get(functionName).size() - 1;
			}
			
			if(passedParameterCount == functionParameterCount)
			{				
				scopeLevel++;
				variableStack.put(Integer.toString(scopeLevel), new HashMap<String, List<DataType>>());
				typeStack.put(Integer.toString(scopeLevel), new HashMap<String, String>());
				constStack.put(Integer.toString(scopeLevel), new HashMap<String, String>());
				if(ctx.getChildCount() > 3)
				{
					int level = 0;
					DataType data = new DataType(0);
					boolean variable = false;
					//store data to be used in function
					for(int i = 0; i < ctx.expr().size(); i++)
					{
						level = scopeLevel;
						data = visit(ctx.expr(i));
						while(level > scopeLimit - 1)
						{
							if(variableStack.containsKey(Integer.toString(level)))
							{
								if(variableStack.get(Integer.toString(level)).containsKey(data.makeString()))
								{
									if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(0).equals("int"))
									{
										List<DataType> newData = new ArrayList<DataType>();
										newData.add(new DataType(variableStack.get(Integer.toString(level)).get(data.makeString()).get(0).INT()));
										variableStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), newData);
										typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), "int");
										level = scopeLimit;
										variable = true;
									}
									else if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(0).equals("double"))
									{
										List<DataType> newData = new ArrayList<DataType>();
										newData.add(new DataType(variableStack.get(Integer.toString(level)).get(data.makeString()).get(0).DOUBLE()));
										variableStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), newData);
										typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), "double");
										level = scopeLimit;
										variable = true;
									}
									else if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(0).equals("bool"))
									{
										List<DataType> newData = new ArrayList<DataType>();
										newData.add(new DataType(variableStack.get(Integer.toString(level)).get(data.makeString()).get(0).BOOL()));
										variableStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), newData);
										typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), "bool");
										level = scopeLimit;
										variable = true;
									}
									else if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(0).equals("string"))
									{
										List<DataType> newData = new ArrayList<DataType>();
										newData.add(new DataType(variableStack.get(Integer.toString(level)).get(data.makeString()).get(0).makeString()));
										variableStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), newData);
										typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), "string");
										level = scopeLimit;
										variable = true;
									}
									else if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(0).equals("const"))
									{
										if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1).equals("int"))
										{
											List<DataType> newData = new ArrayList<DataType>();
											newData.add(new DataType(variableStack.get(Integer.toString(level)).get(data.makeString()).get(0).INT()));
											variableStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), newData);
											typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), "int");
											constStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), "const");
											level = scopeLimit;
											variable = true;
										}
										else if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1).equals("double"))
										{
											List<DataType> newData = new ArrayList<DataType>();
											newData.add(new DataType(variableStack.get(Integer.toString(level)).get(data.makeString()).get(0).DOUBLE()));
											variableStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), newData);
											typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), "double");
											constStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), "const");
											level = scopeLimit;
											variable = true;
										}
										else if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1).equals("bool"))
										{
											List<DataType> newData = new ArrayList<DataType>();
											newData.add(new DataType(variableStack.get(Integer.toString(level)).get(data.makeString()).get(0).BOOL()));
											variableStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), newData);
											typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), "bool");
											constStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), "const");
											level = scopeLimit;
											variable = true;
										}
										else if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1).equals("string"))
										{
											List<DataType> newData = new ArrayList<DataType>();
											newData.add(new DataType(variableStack.get(Integer.toString(level)).get(data.makeString()).get(0).makeString()));
											variableStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), newData);
											typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), "string");
											constStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), "const");
											level = scopeLimit;
											variable = true;
										}
									}
									else if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(0).equals("List"))
									{
										if(typeStack.get(Integer.toString(level)).get(ctx.expr(i).getText()).equals(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1)))
										{
											variableStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), variableStack.get(Integer.toString(level)).get(data.makeString()));
											if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1).equals("List<int>"))
											{
												typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), "List<int>");
												variable = true;
											}
											else if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1).equals("List<double>"))
											{
												typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), "List<double>");
												variable = true;
											}
											else if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1).equals("List<bool>"))
											{
												typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), "List<bool>");
												variable = true;
											}
											else if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1).equals("List<string>"))
											{
												typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), "List<string>");
												variable = true;
											}
										}
										else
										{
											System.out.println("Cannot pass a list to a function of a different type than the list declared in the function");
										}
										
									}
								}
									
							}
							level--;
						}
						if(!variable)
						{
							if(data.getObject() instanceof Integer || data.getObject() instanceof Double)
							{
								if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(0).equals("int"))
								{
									List<DataType> newData = new ArrayList<DataType>();
									newData.add(new DataType(data.INT()));
									variableStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), newData);
									typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), "int");
								}
								else if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(0).equals("double"))
								{
									List<DataType> newData = new ArrayList<DataType>();
									newData.add(new DataType(data.DOUBLE()));
									variableStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), newData);
									typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), "double");
								}
								else if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(0).equals("bool"))
								{
									List<DataType> newData = new ArrayList<DataType>();
									newData.add(new DataType(data.BOOL()));
									variableStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), newData);
									typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), "bool");
								}
								else if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(0).equals("const"))
								{
									if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(0).equals("int"))
									{
										List<DataType> newData = new ArrayList<DataType>();
										newData.add(new DataType(data.INT()));
										variableStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), newData);
										typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), "int");
										constStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), "const");
									}
									else if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(0).equals("double"))
									{
										List<DataType> newData = new ArrayList<DataType>();
										newData.add(new DataType(data.DOUBLE()));
										variableStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), newData);
										typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), "double");
										constStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), "const");
									}
									else if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(0).equals("bool"))
									{
										List<DataType> newData = new ArrayList<DataType>();
										newData.add(new DataType(data.BOOL()));
										variableStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), newData);
										typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), "bool");
										constStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), "const");
									}
								}
							}
							else if(data.getObject() instanceof String)
							{
								if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(0).equals("bool"))
								{
									if(data.makeString().equals("true"))
									{
										List<DataType> newData = new ArrayList<DataType>();
										newData.add(new DataType(1));
										variableStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), newData);
										typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), "bool");
									}
									else if(data.makeString().equals("false"))
									{
										List<DataType> newData = new ArrayList<DataType>();
										newData.add(new DataType(0));
										variableStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), newData);
										typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), "bool");
									}
								}
								else if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(0).equals("string"))
								{
									 if(data.makeString().contains("\""))
									{
										List<DataType> newData = new ArrayList<DataType>();
										int index = data.makeString().lastIndexOf("\"");
										newData.add(new DataType(data.makeString().substring(1, index)));
										variableStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), newData);
										typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), "string");
									}
									else
									{
										System.out.println("When sending string data to a function it must be enclosed with the \"\" marks");
									}
								}
								else if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(0).equals("const"))
								{
									if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(0).equals("bool"))
									{
										if(data.makeString().equals("true"))
										{
											List<DataType> newData = new ArrayList<DataType>();
											newData.add(new DataType(1));
											variableStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), newData);
											typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), "bool");
											constStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), "const");
										}
										else if(data.makeString().equals("false"))
										{
											List<DataType> newData = new ArrayList<DataType>();
											newData.add(new DataType(0));
											variableStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), newData);
											typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), "bool");
											constStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), "const");
										}
									}
									else if(funcTypesStack.get(functionName).get(Integer.toString(i)).get(0).equals("string"))
									{
										 if(data.makeString().contains("\""))
										{
											List<DataType> newData = new ArrayList<DataType>();
											int index = data.makeString().lastIndexOf("\"");
											newData.add(new DataType(data.makeString().substring(1, index)));
											variableStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), newData);
											typeStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(1), "string");
											constStack.get(Integer.toString(scopeLevel)).put(funcTypesStack.get(functionName).get(Integer.toString(i)).get(2), "const");
										}
										 else
										 {
											 System.out.println("When sending string data to a function it must be enclosed with the \"\" marks");
										 }
									}
								}
							}
							else
							{
								System.out.println("Variable passed to function does not exist");
							}
						}
					}
				}
				int oldScopeLimit = 0;
				oldScopeLimit = scopeLimit;
				scopeLimit = scopeLevel;
				ParseTree tree = function.get(functionName).asTree();
				visit(tree);
				boolean returnSucceeded = true;
				
				if(funcTypesStack.get(ctx.ID().getText()).containsKey("returnType"))
				{	
					if(!funcTypesStack.get(ctx.ID().getText()).get("returnType").get(0).equals("void"))
					{
						if(variableStack.get(Integer.toString(scopeLevel-1)).containsKey("returnedData"))
						{
							if(funcTypesStack.get(ctx.ID().getText()).get("returnType").get(0).equals("int"))
							{						
								if(variableStack.get(Integer.toString(scopeLevel-1)).get("returnedData").get(0).getObject() instanceof Integer)
								{
									typeStack.get(Integer.toString(scopeLevel-1)).put("returnedData", "int");
								}
								else
								{
									System.out.println("Returned data is not of the type specified in called function");
									returnSucceeded = false;
								
								}
								
							}
							else if(funcTypesStack.get(ctx.ID().getText()).get("returnType").get(0).equals("double"))
							{
								if(variableStack.get(Integer.toString(scopeLevel-1)).get("returnedData").get(0).getObject() instanceof Double)
								{
									typeStack.get(Integer.toString(scopeLevel-1)).put("returnedData", "double");
								}
								else
								{
									System.out.println("Returned data is not of the type specified in called function");
									returnSucceeded = false;
								}
							}
							else if(funcTypesStack.get(ctx.ID().getText()).get("returnType").get(0).equals("bool"))
							{
								if(variableStack.get(Integer.toString(scopeLevel-1)).get("returnedData").get(0).makeString().equals("true"))
								{
									variableStack.get(Integer.toString(scopeLevel-1)).get("returnedData").set(0, new DataType(1));
									typeStack.get(Integer.toString(scopeLevel-1)).put("returnedData", "bool");
								}
								else if(variableStack.get(Integer.toString(scopeLevel-1)).get("returnedData").get(0).makeString().equals("false"))
								{
									variableStack.get(Integer.toString(scopeLevel-1)).get("returnedData").set(0, new DataType(0));
									typeStack.get(Integer.toString(scopeLevel-1)).put("returnedData", "bool");
								}
								else if(variableStack.get(Integer.toString(scopeLevel-1)).get("returnedData").get(0).getObject() instanceof Integer)
								{
									typeStack.get(Integer.toString(scopeLevel-1)).put("returnedData", "bool");
								}
								else
								{
									System.out.println("Returned data is not of the type specified in called function");
									returnSucceeded = false;
								}
							}
							else if(funcTypesStack.get(ctx.ID().getText()).get("returnType").get(0).equals("string"))
							{
								if(variableStack.get(Integer.toString(scopeLevel-1)).get("returnedData").get(0).getObject() instanceof String)
								{
									typeStack.get(Integer.toString(scopeLevel-1)).put("returnedData", "String");
								}
								else
								{
									System.out.println("Returned data is not of the type specified in called function");
									returnSucceeded = false;
								}
							}
							else if(funcTypesStack.get(ctx.ID().getText()).get("returnType").get(0).equals("List<int>"))
							{
								if(typeStack.get(Integer.toString(scopeLevel-1)).get("returnedData").equals("List<int>"))
								{
									typeStack.get(Integer.toString(scopeLevel-1)).put("returnedData", "List<int>");
								}
								else
								{
									System.out.println("Returned data is not of the type specified in called function");
									returnSucceeded = false;
								}
							}
							else if(funcTypesStack.get(ctx.ID().getText()).get("returnType").get(0).equals("List<double>"))
							{
								if(typeStack.get(Integer.toString(scopeLevel-1)).get("returnedData").equals("List<double>"))
								{
									typeStack.get(Integer.toString(scopeLevel-1)).put("returnedData", "List<double>");
								}
								else
								{
									System.out.println("Returned data is not of the type specified in called function");
									returnSucceeded = false;
								}
							}
							else if(funcTypesStack.get(ctx.ID().getText()).get("returnType").get(0).equals("List<bool>"))
							{
								if(typeStack.get(Integer.toString(scopeLevel-1)).get("returnedData").equals("List<bool>"))
								{
									typeStack.get(Integer.toString(scopeLevel-1)).put("returnedData", "List<bool>");
								}
								else
								{
									System.out.println("Returned data is not of the type specified in called function");
									returnSucceeded = false;
								}
							}
							else if(funcTypesStack.get(ctx.ID().getText()).get("returnType").get(0).equals("List<string>"))
							{
								if(typeStack.get(Integer.toString(scopeLevel-1)).get("returnedData").equals("List<string>"))
								{
									typeStack.get(Integer.toString(scopeLevel-1)).put("returnedData", "List<string>");
								}
								else
								{
									System.out.println("Returned data is not of the type specified in called function");
									returnSucceeded = false;
								}
							}
						}
						else
						{
							System.out.println("You have not returned anything from a function that you declared a return type of not void");
							returnSucceeded = false;
						}
					}
				}
				variableStack.remove(Integer.toString(scopeLevel));
				typeStack.remove(Integer.toString(scopeLevel));
				constStack.remove(Integer.toString(scopeLevel));
				scopeLevel--;
				scopeLimit = oldScopeLimit;
				if(!returnSucceeded)
				{
					return new DataType(0);
				}
				else
				{
					return new DataType("returnedData");
				}
			}
			else
			{
				System.out.println("Wrong number of variables submitted in function call");
				return new DataType(0);
			}
			
			
		}
		else
		{
			System.out.println("Function not found");
			return new DataType(0);
		}		
	}
	
	public DataType visitReturnData(GrammarParser.ReturnDataContext ctx)
	{
		int level = scopeLevel;
		DataType data = visit(ctx.expr());
		int varLevel = 0;
		List<DataType> returnData = new ArrayList<DataType>();
		boolean isVariable = false;
		while(level > scopeLimit - 1)
		{
			if(variableStack.containsKey(Integer.toString(level)))
			{
				if(variableStack.get(Integer.toString(level)).containsKey(data.makeString()))
				{
					returnData = variableStack.get(Integer.toString(level)).get(data.makeString());
					isVariable = true;
					varLevel = level;
					level = scopeLimit;
				}
			}
			level--;
		}
		
		if(isVariable)
		{
			variableStack.get(Integer.toString(scopeLevel-1)).put("returnedData", returnData);
			typeStack.get(Integer.toString(scopeLevel-1)).put("returnedData", typeStack.get(Integer.toString(varLevel)).get(data.makeString()));
		}
		else
		{
			returnData.add(data);
			variableStack.get(Integer.toString(scopeLevel-1)).put("returnedData", returnData);			
		}
				
		return new DataType(0);
	}
	
	//LIST------------------------------------------------------------------------------------------
	public DataType visitAddToList(GrammarParser.AddToListContext ctx)
	{
		String varName = ctx.ID().getText();
		DataType data = visit(ctx.expr());
		int level = scopeLevel;
		int listLevel = 0;
		int dataLevel = 0;
		if(ctx.expr().getChild(0).getText().contains("LIST_GET"))
		{
			data = new DataType("\"" + data.makeString() + "\"");
		}
		while(level > scopeLimit - 1)
		{
			if(variableStack.containsKey(Integer.toString(level)) && typeStack.containsKey(Integer.toString(level)))
			{
				if(variableStack.get(Integer.toString(level)).containsKey(varName) && typeStack.get(Integer.toString(level)).containsKey(varName))
				{
					if(listLevel == 0)
					{
						listLevel = level;
					}
				}
				
				if(variableStack.get(Integer.toString(level)).containsKey(data.makeString()) && typeStack.get(Integer.toString(level)).containsKey(data.makeString()))
				{
					if(dataLevel == 0)
					{
						dataLevel = level;
					}
				}
				
				if(dataLevel > 0 && listLevel > 0)
				{
					level = scopeLimit;
				}
			}
			level--;
		}
		
		if(listLevel > 0)
		{
			if(dataLevel > 0)
			{
				if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<int>"))
				{
					variableStack.get(Integer.toString(listLevel)).get(varName).add(new DataType(variableStack.get(Integer.toString(dataLevel)).get(data.makeString()).get(0).INT()));
				}
				else if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<double>"))
				{
					variableStack.get(Integer.toString(listLevel)).get(varName).add(new DataType(variableStack.get(Integer.toString(dataLevel)).get(data.makeString()).get(0).DOUBLE()));
				}
				else if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<bool>"))
				{
					variableStack.get(Integer.toString(listLevel)).get(varName).add(new DataType(variableStack.get(Integer.toString(dataLevel)).get(data.makeString()).get(0).BOOL()));
				}
				else if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<string>"))
				{
					variableStack.get(Integer.toString(listLevel)).get(varName).add(new DataType(variableStack.get(Integer.toString(dataLevel)).get(data.makeString()).get(0).makeString()));
				}
			}
			else
			{
				if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<int>"))
				{
					variableStack.get(Integer.toString(listLevel)).get(varName).add(new DataType(data.INT()));
				}
				else if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<double>"))
				{
					variableStack.get(Integer.toString(listLevel)).get(varName).add(new DataType(data.DOUBLE()));
				}
				else if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<bool>"))
				{
					variableStack.get(Integer.toString(listLevel)).get(varName).add(new DataType(data.BOOL()));
				}
				else if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<string>"))
				{
					if(data.makeString().contains("\""))
					{
						int index = data.makeString().lastIndexOf("\"");
						data = new DataType(data.makeString().substring(1, index));
						variableStack.get(Integer.toString(listLevel)).get(varName).add(new DataType(data.makeString()));
					}
					else
					{
						System.out.println("When adding data to a List<string> it must be formatted with the \"\" marks unless it is data from a variable");
					}
					
				}
			}
		}
		else
		{
			System.out.println("List does not exit");
		}
		return new DataType(0);
	}
	
	public DataType visitGetFromList(GrammarParser.GetFromListContext ctx)
	{
		String varName = ctx.ID().getText();
		DataType index = visit(ctx.expr());
		int level = scopeLevel;
		int listLevel = 0;
		int indexLevel = 0;
		
		while(level > scopeLimit - 1)
		{
			if(variableStack.containsKey(Integer.toString(level)) && typeStack.containsKey(Integer.toString(level)))
			{
				if(variableStack.get(Integer.toString(level)).containsKey(varName) && typeStack.get(Integer.toString(level)).containsKey(varName))
				{
					if(listLevel == 0)
					{
						listLevel = level;
					}
				}
				
				if(variableStack.get(Integer.toString(level)).containsKey(index.makeString()) && typeStack.get(Integer.toString(level)).containsKey(index.makeString()))
				{
					if(indexLevel == 0)
					{
						indexLevel = level;
					}
				}
				
				if(indexLevel > 0 && listLevel > 0)
				{
					level = scopeLimit; //try and implement this in more code - is more efficient.
				}
			}
			level--;
		}
		
		if(listLevel > 0)
		{
			if(indexLevel > 0)
			{
				if(typeStack.get(Integer.toString(indexLevel)).get(index.makeString()).equals("int"))
				{
					return variableStack.get(Integer.toString(listLevel)).get(varName).get(variableStack.get(Integer.toString(indexLevel)).get(index.makeString()).get(0).INT());
				}
				else
				{
					System.out.println("Index value of .GET() cannot be a non integer");
				}
			}
			else
			{
				if(index.getObject() instanceof Integer)
				{
					return variableStack.get(Integer.toString(listLevel)).get(varName).get(index.INT());
					
				}
				else
				{
					System.out.println("Index value of .GET() cannot be a non integer");
				}
			}
		}
		else
		{
			System.out.println("List does not exit");
		}
		
		
		return new DataType(0);
	}
	
	public DataType visitGetListSize(GrammarParser.GetListSizeContext ctx)
	{
		String varName = ctx.ID().getText();
		int level = scopeLevel;
		int listLevel = 0;
		while(level > scopeLimit - 1)
		{
			if(variableStack.containsKey(Integer.toString(level)));
			{
				if(variableStack.get(Integer.toString(level)).containsKey(varName))
				{
					if(listLevel == 0)
					{
						listLevel = level;
						level = scopeLimit;
					}
				}
			}
			level--;
		}
		return new DataType(variableStack.get(Integer.toString(listLevel)).get(varName).size());
	}
	
	public DataType visitSetInList(GrammarParser.SetInListContext ctx)
	{
		String varName = ctx.ID().getText();
		DataType index = visit(ctx.expr(0));
		DataType newVal = visit(ctx.expr(1));
		int level = scopeLevel;
		int listLevel = 0;
		int indexLevel = 0;
		int newValLevel = 0;
		if(ctx.expr(1).getChild(0).getText().contains("LIST_GET"))
		{
			newVal = new DataType("\"" + newVal.makeString() + "\"");
		}
		
		while(level > scopeLimit - 1)
		{
			if(variableStack.containsKey(Integer.toString(level)) && typeStack.containsKey(Integer.toString(level)))
			{
				if(variableStack.get(Integer.toString(level)).containsKey(varName) && typeStack.get(Integer.toString(level)).containsKey(varName))
				{
					if(listLevel == 0)
					{
						listLevel = level;
					}
				}
				
				if(variableStack.get(Integer.toString(level)).containsKey(index.makeString()) && typeStack.get(Integer.toString(level)).containsKey(index.makeString()))
				{
					if(indexLevel == 0)
					{
						indexLevel = level;
					}
				}
				
				if(variableStack.get(Integer.toString(level)).containsKey(newVal.makeString()) && typeStack.get(Integer.toString(level)).containsKey(newVal.makeString()))
				{
					if(newValLevel == 0)
					{
						newValLevel = level;
					}
				}
				
				if(indexLevel > 0 && listLevel > 0 && newValLevel > 0)
				{
					level = scopeLimit; //try and implement this in more code - is more efficient.
				}
			}
			level--;
		}
		
		if(listLevel > 0)
		{
			if(indexLevel > 0)
			{
				if(typeStack.get(Integer.toString(indexLevel)).get(index.makeString()).equals("int"))
				{
					if(newValLevel > 0)
					{
						if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<int>"))
						{
							variableStack.get(Integer.toString(listLevel)).get(varName).set(variableStack.get(Integer.toString(indexLevel)).get(index.makeString()).get(0).INT(), new DataType(variableStack.get(Integer.toString(newValLevel)).get(newVal.makeString()).get(0).INT()));
						}
						else if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<double>"))
						{
							variableStack.get(Integer.toString(listLevel)).get(varName).set(variableStack.get(Integer.toString(indexLevel)).get(index.makeString()).get(0).INT(), new DataType(variableStack.get(Integer.toString(newValLevel)).get(newVal.makeString()).get(0).DOUBLE()));
						}
						else if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<bool>"))
						{
							variableStack.get(Integer.toString(listLevel)).get(varName).set(variableStack.get(Integer.toString(indexLevel)).get(index.makeString()).get(0).INT(), new DataType(variableStack.get(Integer.toString(newValLevel)).get(newVal.makeString()).get(0).BOOL()));
						}
						else if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<string>"))
						{
							variableStack.get(Integer.toString(listLevel)).get(varName).set(variableStack.get(Integer.toString(indexLevel)).get(index.makeString()).get(0).INT(), new DataType(variableStack.get(Integer.toString(newValLevel)).get(newVal.makeString()).get(0).makeString()));
						}
					}
					else
					{
						if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<int>"))
						{
							variableStack.get(Integer.toString(listLevel)).get(varName).set(variableStack.get(Integer.toString(indexLevel)).get(index.makeString()).get(0).INT(), new DataType(newVal.INT()));
						}
						else if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<double>"))
						{
							variableStack.get(Integer.toString(listLevel)).get(varName).set(variableStack.get(Integer.toString(indexLevel)).get(index.makeString()).get(0).INT(), new DataType(newVal.DOUBLE()));
						}
						else if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<bool>"))
						{
							variableStack.get(Integer.toString(listLevel)).get(varName).set(variableStack.get(Integer.toString(indexLevel)).get(index.makeString()).get(0).INT(), new DataType(newVal.BOOL()));
						}
						else if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<string>"))
						{
							if(newVal.makeString().contains("\""))
							{
								int characterIndex = newVal.makeString().lastIndexOf("\"");
								newVal = new DataType(newVal.makeString().substring(1, characterIndex));
								variableStack.get(Integer.toString(listLevel)).get(varName).set(index.INT(), new DataType(newVal.makeString()));
							}
							else
							{
								System.out.println("When adding data to a List<string> it must be formatted with the \"\" marks unless it is data from a variable");
							}
						}
					}
				}
				else
				{
					System.out.println("Index value of .SET() cannot be a non integer");
				}
			}
			else
			{
				if(index.getObject() instanceof Integer)
				{
					if(newValLevel > 0)
					{
						if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<int>"))
						{
							variableStack.get(Integer.toString(listLevel)).get(varName).set(index.INT(), new DataType(variableStack.get(Integer.toString(newValLevel)).get(newVal.makeString()).get(0).INT()));
						}
						else if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<double>"))
						{
							variableStack.get(Integer.toString(listLevel)).get(varName).set(index.INT(), new DataType(variableStack.get(Integer.toString(newValLevel)).get(newVal.makeString()).get(0).DOUBLE()));
						}
						else if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<bool>"))
						{
							variableStack.get(Integer.toString(listLevel)).get(varName).set(index.INT(), new DataType(variableStack.get(Integer.toString(newValLevel)).get(newVal.makeString()).get(0).BOOL()));
						}
						else if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<string>"))
						{
							variableStack.get(Integer.toString(listLevel)).get(varName).set(index.INT(), new DataType(variableStack.get(Integer.toString(newValLevel)).get(newVal.makeString()).get(0).makeString()));
						}
					}
					else
					{
						if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<int>"))
						{
							variableStack.get(Integer.toString(listLevel)).get(varName).set(index.INT(), new DataType(newVal.INT()));
						}
						else if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<double>"))
						{
							variableStack.get(Integer.toString(listLevel)).get(varName).set(index.INT(), new DataType(newVal.DOUBLE()));
						}
						else if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<bool>"))
						{
							variableStack.get(Integer.toString(listLevel)).get(varName).set(index.INT(), new DataType(newVal.BOOL()));
						}
						else if(typeStack.get(Integer.toString(listLevel)).get(varName).equals("List<string>"))
						{
							if(newVal.makeString().contains("\""))
							{
								int characterIndex = newVal.makeString().lastIndexOf("\"");
								newVal = new DataType(newVal.makeString().substring(1, characterIndex));
								variableStack.get(Integer.toString(listLevel)).get(varName).set(index.INT(), new DataType(newVal.makeString()));
								
							}
							else
							{
								System.out.println("When adding data to a List<string> it must be formatted with the \"\" marks unless it is data from a variable");
							}
						}
					}
				}
				else
				{
					System.out.println("Index value of .SET() cannot be a non integer");
				}
			}
		}
		else
		{
			System.out.println("List does not exit");
		}
		return new DataType(0);
	}
	
	public DataType visitRemoveFromList(GrammarParser.RemoveFromListContext ctx)
	{
		String varName = ctx.ID().getText();
		DataType index = visit(ctx.expr());
		int level = scopeLevel;
		int listLevel = 0;
		int indexLevel = 0;
		
		while(level > scopeLimit - 1)
		{
			if(variableStack.containsKey(Integer.toString(level)))
			{
				if(variableStack.get(Integer.toString(level)).containsKey(varName) && typeStack.get(Integer.toString(level)).containsKey(varName))
				{
					if(listLevel == 0)
					{
						listLevel = level;
					}
				}
				
				if(variableStack.get(Integer.toString(level)).containsKey(index.makeString()) && typeStack.get(Integer.toString(level)).containsKey(index.makeString()))
				{
					if(indexLevel == 0)
					{
						indexLevel = level;
					}
				}
				
				if(indexLevel > 0 && listLevel > 0)
				{
					level = scopeLimit;
				}
			}
			level--;
		}
		
		if(listLevel > 0)
		{
			if(indexLevel > 0)
			{
				if(typeStack.get(Integer.toString(indexLevel)).get(index.makeString()).equals("int"))
				{
					variableStack.get(Integer.toString(listLevel)).get(varName).remove((int)variableStack.get(Integer.toString(indexLevel)).get(index.makeString()).get(0).INT());
				}
				else
				{
					System.out.println("Index value of .REMOVEAT() cannot be a non integer");
				}
			}
			else
			{
				if(index.getObject() instanceof Integer)
				{
					variableStack.get(Integer.toString(listLevel)).get(varName).remove((int)index.INT());
				}
				else
				{
					System.out.println("Index value of .REMOVEAT() cannot be a non integer");
				}
			}
		}
		else
		{
			System.out.println("List does not exit");
		}
		
		return new DataType(0);
	}
	
	public DataType visitClearList(GrammarParser.ClearListContext ctx)
	{
		String varName = ctx.ID().getText();
		int level = scopeLevel;
		boolean deleted = false;
		
		while(level > scopeLimit - 1)
		{
			if(variableStack.containsKey(Integer.toString(level)))
			{
				if(variableStack.get(Integer.toString(level)).containsKey(varName))
				{
					variableStack.get(Integer.toString(level)).get(varName).clear();
					deleted = true;
				}
			}
			level--;
		}
		
		if(deleted == false)
		{
			System.out.println("List does not exist");
		}
		
		return new DataType(0);
	}
}