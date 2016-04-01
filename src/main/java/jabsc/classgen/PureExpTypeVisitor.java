package jabsc.classgen;

import bnfc.abs.Absyn.Case;
import bnfc.abs.Absyn.EAdd;
import bnfc.abs.Absyn.EAnd;
import bnfc.abs.Absyn.EDiv;
import bnfc.abs.Absyn.EEq;
import bnfc.abs.Absyn.EFunCall;
import bnfc.abs.Absyn.EGe;
import bnfc.abs.Absyn.EGt;
import bnfc.abs.Absyn.EIntNeg;
import bnfc.abs.Absyn.ELe;
import bnfc.abs.Absyn.ELit;
import bnfc.abs.Absyn.ELogNeg;
import bnfc.abs.Absyn.ELt;
import bnfc.abs.Absyn.EMod;
import bnfc.abs.Absyn.EMul;
import bnfc.abs.Absyn.ENaryFunCall;
import bnfc.abs.Absyn.ENaryQualFunCall;
import bnfc.abs.Absyn.ENeq;
import bnfc.abs.Absyn.EOr;
import bnfc.abs.Absyn.EParamConstr;
import bnfc.abs.Absyn.EQualFunCall;
import bnfc.abs.Absyn.EQualVar;
import bnfc.abs.Absyn.ESinglConstr;
import bnfc.abs.Absyn.ESub;
import bnfc.abs.Absyn.EThis;
import bnfc.abs.Absyn.EVar;
import bnfc.abs.Absyn.If;
import bnfc.abs.Absyn.Let;
import bnfc.abs.Absyn.PureExp.Visitor;
import jabsc.classgen.VisitorState.ModuleInfo;


final class PureExpTypeVisitor implements Visitor<StringBuilder, StringBuilder> {
    
    private final MethodState methodState;
    private final ModuleInfo currentModule;
    
    PureExpTypeVisitor(MethodState methodState, ModuleInfo currentModule) {
        this.methodState = methodState;
        this.currentModule = currentModule;
    }

    @Override
    public StringBuilder visit(EOr p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(EAnd p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(EEq p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(ENeq p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(ELt p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(ELe p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(EGt p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(EGe p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(EAdd p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(ESub p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(EMul p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(EDiv p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(EMod p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(ELogNeg p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(EIntNeg p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(EFunCall p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(EQualFunCall p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(ENaryFunCall p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(ENaryQualFunCall p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(EVar p, StringBuilder arg) {
        return arg.append(methodState.getLocalVariableType(p.lident_));
    }

    @Override
    public StringBuilder visit(EThis p, StringBuilder arg) {
        String fullyQualifiedName = methodState.getClassName() + "." + p.lident_;
        String type = currentModule.getNameToSignature().get(fullyQualifiedName);
        return arg.append(type);
    }

    @Override
    public StringBuilder visit(EQualVar p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(ESinglConstr p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(EParamConstr p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(ELit p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(Let p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(If p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(Case p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

}
