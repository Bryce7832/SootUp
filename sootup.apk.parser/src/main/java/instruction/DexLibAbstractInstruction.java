package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.FiveRegisterInstruction;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.RegisterRangeInstruction;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.Type;

import java.util.*;

/**
 * This class represents a wrapper around dexlib instruction.
 *
 */
public abstract class DexLibAbstractInstruction {
    protected int lineNumber = -1;

    protected final Instruction instruction;

//    protected HashMap<BranchingStmt, DexLibAbstractInstruction> branchingStmtInstructionHashMap = new HashMap<>();

    protected final int codeAddress;

    protected Stmt stmt;

    public Instruction getInstruction() {
        return instruction;
    }

//    public void addBranchingStmtMap(BranchingStmt branchingStmt, DexLibAbstractInstruction dexLibAbstractInstruction){
//        branchingStmtInstructionHashMap.put(branchingStmt, dexLibAbstractInstruction);
//    }

//    public BranchingStmt getFirstEntryFromBranchingMap(){
//        Iterator<Map.Entry<BranchingStmt, DexLibAbstractInstruction>> iterator = branchingStmtInstructionHashMap.entrySet().iterator();
//        return iterator.hasNext() ? iterator.next().getKey() : null;
//    }

//    public HashMap<BranchingStmt, DexLibAbstractInstruction> getBranchingStmtInstructionHashMap() {
//        return branchingStmtInstructionHashMap;
//    }

    /**
     * Jimplify this instruction.
     *
     * @param body
     *          to jimplify into.
     */
    public abstract void jimplify(DexBody body);

    /**
     * Return the source register that is moved to the given register. For instruction such as v0 = v3 (v0 gets the content of
     * v3), movesToRegister(3) returns -1 movesToRegister(0) returns 3
     *
     * Instructions should override this if they copy register content.
     *
     * @param register
     *          the number of the register
     * @return the source register number or -1 if it does not move.
     */
    int movesToRegister(int register) {
        return -1;
    }

    /**
     * Return the target register that is a copy of the given register. For instruction such as v0 = v3 (v0 gets the content of
     * v3), movesRegister(3) returns 0 movesRegister(0) returns -1
     *
     * Instructions should override this if they copy register content.
     *
     * @param register
     *          the number of the register
     * @return the new register number or -1 if it does not move.
     */
    int movesRegister(int register) {
        return -1;
    }


    /**
     * Return if the instruction overrides the value in the register.
     *
     * Instructions should override this if they modify the registers.
     *
     * @param register
     *          the number of the register
     */
    boolean overridesRegister(int register) {
        return false;
    }

    /**
     * Return the types that are be introduced by this instruction.
     *
     * Instructions that may introduce types should override this.
     */
    public Set<Type> introducedTypes() {
        return Collections.emptySet();
    }


    /**
     * @param instruction
     *          the underlying dexlib instruction
     * @param codeAddress
     *          the bytecode address of this instruction
     */
    public DexLibAbstractInstruction(Instruction instruction, int codeAddress) {
        this.instruction = instruction;
        this.codeAddress = codeAddress;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    protected void setStmt(Stmt stmt){
        this.stmt = stmt;
    }

    public Stmt getStmt(){
        return stmt;
    }

    /**
     * Return the indices used in the given instruction.
     *
     * @param instruction
     *          a range invocation instruction
     * @return a list of register indices
     */
    protected List<Integer> getUsedRegistersNums(RegisterRangeInstruction instruction) {
        List<Integer> regs = new ArrayList<Integer>();
        int start = instruction.getStartRegister();
        for (int i = start; i < start + instruction.getRegisterCount(); i++) {
            regs.add(i);
        }

        return regs;
    }

    /**
     * Return the indices used in the given instruction.
     *
     * @param instruction
     *          a invocation instruction
     * @return a list of register indices
     */
    protected List<Integer> getUsedRegistersNums(FiveRegisterInstruction instruction) {
        final int regCount = instruction.getRegisterCount();
        int[] regs = { instruction.getRegisterC(), instruction.getRegisterD(), instruction.getRegisterE(),
                instruction.getRegisterF(), instruction.getRegisterG(), };
        List<Integer> l = new ArrayList<Integer>();
        // We have at least one app with regCount=6, reg=35c.
        // App is "com.mobirix.gk2019.apk" from 2020 PlayStore data set
        for (int i = 0; i < Math.min(regCount, regs.length); i++) {
            l.add(regs[i]);
        }
        return l;
    }




}
