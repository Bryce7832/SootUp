package de.upb.swt.soot.java.bytecode.frontend;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.jimple.basic.NoPositionInformation;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.core.*;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/** A ClassSource that reads from Java bytecode */
class AsmClassSource extends JavaSootClassSource {

  @Nonnull private final ClassNode classNode;

  public AsmClassSource(
      AnalysisInputLocation inputLocation,
      Path sourcePath,
      JavaClassType javaClassType,
      @Nonnull ClassNode classNode) {
    super(inputLocation, javaClassType, sourcePath);
    this.classNode = classNode;
  }

  private static Set<JavaSootField> resolveFields(
      List<FieldNode> fieldNodes, IdentifierFactory signatureFactory, ClassType classSignature) {
    return fieldNodes.stream()
        .map(
            fieldNode -> {
              String fieldName = fieldNode.name;
              Type fieldType = AsmUtil.toJimpleType(fieldNode.desc);
              FieldSignature fieldSignature =
                  signatureFactory.getFieldSignature(fieldName, classSignature, fieldType);
              EnumSet<Modifier> modifiers = AsmUtil.getModifiers(fieldNode.access);

              // FIXME: implement support for annotation
              return new JavaSootField(fieldSignature, modifiers, Collections.emptyList());
            })
        .collect(Collectors.toSet());
  }

  private static Stream<JavaSootMethod> resolveMethods(
      List<MethodNode> methodNodes, IdentifierFactory signatureFactory, ClassType cs) {
    return methodNodes.stream()
        .map(
            methodSource -> {
              if (!(methodSource instanceof AsmMethodSource)) {
                throw new AsmFrontendException(
                    String.format("Failed to create Method Signature %s", methodSource));
              }

              AsmMethodSource asmClassClassSourceContent = (AsmMethodSource) methodSource;
              asmClassClassSourceContent.setDeclaringClass(cs);

              List<ClassType> exceptions = new ArrayList<>();
              exceptions.addAll(AsmUtil.asmIdToSignature(methodSource.exceptions));

              String methodName = methodSource.name;
              EnumSet<Modifier> modifiers = AsmUtil.getModifiers(methodSource.access);
              List<Type> sigTypes = AsmUtil.toJimpleSignatureDesc(methodSource.desc);
              Type retType = sigTypes.remove(sigTypes.size() - 1);

              MethodSignature methodSignature =
                  signatureFactory.getMethodSignature(methodName, cs, retType, sigTypes);

              // FIXME: implement support for annotation
              return new JavaSootMethod(
                  asmClassClassSourceContent,
                  methodSignature,
                  modifiers,
                  exceptions,
                  Collections.emptyList());
            });
  }

  @Nonnull
  public Collection<SootMethod> resolveMethods() throws ResolveException {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    return resolveMethods(classNode.methods, identifierFactory, classSignature)
        .collect(Collectors.toSet());
  }

  @Override
  @Nonnull
  public Collection<? extends SootField> resolveFields() throws ResolveException {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    return resolveFields(classNode.fields, identifierFactory, classSignature);
  }

  @Nonnull
  public Set<Modifier> resolveModifiers() {
    EnumSet<Modifier> modifiers = AsmUtil.getModifiers(classNode.access);
    return modifiers;
  }

  @Nonnull
  public Set<ClassType> resolveInterfaces() {
    return new HashSet<>(AsmUtil.asmIdToSignature(classNode.interfaces));
  }

  @Nonnull
  public Optional<ClassType> resolveSuperclass() {
    if (classNode.superName == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(AsmUtil.asmIDToSignature(classNode.superName));
  }

  @Nonnull
  public Optional<ClassType> resolveOuterClass() {
    return Optional.ofNullable(AsmUtil.asmIDToSignature(classNode.outerClass));
  }

  @Nonnull
  public Position resolvePosition() {
    return NoPositionInformation.getInstance();
  }

  @Override
  public String toString() {
    return getSourcePath().toString();
  }

  @Nonnull
  @Override
  public Iterable<AnnotationType> resolveAnnotations() {
    // TODO [ms] implement
    return null;
  }

  @Nonnull
  @Override
  public Iterable<AnnotationType> resolveMethodAnnotations() {
    // TODO [ms] implement
    return null;
  }

  @Nonnull
  @Override
  public Iterable<AnnotationType> resolveFieldAnnotations() {
    // TODO [ms] implement
    return null;
  }
}