package sootup.core.model;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Linghui Luo, Christian Brüggemann
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

public enum SourceType {
  Phantom, // in soot: code that we have no access to and only can assume the class layout.. TODO:
           // which seems to makes no sense anymore as we now reference indirectly to SootClasses
           // and dont need a "catch all" Phantom SootClass?
  Application, // code that we want to analyze e.g. in call graph generation its traversed
  Library // code that is given but should not be analyzed e.g. in call graph generation its not
          // traversed
}
