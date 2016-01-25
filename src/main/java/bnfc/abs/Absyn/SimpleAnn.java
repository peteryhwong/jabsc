package bnfc.abs.Absyn; // Java Package generated by the BNF Converter.

public class SimpleAnn extends Ann {
  public final PureExp pureexp_;
  public SimpleAnn(PureExp p1) { pureexp_ = p1; }

  public <R,A> R accept(bnfc.abs.Absyn.Ann.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof bnfc.abs.Absyn.SimpleAnn) {
      bnfc.abs.Absyn.SimpleAnn x = (bnfc.abs.Absyn.SimpleAnn)o;
      return this.pureexp_.equals(x.pureexp_);
    }
    return false;
  }

  public int hashCode() {
    return this.pureexp_.hashCode();
  }


}
