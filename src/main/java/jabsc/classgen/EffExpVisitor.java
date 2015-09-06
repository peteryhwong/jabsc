package jabsc.classgen;

import bnfc.abs.Absyn.AsyncMethCall;
import bnfc.abs.Absyn.Get;
import bnfc.abs.Absyn.New;
import bnfc.abs.Absyn.NewLocal;
import bnfc.abs.Absyn.Spawns;
import bnfc.abs.Absyn.SyncMethCall;
import bnfc.abs.Absyn.ThisAsyncMethCall;
import bnfc.abs.Absyn.ThisSyncMethCall;

import bnfc.abs.Absyn.EffExp.Visitor;
import javassist.bytecode.Bytecode;

final class EffExpVisitor implements Visitor<Bytecode, Bytecode> {

    private final VisitorState state;
    
    EffExpVisitor(VisitorState state) {
        this.state = state;
    }
    
    @Override
    public Bytecode visit(New p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(NewLocal p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(SyncMethCall p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(ThisSyncMethCall p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(AsyncMethCall p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(ThisAsyncMethCall p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(Get p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(Spawns p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

}
