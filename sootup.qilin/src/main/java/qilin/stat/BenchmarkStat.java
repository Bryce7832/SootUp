/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.stat;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import qilin.CoreConfig;
import qilin.core.PTA;
import qilin.core.PTAScene;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;
import sootup.core.views.View;

public class BenchmarkStat implements AbstractStat {
  private final PTA pta;

  private int classes = 0;
  private int appClasses = 0;
  private int phantomClasses = 0;
  private int libClasses = 0;
  private Set<SootClass> reachableClasses;
  private Set<SootClass> reachableAppClasses;
  int libReachableClasses = 0;

  public BenchmarkStat(PTA pta) {
    this.pta = pta;
    init();
  }

  private void init() {
    View view = pta.getView();
    reachableClasses =
        pta.getNakedReachableMethods().stream()
            .map(
                m -> {
                  ClassType classType = m.getDeclaringClassType();
                  return view.getClass(classType);
                })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());
    reachableAppClasses =
        reachableClasses.stream().filter(SootClass::isApplicationClass).collect(Collectors.toSet());
    PTAScene scene = pta.getScene();
    classes = scene.getClasses().size();
    appClasses = scene.getApplicationClasses().size();
    phantomClasses = scene.getPhantomClasses().size();
    libClasses = classes - appClasses - phantomClasses;
    libReachableClasses = (reachableClasses.size() - reachableAppClasses.size() - 1); // -FakeMain
  }

  @Override
  public void export(Exporter exporter) {
    exporter.collectMetric("#Class:", String.valueOf(classes));
    exporter.collectMetric("#Appclass:", String.valueOf(appClasses));
    exporter.collectMetric("#Libclass:", String.valueOf(libClasses));
    exporter.collectMetric("#Phantomclass:", String.valueOf(phantomClasses));
    exporter.collectMetric("#Class(reachable):", String.valueOf(reachableClasses.size()));
    exporter.collectMetric("#Appclass(reachable):", String.valueOf(reachableAppClasses.size()));
    exporter.collectMetric("#Libclass(reachable):", String.valueOf(libReachableClasses));

    if (CoreConfig.v().getOutConfig().dumpStats) {
      exporter.dumpClassTypes(pta.getScene().getClasses());
    }
  }
}
