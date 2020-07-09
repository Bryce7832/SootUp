package de.upb.swt.soot.java.sourcecode.frontend;

import com.ibm.wala.cast.ir.ssa.AssignInstruction;
import com.ibm.wala.cast.ir.ssa.AstAssertInstruction;
import com.ibm.wala.cast.ir.ssa.AstLexicalAccess.Access;
import com.ibm.wala.cast.ir.ssa.AstLexicalRead;
import com.ibm.wala.cast.ir.ssa.AstLexicalWrite;
import com.ibm.wala.cast.ir.ssa.CAstBinaryOp;
import com.ibm.wala.cast.java.ssa.AstJavaInvokeInstruction;
import com.ibm.wala.cast.java.ssa.EnclosingObjectReference;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cast.loader.AstMethod.DebuggingInformation;
import com.ibm.wala.cast.tree.CAstSourcePositionMap;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.shrikeBT.IBinaryOpInstruction;
import com.ibm.wala.shrikeBT.IConditionalBranchInstruction.IOperator;
import com.ibm.wala.shrikeBT.IConditionalBranchInstruction.Operator;
import com.ibm.wala.shrikeBT.IShiftInstruction;
import com.ibm.wala.ssa.SSAArrayLengthInstruction;
import com.ibm.wala.ssa.SSAArrayLoadInstruction;
import com.ibm.wala.ssa.SSAArrayReferenceInstruction;
import com.ibm.wala.ssa.SSAArrayStoreInstruction;
import com.ibm.wala.ssa.SSABinaryOpInstruction;
import com.ibm.wala.ssa.SSACheckCastInstruction;
import com.ibm.wala.ssa.SSAComparisonInstruction;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAConversionInstruction;
import com.ibm.wala.ssa.SSAFieldAccessInstruction;
import com.ibm.wala.ssa.SSAGetCaughtExceptionInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAGotoInstruction;
import com.ibm.wala.ssa.SSAInstanceofInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSALoadMetadataInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSASwitchInstruction;
import com.ibm.wala.ssa.SSAThrowInstruction;
import com.ibm.wala.ssa.SSAUnaryOpInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.constant.BooleanConstant;
import de.upb.swt.soot.core.jimple.common.constant.ClassConstant;
import de.upb.swt.soot.core.jimple.common.constant.Constant;
import de.upb.swt.soot.core.jimple.common.constant.DoubleConstant;
import de.upb.swt.soot.core.jimple.common.constant.FloatConstant;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.constant.LongConstant;
import de.upb.swt.soot.core.jimple.common.constant.NullConstant;
import de.upb.swt.soot.core.jimple.common.expr.*;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.ref.JCaughtExceptionRef;
import de.upb.swt.soot.core.jimple.common.ref.JInstanceFieldRef;
import de.upb.swt.soot.core.jimple.common.ref.JStaticFieldRef;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JGotoStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JIfStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JInvokeStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JThrowStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JSwitchStmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.NullType;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.ReferenceType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.UnknownType;
import de.upb.swt.soot.core.types.VoidType;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.*;

/**
 * This class converts wala instruction to jimple statement.
 *
 * @author Linghui Luo
 */
public class InstructionConverter {

  private final WalaIRToJimpleConverter converter;
  private final MethodSignature methodSignature;
  private final AstMethod walaMethod;
  private final SymbolTable symbolTable;
  private final LocalGenerator localGenerator;

  // TODO: [ms] merge into a single insertion sorted (linkedhash)map
  private final Map<JIfStmt, Integer> targetsOfIfStmts;
  private final Map<JGotoStmt, Integer> targetsOfGotoStmts;
  private final Map<JSwitchStmt, List<Integer>> targetsOfLookUpSwitchStmts;

  private final Map<Integer, Local> locals;
  private final IdentifierFactory identifierFactory;

  InstructionConverter(
      WalaIRToJimpleConverter converter,
      MethodSignature methodSignature,
      AstMethod walaMethod,
      LocalGenerator localGenerator) {
    this.converter = converter;
    this.methodSignature = methodSignature;
    this.walaMethod = walaMethod;
    this.symbolTable = walaMethod.symbolTable();
    this.localGenerator = localGenerator;
    this.targetsOfIfStmts = new LinkedHashMap<>();
    this.targetsOfGotoStmts = new LinkedHashMap<>();
    this.targetsOfLookUpSwitchStmts = new LinkedHashMap<>();
    this.locals = new HashMap<>();
    this.identifierFactory = converter.identifierFactory;
  }

  public List<Stmt> convertInstruction(
      DebuggingInformation debugInfo, SSAInstruction inst, HashMap<Stmt, Integer> stmt2iIndex) {
    List<Stmt> stmts = new ArrayList();
    if ((inst instanceof SSAConditionalBranchInstruction)) {
      stmts.addAll(convertBranchInstruction(debugInfo, (SSAConditionalBranchInstruction) inst));
    } else if ((inst instanceof SSAGotoInstruction)) {
      stmts.add(convertGoToInstruction(debugInfo, (SSAGotoInstruction) inst));
    } else if ((inst instanceof SSAReturnInstruction)) {
      stmts.add(convertReturnInstruction(debugInfo, (SSAReturnInstruction) inst));
    } else if ((inst instanceof AstJavaInvokeInstruction)) {
      stmts.add(convertInvokeInstruction(debugInfo, (AstJavaInvokeInstruction) inst));
    } else if ((inst instanceof SSAFieldAccessInstruction)) {
      if ((inst instanceof SSAGetInstruction)) {
        stmts.add(convertGetInstruction(debugInfo, (SSAGetInstruction) inst));
      } else if ((inst instanceof SSAPutInstruction)) {
        stmts.add(convertPutInstruction(debugInfo, (SSAPutInstruction) inst));
      } else {
        throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
      }
    } else if ((inst instanceof SSANewInstruction)) {
      stmts.add(convertNewInstruction(debugInfo, (SSANewInstruction) inst));
    } else if ((inst instanceof SSAConversionInstruction)) {
      stmts.add(convertConversionInstruction(debugInfo, (SSAConversionInstruction) inst));
    } else if ((inst instanceof SSAInstanceofInstruction)) {
      stmts.add(convertInstanceofInstruction(debugInfo, (SSAInstanceofInstruction) inst));
    } else if (inst instanceof SSABinaryOpInstruction) {
      stmts.addAll(convertBinaryOpInstruction(debugInfo, (SSABinaryOpInstruction) inst));
    } else if (inst instanceof SSAUnaryOpInstruction) {
      stmts.add(convertUnaryOpInstruction(debugInfo, (SSAUnaryOpInstruction) inst));
    } else if (inst instanceof SSAThrowInstruction) {
      stmts.add(convertThrowInstruction(debugInfo, (SSAThrowInstruction) inst));
    } else if (inst instanceof SSASwitchInstruction) {
      stmts.add(convertSwitchInstruction(debugInfo, (SSASwitchInstruction) inst));
    } else if (inst instanceof SSALoadMetadataInstruction) {
      stmts.add(convertLoadMetadataInstruction(debugInfo, (SSALoadMetadataInstruction) inst));
    } else if (inst instanceof EnclosingObjectReference) {
      stmts.add(convertEnclosingObjectReference(debugInfo, (EnclosingObjectReference) inst));
    } else if (inst instanceof AstLexicalRead) {
      stmts = (convertAstLexicalRead(debugInfo, (AstLexicalRead) inst));
    } else if (inst instanceof AstLexicalWrite) {
      stmts = (convertAstLexicalWrite(debugInfo, (AstLexicalWrite) inst));
    } else if (inst instanceof AstAssertInstruction) {
      stmts = convertAssertInstruction(debugInfo, (AstAssertInstruction) inst, stmt2iIndex);
    } else if (inst instanceof SSACheckCastInstruction) {
      stmts.add(convertCheckCastInstruction(debugInfo, (SSACheckCastInstruction) inst));
    } else if (inst instanceof SSAMonitorInstruction) {
      stmts.add(
          convertMonitorInstruction(
              debugInfo, (SSAMonitorInstruction) inst)); // for synchronized statement
    } else if (inst instanceof SSAGetCaughtExceptionInstruction) {
      stmts.add(
          convertGetCaughtExceptionInstruction(debugInfo, (SSAGetCaughtExceptionInstruction) inst));
    } else if (inst instanceof SSAArrayLengthInstruction) {
      stmts.add(convertArrayLengthInstruction(debugInfo, (SSAArrayLengthInstruction) inst));
    } else if (inst instanceof SSAArrayReferenceInstruction) {
      if (inst instanceof SSAArrayLoadInstruction) {
        stmts.add(convertArrayLoadInstruction(debugInfo, (SSAArrayLoadInstruction) inst));
      } else if (inst instanceof SSAArrayStoreInstruction) {
        stmts.add(convertArrayStoreInstruction(debugInfo, (SSAArrayStoreInstruction) inst));
      } else {
        throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
      }
    } else {
      throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
    }
    return stmts;
  }

  private Stmt convertArrayStoreInstruction(
      DebuggingInformation debugInfo, SSAArrayStoreInstruction inst) {
    Local base = getLocal(UnknownType.getInstance(), inst.getArrayRef());
    int i = inst.getIndex();
    Immediate index = null;
    if (symbolTable.isConstant(i)) {
      index = getConstant(i);
    } else {
      index = getLocal(PrimitiveType.getInt(), i);
    }
    JArrayRef arrayRef = JavaJimple.getInstance().newArrayRef(base, index);
    Value rvalue = null;
    int value = inst.getValue();
    if (symbolTable.isConstant(value)) {
      rvalue = getConstant(value);
    } else {
      rvalue = getLocal(base.getType(), value);
    }

    Position[] operandPos = new Position[1];
    // TODO: written arrayindex position info is missing
    // operandPos[0] = debugInfo.getOperandPosition(inst.iindex, 0);

    return Jimple.newAssignStmt(
        arrayRef,
        rvalue,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertArrayLoadInstruction(
      DebuggingInformation debugInfo, SSAArrayLoadInstruction inst) {
    Local base = getLocal(UnknownType.getInstance(), inst.getArrayRef());
    int i = inst.getIndex();
    Immediate index;
    if (symbolTable.isConstant(i)) {
      index = getConstant(i);
    } else {
      index = getLocal(PrimitiveType.getInt(), i);
    }
    JArrayRef arrayRef = JavaJimple.getInstance().newArrayRef(base, index);
    Value left = null;
    int def = inst.getDef();
    left = getLocal(base.getType(), def);

    Position[] operandPos = new Position[1];
    // TODO: loaded arrayindex position info is missing
    // operandPos[0] = debugInfo.getOperandPosition(inst.iindex, 0);

    return Jimple.newAssignStmt(
        left,
        arrayRef,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertArrayLengthInstruction(
      DebuggingInformation debugInfo, SSAArrayLengthInstruction inst) {
    int result = inst.getDef();
    Local left = getLocal(PrimitiveType.getInt(), result);
    int arrayRef = inst.getArrayRef();
    Local arrayLocal = getLocal(UnknownType.getInstance(), arrayRef);
    Value right = Jimple.newLengthExpr(arrayLocal);

    Position[] operandPos = new Position[1];
    Position p1 = debugInfo.getOperandPosition(inst.iIndex(), 0);
    operandPos[0] = p1;
    // TODO: [ms] stmt position ends at variablename of the array
    return Jimple.newAssignStmt(
        left,
        right,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertGetCaughtExceptionInstruction(
      DebuggingInformation debugInfo, SSAGetCaughtExceptionInstruction inst) {
    int exceptionValue = inst.getException();
    Local local =
        getLocal(
            JavaIdentifierFactory.getInstance().getClassType("java.lang.Throwable"),
            exceptionValue);
    JCaughtExceptionRef caught = JavaJimple.getInstance().newCaughtExceptionRef();

    Position[] operandPos = new Position[1];
    // TODO: [ms] position info of parameter, target is missing
    // operandPos[0] = debugInfo.getOperandPosition(inst.iindex, 0);

    return Jimple.newIdentityStmt(
        local,
        caught,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertMonitorInstruction(
      DebuggingInformation debugInfo, SSAMonitorInstruction inst) {
    Immediate op = getLocal(UnknownType.getInstance(), inst.getRef());

    Position[] operandPos = new Position[1];
    // TODO: [ms] referenced object position info is missing
    // operandPos[0] = debugInfo.getOperandPosition(inst.iindex, 0);

    if (inst.isMonitorEnter()) {
      return Jimple.newEnterMonitorStmt(
          op,
          WalaIRToJimpleConverter.convertPositionInfo(
              debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    } else {
      return Jimple.newExitMonitorStmt(
          op,
          WalaIRToJimpleConverter.convertPositionInfo(
              debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    }
  }

  private List<Stmt> convertAssertInstruction(
      DebuggingInformation debugInfo,
      AstAssertInstruction inst,
      HashMap<Stmt, Integer> stmt2iIndex) {
    List<Stmt> stmts = new ArrayList<>();
    // create a static field for checking if assertion is disabled.
    JavaClassType cSig = (JavaClassType) methodSignature.getDeclClassType();
    FieldSignature fieldSig =
        identifierFactory.getFieldSignature("$assertionsDisabled", cSig, "boolean");
    SootField assertionsDisabled =
        new SootField(fieldSig, EnumSet.of(Modifier.FINAL, Modifier.STATIC));

    converter.addSootField(assertionsDisabled);
    Local testLocal = localGenerator.generateLocal(PrimitiveType.getBoolean());
    JStaticFieldRef assertFieldRef = Jimple.newStaticFieldRef(fieldSig);
    Position[] operandPos = new Position[1];
    operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
    JAssignStmt assignStmt =
        Jimple.newAssignStmt(
            testLocal,
            assertFieldRef,
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    stmts.add(assignStmt);

    // add ifStmt for testing assertion is disabled.
    JEqExpr condition = Jimple.newEqExpr(testLocal, IntConstant.getInstance(1));
    JNopStmt nopStmt =
        Jimple.newNopStmt(
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(inst.iIndex()), operandPos));

    // TODO: [ms] clean way to handle multiple assertions in one body -> own nop -> own link to
    // target
    int stmtAfterAssertion = -42 - inst.iIndex();
    stmt2iIndex.put(nopStmt, stmtAfterAssertion);

    JIfStmt ifStmt =
        Jimple.newIfStmt(
            condition,
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    targetsOfIfStmts.put(ifStmt, stmtAfterAssertion);
    stmts.add(ifStmt);

    // create ifStmt for the actual assertion.
    Local assertLocal = getLocal(PrimitiveType.getBoolean(), inst.getUse(0));
    JEqExpr assertionExpr = Jimple.newEqExpr(assertLocal, IntConstant.getInstance(1));

    JIfStmt assertIfStmt =
        Jimple.newIfStmt(
            assertionExpr,
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    stmts.add(assertIfStmt);
    targetsOfIfStmts.put(assertIfStmt, stmtAfterAssertion);
    // create failed assertion code.

    ReferenceType assertionErrorType =
        JavaIdentifierFactory.getInstance().getClassType("java.lang.AssertionError");
    Local failureLocal = localGenerator.generateLocal(assertionErrorType);
    JNewExpr newExpr = Jimple.newNewExpr(assertionErrorType);

    JAssignStmt newAssignStmt =
        Jimple.newAssignStmt(
            failureLocal,
            newExpr,
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    stmts.add(newAssignStmt);
    MethodSignature methodSig =
        identifierFactory.getMethodSignature(
            "<init>", "java.lang.AssertionError", "void", Collections.emptyList());
    JSpecialInvokeExpr invoke = Jimple.newSpecialInvokeExpr(failureLocal, methodSig);
    JInvokeStmt invokeStmt =
        Jimple.newInvokeStmt(
            invoke,
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    stmts.add(invokeStmt);

    JThrowStmt throwStmt =
        Jimple.newThrowStmt(
            failureLocal,
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    stmts.add(throwStmt);

    // add nop in the end
    stmts.add(
        nopStmt); // TODO [LL] This should be removed later [ms] with the following statement after
    // assert
    return stmts;
  }

  private List<Stmt> convertAstLexicalWrite(DebuggingInformation debugInfo, AstLexicalWrite inst) {
    List<Stmt> stmts = new ArrayList<>();
    for (int i = 0; i < inst.getAccessCount(); i++) {
      Access access = inst.getAccess(i);
      Type type = converter.convertType(access.type);
      Value right;
      if (symbolTable.isConstant(access.valueNumber)) {
        right = getConstant(access.valueNumber);
      } else {
        right = getLocal(type, access.valueNumber);
      }
      JavaClassType cSig = (JavaClassType) methodSignature.getDeclClassType();
      // TODO check modifier
      Value left;
      if (!walaMethod.isStatic()) {
        FieldSignature fieldSig =
            identifierFactory.getFieldSignature(
                "val$" + access.variableName, cSig, type.toString());
        SootField field = new SootField(fieldSig, EnumSet.of(Modifier.FINAL));
        left = Jimple.newInstanceFieldRef(localGenerator.getThisLocal(), fieldSig);
        converter.addSootField(field); // add this field to class
        // TODO in old jimple this is not supported
      } else {
        left = localGenerator.generateLocal(type);
      }
      // TODO: [ms] no instruction example found to add positioninfo
      stmts.add(
          Jimple.newAssignStmt(
              left,
              right,
              WalaIRToJimpleConverter.convertPositionInfo(
                  debugInfo.getInstructionPosition(inst.iIndex()), null)));
    }
    return stmts;
  }

  private List<Stmt> convertAstLexicalRead(DebuggingInformation debugInfo, AstLexicalRead inst) {
    List<Stmt> stmts = new ArrayList<>();
    for (int i = 0; i < inst.getAccessCount(); i++) {
      Access access = inst.getAccess(i);
      Type type = converter.convertType(access.type);
      Local left = getLocal(type, access.valueNumber);
      JavaClassType cSig = (JavaClassType) methodSignature.getDeclClassType();
      // TODO check modifier
      Value rvalue = null;
      if (!walaMethod.isStatic()) {
        FieldSignature fieldSig =
            identifierFactory.getFieldSignature(
                "val$" + access.variableName, cSig, type.toString());
        SootField field = new SootField(fieldSig, EnumSet.of(Modifier.FINAL));
        rvalue = Jimple.newInstanceFieldRef(localGenerator.getThisLocal(), fieldSig);
        converter.addSootField(field); // add this field to class
      } else {
        rvalue = localGenerator.generateLocal(type);
      }

      // TODO: [ms] no instruction example found to add positioninfo
      stmts.add(
          Jimple.newAssignStmt(
              left,
              rvalue,
              WalaIRToJimpleConverter.convertPositionInfo(
                  debugInfo.getInstructionPosition(inst.iIndex()), null)));
    }
    return stmts;
  }

  private Stmt convertEnclosingObjectReference(
      DebuggingInformation debugInfo, EnclosingObjectReference inst) {
    Type enclosingType = converter.convertType(inst.getEnclosingType());
    Value variable = getLocal(enclosingType, inst.getDef());
    JavaClassType cSig = (JavaClassType) methodSignature.getDeclClassType();

    // TODO check modifier
    FieldSignature fieldSig =
        identifierFactory.getFieldSignature("this$0", cSig, enclosingType.toString());

    JInstanceFieldRef rvalue = Jimple.newInstanceFieldRef(localGenerator.getThisLocal(), fieldSig);

    // TODO: [ms] no instruction example found to add positioninfo
    return Jimple.newAssignStmt(
        variable,
        rvalue,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), null));
  }

  private Stmt convertCheckCastInstruction(
      DebuggingInformation debugInfo, SSACheckCastInstruction inst) {
    TypeReference[] types = inst.getDeclaredResultTypes();
    Local result = getLocal(converter.convertType(types[0]), inst.getResult());
    Immediate rvalue = null;
    int val = inst.getVal();
    if (symbolTable.isConstant(val)) {
      rvalue = getConstant(val);
    } else {
      rvalue = getLocal(converter.convertType(types[0]), val);
    }
    // TODO declaredResultType is wrong
    JCastExpr castExpr = Jimple.newCastExpr(rvalue, converter.convertType(types[0]));

    // TODO: [ms] no instruction example found to add positioninfo
    return Jimple.newAssignStmt(
        result,
        castExpr,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), null));
  }

  private Stmt convertLoadMetadataInstruction(
      DebuggingInformation debugInfo, SSALoadMetadataInstruction inst) {
    Local lval = getLocal(converter.convertType(inst.getType()), inst.getDef());
    TypeReference token = (TypeReference) inst.getToken();
    ClassConstant c = JavaJimple.getInstance().newClassConstant(token.getName().toString());

    // TODO: [ms] no instruction example found to add positioninfo
    return Jimple.newAssignStmt(
        lval,
        c,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), null));
  }

  private Stmt convertSwitchInstruction(DebuggingInformation debugInfo, SSASwitchInstruction inst) {
    int val = inst.getUse(0);
    Local local = getLocal(UnknownType.getInstance(), val);
    int[] cases = inst.getCasesAndLabels();
    int defaultCase = inst.getDefault();
    List<IntConstant> lookupValues = new ArrayList<>();
    List<Integer> targetList = new ArrayList<>();
    targetList.add(defaultCase);
    for (int i = 0; i < cases.length; i++) {
      int c = cases[i];
      if (i % 2 == 0) {
        IntConstant cValue = IntConstant.getInstance(c);
        lookupValues.add(cValue);
      } else {
        targetList.add(c);
      }
    }

    Position[] operandPos = new Position[2];
    // TODO: [ms] how to organize the operands
    // TODO: has no operand positions yet for
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), ); // key
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), ); // default
    // operandPos[i] = debugInfo.getOperandPosition(inst.iIndex(), ); // lookups
    // operandPos[i] = debugInfo.getOperandPosition(inst.iIndex(), ); // targets

    JSwitchStmt stmt =
        Jimple.newLookupSwitchStmt(
            local,
            lookupValues,
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    targetsOfLookUpSwitchStmts.put(stmt, targetList);
    return stmt;
  }

  private Stmt convertThrowInstruction(DebuggingInformation debugInfo, SSAThrowInstruction inst) {
    int exception = inst.getException();
    Local local = getLocal(UnknownType.getInstance(), exception);

    Position[] operandPos = new Position[1];
    // TODO: has no operand position yet for throwable
    operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);

    return Jimple.newThrowStmt(
        local,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertUnaryOpInstruction(
      DebuggingInformation debugInfo, SSAUnaryOpInstruction inst) {
    int def = inst.getDef();
    int use = inst.getUse(0);
    Immediate op;
    Type type = UnknownType.getInstance();
    if (symbolTable.isConstant(use)) {
      op = getConstant(use);
    } else {
      op = getLocal(type, use);
    }

    type = op.getType();
    // is it just variable declaration?
    if (type == NullType.getInstance()) {
      // FIXME: [ms] determine type of def side
      // if null is assigned or if its just a local declaration we can't use the right side (i.e.
      // null) to determine the locals type
      type = UnknownType.getInstance();
    }
    Local left = getLocal(type, def);

    Position[] operandPos = new Position[2];
    // TODO: has no operand positions yet for right side or assigned variable
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), 1);

    if (inst instanceof AssignInstruction) {
      return Jimple.newAssignStmt(
          left,
          op,
          WalaIRToJimpleConverter.convertPositionInfo(
              debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    } else {
      JNegExpr expr = Jimple.newNegExpr(op);

      return Jimple.newAssignStmt(
          left,
          expr,
          WalaIRToJimpleConverter.convertPositionInfo(
              debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    }
  }

  private Stmt convertPutInstruction(DebuggingInformation debugInfo, SSAPutInstruction inst) {
    FieldReference fieldRef = inst.getDeclaredField();
    Type fieldType = converter.convertType(inst.getDeclaredFieldType());
    String walaClassName = fieldRef.getDeclaringClass().getName().toString();
    JavaClassType classSig =
        (JavaClassType)
            identifierFactory.getClassType(converter.convertClassNameFromWala(walaClassName));
    FieldSignature fieldSig =
        identifierFactory.getFieldSignature(
            fieldRef.getName().toString(), classSig, fieldType.toString());
    Value fieldValue;
    if (inst.isStatic()) {
      fieldValue = Jimple.newStaticFieldRef(fieldSig);
    } else {
      int ref = inst.getRef();
      Local base = getLocal(classSig, ref);
      fieldValue = Jimple.newInstanceFieldRef(base, fieldSig);
    }
    Immediate value;
    int val = inst.getVal();
    if (symbolTable.isConstant(val)) {
      value = getConstant(val);
    } else {
      value = getLocal(fieldType, val);
    }

    Position[] operandPos = new Position[2];
    // TODO: has no operand positions yet for value, rvalue
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), 1);
    return Jimple.newAssignStmt(
        fieldValue,
        value,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertNewInstruction(DebuggingInformation debugInfo, SSANewInstruction inst) {
    int result = inst.getDef();
    Type type = converter.convertType(inst.getNewSite().getDeclaredType());
    Value var = getLocal(type, result);
    Value rvalue;
    if (type instanceof ArrayType) {
      int use = inst.getUse(0);
      Immediate size;
      if (symbolTable.isConstant(use)) {
        size = getConstant(use);
      } else {
        // TODO: size type unsure
        size = getLocal(PrimitiveType.getInt(), use);
      }
      Type baseType =
          converter.convertType(inst.getNewSite().getDeclaredType().getArrayElementType());
      rvalue = JavaJimple.getInstance().newNewArrayExpr(baseType, size);
    } else {
      rvalue = Jimple.newNewExpr((ReferenceType) type);
    }

    Position[] operandPos = new Position[2];
    // TODO: has no operand positions yet for type, size
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), 1);

    return Jimple.newAssignStmt(
        var,
        rvalue,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertComparisonInstruction(
      DebuggingInformation debugInfo, SSAComparisonInstruction inst) {
    // TODO imlement
    return Jimple.newNopStmt(
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), null));
  }

  private Stmt convertInstanceofInstruction(
      DebuggingInformation debugInfo, SSAInstanceofInstruction inst) {
    int result = inst.getDef();
    int ref = inst.getRef();
    Type checkedType = converter.convertType(inst.getCheckedType());
    // TODO. how to get type of ref?
    Local op = getLocal(UnknownType.getInstance(), ref);
    JInstanceOfExpr expr = Jimple.newInstanceOfExpr(op, checkedType);
    Value left = getLocal(PrimitiveType.getBoolean(), result);

    Position[] operandPos = new Position[2];
    // TODO: has no operand positions yet for checked and expected side
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), 1);

    return Jimple.newAssignStmt(
        left,
        expr,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertConversionInstruction(
      DebuggingInformation debugInfo, SSAConversionInstruction inst) {
    Type fromType = converter.convertType(inst.getFromType());
    Type toType = converter.convertType(inst.getToType());
    int def = inst.getDef();
    int use = inst.getUse(0);
    Value lvalue = getLocal(toType, def);
    Immediate rvalue;
    if (symbolTable.isConstant(use)) {
      rvalue = getConstant(use);
    } else {
      rvalue = getLocal(fromType, use);
    }
    JCastExpr cast = Jimple.newCastExpr(rvalue, toType);

    Position[] operandPos = new Position[2];
    // TODO: has no positions for lvalue, rvalue yet
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), 1);

    return Jimple.newAssignStmt(
        lvalue,
        cast,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertInvokeInstruction(
      DebuggingInformation debugInfo, AstJavaInvokeInstruction invokeInst) {
    AbstractInvokeExpr invoke;
    CallSiteReference callee = invokeInst.getCallSite();
    MethodReference target = invokeInst.getDeclaredTarget();
    String declaringClassSignature =
        converter.convertClassNameFromWala(target.getDeclaringClass().getName().toString());
    String returnType = converter.convertType(target.getReturnType()).toString();
    List<String> parameters = new ArrayList<>();
    List<Type> paraTypes = new ArrayList<>();
    List<Immediate> args = new ArrayList<>();
    for (int i = 0; i < target.getNumberOfParameters(); i++) {
      Type paraType = converter.convertType(target.getParameterType(i)); // note
      // the
      // parameters
      // do
      // not
      // include
      // "this"
      paraTypes.add(paraType);
      parameters.add(paraType.toString());
    }
    Position[] operandPos = new Position[invokeInst.getNumberOfUses()];
    for (int j = 0; j < invokeInst.getNumberOfUses(); j++) {
      operandPos[j] = debugInfo.getOperandPosition(invokeInst.iIndex(), j);
    }
    int i = 0;
    if (!callee.isStatic()) {
      i = 1; // non-static invoke this first use is thisRef.
    }
    for (; i < invokeInst.getNumberOfUses(); i++) {
      int use = invokeInst.getUse(i);
      Immediate arg;
      if (symbolTable.isConstant(use)) {
        arg = getConstant(use);
      } else {
        if (invokeInst.getNumberOfUses() > paraTypes.size()) {
          arg = getLocal(paraTypes.get(i - 1), use);
        } else {
          arg = getLocal(paraTypes.get(i), use);
        }
      }
      assert (arg != null);
      args.add(arg);
    }

    MethodSignature methodSig =
        identifierFactory.getMethodSignature(
            target.getName().toString(), declaringClassSignature, returnType, parameters);

    if (!callee.isStatic()) {
      int receiver = invokeInst.getReceiver();
      Type classType = converter.convertType(target.getDeclaringClass());
      Local base = getLocal(classType, receiver);
      if (callee.isSpecial()) {
        Type baseType = UnknownType.getInstance();
        // TODO. baseType could be a problem.
        base = getLocal(baseType, receiver);
        invoke = Jimple.newSpecialInvokeExpr(base, methodSig, args); // constructor
      } else if (callee.isVirtual()) {
        invoke = Jimple.newVirtualInvokeExpr(base, methodSig, args);
      } else if (callee.isInterface()) {
        invoke = Jimple.newInterfaceInvokeExpr(base, methodSig, args);
      } else {
        throw new RuntimeException("Unsupported invoke instruction: " + callee.toString());
      }
    } else {
      invoke = Jimple.newStaticInvokeExpr(methodSig, args);
    }

    if (invokeInst.hasDef()) {
      Type type = converter.convertType(invokeInst.getDeclaredResultType());
      Local v = getLocal(type, invokeInst.getDef());
      return Jimple.newAssignStmt(
          v,
          invoke,
          WalaIRToJimpleConverter.convertPositionInfo(
              debugInfo.getInstructionPosition(invokeInst.iIndex()), operandPos));
    } else {
      return Jimple.newInvokeStmt(
          invoke,
          WalaIRToJimpleConverter.convertPositionInfo(
              debugInfo.getInstructionPosition(invokeInst.iIndex()), operandPos));
    }
  }

  private List<Stmt> convertBranchInstruction(
      DebuggingInformation debugInfo, SSAConditionalBranchInstruction condInst) {
    StmtPositionInfo posInfo =
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(condInst.iIndex()), null);
    List<Stmt> stmts = new ArrayList<>();
    int val1 = condInst.getUse(0);
    int val2 = condInst.getUse(1);
    Immediate value1 = extractValueAndAddAssignStmt(posInfo, stmts, val1);
    Immediate value2 = extractValueAndAddAssignStmt(posInfo, stmts, val2);
    AbstractConditionExpr condition;
    IOperator op = condInst.getOperator();
    if (op.equals(Operator.EQ)) {
      condition = Jimple.newEqExpr(value1, value2);
    } else if (op.equals(Operator.NE)) {
      condition = Jimple.newNeExpr(value1, value2);
    } else if (op.equals(Operator.LT)) {
      condition = Jimple.newLtExpr(value1, value2);
    } else if (op.equals(Operator.GE)) {
      condition = Jimple.newGeExpr(value1, value2);
    } else if (op.equals(Operator.GT)) {
      condition = Jimple.newGtExpr(value1, value2);
    } else if (op.equals(Operator.LE)) {
      condition = Jimple.newLtExpr(value1, value2);
    } else {
      throw new RuntimeException("Unsupported conditional operator: " + op);
    }

    JIfStmt ifStmt = Jimple.newIfStmt(condition, posInfo);
    // target equals -1 refers to the end of the method
    targetsOfIfStmts.put(ifStmt, condInst.getTarget());
    stmts.add(ifStmt);
    return stmts;
  }

  private Immediate extractValueAndAddAssignStmt(
      StmtPositionInfo posInfo, List<Stmt> addTo, int val) {
    Immediate value;
    Integer constant = null;
    if (symbolTable.isZero(val)) {
      value = IntConstant.getInstance(0);
    } else {
      if (symbolTable.isConstant(val)) {
        Object c = symbolTable.getConstantValue(val);
        if (c instanceof Boolean) {
          constant = c.equals(true) ? 1 : 0;
        }
      }
      value = getLocal(PrimitiveType.getInt(), val);
    }
    if (constant != null) {
      JAssignStmt assignStmt =
          Jimple.newAssignStmt(value, IntConstant.getInstance(constant), posInfo);
      addTo.add(assignStmt);
    }
    return value;
  }

  private Stmt convertReturnInstruction(DebuggingInformation debugInfo, SSAReturnInstruction inst) {
    int result = inst.getResult();
    if (inst.returnsVoid()) {
      // this is return void stmt
      return Jimple.newReturnVoidStmt(
          WalaIRToJimpleConverter.convertPositionInfo(
              debugInfo.getInstructionPosition(inst.iIndex()), null));
    } else {
      Immediate ret;
      if (symbolTable.isConstant(result)) {
        ret = getConstant(result);
      } else {
        ret = getLocal(UnknownType.getInstance(), result);
      }

      Position[] operandPos = new Position[1];
      operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
      return Jimple.newReturnStmt(
          ret,
          WalaIRToJimpleConverter.convertPositionInfo(
              debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    }
  }

  private List<Stmt> convertStringAddition(
      Immediate op1,
      Immediate op2,
      Immediate result,
      Type type,
      int iindex,
      AstMethod.DebuggingInformation debugInfo) {
    List<Stmt> ret = new ArrayList();
    Position p1 = debugInfo.getOperandPosition(iindex, 0);
    Position p2 = debugInfo.getOperandPosition(iindex, 1);
    Position stmtPosition = debugInfo.getInstructionPosition(iindex);

    JavaClassType sbType =
        (JavaClassType) identifierFactory.getClassType("java.lang.StringBuilder");
    Local strBuilderLocal = localGenerator.generateLocal(sbType);

    Stmt newStmt =
        Jimple.newAssignStmt(
            strBuilderLocal,
            Jimple.newNewExpr(sbType),
            WalaIRToJimpleConverter.convertPositionInfo(stmtPosition, null));
    ret.add(newStmt);

    MethodSignature initMethod =
        identifierFactory.getMethodSignature(
            "<init>",
            sbType.getFullyQualifiedName(),
            VoidType.getInstance().toString(),
            Collections.singletonList(type.toString()));
    CAstSourcePositionMap.Position[] pos1 = new CAstSourcePositionMap.Position[2];
    pos1[0] = null;
    pos1[1] = p1;

    Stmt specStmt =
        Jimple.newInvokeStmt(
            Jimple.newSpecialInvokeExpr(strBuilderLocal, initMethod, op1),
            WalaIRToJimpleConverter.convertPositionInfo(stmtPosition, pos1));

    ret.add(specStmt);

    MethodSignature appendMethod =
        identifierFactory.getMethodSignature(
            "append",
            sbType.getFullyQualifiedName(),
            sbType.toString(),
            Collections.singletonList(type.toString()));
    Local strBuilderLocal2 = localGenerator.generateLocal(sbType);
    CAstSourcePositionMap.Position[] pos2 = new CAstSourcePositionMap.Position[2];
    pos2[0] = null;
    pos2[1] = p2;

    Stmt virStmt =
        Jimple.newAssignStmt(
            strBuilderLocal2,
            Jimple.newVirtualInvokeExpr(strBuilderLocal, appendMethod, op2),
            WalaIRToJimpleConverter.convertPositionInfo(stmtPosition, pos2));

    ret.add(virStmt);

    MethodSignature toStringMethod =
        identifierFactory.getMethodSignature(
            "toString", sbType.getFullyQualifiedName(), sbType.toString(), Collections.emptyList());

    Stmt toStringStmt =
        Jimple.newAssignStmt(
            result,
            Jimple.newVirtualInvokeExpr(strBuilderLocal2, toStringMethod),
            WalaIRToJimpleConverter.convertPositionInfo(stmtPosition, null));

    ret.add(toStringStmt);
    return ret;
  }

  private List<Stmt> convertBinaryOpInstruction(
      DebuggingInformation debugInfo, SSABinaryOpInstruction binOpInst) {
    List<Stmt> ret = new ArrayList<>();
    int def = binOpInst.getDef();
    int val1 = binOpInst.getUse(0);
    int val2 = binOpInst.getUse(1);
    Type type = UnknownType.getInstance();
    Immediate op1;
    if (symbolTable.isConstant(val1)) {
      op1 = getConstant(val1);
    } else {
      op1 = getLocal(type, val1);
    }
    type = op1.getType();
    Immediate op2;
    if (symbolTable.isConstant(val2)) {
      op2 = getConstant(val2);
    } else {
      op2 = getLocal(type, val2);
    }
    if (type.equals(UnknownType.getInstance())) type = op2.getType();
    AbstractBinopExpr binExpr;
    IBinaryOpInstruction.IOperator operator = binOpInst.getOperator();
    if (operator.equals(IBinaryOpInstruction.Operator.ADD)) {
      if (type.toString().equals("java.lang.String")) {
        // from wala java source code frontend we get also string addition(concatenation).
        Immediate result = getLocal(type, def);
        return convertStringAddition(op1, op2, result, type, binOpInst.iIndex(), debugInfo);
      }
      binExpr = Jimple.newAddExpr(op1, op2);
    } else if (operator.equals(IBinaryOpInstruction.Operator.SUB)) {
      binExpr = Jimple.newSubExpr(op1, op2);
    } else if (operator.equals(IBinaryOpInstruction.Operator.MUL)) {
      binExpr = Jimple.newMulExpr(op1, op2);
    } else if (operator.equals(IBinaryOpInstruction.Operator.DIV)) {
      binExpr = Jimple.newDivExpr(op1, op2);
    } else if (operator.equals(IBinaryOpInstruction.Operator.REM)) {
      binExpr = Jimple.newRemExpr(op1, op2);
    } else if (operator.equals(IBinaryOpInstruction.Operator.AND)) {
      binExpr = Jimple.newAndExpr(op1, op2);
    } else if (operator.equals(IBinaryOpInstruction.Operator.OR)) {
      binExpr = Jimple.newOrExpr(op1, op2);
    } else if (operator.equals(IBinaryOpInstruction.Operator.XOR)) {
      binExpr = Jimple.newXorExpr(op1, op2);
    } else if (operator.equals(CAstBinaryOp.EQ)) {
      binExpr = Jimple.newEqExpr(op1, op2);
      type = PrimitiveType.getBoolean();
    } else if (operator.equals(CAstBinaryOp.NE)) {
      binExpr = Jimple.newNeExpr(op1, op2);
      type = PrimitiveType.getBoolean();
    } else if (operator.equals(CAstBinaryOp.LT)) {
      binExpr = Jimple.newLtExpr(op1, op2);
      type = PrimitiveType.getBoolean();
    } else if (operator.equals(CAstBinaryOp.GE)) {
      binExpr = Jimple.newGeExpr(op1, op2);
      type = PrimitiveType.getBoolean();
    } else if (operator.equals(CAstBinaryOp.GT)) {
      binExpr = Jimple.newGtExpr(op1, op2);
      type = PrimitiveType.getBoolean();
    } else if (operator.equals(CAstBinaryOp.LE)) {
      binExpr = Jimple.newLeExpr(op1, op2);
      type = PrimitiveType.getBoolean();
    } else if (operator.equals(IShiftInstruction.Operator.SHL)) {
      binExpr = Jimple.newShlExpr(op1, op2);
    } else if (operator.equals(IShiftInstruction.Operator.SHR)) {
      binExpr = Jimple.newShrExpr(op1, op2);
    } else if (operator.equals(IShiftInstruction.Operator.USHR)) {
      binExpr = Jimple.newUshrExpr(op1, op2);
    } else {
      throw new RuntimeException("Unsupported binary operator: " + operator.getClass());
    }
    Position[] operandPos = new Position[2];
    Position p1 = debugInfo.getOperandPosition(binOpInst.iIndex(), 0);
    operandPos[0] = p1;
    Position p2 = debugInfo.getOperandPosition(binOpInst.iIndex(), 1);
    operandPos[1] = p2;
    Value result = getLocal(type, def);
    ret.add(
        Jimple.newAssignStmt(
            result,
            binExpr,
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(binOpInst.iIndex()), operandPos)));
    return ret;
  }

  private Stmt convertGoToInstruction(DebuggingInformation debugInfo, SSAGotoInstruction gotoInst) {
    JGotoStmt gotoStmt =
        Jimple.newGotoStmt(
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(gotoInst.iIndex()), null));
    targetsOfGotoStmts.put(gotoStmt, gotoInst.getTarget());
    return gotoStmt;
  }

  private Stmt convertGetInstruction(DebuggingInformation debugInfo, SSAGetInstruction inst) {
    int def = inst.getDef(0);
    FieldReference fieldRef = inst.getDeclaredField();
    Type fieldType = converter.convertType(inst.getDeclaredFieldType());
    String walaClassName = fieldRef.getDeclaringClass().getName().toString();
    JavaClassType classSig =
        (JavaClassType)
            identifierFactory.getClassType(converter.convertClassNameFromWala(walaClassName));
    FieldSignature fieldSig =
        identifierFactory.getFieldSignature(
            fieldRef.getName().toString(), classSig, fieldType.toString());
    Value rvalue = null;
    if (inst.isStatic()) {
      rvalue = Jimple.newStaticFieldRef(fieldSig);
    } else {
      int ref = inst.getRef();
      Local base = getLocal(classSig, ref);
      rvalue = Jimple.newInstanceFieldRef(base, fieldSig);
    }

    Position[] operandPos = new Position[1];
    operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);

    Value var = getLocal(fieldType, def);
    return Jimple.newAssignStmt(
        var,
        rvalue,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Constant getConstant(int valueNumber) {
    Object value = symbolTable.getConstantValue(valueNumber);
    if (value instanceof Boolean) {
      return BooleanConstant.getInstance((boolean) value);
    } else if (value instanceof Byte
        || value instanceof Character
        || value instanceof Short
        || value instanceof Integer) {
      return IntConstant.getInstance((int) value);
    } else if (symbolTable.isLongConstant(valueNumber)) {
      return LongConstant.getInstance((long) value);
    } else if (symbolTable.isDoubleConstant(valueNumber)) {
      return DoubleConstant.getInstance((double) value);
    } else if (symbolTable.isFloatConstant(valueNumber)) {
      return FloatConstant.getInstance((float) value);
    } else if (symbolTable.isStringConstant(valueNumber)) {
      return JavaJimple.getInstance().newStringConstant((String) value);
    } else if (symbolTable.isNullConstant(valueNumber)) {
      return NullConstant.getInstance();
    } else {
      throw new RuntimeException("Unsupported constant type: " + value.getClass().toString());
    }
  }

  private Local getLocal(Type type, int valueNumber) {
    final Local cachedLocal = locals.get(valueNumber);
    if (cachedLocal != null) {
      return cachedLocal;
    }
    if (valueNumber == 1) {
      // in wala symbol numbers start at 1 ... the "this" parameter will be symbol
      // number 1 in a non-static method.
      if (!walaMethod.isStatic()) {
        Local thisLocal = localGenerator.getThisLocal();
        locals.put(valueNumber, thisLocal);
        return thisLocal;
      }
    }
    if (symbolTable.isParameter(valueNumber)) {
      Local para = localGenerator.getParameterLocal(valueNumber - 1);
      if (para != null) {
        return para;
      }
    }

    Local ret = locals.computeIfAbsent(valueNumber, key -> localGenerator.generateLocal(type));

    if (!ret.getType().equals(type)) {
      // ret.setType(ret.getType().merge(type));
      // TODO: re-implement merge. [CB] Don't forget type can also be UnknownType.
      // throw new RuntimeException("Different types for same local
      // variable: "+ret.getType()+"<->"+type);
    }
    return ret;
  }

  /**
   * @param
   * @param builder
   * @return This methods returns a list of stmts with all branch stmts ({@link JIfStmt}, {@link
   *     JGotoStmt}, {@link JSwitchStmt}) having set up their target stmts.
   */
  protected void setUpTargets(Map<Stmt, Integer> stmt2iIndex, Body.BodyBuilder builder) {

    for (Map.Entry<JIfStmt, Integer> ifStmt : targetsOfIfStmts.entrySet()) {
      final JIfStmt key = ifStmt.getKey();
      final Integer value = ifStmt.getValue();

      for (Map.Entry<Stmt, Integer> entry : stmt2iIndex.entrySet()) {
        final Stmt target = entry.getKey();
        final Integer iTarget = entry.getValue();

        if (value.equals(iTarget)) {
          builder.addFlow(key, target);
          break;
        }
      }
    }

    for (Map.Entry<JGotoStmt, Integer> gotoStmt : targetsOfGotoStmts.entrySet()) {
      final JGotoStmt key = gotoStmt.getKey();
      final Integer value = gotoStmt.getValue();

      for (Map.Entry<Stmt, Integer> entry : stmt2iIndex.entrySet()) {
        final Stmt target = entry.getKey();
        final Integer iTarget = entry.getValue();

        if (value.equals(iTarget)) {
          builder.addFlow(key, target);
          break;
        }
      }
    }

    for (Map.Entry<JSwitchStmt, List<Integer>> item : targetsOfLookUpSwitchStmts.entrySet()) {
      final JSwitchStmt switchStmt = item.getKey();
      final List<Integer> targetIdxList = item.getValue();

      // assign target for every idx in targetIdxList of switchStmt
      for (Integer targetIdx : targetIdxList) {
        // search for matching index/stmt
        for (Map.Entry<Stmt, Integer> jumptableEntry : stmt2iIndex.entrySet()) {
          final Stmt stmt = jumptableEntry.getKey();
          final Integer idx = jumptableEntry.getValue();

          if (targetIdx.equals(idx)) {
            builder.addFlow(switchStmt, stmt);
            break;
          }
        }
      }
    }
  }

  /**
   * determines wheter a given wala index is a target of a Branching Instruction. e.g. used for
   * detection of implicit return statements in void methods.
   */
  public boolean hasJumpTarget(Integer i) {
    if (targetsOfIfStmts.containsValue(i)) return true;
    if (targetsOfGotoStmts.containsValue(i)) return true;
    for (List<Integer> list : targetsOfLookUpSwitchStmts.values()) {
      if (list.contains(i)) {
        return true;
      }
    }
    return false;
  }
}