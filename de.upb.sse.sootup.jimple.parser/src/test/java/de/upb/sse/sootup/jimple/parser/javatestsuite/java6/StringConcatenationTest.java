package de.upb.sse.sootup.jimple.parser.javatestsuite.java6;

import de.upb.sse.sootup.core.model.SootMethod;
import de.upb.sse.sootup.core.signatures.MethodSignature;
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
public class StringConcatenationTest extends JimpleTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "stringConcatenation", "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmts() {
    return Stream.of("l0 := @this: StringConcatenation", "l1 = \"thestring\"", "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}