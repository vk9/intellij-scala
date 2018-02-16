package org.jetbrains.plugins.scala.codeInsight.implicits

import com.intellij.codeHighlighting.EditorBoundHighlightingPass
import com.intellij.codeInsight.hints.InlayInfo
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.util.CaretVisualPositionKeeper
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiNamedElement
import com.intellij.util.DocumentUtil
import org.jetbrains.plugins.scala.actions.ShowImplicitArgumentsAction
import org.jetbrains.plugins.scala.annotator.ScalaAnnotator
import org.jetbrains.plugins.scala.codeInsight.implicits.ImplicitHintsPass._
import org.jetbrains.plugins.scala.extensions._
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiElement
import org.jetbrains.plugins.scala.lang.psi.api.ImplicitParametersOwner
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.ScReferencePattern
import org.jetbrains.plugins.scala.lang.psi.api.expr.{ScExpression, ScNewTemplateDefinition}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScMember
import org.jetbrains.plugins.scala.lang.resolve.ScalaResolveResult

private class ImplicitHintsPass(editor: Editor, rootElement: ScalaPsiElement)
  extends EditorBoundHighlightingPass(editor, rootElement.getContainingFile, true) {

  private var hints: Seq[Hint] = Seq.empty

  override def doCollectInformation(indicator: ProgressIndicator): Unit = {
    hints = Seq.empty

    if (myDocument != null && rootElement.containingVirtualFile.isDefined) {
      collectConversionsAndArguments()
    }
  }

  private def collectConversionsAndArguments(): Unit = {
    rootElement.depthFirst().foreach {
      case e: ScExpression =>
        if (ImplicitHints.enabled) {
          e.implicitConversion().foreach { conversion =>
            hints +:= implicitConversionHint(e, conversion)
          }
        }

        e match {
          case owner@(_: ImplicitParametersOwner | _: ScNewTemplateDefinition) =>
            ShowImplicitArgumentsAction.implicitParams(owner).foreach { arguments =>
              val typeAware = ScalaAnnotator.isAdvancedHighlightingEnabled(e) && !e.isInDottyModule
              def argumentsMissing = arguments.exists(ShowImplicitArgumentsAction.missingImplicitArgumentIn(_).isDefined)
              if (ImplicitHints.enabled || (typeAware && argumentsMissing)) {
                hints +:= implicitArgumentsHint(owner, arguments)
              }
            }
          case _ =>
        }
      case _ =>
    }
  }

  override def doApplyInformationToEditor(): Unit = {
    val caretKeeper = new CaretVisualPositionKeeper(myEditor)
    regenerateHints()
    caretKeeper.restoreOriginalLocation(false)

    if (rootElement == myFile) {
      ImplicitHints.setUpToDate(myEditor, myFile)
    }
  }

  private def regenerateHints(): Unit = {
    val inlayModel = myEditor.getInlayModel
    val existingInlays = inlayModel.inlaysIn(rootElement.getTextRange)

    val bulkChange = existingInlays.length + hints.length  > BulkChangeThreshold

    DocumentUtil.executeInBulk(myEditor.getDocument, bulkChange, () => {
      existingInlays.foreach(Disposer.dispose)

      hints.foreach { case (prefix, e, suffix) =>
        if (prefix.nonEmpty) {
          val info = new InlayInfo(prefix, e.getTextRange.getStartOffset, false, true, false)
          inlayModel.addInlay(info, error = prefix.contains(MissingImplicitArgument))
        }
        if (suffix.nonEmpty) {
          val info = new InlayInfo(suffix, e.getTextRange.getEndOffset, false, true, true)
          inlayModel.addInlay(info, error = suffix.contains(MissingImplicitArgument))
        }
      }
    })
  }
}

private object ImplicitHintsPass {
  private final val BulkChangeThreshold = 1000
  private final val MissingImplicitArgument = "?: "

  def implicitConversionHint(e: ScExpression, conversion: ScalaResolveResult): Hint = {
    val name = nameOf(conversion.element)
    (name + "(", e, if (conversion.implicitParameters.nonEmpty) ")(...)" else ")")
  }

  private def nameOf(e: PsiNamedElement): String = e match {
    case member: ScMember => nameOf(member)
    case (_: ScReferencePattern) && Parent(Parent(member: ScMember with PsiNamedElement)) => nameOf(member)
    case it => it.name
  }

  private def nameOf(member: ScMember with PsiNamedElement) =
    Option(member.containingClass).map(_.name + ".").mkString + member.name

  def implicitArgumentsHint(e: ScExpression, arguments: Seq[ScalaResolveResult]): Hint =
    ("", e, arguments.map(presentationOf).mkString("(", ", ", ")"))

  // TODO Show missing implicit parameter name?
  private def presentationOf(argument: ScalaResolveResult): String = {
    ShowImplicitArgumentsAction.missingImplicitArgumentIn(argument)
      .map(MissingImplicitArgument + _.map(_.presentableText).getOrElse("NotInferred"))
      .getOrElse {
        val name = nameOf(argument.element)
        if (argument.implicitParameters.nonEmpty) name + "(...)" else name
      }
  }
}
