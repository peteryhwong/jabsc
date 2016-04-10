package jabsc.classgen;

import bnfc.abs.Absyn.AndGuard;
import bnfc.abs.Absyn.ExpGuard;
import bnfc.abs.Absyn.FieldGuard;
import bnfc.abs.Absyn.Guard.Visitor;
import bnfc.abs.Absyn.VarGuard;


final class GuardNameVisitor implements Visitor<String, Void> {

    @Override
    public String visit(VarGuard p, Void arg) {
        return p.lident_;
    }

    @Override
    public String visit(FieldGuard p, Void arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String visit(ExpGuard p, Void arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String visit(AndGuard p, Void arg) {
        throw new UnsupportedOperationException();
    }

}
