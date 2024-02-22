package sootup.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;

import categories.Java8Test;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.signatures.PackageName;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaSootClass;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.AnnotationType;

@Category(Java8Test.class)
public class AnnotationUsageInheritedTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void testInheritedAnnotationOnClass() {
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
    Map<String, Object> annotationParamMap = new HashMap<>();

    annotationParamMap.put("sthBlue", IntConstant.getInstance(42));
    annotationParamMap.put("author", JavaJimple.getInstance().newStringConstant("GeorgeLucas"));

    assertEquals(
        Collections.singletonList(
            new AnnotationUsage(
                new AnnotationType("OnClass", new PackageName(""), true), annotationParamMap)),
        sootClass.getAnnotations(Optional.of(customTestWatcher.getJavaView())));
  }
}
