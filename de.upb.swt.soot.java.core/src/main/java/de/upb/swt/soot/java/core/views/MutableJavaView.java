package de.upb.swt.soot.java.core.views;

import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.ViewChangeListener;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSubSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.views.MutableView;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.JavaSootMethod;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class MutableJavaView extends JavaView implements MutableView {
  private final List<ViewChangeListener> changeListeners = new LinkedList<>();

  public MutableJavaView(@Nonnull Project<JavaSootClass, ? extends JavaView> project) {
    super(project);
  }

  public void addClass(JavaSootClass clazz) {
    ClassType classType = clazz.getClassSource().getClassType();
    if (this.cache.containsKey(classType)) {
      System.out.println("Class " + classType + " already exists in view.");
      return;
    }
    this.cache.putIfAbsent(classType, clazz);
    this.fireAddition(clazz);
  }

  public void removeClass(ClassType classType) {
    JavaSootClass removedClass = this.cache.remove(classType);
    this.fireRemoval(removedClass);
  }

  public void removeClass(JavaSootClass clazz) {
    ClassType classType = clazz.getClassSource().getClassType();
    this.removeClass(classType);
  }

  public void replaceClass(JavaSootClass oldClass, JavaSootClass newClass) {
    this.removeClass(oldClass);
    this.addClass(newClass);
  }

  public void removeMethod(JavaSootMethod method) {
    ClassType classType = method.getDeclaringClassType();
    MethodSubSignature mss = method.getSignature().getSubSignature();

    JavaSootClass clazz = this.cache.get(classType);
    if (clazz == null) return;

    Set<? extends JavaSootMethod> methods = clazz.getMethods();
    Set<SootMethod> filteredMethods =
        methods.stream()
            .filter(met -> !met.getSignature().getSubSignature().equals(mss))
            .collect(Collectors.toSet());
    JavaSootClass newClazz = clazz.withMethods(filteredMethods);

    this.replaceClass(clazz, newClazz);
    this.fireRemoval(method);
  }

  public void addMethod(JavaSootMethod method) {
    ClassType classType = method.getDeclaringClassType();

    JavaSootClass clazz = this.cache.get(classType);
    if (clazz == null) return;

    Set<? extends JavaSootMethod> methods = clazz.getMethods();
    Set<SootMethod> newMethods = new HashSet<>(methods);
    newMethods.add(method);
    JavaSootClass newClazz = clazz.withMethods(newMethods);

    this.replaceClass(clazz, newClazz);
    this.fireAddition(method);
  }

  public void replaceMethod(JavaSootMethod oldMethod, JavaSootMethod newMethod) {
    this.removeMethod(oldMethod);
    this.addMethod(newMethod);
  }

  @Override
  public void addChangeListener(ViewChangeListener listener) {
    changeListeners.add(listener);
  }

  @Override
  public void removeChangeListener(ViewChangeListener listener) {
    changeListeners.remove(listener);
  }

  private void fireAddition(JavaSootClass clazz) {
    for (ViewChangeListener viewChangeListener : changeListeners) {
      viewChangeListener.classAdded(clazz);
    }
  }

  private void fireAddition(JavaSootMethod method) {
    for (ViewChangeListener viewChangeListener : changeListeners) {
      viewChangeListener.methodAdded(method);
    }
  }

  private void fireRemoval(JavaSootClass clazz) {
    for (ViewChangeListener viewChangeListener : changeListeners) {
      viewChangeListener.classRemoved(clazz);
    }
  }

  private void fireRemoval(JavaSootMethod method) {
    for (ViewChangeListener viewChangeListener : changeListeners) {
      viewChangeListener.methodRemoved(method);
    }
  }
}
