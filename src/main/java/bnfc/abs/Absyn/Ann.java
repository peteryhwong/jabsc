package bnfc.abs.Absyn; // Java Package generated by the BNF Converter.

public abstract class Ann implements java.io.Serializable {
  public abstract <R,A> R accept(Ann.Visitor<R,A> v, A arg);
  public interface Visitor <R,A> {
    public R visit(bnfc.abs.Absyn.NoAnn p, A arg);
    public R visit(bnfc.abs.Absyn.SimpleAnn p, A arg);
    public R visit(bnfc.abs.Absyn.MappedAnn p, A arg);

  }

}
