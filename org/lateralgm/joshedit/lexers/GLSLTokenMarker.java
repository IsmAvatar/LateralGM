/**
* @file  GLSLTokenMarker.java
* @brief Class implementing a GLSL lexer for syntax highlighting.
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

import java.awt.Color;
import java.awt.Font;
import java.util.Collections;

import org.lateralgm.joshedit.lexers.DefaultKeywords.Constant;
import org.lateralgm.joshedit.lexers.DefaultKeywords.Construct;
import org.lateralgm.joshedit.lexers.DefaultKeywords.Function;
import org.lateralgm.joshedit.lexers.DefaultKeywords.Keyword;
import org.lateralgm.joshedit.lexers.DefaultKeywords.Operator;
import org.lateralgm.joshedit.lexers.DefaultKeywords.Variable;

/**
 * Sample GLSL token marker class based on the default token marker.
 */
public class GLSLTokenMarker extends DefaultTokenMarker
{

private static final Color BROWN = new Color(200,0,0);
private static final Color FUNCTION = new Color(0,100,150);
//new Color(255,0,128);
static KeywordSet resNames, scrNames, constructs, functions, operators, constants, variables;

	/** Construct, populating language data. */
	public GLSLTokenMarker()
	{
		super();
		schemes.add(new BlockDescriptor("Javadoc","/\\*(?=\\*)","\\*/",new Color(128,128,255),Font.BOLD));
		schemes.add(new BlockDescriptor("Block Comment","/(?=\\*)","\\*/",new Color(13,135,13),
				Font.ITALIC));
		schemes.add(new BlockDescriptor("Doc Line Comment","///","$",new Color(128,128,255),Font.BOLD));
		schemes.add(new BlockDescriptor("Line Comment","//","$",new Color(13,135,13),Font.ITALIC));
		schemes.add(new BlockDescriptor("Double Quote String","\"","\"",new Color(0,0,255),0));
		schemes.add(new BlockDescriptor("Single Quote String","'","'",new Color(0,0,255),0));
		
		functions = addKeywordSet("Functions", FUNCTION, Font.PLAIN);
		for (Function f : GLSLKeywords.FUNCTIONS) {
			Collections.addAll(functions.words, f.getName());
		}
		constructs = addKeywordSet("Constructs", FUNCTION, Font.BOLD);
		for (Construct c : GLSLKeywords.CONSTRUCTS) {
			Collections.addAll(constructs.words, c.getName());
		}
		operators = addKeywordSet("Operators", Color.BLACK ,Font.BOLD);
		for (Operator o : GLSLKeywords.OPERATORS) {
			Collections.addAll(functions.words, o.getName());
		}
		constants = addKeywordSet("Constants", BROWN, Font.PLAIN);
		for (Constant c : GLSLKeywords.CONSTANTS) {
			Collections.addAll(constants.words, c.getName());
		}
		variables = addKeywordSet("Variables", Color.BLUE, Font.ITALIC);
		for (Variable v : GLSLKeywords.VARIABLES) {
			Collections.addAll(variables.words, v.getName());
		}
		
		//tmKeywords.add(kws);
		tmKeywords.add(functions);
		tmKeywords.add(constructs);
		tmKeywords.add(operators);
		tmKeywords.add(constants);
		tmKeywords.add(variables);

		CharSymbolSet css = new CharSymbolSet("Operators and Separators",new Color(200,0,0),0);
		char[] ca = "{[()]}!@%^&*-/+=?:~<>.,;".toCharArray();
		for (int i = 0; i < ca.length; i++)
			css.chars.add(ca[i]);
		tmChars.add(css);

		otherTokens.add(new SimpleToken("Numeric literal","[0-9]+",0,new Color(20, 50, 90)));
		otherTokens.add(new SimpleToken("Hex literal","\\$[0-9A-Fa-f]+",0,new Color(100,100,255)));
	}

	@Override
	public Keyword[][] GetKeywords()
		{
		DefaultKeywords.Keyword[][] GLSL_KEYWORDS = { GLSLKeywords.CONSTRUCTS,
		GLSLKeywords.FUNCTIONS,GLSLKeywords.VARIABLES,GLSLKeywords.OPERATORS,GLSLKeywords.CONSTANTS };
		return GLSL_KEYWORDS;
		}
}
