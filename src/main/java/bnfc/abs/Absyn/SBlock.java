package bnfc.abs.Absyn; // Java Package generated by the BNF Converter.

public class SBlock extends Stm {
  public final ListStm liststm_;
  public SBlock(ListStm p1) { liststm_ = p1; }

  public <R,A> R accept(bnfc.abs.Absyn.Stm.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof bnfc.abs.Absyn.SBlock) {
      bnfc.abs.Absyn.SBlock x = (bnfc.abs.Absyn.SBlock)o;
      return this.liststm_.equals(x.liststm_);
    }
    return false;
  }

  public int hashCode() {
    return this.liststm_.hashCode();
  }


}
