/**
* @file  GLSLESKeywords.java
* @brief Class implementing a GLSLES keyword container.
*
* @section License
*
* Copyright (C) 2013 Robert B. Colton
* This file is a part of the LateralGM IDE.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
**/

package org.lateralgm.joshedit.lexers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;
import org.lateralgm.joshedit.lexers.DefaultKeywords.Constant;
import org.lateralgm.joshedit.lexers.DefaultKeywords.Construct;
import org.lateralgm.joshedit.lexers.DefaultKeywords.Function;
import org.lateralgm.joshedit.lexers.DefaultKeywords.Operator;
import org.lateralgm.joshedit.lexers.DefaultKeywords.Variable;

public final class GLESKeywords
	{
	public static Construct[] CONSTRUCTS;
	public static Operator[] OPERATORS;
	public static Variable[] VARIABLES;
	public static Constant[] CONSTANTS;
	public static Function[] FUNCTIONS;

	static
		{
		InputStream is = GLESKeywords.class.getResourceAsStream("glsleskeywords.properties");
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
		InputStream is2 = GLESKeywords.class.getResourceAsStream("glslesfunctions.txt");
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

	public GLESKeywords()
		{
		}

	}
