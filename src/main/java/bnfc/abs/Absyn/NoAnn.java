package bnfc.abs.Absyn; // Java Package generated by the BNF Converter.

public class NoAnn extends Ann {
  public NoAnn() { }

  public <R,A> R accept(bnfc.abs.Absyn.Ann.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof bnfc.abs.Absyn.NoAnn) {
      return true;
    }
    return false;
  }

  public int hashCode() {
    return 37;
  }


}
