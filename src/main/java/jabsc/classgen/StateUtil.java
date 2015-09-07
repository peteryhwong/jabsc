package jabsc.classgen;

import abs.api.Functional;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.lang.model.element.Modifier;

import bnfc.abs.Absyn.ClassDecl;
import bnfc.abs.Absyn.ClassImplements;
import bnfc.abs.Absyn.ClassParamDecl;
import bnfc.abs.Absyn.ClassParamImplements;
import bnfc.abs.Absyn.DataDecl;
import bnfc.abs.Absyn.DataParDecl;
import bnfc.abs.Absyn.Decl;
import bnfc.abs.Absyn.ExtendsDecl;
import bnfc.abs.Absyn.FunDecl;
import bnfc.abs.Absyn.FunParDecl;
import bnfc.abs.Absyn.InterfDecl;
import bnfc.abs.Absyn.TypeDecl;
import bnfc.abs.Absyn.TypeParDecl;
import abs.api.Actor;

final class StateUtil {

    static final String VOID_WRAPPER_CLASS_NAME = "Void";
    static final String VOID_PRIMITIVE_NAME = "void";
    static final String LITERAL_THIS = "this";
    static final String LITERAL_NULL = "null";
    static final String FUNCTIONS_CLASS_NAME = "Functions";
    static final String MAIN_CLASS_NAME = "Main";
    static final String COMMA_SPACE = ", ";
    static final String ABS_API_ACTOR_CLASS = Actor.class.getName();
    static final Set<Modifier> DEFAULT_MODIFIERS = Collections.singleton(Modifier.PUBLIC);
    static final String[] DEFAULT_IMPORTS =
        new String[] {Collection.class.getPackage().getName() + ".*",
                Function.class.getPackage().getName() + ".*",
                Callable.class.getPackage().getName() + ".*",
                Actor.class.getPackage().getName() + ".*"};
    static final String[] DEFAULT_STATIC_IMPORTS = new String[] {Functional.class.getPackage()
        .getName() + "." + Functional.class.getSimpleName() + ".*"};

    static Predicate<Decl> isTheSameTopLevelDeclIdentifier(String className) {
        return decl -> className.equals(getTopLevelDeclIdentifier(decl));
    }

    static String getTopLevelDeclIdentifier(Decl decl) {
        if (decl instanceof ClassDecl) {
            return ((ClassDecl) decl).uident_;
        }
        if (decl instanceof ClassImplements) {
            return ((ClassImplements) decl).uident_;
        }
        if (decl instanceof ClassParamDecl) {
            return ((ClassParamDecl) decl).uident_;
        }
        if (decl instanceof ClassParamImplements) {
            return ((ClassParamImplements) decl).uident_;
        }
        if (decl instanceof ExtendsDecl) {
            return ((ExtendsDecl) decl).uident_;
        }
        if (decl instanceof InterfDecl) {
            return ((InterfDecl) decl).uident_;
        }
        if (decl instanceof FunDecl) {
            return ((FunDecl) decl).lident_;
        }
        if (decl instanceof DataDecl) {
            return ((DataDecl) decl).uident_;
        }
        if (decl instanceof DataParDecl) {
            return ((DataParDecl) decl).uident_;
        }
        if (decl == null) {
            return MAIN_CLASS_NAME;
        }
        throw new IllegalArgumentException("Unknown top level type: " + decl);
    }

    static boolean isAbsInterfaceDecl(Decl decl) {
        return decl instanceof InterfDecl || decl instanceof ExtendsDecl;
    }

    static boolean isAbsClassDecl(Decl decl) {
        return decl instanceof ClassDecl || decl instanceof ClassImplements
            || decl instanceof ClassParamDecl || decl instanceof ClassParamImplements;
    }

    static boolean isAbsFunctionDecl(Decl decl) {
        return decl instanceof FunDecl || decl instanceof FunParDecl;
    }

    static boolean isAbsDataTypeDecl(Decl decl) {
        return decl instanceof DataDecl || decl instanceof DataParDecl;
    }

    static boolean isAbsAbstractTypeDecl(Decl decl) {
        return decl instanceof TypeDecl || decl instanceof TypeParDecl;
    }


}