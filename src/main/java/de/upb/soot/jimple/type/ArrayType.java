package de.upb.soot.jimple.type;

import de.upb.soot.jimple.Switch;

public class ArrayType extends Type {

  public int numDimensions;
  public Type baseType;

  @Override
  public void apply(Switch sw) {
    // TODO Auto-generated method stub

  }

  public Type getElementType() {
    // TODO Auto-generated method stub
    return null;
  }

  public static ArrayType v(Type baseType, int numDimensions) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String toString() {
    // TODO Auto-generated method stub
    return null;
  }

}
