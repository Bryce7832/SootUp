package de.upb.soot.typehierarchy;

import de.upb.soot.types.ArrayType;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.NullType;
import de.upb.soot.types.PrimitiveType;
import de.upb.soot.types.ReferenceType;
import de.upb.soot.types.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a type hierarchy
 *
 * @author Linghui Luo
 * @author Ben Hermann
 * @author Christian Brüggemann
 */
public interface TypeHierarchy {

  /**
   * Returns all classes that implement the specified interface. This is transitive: If class <code>
   * A extends B</code> and <code>B implements interface</code>, then this method will return both A
   * and B as implementers of <code>interfaceType</code>.
   *
   * <p>This includes interfaces extending <code>interfaceType</code> as they may contain default
   * implementations of methods.
   */
  @Nonnull
  Set<JavaClassType> implementersOf(@Nonnull JavaClassType interfaceType);

  /**
   * Returns all classes that extend the specified class. This is transitive: If <code>A extends B
   * </code> and <code>B extends classType</code>, then this method will return both A and B as
   * extenders of <code>classType</code>.
   */
  @Nonnull
  Set<JavaClassType> subclassesOf(@Nonnull JavaClassType classType);

  /**
   * Returns the interfaces implemented by <code>type</code> if it is a class or extended by <code>
   * type</code> if it is an interface. This includes interfaces implemented by superclasses and
   * also covers the case where <code>classType</code> directly or indirectly implements an
   * interface <code>I1</code> that extends another interface <code>I2
   * </code>. <code>I2</code> will be considered an implemented interface of <code>classType</code>.
   */
  @Nonnull
  Set<JavaClassType> implementedInterfacesOf(@Nonnull JavaClassType type);

  /**
   * For an interface type, this does the same as {@link #implementersOf(JavaClassType)}. For a
   * class type, this does the same as {@link #subclassesOf(JavaClassType)}.
   */
  @Nonnull
  Set<JavaClassType> subtypesOf(@Nonnull JavaClassType type);

  /**
   * Returns the direct superclass of <code>classType</code>. If <code>classType == java.lang.Object
   * </code>, this method returns null.
   */
  @Nullable
  JavaClassType superClassOf(@Nonnull JavaClassType classType);

  /**
   * Returns true if <code>potentialSubtype</code> is a subtype of <code>supertype</code>. If they
   * are identical, this will return false.
   *
   * <p>This method relies on {@link #implementedInterfacesOf(JavaClassType)} and {@link
   * #superClassOf(JavaClassType)}.
   */
  default boolean isSubtype(@Nonnull Type supertype, @Nonnull Type potentialSubtype) {
    if (!(supertype instanceof ReferenceType) || !(potentialSubtype instanceof ReferenceType)) {
      // Subtyping applies to ReferenceTypes only
      return false;
    }

    if (supertype instanceof NullType) {
      // NullType has no subtypes
      return false;
    }

    if (potentialSubtype instanceof NullType) {
      // Null can be assigned to any type
      return true;
    }

    if (supertype instanceof ArrayType) {
      if (!(potentialSubtype instanceof ArrayType)) {
        return false;
      }

      ArrayType superArrayType = (ArrayType) supertype;
      ArrayType potentialSubArrayType = (ArrayType) potentialSubtype;
      if (superArrayType.getBaseType() instanceof PrimitiveType) {
        // Arrays of primitives have no subtypes
        return false;
      }

      assert superArrayType.getBaseType() instanceof ReferenceType;

      if (superArrayType.getBaseType().equals(potentialSubArrayType.getBaseType())) {
        // Object[] x = new Object[0][0];
        return potentialSubArrayType.getDimension() > superArrayType.getDimension();
      } else if (isSubtype(superArrayType.getBaseType(), potentialSubArrayType.getBaseType())
          && potentialSubArrayType.getDimension() == superArrayType.getDimension()) {
        // Arrays are covariant: Object[] x = new String[0];
        return true;
      } else if (superArrayType.getBaseType() instanceof JavaClassType
          && (((JavaClassType) superArrayType.getBaseType())
                  .getFullyQualifiedName()
                  .equals("java.lang.Object")
              || ((JavaClassType) superArrayType.getBaseType())
                  .getFullyQualifiedName()
                  .equals("java.io.Serializable")
              || ((JavaClassType) superArrayType.getBaseType())
                  .getFullyQualifiedName()
                  .equals("java.lang.Cloneable"))) {
        // Special case: Object[] x = new double[0][0], Object[][] y = new double[0][0][0], ...
        return potentialSubArrayType.getDimension() > superArrayType.getDimension();
      } else if (superArrayType.getDimension() > 1
          && potentialSubArrayType.getDimension() == superArrayType.getDimension()) {
        ArrayType superArrayTypeLessDim =
            new ArrayType(superArrayType.getBaseType(), superArrayType.getDimension() - 1);
        ArrayType potentialSubArrayTypeLessDim =
            new ArrayType(
                potentialSubArrayType.getBaseType(), potentialSubArrayType.getDimension() - 1);
        return isSubtype(superArrayTypeLessDim, potentialSubArrayTypeLessDim);
      } else {
        return false;
      }
    } else if (supertype instanceof JavaClassType) {
      if (potentialSubtype instanceof JavaClassType) {
        // First condition is a fast path
        return supertype.equals(superClassOf((JavaClassType) potentialSubtype))
            || superClassesOf((JavaClassType) potentialSubtype).contains(supertype)
            || implementedInterfacesOf((JavaClassType) potentialSubtype).contains(supertype);
      } else if (potentialSubtype instanceof ArrayType) {
        // Arrays are subtypes of java.lang.Object, java.io.Serializable and java.lang.Cloneable
        String fullyQualifiedName = ((JavaClassType) supertype).getFullyQualifiedName();
        return fullyQualifiedName.equals("java.lang.Object")
            || fullyQualifiedName.equals("java.io.Serializable")
            || fullyQualifiedName.equals("java.lang.Cloneable");
      } else {
        throw new AssertionError("potentialSubtype has unexpected type");
      }
    } else {
      throw new AssertionError("supertype has unexpected type");
    }
  }

  /**
   * Returns all superclasses of <code>classType</code> up to <code>java.lang.Object</code>, which
   * will be the last entry in the list.
   */
  @Nonnull
  default List<JavaClassType> superClassesOf(@Nonnull JavaClassType classType) {
    List<JavaClassType> superClasses = new ArrayList<>();
    JavaClassType currentSuperClass = superClassOf(classType);
    while (currentSuperClass != null) {
      superClasses.add(currentSuperClass);
      currentSuperClass = superClassOf(currentSuperClass);
    }
    return superClasses;
  }
}