package com.kozhun.commitmessagetemplate.language.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.kozhun.commitmessagetemplate.language.psi.CMTTypes.*;

%%

%{
  public _CMTLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _CMTLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

WHITE_SPACE=[ \t\n\x0B\f\r]+
TEXT_SAFE=[^$\s]+

%%
<YYINITIAL> {
  "\$TASK_ID"         { return TASK_ID; }
  "\$TYPE"            { return TYPE; }
  "\$SCOPE"           { return SCOPE; }
  "\$CARET_POSITION"  { return CARET_POSITION; }
  {WHITE_SPACE}       { return WHITE_SPACE; }
  {TEXT_SAFE}         { return OTHER_TEXT; }
  "$"                 { return OTHER_TEXT; }
}

[^] { return OTHER_TEXT; }
