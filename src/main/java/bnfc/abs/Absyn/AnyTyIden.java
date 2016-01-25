package bnfc.abs.Absyn; // Java Package generated by the BNF Converter.

public class AnyTyIden extends AnyIdent {
  public final String uident_;
  public AnyTyIden(String p1) { uident_ = p1; }

  public <R,A> R accept(bnfc.abs.Absyn.AnyIdent.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof bnfc.abs.Absyn.AnyTyIden) {
      bnfc.abs.Absyn.AnyTyIden x = (bnfc.abs.Absyn.AnyTyIden)o;
      return this.uident_.equals(x.uident_);
    }
    return false;
  }

  public int hashCode() {
    return this.uident_.hashCode();
  }


}
