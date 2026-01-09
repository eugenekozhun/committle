// This is a generated file. Not intended for manual editing.
package com.kozhun.commitmessagetemplate.language.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.kozhun.commitmessagetemplate.language.grammar.CMTElementType;
import com.kozhun.commitmessagetemplate.language.grammar.CMTTokenType;
import com.kozhun.commitmessagetemplate.language.psi.impl.*;

public interface CMTTypes {

  IElementType CARET_POSITION = new CMTElementType("CARET_POSITION");
  IElementType PLAIN_TEXT = new CMTElementType("PLAIN_TEXT");
  IElementType SCOPE = new CMTElementType("SCOPE");
  IElementType TASK_ID = new CMTElementType("TASK_ID");
  IElementType TYPE = new CMTElementType("TYPE");

  IElementType OTHER_TEXT = new CMTTokenType("OTHER_TEXT");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == CARET_POSITION) {
        return new CMTCaretPositionImpl(node);
      }
      else if (type == PLAIN_TEXT) {
        return new CMTPlainTextImpl(node);
      }
      else if (type == SCOPE) {
        return new CMTScopeImpl(node);
      }
      else if (type == TASK_ID) {
        return new CMTTaskIdImpl(node);
      }
      else if (type == TYPE) {
        return new CMTTypeImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
