/*
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2009, 2010 IsmAvatar <IsmAvatar@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.jedit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GMLKeywords
	{
	public static Construct[] CONSTRUCTS;
	public static Operator[] OPERATORS;
	public static Variable[] VARIABLES;
	public static Constant[] CONSTANTS;
	public static Function[] FUNCTIONS;

	static
		{
		InputStream is = GMLKeywords.class.getResourceAsStream("gmlkeywords.properties");
		Properties p = new Properties();
		try
			{
			p.load(is);
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		String[] s = p.getProperty("CONSTRUCTS").split("\\s+");
		CONSTRUCTS = new Construct[s.length];
		for (int i = 0; i < s.length; i++)
			CONSTRUCTS[i] = new Construct(s[i]);
		s = p.getProperty("OPERATORS").split("\\s+");
		OPERATORS = new Operator[s.length];
		for (int i = 0; i < s.length; i++)
			OPERATORS[i] = new Operator(s[i]);
		s = p.getProperty("VARIABLES").split("\\s+");
		VARIABLES = new Variable[s.length];
		for (int i = 0; i < s.length; i++)
			VARIABLES[i] = new Variable(s[i]);
		s = p.getProperty("CONSTANTS").split("\\s+");
		CONSTANTS = new Constant[s.length];
		for (int i = 0; i < s.length; i++)
			CONSTANTS[i] = new Constant(s[i]);
		p.clear();

		//read functions
		InputStream is2 = GMLKeywords.class.getResourceAsStream("functions.txt");
		BufferedReader	br2 = new BufferedReader(new InputStreamReader(is2));
		ArrayList<Function> funcList = new ArrayList<Function>();

		try
			{
			String func;
			while ((func = br2.readLine()) != null)
				{
				String args = br2.readLine();
				String desc = br2.readLine();
				funcList.add(new Function(func,args,desc));
				}
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}

		FUNCTIONS = funcList.toArray(new Function[0]);
		}

	private GMLKeywords()
		{
		}

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
