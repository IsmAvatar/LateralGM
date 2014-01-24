package org.lateralgm.joshedit.lexers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultKeywords
	{
	
	public abstract static class Keyword
	{
	protected String name;

	public String getName()
		{
		return name;
		}
	}

public static class Construct extends Keyword
	{
	public Construct(String input)
		{
		name = input;
		}
	}

public static class Operator extends Keyword
	{
	public Operator(String input)
		{
		name = input;
		}
	}

public static class Variable extends Keyword
	{
	public final boolean readOnly;
	public final int arraySize;

	public Variable(String input)
		{
		Matcher m = Pattern.compile("(\\w+)(\\[(\\d+)])?(\\*)?").matcher(input);
		if (!m.matches()) System.err.println("Invalid variable: " + input); //$NON-NLS-1$
		name = m.group(1);
		String s = m.group(3);
		arraySize = s != null ? Integer.valueOf(m.group(3)) : 0;
		readOnly = "*".equals(m.group(4)); //$NON-NLS-1$
		}
	}

public static class Constant extends Keyword
	{
	public Constant(String input)
		{
		name = input;
		}
	}

public static class Function extends Keyword
	{
	public final String description;
	public final String[] arguments;
	public final int dynArgIndex;
	public final int dynArgMin;
	public final int dynArgMax;

	public Function(String input)
		{
		//  1   1  23    3 245   5  6   6 7   7 8        84 9   9
		// /(\w+)\(((\w+,)*)((\w+)\{(\d+),(\d+)}((?=\))|,))?(\w+)?\)/
		//   fun  (  arg,     arg  { 0   , 9   }        ,    arg   )
		String re = "(\\w+)\\(((\\w+,)*)((\\w+)\\{(\\d+),(\\d+)}((?=\\))|,))?(\\w+)?\\)";
		Matcher m = Pattern.compile(re).matcher(input);
		if (!m.matches()) System.err.println("Invalid function: " + input); //$NON-NLS-1$
		name = m.group(1); //the function name
		String a1 = m.group(2); //plain arguments with commas
		String da = m.group(5); //argument with range
		String daMin = m.group(6); //range min
		String daMax = m.group(7); //range max
		String a2 = m.group(9); //last argument
		String[] aa1 = a1.length() > 0 ? a1.split(",") : new String[0];
		arguments = new String[aa1.length + (da != null ? 1 : 0) + (a2 != null ? 1 : 0)];
		System.arraycopy(aa1,0,arguments,0,aa1.length);
		if (da == null)
			{
			dynArgIndex = -1;
			dynArgMin = 0;
			dynArgMax = 0;
			}
		else
			{
			dynArgIndex = aa1.length;
			dynArgMin = Integer.parseInt(daMin);
			dynArgMax = Integer.parseInt(daMax);
			arguments[aa1.length] = da;
			}
		if (a2 != null) arguments[arguments.length - 1] = a2;
		description = ""; //$NON-NLS-1$
		}

	public Function(String func, String args, String desc)
		{
		name = func;
		arguments = args.split(",");
		description = desc;

		dynArgIndex = -1;
		dynArgMin = 0;
		dynArgMax = 0;
		}
	
	}
	
	}

