package de.upb.soot.frontends;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.util.CollectionUtils;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Allows for replacing specific parts of a class, such as fields and methods. By default, it
 * delegates to the {@link ClassSource} delegate provided in the constructor.
 *
 * <p>To alter the results of invocations to e.g. {@link #resolveFields()}, simply call {@link
 * #withFields(Collection)} to obtain a new {@link OverridingClassSource}. The new instance will
 * then use the supplied value instead of calling {@link #resolveFields()} on the delegate.
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalAssignedToNull"})
public class OverridingClassSource extends ClassSource {

  @Nullable private final Collection<SootMethod> overriddenSootMethods;
  @Nullable private final Collection<SootField> overriddenSootFields;
  @Nullable private final Set<Modifier> overriddenModifiers;
  @Nullable private final Set<JavaClassType> overriddenInterfaces;
  @Nullable private final Optional<JavaClassType> overriddenSuperclass;
  @Nullable private final Optional<JavaClassType> overriddenOuterClass;

  // Since resolvePosition may return null, we cannot use `null` here to indicate that `position`
  // is not overridden.
  private final boolean overriddenPosition;
  @Nullable private final Position position;

  @Nonnull private final ClassSource delegate;

  public OverridingClassSource(@Nonnull ClassSource delegate) {
    super(delegate.srcNamespace, delegate.sourcePath, delegate.classSignature);
    this.delegate = delegate;
    overriddenSootMethods = null;
    overriddenSootFields = null;
    overriddenModifiers = null;
    overriddenInterfaces = null;
    overriddenSuperclass = null;
    overriddenOuterClass = null;
    overriddenPosition = false;
    position = null;
  }

  private OverridingClassSource(
      @Nullable Collection<SootMethod> overriddenSootMethods,
      @Nullable Collection<SootField> overriddenSootFields,
      @Nullable Set<Modifier> overriddenModifiers,
      @Nullable Set<JavaClassType> overriddenInterfaces,
      @Nullable Optional<JavaClassType> overriddenSuperclass,
      @Nullable Optional<JavaClassType> overriddenOuterClass,
      boolean overriddenPosition,
      @Nullable Position position,
      @Nonnull ClassSource delegate) {
    super(delegate.srcNamespace, delegate.sourcePath, delegate.classSignature);
    this.overriddenSootMethods = overriddenSootMethods;
    this.overriddenSootFields = overriddenSootFields;
    this.overriddenModifiers = overriddenModifiers;
    this.overriddenInterfaces = overriddenInterfaces;
    this.overriddenSuperclass = overriddenSuperclass;
    this.overriddenOuterClass = overriddenOuterClass;
    this.overriddenPosition = overriddenPosition;
    this.position = position;
    this.delegate = delegate;
  }

  @Nonnull
  @Override
  public Collection<SootMethod> resolveMethods() throws ResolveException {
    return overriddenSootMethods != null ? overriddenSootMethods : delegate.resolveMethods();
  }

  @Nonnull
  @Override
  public Collection<SootField> resolveFields() throws ResolveException {
    return overriddenSootFields != null ? overriddenSootFields : delegate.resolveFields();
  }

  @Nonnull
  @Override
  public Set<Modifier> resolveModifiers() {
    return overriddenModifiers != null ? overriddenModifiers : delegate.resolveModifiers();
  }

  @Nonnull
  @Override
  public Set<JavaClassType> resolveInterfaces() {
    return overriddenInterfaces != null ? overriddenInterfaces : delegate.resolveInterfaces();
  }

  @Nonnull
  @Override
  public Optional<JavaClassType> resolveSuperclass() {
    return overriddenSuperclass != null ? overriddenSuperclass : delegate.resolveSuperclass();
  }

  @Nonnull
  @Override
  public Optional<JavaClassType> resolveOuterClass() {
    return overriddenOuterClass != null ? overriddenOuterClass : delegate.resolveOuterClass();
  }

  @Override
  public Position resolvePosition() {
    return overriddenPosition ? position : delegate.resolvePosition();
  }

  @Nonnull
  public OverridingClassSource withReplacedMethod(SootMethod toReplace, SootMethod replacement) {
    Set<SootMethod> newMethods = new HashSet<>(resolveMethods());
    CollectionUtils.replace(newMethods, toReplace, replacement);
    return withMethods(newMethods);
  }

  @Nonnull
  public OverridingClassSource withMethods(Collection<SootMethod> overriddenSootMethods) {
    return new OverridingClassSource(
        overriddenSootMethods,
        overriddenSootFields,
        overriddenModifiers,
        overriddenInterfaces,
        overriddenSuperclass,
        overriddenOuterClass,
        overriddenPosition,
        position,
        delegate);
  }

  @Nonnull
  public OverridingClassSource withReplacedField(SootField toReplace, SootField replacement) {
    Set<SootField> newFields = new HashSet<>(resolveFields());
    CollectionUtils.replace(newFields, toReplace, replacement);
    return withFields(newFields);
  }

  @Nonnull
  public OverridingClassSource withFields(Collection<SootField> overriddenSootFields) {
    return new OverridingClassSource(
        overriddenSootMethods,
        overriddenSootFields,
        overriddenModifiers,
        overriddenInterfaces,
        overriddenSuperclass,
        overriddenOuterClass,
        overriddenPosition,
        position,
        delegate);
  }

  @Nonnull
  public OverridingClassSource withModifiers(Set<Modifier> overriddenModifiers) {
    return new OverridingClassSource(
        overriddenSootMethods,
        overriddenSootFields,
        overriddenModifiers,
        overriddenInterfaces,
        overriddenSuperclass,
        overriddenOuterClass,
        overriddenPosition,
        position,
        delegate);
  }

  @Nonnull
  public OverridingClassSource withInterfaces(Set<JavaClassType> overriddenInterfaces) {
    return new OverridingClassSource(
        overriddenSootMethods,
        overriddenSootFields,
        overriddenModifiers,
        overriddenInterfaces,
        overriddenSuperclass,
        overriddenOuterClass,
        overriddenPosition,
        position,
        delegate);
  }

  @Nonnull
  public OverridingClassSource withSuperclass(Optional<JavaClassType> overriddenSuperclass) {
    return new OverridingClassSource(
        overriddenSootMethods,
        overriddenSootFields,
        overriddenModifiers,
        overriddenInterfaces,
        overriddenSuperclass,
        overriddenOuterClass,
        overriddenPosition,
        position,
        delegate);
  }

  @Nonnull
  public OverridingClassSource withOuterClass(Optional<JavaClassType> overriddenOuterClass) {
    return new OverridingClassSource(
        overriddenSootMethods,
        overriddenSootFields,
        overriddenModifiers,
        overriddenInterfaces,
        overriddenSuperclass,
        overriddenOuterClass,
        overriddenPosition,
        position,
        delegate);
  }

  @Nonnull
  public OverridingClassSource withPosition(@Nullable Position position) {
    return new OverridingClassSource(
        overriddenSootMethods,
        overriddenSootFields,
        overriddenModifiers,
        overriddenInterfaces,
        overriddenSuperclass,
        overriddenOuterClass,
        true,
        position,
        delegate);
  }
}