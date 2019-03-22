package org.jetbrains.plugins.scala.lang.psi.impl.toplevel.typedef

import com.intellij.psi._
import org.jetbrains.plugins.scala.extensions.{PsiModifierListOwnerExt, PsiNamedElementExt}
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiUtil
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiUtil.inNameContext
import org.jetbrains.plugins.scala.lang.psi.api.base.{ScAccessModifier, ScFieldId}
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScClassParameter
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScFunctionDeclaration, ScFunctionDefinition, ScTypeAliasDeclaration}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScMember
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.{ScModifierListOwner, ScNamedElement}
import org.jetbrains.plugins.scala.lang.psi.types.Signature
import org.jetbrains.plugins.scala.lang.refactoring.util.ScalaNamesUtil

trait SignatureStrategy[T] {
  def equiv(t1: T, t2: T): Boolean
  def computeHashCode(t: T): Int

  def namedElement(t: T): PsiNamedElement

  def same(t1: T, t2: T): Boolean = elemName(t1) == elemName(t2) && (namedElement(t1) eq namedElement(t2))

  def identityHashCode(t: T): Int = elemName(t).hashCode + 31 * namedElement(t).hashCode()

  def elemName(t: T): String
  def isAbstract(t: T): Boolean
  def isImplicit(t: T): Boolean
  def isSynthetic(t: T): Boolean

  def isPrivate(t: T): Boolean = SignatureStrategy.isPrivateImpl(namedElement(t))
}

object SignatureStrategy {
  def apply[T: SignatureStrategy]: SignatureStrategy[T] = implicitly[SignatureStrategy[T]]

  implicit val signature: SignatureStrategy[Signature] = new SignatureStrategy[Signature] {
    def namedElement(t: Signature): PsiNamedElement = t.namedElement

    def equiv(s1: Signature, s2: Signature): Boolean = s1 equiv s2

    def computeHashCode(s: Signature): Int = s.simpleHashCode

    def elemName(t: Signature): String = t.name

    override def isAbstract(t: Signature): Boolean = t.namedElement match {
      case _: ScFunctionDeclaration => true
      case _: ScFunctionDefinition => false
      case _: ScFieldId => true
      case m: PsiModifierListOwner if m.hasModifierPropertyScala(PsiModifier.ABSTRACT) => true
      case _ => false
    }

    def isSynthetic(s: Signature): Boolean = s.namedElement match {
      case m: ScMember                => m.isSynthetic
      case inNameContext(m: ScMember) => m.isSynthetic
      case _                          => false
    }

    def isImplicit(t: Signature): Boolean = ScalaPsiUtil.isImplicit(t.namedElement)
  }

  implicit val types: SignatureStrategy[PsiNamedElement] = new SignatureStrategy[PsiNamedElement] {

    def namedElement(t: PsiNamedElement): PsiNamedElement = t

    def equiv(t1: PsiNamedElement, t2: PsiNamedElement): Boolean = t1.name == t2.name

    def computeHashCode(t: PsiNamedElement): Int = t.name.hashCode

    def elemName(t: PsiNamedElement): String = ScalaNamesUtil.clean(t.name)

    def isAbstract(t: PsiNamedElement): Boolean = t match {
      case _: ScTypeAliasDeclaration => true
      case _ => false
    }

    def isImplicit(t: PsiNamedElement) = false

    def isSynthetic(t: PsiNamedElement): Boolean = false
  }

  private def isPrivateImpl(named: PsiNamedElement): Boolean = {
    named match {
      case param: ScClassParameter if !param.isClassMember => true
      case inNameContext(s: ScModifierListOwner) =>
        s.getModifierList.accessModifier match {
          case Some(a: ScAccessModifier) => a.isUnqualifiedPrivateOrThis
          case _ => false
        }
      case s: ScNamedElement => false
      case n: PsiModifierListOwner => n.hasModifierPropertyScala("private")
      case _ => false
    }
  }

}