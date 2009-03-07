/*
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2009 IsmAvatar <IsmAvatar@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.jedit;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lateralgm.main.LGM;

public final class GMLKeywords
	{
	public static final Construct[] CONSTRUCTS;
	public static final Operator[] OPERATORS;
	public static final Variable[] VARIABLES;
	public static final Constant[] CONSTANTS;
	public static final Function[] FUNCTIONS;

	static
		{
		final String fn = "gmlkeywords.properties";
		InputStream is;
		File dir = LGM.workDir;
		if (!dir.isDirectory()) dir = dir.getParentFile();
		try
			{
			is = new BufferedInputStream(new FileInputStream(new File(dir,fn)));
			}
		catch (FileNotFoundException e1)
			{
			is = GMLTokenMarker.class.getResourceAsStream(fn);
			}
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
		s = p.getProperty("FUNCTIONS").split("\\s+");
		FUNCTIONS = new Function[s.length];
		for (int i = 0; i < s.length; i++)
			FUNCTIONS[i] = new Function(s[i]);
		p.clear();
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
			if (!m.matches()) System.err.println("Invalid variable: " + input);
			name = m.group(1);
			String s = m.group(3);
			arraySize = s != null ? Integer.valueOf(m.group(3)) : 0;
			readOnly = "*".equals(m.group(4));
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
		public final String[] arguments;
		public final int dynArgIndex;
		public final int dynArgMin;
		public final int dynArgMax;

		public Function(String input)
			{
			String re = "(\\w+)\\(((\\w+,)*)((\\w+)\\{(\\d+),(\\d+)}((?=\\))|,))?(\\w+)?\\)";
			Matcher m = Pattern.compile(re).matcher(input);
			if (!m.matches()) System.err.println("Invalid function: " + input);
			name = m.group(1);
			String a1 = m.group(2);
			String da = m.group(5);
			String daMin = m.group(6);
			String daMax = m.group(7);
			String a2 = m.group(9);
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
			}
		}
	}
