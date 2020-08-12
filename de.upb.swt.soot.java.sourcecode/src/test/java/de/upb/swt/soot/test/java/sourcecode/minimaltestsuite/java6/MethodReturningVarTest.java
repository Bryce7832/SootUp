package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Hasitha Rajapakse
 * @author Kaustubh Kelkar
 */
@Category(Java8Test.class)
public class MethodReturningVarTest extends MinimalSourceTestSuiteBase {

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature("short"));
    assertJimpleStmts(method, expectedBodyStmtsShort());

    method = loadMethod(getMethodSignature("byte"));
    assertJimpleStmts(method, expectedBodyStmtsByte());

    method = loadMethod(getMethodSignature("char"));
    assertJimpleStmts(method, expectedBodyStmtsChar());

    method = loadMethod(getMethodSignature("int"));
    assertJimpleStmts(method, expectedBodyStmtsInt());

    method = loadMethod(getMethodSignature("long"));
    assertJimpleStmts(method, expectedBodyStmtsLong());

    method = loadMethod(getMethodSignature("float"));
    assertJimpleStmts(method, expectedBodyStmtsFloat());

    method = loadMethod(getMethodSignature("double"));
    assertJimpleStmts(method, expectedBodyStmtsDouble());
  }

  public MethodSignature getMethodSignature(String datatype) {
    return identifierFactory.getMethodSignature(
        datatype + "Variable", getDeclaredClassSignature(), datatype, Collections.emptyList());
  }

  public List<String> expectedBodyStmtsShort() {
    return Stream.of("r0 := @this: MethodReturningVar", "$i0 = 10", "return $i0")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsByte() {
    return Stream.of("r0 := @this: MethodReturningVar", "$i0 = 0", "return $i0")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsChar() {
    return Stream.of("r0 := @this: MethodReturningVar", "$i0 = 97", "return $i0")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsInt() {
    return Stream.of("r0 := @this: MethodReturningVar", "$i0 = 512", "return $i0")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsLong() {
    return Stream.of("r0 := @this: MethodReturningVar", "$i0 = 123456789", "return $i0")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsFloat() {
    return Stream.of("r0 := @this: MethodReturningVar", "$f0 = 3.14F", "return $f0")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsDouble() {
    return Stream.of("r0 := @this: MethodReturningVar", "$d0 = 1.96969654", "return $d0")
        .collect(Collectors.toList());
  }
}
