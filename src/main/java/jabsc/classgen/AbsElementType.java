package jabsc.classgen;

import javax.lang.model.element.ElementKind;

/**
 * High-level element types that an ABS program/module can have.
 */
enum AbsElementType {
    
    /**
     * Equivalent to Java's {@link ElementKind#INTERFACE}
     */
    INTERFACE,

    /**
     * Same as Java's {@link ElementKind#CLASS}
     */
    CLASS,

    /**
     * An abstract data type declaration.
     */
    DATA,

    /**
     * Equivalent of a set of Java's <code>static</code> functions.
     */
    FUNCTION,

    /**
     * An abstract data type declaration.
     */
    TYPE;
    
}
