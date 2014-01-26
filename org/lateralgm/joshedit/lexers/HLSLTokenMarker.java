/**
* @file  HLSLTokenMarker.java
* @brief Class implementing an HLSL lexer for syntax highlighting.
*
* @section License
*
* Copyright (C) 2013-2014 Robert B. Colton
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

import org.lateralgm.joshedit.DefaultKeywords;
import org.lateralgm.joshedit.DefaultTokenMarker;
import org.lateralgm.joshedit.DefaultKeywords.Constant;
import org.lateralgm.joshedit.DefaultKeywords.Construct;
import org.lateralgm.joshedit.DefaultKeywords.Function;
import org.lateralgm.joshedit.DefaultKeywords.Keyword;
import org.lateralgm.joshedit.DefaultKeywords.Operator;
import org.lateralgm.joshedit.DefaultKeywords.Variable;

/**
 * Sample HLSL token marker class based on the default token marker.
 */
public class HLSLTokenMarker extends DefaultTokenMarker
{

private static final Color BROWN = new Color(200,0,0);
private static final Color FUNCTION = new Color(0,100,150);
static KeywordSet resNames, scrNames, constructs, functions, operators, constants, variables;

	/** Construct, populating language data. */
	public HLSLTokenMarker()
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
		for (Function f : HLSLKeywords.FUNCTIONS) {
			Collections.addAll(functions.words, f.getName());
		}
		constructs = addKeywordSet("Constructs", FUNCTION, Font.BOLD);
		for (Construct c : HLSLKeywords.CONSTRUCTS) {
			Collections.addAll(constructs.words, c.getName());
		}
		operators = addKeywordSet("Operators", Color.BLACK ,Font.BOLD);
		for (Operator o : HLSLKeywords.OPERATORS) {
			Collections.addAll(functions.words, o.getName());
		}
		constants = addKeywordSet("Constants", BROWN, Font.PLAIN);
		for (Constant c : HLSLKeywords.CONSTANTS) {
			Collections.addAll(constants.words, c.getName());
		}
		variables = addKeywordSet("Variables", Color.BLUE, Font.ITALIC);
		for (Variable v : HLSLKeywords.VARIABLES) {
			Collections.addAll(variables.words, v.getName());
		}
		
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
		DefaultKeywords.Keyword[][] HLSL_KEYWORDS = { HLSLKeywords.CONSTRUCTS,
		HLSLKeywords.FUNCTIONS,HLSLKeywords.VARIABLES,HLSLKeywords.OPERATORS,HLSLKeywords.CONSTANTS };
		return HLSL_KEYWORDS;
		}
}
