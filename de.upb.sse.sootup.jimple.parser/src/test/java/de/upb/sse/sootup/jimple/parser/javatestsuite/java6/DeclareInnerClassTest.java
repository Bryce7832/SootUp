package de.upb.sse.sootup.jimple.parser.javatestsuite.java6;

import de.upb.sse.sootup.core.model.SootMethod;
import de.upb.sse.sootup.core.signatures.MethodSignature;
import de.upb.sse.sootup.java.core.JavaIdentifierFactory;
import de.upb.sse.sootup.java.core.types.JavaClassType;
import de.upb.sse.sootup.jimple.parser.categories.Java8Test;
import de.upb.sse.sootup.jimple.parser.javatestsuite.JimpleTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class DeclareInnerClassTest extends JimpleTestSuiteBase {

  final JavaClassType innerClassType =
      JavaIdentifierFactory.getInstance()
          .getClassType(getDeclaredClassSignature().getFullyQualifiedName() + "$InnerClass");

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "methodDisplayOuter", "void", Collections.emptyList());
  }

  public MethodSignature getInnerMethodSignature() {
    return identifierFactory.getMethodSignature(
        innerClassType, "methodDisplayInner", "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    //    SootClass sootClass = loadClass(getDeclaredClassSignature());

    SootMethod sootMethod = loadMethod(getMethodSignature());
    assertJimpleStmts(sootMethod, expectedBodyStmts());

    //    SootClass innerClass = loadClass(innerClassType);
    SootMethod innerMethod = loadMethod(getInnerMethodSignature());
    assertJimpleStmts(innerMethod, expectedInnerClassBodyStmts());
  }

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: DeclareInnerClass",
            "$stack1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack1.<java.io.PrintStream: void println(java.lang.String)>(\"methodDisplayOuter\")",
            "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedInnerClassBodyStmts() {
    return Stream.of(
            "l0 := @this: DeclareInnerClass$InnerClass",
            "$stack1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack1.<java.io.PrintStream: void println(java.lang.String)>(\"methodDisplayInner\")",
            "return")
        .collect(Collectors.toList());
  }
}