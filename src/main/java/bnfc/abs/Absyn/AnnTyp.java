package bnfc.abs.Absyn; // Java Package generated by the BNF Converter.

public class AnnTyp extends AnnType {
  public final ListAnn listann_;
  public final Type type_;
  public AnnTyp(ListAnn p1, Type p2) { listann_ = p1; type_ = p2; }

  public <R,A> R accept(bnfc.abs.Absyn.AnnType.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof bnfc.abs.Absyn.AnnTyp) {
      bnfc.abs.Absyn.AnnTyp x = (bnfc.abs.Absyn.AnnTyp)o;
      return this.listann_.equals(x.listann_) && this.type_.equals(x.type_);
    }
    return false;
  }

  public int hashCode() {
    return 37*(this.listann_.hashCode())+this.type_.hashCode();
  }


}
