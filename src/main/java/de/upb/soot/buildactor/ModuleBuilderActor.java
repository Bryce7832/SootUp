package de.upb.soot.buildactor;

import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootModuleInfo;
import de.upb.soot.namespaces.classprovider.ClassSource;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.ModuleSignatureFactory;
import de.upb.soot.views.Project;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModuleBuilderActor extends AbstractLoggingActor {

  private final Project project;
  private final ClassSource classSource;
  private SootModuleInfo module;

  public ModuleBuilderActor(Project project, ClassSource classSource) {
    this.project = project;
    this.classSource = classSource;
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
            .match(ReifyMessage.class, this::reify)
            .match(ResolveMessage.class, this::resolve)
            .build();

  }

  private void reify(ReifyMessage m) {
    log().info("Start reifying for %s.", classSource.getClassSignature().toString());

    module = getSootModule(classSource, new UnresolvedModuleVisitor());

    sender().tell(module, this.getSelf());

    log().info("Completed reifying for %s.", classSource.getClassSignature().toString());
  }

  private void resolve(ResolveMessage m) {
    log().info("Full resolve for %s.", classSource.getClassSignature().toString());
    if (module == null)
      throw new IllegalStateException();

    module = getSootModule(classSource, new ResolveModuleVisitor(module));

    sender().tell(module, this.getSelf());

    log().info("Completed resolve for %s", classSource.getClassSignature().toString());
  }

  private SootModuleInfo getSootModule(ClassSource classSource, ModuleVisitor visitor) {
    SootModuleBuilder scb = new SootModuleBuilder(classSource, visitor);

    try {
      ClassReader clsr = new ClassReader(Files.newInputStream(classSource.getSourcePath()));

      clsr.accept(scb, ClassReader.SKIP_FRAMES);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  public static Props props(ClassSource classSource) {
    return Props.create(ModuleBuilderActor.class, classSource);
  }

  public static class SootModuleBuilder extends ClassVisitor {

    private final ModuleVisitor visitor;
    private SootModuleInfo result;
    private ClassSource source;

    public SootModuleBuilder(ClassSource source, ModuleVisitor visitor) {
      super(Opcodes.ASM6);
      this.source = source;
      this.visitor = visitor;
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
      result = new SootModuleInfo(source, name, access, version);

     return visitor;
    }
  }

  private class UnresolvedModuleVisitor extends ModuleVisitor {
    public UnresolvedModuleVisitor() {
      super(Opcodes.ASM6);
    }
  }

  private class ResolveModuleVisitor extends ModuleVisitor {
    private final SootModuleInfo moduleInfo;

    public ResolveModuleVisitor(SootModuleInfo moduleInfo) {
      super(Opcodes.ASM6);
      this.moduleInfo = moduleInfo;
    }

    @Override
    public void visitRequire(String module, int access, String version) {
      super.visitRequire(module, access, version);

      moduleInfo.addRequire(resolve(module), access, version);
    }

    @Override
    public void visitExport(String packaze, int access, String... modules) {
      super.visitExport(packaze, access, modules);

      moduleInfo.addExport(packaze, access, resolve(modules));
    }

    @Override
    public void visitOpen(String packaze, int access, String... modules) {
      super.visitOpen(packaze, access, modules);

      moduleInfo.addOpen(packaze, access, resolve(modules));
    }

    @Override
    public void visitUse(String service) {
      super.visitUse(service);

      moduleInfo.addUse(resolveServiceClass(service));
    }

    @Override
    public void visitProvide(String service, String... providers) {
      super.visitProvide(service, providers);

      moduleInfo.addProvide(service, resolveServiceClass(providers));
    }

    private SootModuleInfo resolve(String module) {
      ClassSignature moduleSignature = new ModuleSignatureFactory().getClassSignature("module-info", "", module);
      Optional<SootClass> moduleClass = project.getClass(moduleSignature);

      // FIXME Ugly ugly cast... *würg*
      return (SootModuleInfo) moduleClass.get();
    }

    private Iterable<SootModuleInfo> resolve(String[] modules) {
      return Arrays.stream(modules).map(m -> resolve(m)).collect(Collectors.toList());
    }

    private SootClass resolveServiceClass(String service) {
      return project.getClass(new ModuleSignatureFactory().getClassSignature(service)).get();
    }

    private Iterable<SootClass> resolveServiceClass(String[] providers) {
      return Arrays.stream(providers).map(p -> resolveServiceClass(p)).collect(Collectors.toList());
    }
  }

}
