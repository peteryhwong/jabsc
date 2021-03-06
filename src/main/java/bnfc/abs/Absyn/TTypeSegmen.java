package bnfc.abs.Absyn; // Java Package generated by the BNF Converter.

public class TTypeSegmen extends TTypeSegment {
  public final String uident_;
  public TTypeSegmen(String p1) { uident_ = p1; }

  public <R,A> R accept(bnfc.abs.Absyn.TTypeSegment.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof bnfc.abs.Absyn.TTypeSegmen) {
      bnfc.abs.Absyn.TTypeSegmen x = (bnfc.abs.Absyn.TTypeSegmen)o;
      return this.uident_.equals(x.uident_);
    }
    return false;
  }

  public int hashCode() {
    return this.uident_.hashCode();
  }


}
