package jabsc.classgen;

import bnfc.abs.Absyn.ClassDecl;
import bnfc.abs.Absyn.ClassImplements;
import bnfc.abs.Absyn.ClassParamDecl;
import bnfc.abs.Absyn.ClassParamImplements;
import bnfc.abs.Absyn.DataDecl;
import bnfc.abs.Absyn.DataParDecl;
import bnfc.abs.Absyn.Decl.Visitor;
import bnfc.abs.Absyn.ExceptionDecl;
import bnfc.abs.Absyn.ExtendsDecl;
import bnfc.abs.Absyn.FunDecl;
import bnfc.abs.Absyn.FunParDecl;
import bnfc.abs.Absyn.InterfDecl;
import bnfc.abs.Absyn.TypeDecl;
import bnfc.abs.Absyn.TypeParDecl;


final class DeclNameVisitor implements Visitor<String, Void> {

    @Override
    public String visit(TypeDecl p, Void arg) {
        return p.uident_;
    }

    @Override
    public String visit(TypeParDecl p, Void arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String visit(ExceptionDecl p, Void arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String visit(DataDecl p, Void arg) {
        return p.uident_;
    }

    @Override
    public String visit(DataParDecl p, Void arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String visit(FunDecl p, Void arg) {
        return p.lident_;
    }

    @Override
    public String visit(FunParDecl p, Void arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String visit(InterfDecl p, Void arg) {
        return p.uident_;
    }

    @Override
    public String visit(ExtendsDecl p, Void arg) {
        return p.uident_;
    }

    @Override
    public String visit(ClassDecl p, Void arg) {
        return p.uident_;
    }

    @Override
    public String visit(ClassParamDecl p, Void arg) {
        return p.uident_;
    }

    @Override
    public String visit(ClassImplements p, Void arg) {
        return p.uident_;
    }

    @Override
    public String visit(ClassParamImplements p, Void arg) {
        return p.uident_;
    }

 

}
