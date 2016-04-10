package jabsc.classgen;

import bnfc.abs.Absyn.AndGuard;
import bnfc.abs.Absyn.ExpGuard;
import bnfc.abs.Absyn.FieldGuard;
import bnfc.abs.Absyn.Guard.Visitor;
import bnfc.abs.Absyn.VarGuard;
import javassist.bytecode.Bytecode;


final class GuardVisitor implements Visitor<Bytecode, Bytecode> {

    private final MethodState methodState;
    private final VisitorState state;
    private final TypeVisitor typeVisitor;
    private final PureExpVisitor pureExpVisitor;
    private final PureExpTypeVisitor pureExpTypeVisitor;

    GuardVisitor(MethodState methodState, VisitorState state) {
        this.methodState = methodState;
        this.state = state;
        this.typeVisitor = new TypeVisitor(state::processQType);
        this.pureExpVisitor = new PureExpVisitor(methodState, state);
        this.pureExpTypeVisitor = new PureExpTypeVisitor(methodState, state.getCurrentModule());
    }
    
    @Override
    public Bytecode visit(VarGuard p, Bytecode arg) {
        arg.addAload(methodState.getLocalVariable(p.lident_));
        return arg;
    }

    @Override
    public Bytecode visit(FieldGuard p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(ExpGuard p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(AndGuard p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

}
