package jabsc.classgen;

import abs.api.Actor;
import abs.api.Functional;
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.lang.model.element.Modifier;

final class StateUtil {
    
    static final String OBJECT = "java/lang/Object";
    static final String ACTOR = "abs/api/Actor";
    static final String CONTEXT = "abs/api/Context";
    static final String FUNCTIONAL = "abs/api/Functional";
    static final Function<String, String> ABS_TO_JDK;
    static final Set<String> BUILT_IN_ABS;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("Unit", "V");
        map.put("String", "Ljava/lang/String");
        map.put("Int", "Ljava/lang/Integer");
        map.put("Rat", "Ljava/lang/Double");
        map.put("Bool", "Ljava/lang/Boolean");
        ABS_TO_JDK = s -> { 
            String v = map.get(s);
            return v == null ? "L" + s : v;
        };
        BUILT_IN_ABS = map.keySet();
    }
    
    /**
     * package-name.class-name or package-name/class-name
     */
    static final Pattern UNQUALIFIED_CLASSNAME = Pattern.compile("^.*[\\/\\.]([^\\/\\.]+)$");
    
    /**
     * assume apply to only fully qualified name of a method, a field or a constructor 
     * which has the form package-name.class-name.element-name or package-name/class-name/element-name
     */
    static final Pattern MODULE_NAME = Pattern.compile("^(.*)[\\/\\.][^\\/\\.]+[\\/\\.][^\\/\\.]+$");
    
    static final String VOID_WRAPPER_CLASS_NAME = "Void";
    static final String VOID_PRIMITIVE_NAME = "void";
    static final String LITERAL_THIS = "this";
    static final String LITERAL_NULL = "null";
    static final String LITERAL_GET = "get";
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
    
    static int argumentCounts(String descriptor) {
        int start = descriptor.indexOf('(');
        int end = descriptor.indexOf(')');
        int argument = 0;
        for (int index = start; index < end; index++) {
            if (descriptor.charAt(index) == ';') {
                argument++;
            }
        }
        return argument;
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
