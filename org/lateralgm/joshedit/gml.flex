/*
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This is a text editor. It's free software. You can use,
 * modify, and distribute it under the terms of the GNU
 * General Public License, version 3 or later. 
 */

package org.lateralgm.joshedit;

%%

%public
%class Lexer
%extends Symbol
%unicode
%type Symbol

%{
  StringBuffer string = new StringBuffer();

  private Symbol symbol(Symbol type, Object value) {
    return new Symbol(type, value);
  }
%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]

/* comments */
TraditionalComment   = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment     = "//" {InputCharacter}* {LineTerminator}?
DocumentationComment = "/**" {CommentContent} "*"+ "/"
CommentContent       = ( [^*] | \*+ [^/*] )*
/* comments */

Identifier = [:jletter:] [:jletterdigit:]*

NumberLiteral = "-"? [0-9]* "."? [0-9]+

%state sDSTRING
%state sSSTRING

%%

/* keywords */
<YYINITIAL> "break"              { return KEY_BREAK; }

<YYINITIAL> {
  /* identifiers */
  {Identifier}                   { return IDENT_VAR; }
  {Identifier} "("               { return IDENT_FUNC; }

  /* literals */
  {NumberLiteral}                { return LIT_NUMBER; }
  \"                             { string.setLength(0); yybegin(sDSTRING); }
  "'"                            { string.setLength(0); yybegin(sSSTRING); }

  /* operators */
  "="                            { return OP_EQ; }
  "=="                           { return OP_EQEQ; }
  "+"                            { return OP_PLUS; }

  /* comments */
  {EndOfLineComment}             { return COM_LINE; }
  {TraditionalComment}           { return COM_SPAN; }
  {DocumentationComment}         { return COM_DOC; }
 
  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }

  ";"                            { /* ignore for now */ }
}

<sDSTRING> {
  \"                             { yybegin(YYINITIAL); 
                                   return symbol(LIT_STR_DOUBLE, string.toString()); }
  [^\n\r\"]+                     { string.append( yytext() ); }
}

<sSSTRING> {
  "'"                            { yybegin(YYINITIAL);
                                   return symbol(LIT_STR_SINGLE, string.toString()); }
  [^\n\r']+                      { string.append( yytext() ); }
}

/* error fallback */
.|\n                             { throw new Error("Illegal character <"+yytext()+">"); }