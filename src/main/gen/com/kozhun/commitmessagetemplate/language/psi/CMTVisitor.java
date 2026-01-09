// This is a generated file. Not intended for manual editing.
package com.kozhun.commitmessagetemplate.language.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class CMTVisitor extends PsiElementVisitor {

  public void visitCaretPosition(@NotNull CMTCaretPosition o) {
    visitPsiElement(o);
  }

  public void visitPlainText(@NotNull CMTPlainText o) {
    visitPsiElement(o);
  }

  public void visitScope(@NotNull CMTScope o) {
    visitPsiElement(o);
  }

  public void visitTaskId(@NotNull CMTTaskId o) {
    visitPsiElement(o);
  }

  public void visitType(@NotNull CMTType o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
