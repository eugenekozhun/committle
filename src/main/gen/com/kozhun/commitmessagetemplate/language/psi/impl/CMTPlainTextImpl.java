// This is a generated file. Not intended for manual editing.
package com.kozhun.commitmessagetemplate.language.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.kozhun.commitmessagetemplate.language.psi.CMTTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.kozhun.commitmessagetemplate.language.psi.*;

public class CMTPlainTextImpl extends ASTWrapperPsiElement implements CMTPlainText {

  public CMTPlainTextImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull CMTVisitor visitor) {
    visitor.visitPlainText(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CMTVisitor) accept((CMTVisitor)visitor);
    else super.accept(visitor);
  }

}
