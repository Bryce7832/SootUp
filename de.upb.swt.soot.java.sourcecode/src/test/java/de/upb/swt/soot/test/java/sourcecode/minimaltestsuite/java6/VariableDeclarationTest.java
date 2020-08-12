/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static de.upb.swt.soot.core.util.Utils.filterJimple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import categories.Java8Test;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class VariableDeclarationTest extends MinimalSourceTestSuiteBase {

  // TODO split into multiple test cases
  @Test
  public void test() {

    SootMethod method = loadMethod(getMethodSignature("shortVariable"));
    assertJimpleStmts(method, expectedBodyStmtsShortVariable());

    method = loadMethod(getMethodSignature("byteVariable"));
    assertJimpleStmts(method, expectedBodyStmtsByteVariable());

    method = loadMethod(getMethodSignature("charVariable"));
    assertJimpleStmts(method, expectedBodyStmtsCharVariable());

    method = loadMethod(getMethodSignature("intVariable"));
    assertJimpleStmts(method, expectedBodyStmtsIntVariable());

    method = loadMethod(getMethodSignature("longVariable"));
    assertJimpleStmts(method, expectedBodyStmtsLongVariable());

    method = loadMethod(getMethodSignature("floatVariable"));
    assertJimpleStmts(method, expectedBodyStmtsFloatVariable());

    method = loadMethod(getMethodSignature("doubleVariable"));
    assertJimpleStmts(method, expectedBodyStmtsDoubleVariable());
  }

  @Ignore
  public void classTypeDefWithoutAssignment() {
    // TODO: [ms] fix: Type of Local $r1 is should be (java.lang.)String
    SootMethod method = loadMethod(getMethodSignature("classTypeDefWithoutAssignment"));
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = filterJimple(body.toString());
    assertEquals(
        expectedBodyStmts(
            "java.lang.String $r1",
            "VariableDeclaration r0",
            "r0 := @this: VariableDeclaration",
            "$r1 = null",
            "return"),
        actualStmts);
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmtsShortVariable() {
    return Stream.of("r0 := @this: VariableDeclaration", "$i0 = 10", "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsByteVariable() {
    return Stream.of("r0 := @this: VariableDeclaration", "$i0 = 0", "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsCharVariable() {
    return Stream.of("r0 := @this: VariableDeclaration", "$i0 = 97", "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsIntVariable() {
    return Stream.of("r0 := @this: VariableDeclaration", "$i0 = 512", "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsLongVariable() {
    return Stream.of("r0 := @this: VariableDeclaration", "$i0 = 123456789", "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsFloatVariable() {
    return Stream.of("r0 := @this: VariableDeclaration", "$f0 = 3.14F", "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsDoubleVariable() {
    return Stream.of("r0 := @this: VariableDeclaration", "$d0 = 1.96969654", "return")
        .collect(Collectors.toList());
  }
}
