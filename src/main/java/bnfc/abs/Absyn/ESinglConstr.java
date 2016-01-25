package bnfc.abs.Absyn; // Java Package generated by the BNF Converter.

public class ESinglConstr extends PureExp {
  public final QType qtype_;
  public ESinglConstr(QType p1) { qtype_ = p1; }

  public <R,A> R accept(bnfc.abs.Absyn.PureExp.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof bnfc.abs.Absyn.ESinglConstr) {
      bnfc.abs.Absyn.ESinglConstr x = (bnfc.abs.Absyn.ESinglConstr)o;
      return this.qtype_.equals(x.qtype_);
    }
    return false;
  }

  public int hashCode() {
    return this.qtype_.hashCode();
  }


}
