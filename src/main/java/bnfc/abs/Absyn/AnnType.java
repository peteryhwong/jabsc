package bnfc.abs.Absyn; // Java Package generated by the BNF Converter.

public abstract class AnnType implements java.io.Serializable {
  public abstract <R,A> R accept(AnnType.Visitor<R,A> v, A arg);
  public interface Visitor <R,A> {
    public R visit(bnfc.abs.Absyn.AnnTyp p, A arg);

  }

}
