package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.SwitchElement;
import org.jf.dexlib2.iface.instruction.formats.PackedSwitchPayload;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;

import java.util.ArrayList;
import java.util.List;

public class PackedSwitchInstruction extends SwitchInstruction {

    public PackedSwitchInstruction(Instruction instruction, int codeAddress) {
        super(instruction, codeAddress);
    }

    @Override
    protected Stmt switchStatement(DexBody body, Instruction targetData, Local key) {
        JSwitchStmt jSwitchStmt = Jimple.newLookupSwitchStmt(key, lookupValues, StmtPositionInfo.createNoStmtPositionInfo());
//        body.addBranchingStmt(jSwitchStmt, targets);
//        body.addBranchingStmt(jSwitchStmt, Collections.singletonList(defaultTarget));
        setStmt(jSwitchStmt);
        return jSwitchStmt;
    }

    @Override
    public void computeDataOffsets(DexBody body) {

    }
}
