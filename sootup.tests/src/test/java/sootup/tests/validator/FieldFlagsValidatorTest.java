package sootup.tests.validator;

import static org.junit.Assert.assertEquals;

import categories.Java8Test;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.LocalGenerator;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.model.*;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.validation.FieldFlagsValidator;
import sootup.core.validation.ValidationException;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootField;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.OverridingJavaClassSource;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

@Category(Java8Test.class)
public class FieldFlagsValidatorTest {
  JavaView view;
  FieldFlagsValidator fieldFlagsValidator;

  @Before
  public void setUp() {
    view = new JavaView(Collections.singletonList(new EagerInputLocation()));
    fieldFlagsValidator = new FieldFlagsValidator();
  }

  public JavaSootClass testClassCreatorWithModifiers(
      EnumSet<ClassModifier> classModifierEnumSet, EnumSet<FieldModifier> fieldModifierEnumSet) {

    JavaView view = new JavaView(Collections.singletonList(new EagerInputLocation()));
    ClassType type = view.getIdentifierFactory().getClassType("java.lang.String");

    LocalGenerator generator = new LocalGenerator(new HashSet<>());
    MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature("dummyMain", "main", "void", Collections.emptyList());
    Body.BodyBuilder bodyBuilder = Body.builder();

    final JIdentityStmt firstStmt =
        Jimple.newIdentityStmt(
            generator.generateLocal(type),
            Jimple.newParameterRef(type, 0),
            StmtPositionInfo.getNoStmtPositionInfo());
    final JReturnVoidStmt returnVoidStmt =
        new JReturnVoidStmt(StmtPositionInfo.getNoStmtPositionInfo());

    MutableStmtGraph stmtGraph = bodyBuilder.getStmtGraph();
    stmtGraph.setStartingStmt(firstStmt);
    stmtGraph.putEdge(firstStmt, returnVoidStmt);

    Body body =
        bodyBuilder.setMethodSignature(methodSignature).setLocals(generator.getLocals()).build();
    assertEquals(1, body.getLocalCount());

    JavaSootField dummyField =
        new JavaSootField(
            new FieldSignature(
                new JavaClassType("FieldFlagsValidator", PackageName.DEFAULT_PACKAGE),
                "i",
                PrimitiveType.IntType.getInstance()),
            fieldModifierEnumSet,
            Collections.emptyList(),
            NoPositionInformation.getInstance());

    JavaSootMethod dummyMainMethod =
        new JavaSootMethod(
            new OverridingBodySource(methodSignature, body),
            methodSignature,
            EnumSet.of(MethodModifier.PUBLIC, MethodModifier.STATIC),
            Collections.emptyList(),
            Collections.emptyList(),
            NoPositionInformation.getInstance());

    JavaSootClass mainClass =
        new JavaSootClass(
            new OverridingJavaClassSource(
                new EagerInputLocation(),
                null,
                view.getIdentifierFactory().getClassType("dummyMain"),
                null,
                Collections.emptySet(),
                null,
                Collections.singleton(dummyField),
                Collections.singleton(dummyMainMethod),
                NoPositionInformation.getInstance(),
                classModifierEnumSet,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()),
            SourceType.Application);
    assertEquals(mainClass.getMethods().size(), 1);

    return mainClass;
  }

  @Test
  public void testClassModifiersValidator_success() {
    List<ValidationException> validationExceptions_success = new ArrayList<>();

    JavaSootClass javaSootClass =
        testClassCreatorWithModifiers(
            EnumSet.of(ClassModifier.INTERFACE),
            EnumSet.of(FieldModifier.PUBLIC, FieldModifier.STATIC, FieldModifier.FINAL));

    fieldFlagsValidator.validate(javaSootClass, validationExceptions_success);

    assertEquals(0, validationExceptions_success.size());
  }

  @Test
  public void testClassModifiersValidator_fail1() {
    List<ValidationException> validationExceptions_fail1 = new ArrayList<>();

    JavaSootClass javaSootClass =
        testClassCreatorWithModifiers(
            EnumSet.of(ClassModifier.PUBLIC),
            EnumSet.of(FieldModifier.PUBLIC, FieldModifier.PRIVATE));

    fieldFlagsValidator.validate(javaSootClass, validationExceptions_fail1);

    assertEquals(1, validationExceptions_fail1.size());
  }

  @Test
  public void testClassModifiersValidator_fail2() {
    List<ValidationException> validationExceptions_fail1 = new ArrayList<>();

    JavaSootClass javaSootClass =
        testClassCreatorWithModifiers(
            EnumSet.of(ClassModifier.INTERFACE), EnumSet.of(FieldModifier.PRIVATE));

    fieldFlagsValidator.validate(javaSootClass, validationExceptions_fail1);

    assertEquals(3, validationExceptions_fail1.size());
  }
}
