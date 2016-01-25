package bnfc.abs.Absyn; // Java Package generated by the BNF Converter.

public class NoBlock extends MaybeBlock {
  public NoBlock() { }

  public <R,A> R accept(bnfc.abs.Absyn.MaybeBlock.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof bnfc.abs.Absyn.NoBlock) {
      return true;
    }
    return false;
  }

  public int hashCode() {
    return 37;
  }


}
