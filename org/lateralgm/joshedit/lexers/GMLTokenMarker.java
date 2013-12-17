/**
* @file  GMLTokenMarker.java
* @brief Class implementing a GML lexer for syntax highlighting.
*
* @section License
* 
* Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
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

import org.lateralgm.joshedit.lexers.DefaultTokenMarker.KeywordSet;

/**
 * Sample GML token marker class based on the default token marker.
 */
public class GMLTokenMarker extends DefaultTokenMarker
{

private static final Color BROWN = new Color(150,0,0);
private static final Color FUNCTION = new Color(0,100,150);
static KeywordSet resNames, scrNames, constructs, functions, operators, constants, variables;

	/** Construct, populating language data. */
	public GMLTokenMarker()
	{
		super();
		schemes.add(new BlockDescriptor("Javadoc","/\\*(?=\\*)","\\*/",new Color(54,116,186),Font.BOLD));
		schemes.add(new BlockDescriptor("Block Comment","/(?=\\*)","\\*/",new Color(13,135,13),
				Font.ITALIC));
		schemes.add(new BlockDescriptor("Doc Line Comment","///","$",new Color(54,116,186),Font.BOLD));
		schemes.add(new BlockDescriptor("Line Comment","//","$",new Color(13,135,13),Font.ITALIC));
		schemes.add(new BlockDescriptor("Double Quote String","\"","\"",new Color(0,0,100),0));
		schemes.add(new BlockDescriptor("Single Quote String","'","'",new Color(0,0,100),0));
		
		functions = addKeywordSet("Functions", FUNCTION, Font.PLAIN);
		for (GMLKeywords.Function f : GMLKeywords.FUNCTIONS) {
			Collections.addAll(functions.words, f.getName());
		}
		constructs = addKeywordSet("Constructs", new Color(0,0,200), Font.PLAIN);
		for (GMLKeywords.Construct c : GMLKeywords.CONSTRUCTS) {
			Collections.addAll(constructs.words, c.getName());
		}
		operators = addKeywordSet("Operators", new Color(0,0,200), Font.PLAIN);
		for (GMLKeywords.Operator o : GMLKeywords.OPERATORS) {
			Collections.addAll(operators.words, o.getName());
		}
		constants = addKeywordSet("Constants", BROWN, Font.PLAIN);
		for (GMLKeywords.Constant c : GMLKeywords.CONSTANTS) {
			Collections.addAll(constants.words, c.getName());
		}
		variables = addKeywordSet("Variables", new Color(0,0,100), Font.ITALIC);
		for (GMLKeywords.Variable v : GMLKeywords.VARIABLES) {
			Collections.addAll(variables.words, v.getName());
		}
		
		tmKeywords.add(functions);
		tmKeywords.add(constructs);
		tmKeywords.add(operators);
		tmKeywords.add(constants);
		tmKeywords.add(variables);

		CharSymbolSet css = new CharSymbolSet("Operators and Separators",new Color(0, 0, 200),0);
		char[] ca = "{[()]}!%^&*-/+=?:~<>.,;".toCharArray();
		for (int i = 0; i < ca.length; i++)
			css.chars.add(ca[i]);
		tmChars.add(css);

		otherTokens.add(new SimpleToken("Numeric literal","[0-9]+",0,BROWN));
		otherTokens.add(new SimpleToken("Hex literal","\\$[0-9A-Fa-f]+",0,new Color(100,100,255)));
	}
}
