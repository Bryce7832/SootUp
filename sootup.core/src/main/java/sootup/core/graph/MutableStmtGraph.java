package sootup.core.graph;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Markus Schmidt
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
import java.util.*;
import javax.annotation.Nonnull;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ClassType;

/**
 * @author Markus Schmidt
 *     <p>performance suggestions for multiple operations on sequential Stmts: addNode(): top->down
 *     removeNode(s): bottom->up as then there is no need for copying inside the MutableBasicBlock
 */
public abstract class MutableStmtGraph extends StmtGraph<MutableBasicBlock> {
  @Nonnull
  public abstract StmtGraph<?> unmodifiableStmtGraph();

  public abstract void setStartingStmt(@Nonnull Stmt firstStmt);

  public void addNode(@Nonnull Stmt node) {
    addNode(node, Collections.emptyMap());
  }

  public abstract void addNode(@Nonnull Stmt node, @Nonnull Map<ClassType, Stmt> traps);

  // maybe refactor addBlock into MutableBlockStmtGraph..
  public abstract void addBlock(@Nonnull List<Stmt> stmts, @Nonnull Map<ClassType, Stmt> traps);

  public void addBlock(@Nonnull List<Stmt> stmts) {
    addBlock(stmts, Collections.emptyMap());
  }

  /**
   * Modification of nodes (without manipulating any flows; possible assigned exceptional flows stay
   * the same as well)
   */
  public abstract void replaceNode(@Nonnull Stmt oldStmt, @Nonnull Stmt newStmt);

  public abstract void insertBefore(
      @Nonnull Stmt beforeStmt,
      @Nonnull List<Stmt> stmts,
      @Nonnull Map<ClassType, Stmt> exceptionMap);

  public void insertBefore(@Nonnull Stmt beforeStmt, @Nonnull Stmt stmt) {
    insertBefore(beforeStmt, Collections.singletonList(stmt), Collections.emptyMap());
  }

  public abstract void removeNode(@Nonnull Stmt node);

  /** Modifications of unexceptional flows */
  public abstract void putEdge(@Nonnull Stmt from, @Nonnull Stmt to);

  public abstract void setEdges(@Nonnull Stmt from, @Nonnull List<Stmt> targets);

  public void setEdges(@Nonnull Stmt from, @Nonnull Stmt... targets) {
    setEdges(from, Arrays.asList(targets));
  }

  public abstract void removeEdge(@Nonnull Stmt from, @Nonnull Stmt to);

  /** Modifications of exceptional flows */
  public abstract void clearExceptionalEdges(@Nonnull Stmt node);

  public abstract void addExceptionalEdge(
      @Nonnull Stmt stmt, @Nonnull ClassType exception, @Nonnull Stmt traphandlerStmt);

  public abstract void removeExceptionalEdge(@Nonnull Stmt node, @Nonnull ClassType exception);
}
