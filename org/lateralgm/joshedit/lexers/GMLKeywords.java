/*
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2009, 2010 IsmAvatar <IsmAvatar@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.joshedit.lexers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

import org.lateralgm.joshedit.DefaultKeywords;
import org.lateralgm.joshedit.DefaultKeywords.Constant;
import org.lateralgm.joshedit.DefaultKeywords.Construct;
import org.lateralgm.joshedit.DefaultKeywords.Function;
import org.lateralgm.joshedit.DefaultKeywords.Operator;
import org.lateralgm.joshedit.DefaultKeywords.Variable;

public final class GMLKeywords
	{
	public static DefaultKeywords.Construct[] CONSTRUCTS;
	public static DefaultKeywords.Operator[] OPERATORS;
	public static DefaultKeywords.Variable[] VARIABLES;
	public static DefaultKeywords.Constant[] CONSTANTS;
	public static DefaultKeywords.Function[] FUNCTIONS;
	
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
		InputStream is2 = GMLKeywords.class.getResourceAsStream("gmlfunctions.txt");
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

	public GMLKeywords()
		{
	
		}
		
		
	}
