/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Raja Vallee-Rai
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package de.upb.swt.soot.core.jimple.common.constant;

import de.upb.swt.soot.core.jimple.visitor.ConstantVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;
import javax.annotation.Nonnull;

/** A 64-bit integer constant */
public class LongConstant implements ShiftableConstant<LongConstant> {

  private final long value;

  private LongConstant(@Nonnull long value) {
    this.value = value;
  }

  public static LongConstant getInstance(@Nonnull long value) {
    return new LongConstant(value);
  }

  @Override
  public boolean equals(Object c) {
    return c instanceof LongConstant && ((LongConstant) c).value == value;
  }

  /** Returns a hash code for this DoubleConstant object. */
  @Override
  public int hashCode() {
    return (int) (value ^ (value >>> 32));
  }

  // PTC 1999/06/28
  @Nonnull
  @Override
  public LongConstant add(@Nonnull LongConstant c) {
    return LongConstant.getInstance(value + c.value);
  }

  @Nonnull
  @Override
  public LongConstant subtract(@Nonnull LongConstant c) {
    return LongConstant.getInstance(value - c.value);
  }

  @Nonnull
  @Override
  public LongConstant multiply(@Nonnull LongConstant c) {
    return LongConstant.getInstance(value * c.value);
  }

  @Nonnull
  @Override
  public LongConstant divide(@Nonnull LongConstant c) {
    return LongConstant.getInstance(value / c.value);
  }

  @Nonnull
  @Override
  public LongConstant remainder(@Nonnull LongConstant c) {
    return LongConstant.getInstance(value % c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant equalEqual(@Nonnull LongConstant c) {
    return BooleanConstant.getInstance(value == c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant notEqual(@Nonnull LongConstant c) {
    return BooleanConstant.getInstance(value != c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant lessThan(@Nonnull LongConstant c) {
    return BooleanConstant.getInstance(value < c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant lessThanOrEqual(@Nonnull LongConstant c) {
    return BooleanConstant.getInstance(value <= c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant greaterThan(@Nonnull LongConstant c) {
    return BooleanConstant.getInstance(value > c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant greaterThanOrEqual(@Nonnull LongConstant c) {
    return BooleanConstant.getInstance(value >= c.value);
  }

  /** Compares the value of LongConstant. */
  public IntConstant cmp(LongConstant c) {
    if (value > c.value) {
      return IntConstant.getInstance(1);
    } else if (value == c.value) {
      return IntConstant.getInstance(0);
    } else {
      return IntConstant.getInstance(-1);
    }
  }

  @Nonnull
  @Override
  public LongConstant negate() {
    return LongConstant.getInstance(-(value));
  }

  @Nonnull
  @Override
  public LongConstant and(@Nonnull LongConstant c) {
    return LongConstant.getInstance(value & c.value);
  }

  @Nonnull
  @Override
  public LongConstant or(@Nonnull LongConstant c) {
    return LongConstant.getInstance(value | c.value);
  }

  @Nonnull
  @Override
  public LongConstant xor(@Nonnull LongConstant c) {
    return LongConstant.getInstance(value ^ c.value);
  }

  @Nonnull
  @Override
  public LongConstant shiftLeft(@Nonnull IntConstant c) {
    return LongConstant.getInstance(value << c.getValue());
  }

  @Nonnull
  @Override
  public LongConstant shiftRight(@Nonnull IntConstant c) {
    return LongConstant.getInstance(value >> c.getValue());
  }

  @Nonnull
  @Override
  public LongConstant unsignedShiftRight(@Nonnull IntConstant c) {
    return LongConstant.getInstance(value >>> c.getValue());
  }

  @Override
  public String toString() {
    return value + "L";
  }

  @Override
  public Type getType() {
    return PrimitiveType.getLong();
  }

  @Override
  public void accept(@Nonnull Visitor sw) {
    ((ConstantVisitor) sw).caseLongConstant(this);
  }

  public long getValue() {
    return value;
  }
}