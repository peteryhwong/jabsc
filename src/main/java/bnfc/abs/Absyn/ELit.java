package bnfc.abs.Absyn; // Java Package generated by the BNF Converter.

public class ELit extends PureExp {
  public final Literal literal_;
  public ELit(Literal p1) { literal_ = p1; }

  public <R,A> R accept(bnfc.abs.Absyn.PureExp.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof bnfc.abs.Absyn.ELit) {
      bnfc.abs.Absyn.ELit x = (bnfc.abs.Absyn.ELit)o;
      return this.literal_.equals(x.literal_);
    }
    return false;
  }

  public int hashCode() {
    return this.literal_.hashCode();
  }


}
