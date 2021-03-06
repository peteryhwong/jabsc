package bnfc.abs.Absyn; // Java Package generated by the BNF Converter.

public class ExtendsDecl extends Decl {
  public final Ann ann_;
  public final String uident_;
  public final ListQType listqtype_;
  public final ListMethSignat listmethsignat_;
  public ExtendsDecl(Ann p1, String p2, ListQType p3, ListMethSignat p4) { ann_ = p1; uident_ = p2; listqtype_ = p3; listmethsignat_ = p4; }

  public <R,A> R accept(bnfc.abs.Absyn.Decl.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof bnfc.abs.Absyn.ExtendsDecl) {
      bnfc.abs.Absyn.ExtendsDecl x = (bnfc.abs.Absyn.ExtendsDecl)o;
      return this.ann_.equals(x.ann_) && this.uident_.equals(x.uident_) && this.listqtype_.equals(x.listqtype_) && this.listmethsignat_.equals(x.listmethsignat_);
    }
    return false;
  }

  public int hashCode() {
    return 37*(37*(37*(this.ann_.hashCode())+this.uident_.hashCode())+this.listqtype_.hashCode())+this.listmethsignat_.hashCode();
  }


}
