package jabsc.classgen;

import bnfc.abs.Absyn.AndGuard;
import bnfc.abs.Absyn.ExpGuard;
import bnfc.abs.Absyn.FieldGuard;
import bnfc.abs.Absyn.Guard.Visitor;
import jabsc.classgen.VisitorState.ModuleInfo;
import bnfc.abs.Absyn.VarGuard;


final class GuardTypeVisitor implements Visitor<StringBuilder, StringBuilder> {

    private final MethodState methodState;
    private final ModuleInfo currentModule;
    
    GuardTypeVisitor(MethodState methodState, ModuleInfo currentModule) {
        this.methodState = methodState;
        this.currentModule = currentModule;
    }

    @Override
    public StringBuilder visit(VarGuard p, StringBuilder arg) {
        return arg.append(methodState.getLocalVariableType(p.lident_));
    }

    @Override
    public StringBuilder visit(FieldGuard p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(ExpGuard p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(AndGuard p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

}
