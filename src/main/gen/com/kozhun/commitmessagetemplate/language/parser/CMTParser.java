// This is a generated file. Not intended for manual editing.
package com.kozhun.commitmessagetemplate.language.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.kozhun.commitmessagetemplate.language.psi.CMTTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class CMTParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType root_, PsiBuilder builder_) {
    parseLight(root_, builder_);
    return builder_.getTreeBuilt();
  }

  public void parseLight(IElementType root_, PsiBuilder builder_) {
    boolean result_;
    builder_ = adapt_builder_(root_, builder_, this, null);
    Marker marker_ = enter_section_(builder_, 0, _COLLAPSE_, null);
    result_ = parse_root_(root_, builder_);
    exit_section_(builder_, 0, marker_, root_, result_, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType root_, PsiBuilder builder_) {
    return parse_root_(root_, builder_, 0);
  }

  static boolean parse_root_(IElementType root_, PsiBuilder builder_, int level_) {
    return cmtFile(builder_, level_ + 1);
  }

  /* ********************************************************** */
  // '$CARET_POSITION'
  public static boolean caret_position(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "caret_position")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CARET_POSITION, "<caret position>");
    result_ = consumeToken(builder_, "$CARET_POSITION");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // content_
  static boolean cmtFile(PsiBuilder builder_, int level_) {
    return content_(builder_, level_ + 1);
  }

  /* ********************************************************** */
  // item_*
  static boolean content_(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "content_")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!item_(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "content_", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // variable_ | plain_text
  static boolean item_(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "item_")) return false;
    boolean result_;
    result_ = variable_(builder_, level_ + 1);
    if (!result_) result_ = plain_text(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // OTHER_TEXT+
  public static boolean plain_text(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "plain_text")) return false;
    if (!nextTokenIs(builder_, OTHER_TEXT)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OTHER_TEXT);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, OTHER_TEXT)) break;
      if (!empty_element_parsed_guard_(builder_, "plain_text", pos_)) break;
    }
    exit_section_(builder_, marker_, PLAIN_TEXT, result_);
    return result_;
  }

  /* ********************************************************** */
  // '$SCOPE'
  public static boolean scope(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "scope")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SCOPE, "<scope>");
    result_ = consumeToken(builder_, "$SCOPE");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // '$TASK_ID'
  public static boolean task_id(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "task_id")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TASK_ID, "<task id>");
    result_ = consumeToken(builder_, "$TASK_ID");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // '$TYPE'
  public static boolean type(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "type")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TYPE, "<type>");
    result_ = consumeToken(builder_, "$TYPE");
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // task_id | type | scope | caret_position
  static boolean variable_(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "variable_")) return false;
    boolean result_;
    result_ = task_id(builder_, level_ + 1);
    if (!result_) result_ = type(builder_, level_ + 1);
    if (!result_) result_ = scope(builder_, level_ + 1);
    if (!result_) result_ = caret_position(builder_, level_ + 1);
    return result_;
  }

}
