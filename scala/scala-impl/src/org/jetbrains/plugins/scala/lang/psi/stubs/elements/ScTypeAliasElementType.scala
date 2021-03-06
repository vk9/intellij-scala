package org.jetbrains.plugins.scala
package lang
package psi
package stubs
package elements

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.{IndexSink, StubElement, StubInputStream, StubOutputStream}
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScTypeAlias, ScTypeAliasDeclaration, ScTypeAliasDefinition}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScObject
import org.jetbrains.plugins.scala.lang.psi.stubs.impl.ScTypeAliasStubImpl

/**
  * User: Alexander Podkhalyuzin
  * Date: 18.10.2008
  */
abstract class ScTypeAliasElementType[Func <: ScTypeAlias](debugName: String)
  extends ScStubElementType[ScTypeAliasStub, ScTypeAlias](debugName) {
  override def serialize(stub: ScTypeAliasStub, dataStream: StubOutputStream): Unit = {
    dataStream.writeName(stub.getName)
    dataStream.writeOptionName(stub.typeText)
    dataStream.writeOptionName(stub.lowerBoundText)
    dataStream.writeOptionName(stub.upperBoundText)
    dataStream.writeBoolean(stub.isLocal)
    dataStream.writeBoolean(stub.isDeclaration)
    dataStream.writeBoolean(stub.isStableQualifier)
  }

  override def deserialize(dataStream: StubInputStream, parentStub: StubElement[_ <: PsiElement]): ScTypeAliasStub = {
    new ScTypeAliasStubImpl(parentStub.asInstanceOf[StubElement[PsiElement]], this,
      name = dataStream.readNameString,
      typeText = dataStream.readOptionName,
      lowerBoundText = dataStream.readOptionName,
      upperBoundText = dataStream.readOptionName,
      isLocal = dataStream.readBoolean,
      isDeclaration = dataStream.readBoolean,
      isStableQualifier = dataStream.readBoolean)
  }

  override def createStubImpl(alias: ScTypeAlias, parentStub: StubElement[_ <: PsiElement]): ScTypeAliasStub = {
    val maybeAlias = Option(alias)

    val aliasedTypeText = maybeAlias.collect {
      case definition: ScTypeAliasDefinition => definition
    }.flatMap {
      _.aliasedTypeElement
    }.map {
      _.getText
    }

    val maybeDeclaration = maybeAlias.collect {
      case declaration: ScTypeAliasDeclaration => declaration
    }
    val lowerBoundText = maybeDeclaration.flatMap {
      _.lowerTypeElement
    }.map {
      _.getText
    }
    val upperBoundText = maybeDeclaration.flatMap {
      _.upperTypeElement
    }.map {
      _.getText
    }

    val maybeContainingClass = maybeAlias.map {
      _.containingClass
    }
    val isStableQualifier = maybeContainingClass.collect {
      case o: ScObject if ScalaPsiUtil.hasStablePath(alias) => o
    }.isDefined

    new ScTypeAliasStubImpl(parentStub, this,
      name = alias.name,
      typeText = aliasedTypeText,
      lowerBoundText = lowerBoundText,
      upperBoundText = upperBoundText,
      isLocal = maybeContainingClass.isEmpty,
      isDeclaration = maybeDeclaration.isDefined,
      isStableQualifier = isStableQualifier)
  }

  override def indexStub(stub: ScTypeAliasStub, sink: IndexSink): Unit = {
    val name = stub.getName
    sink.occurrence(index.ScalaIndexKeys.TYPE_ALIAS_NAME_KEY, name)
    if (stub.isStableQualifier) sink.occurrence(index.ScalaIndexKeys.STABLE_ALIAS_NAME_KEY, name)
  }
}