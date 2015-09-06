package jabsc.classgen;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.ClassFile;

import java.nio.file.Path;
import java.util.function.BiFunction;

import javax.lang.model.element.ElementKind;

final class ClassFileWriterSupplier implements BiFunction<String, ElementKind, ClassWriter> {

    private final StringBuilder packageName;
    private final Path outDirPath;

    ClassFileWriterSupplier(String packageName, Path outputDirectory) {
        Path outPath = outputDirectory;
        for (String packagePart : packageName.split("\\.")) {
            outPath = outPath.resolve(packagePart);
        }
        this.outDirPath = outPath;
        this.packageName = new StringBuilder(packageName).append('.');
    }
    
    private String getQualifiedName(String unqualifiedName) {
        String qualifiedName = this.packageName.append(unqualifiedName).toString();
        this.packageName.setLength(this.packageName.length() - unqualifiedName.length());
        return qualifiedName;
    }
    
    @Override
    public ClassWriter apply(String unqualifiedName, ElementKind kind) {
        String qualifiedName = getQualifiedName(unqualifiedName);
        ClassFile classFile;
        switch (kind) {
            case CLASS:
            case ENUM:
                classFile = new ClassFile(false, qualifiedName, null);
                classFile.setAccessFlags(classFile.getAccessFlags() | AccessFlag.PUBLIC | AccessFlag.FINAL);
                break;
            case INTERFACE:
                classFile = new ClassFile(true, qualifiedName, null);
                classFile.setAccessFlags(classFile.getAccessFlags() | AccessFlag.PUBLIC);
                break;
            default:
                throw new IllegalArgumentException("Unsupported Java element kind: " + kind);            
        }
        
        classFile.setMajorVersion(ClassFile.JAVA_8);
        return new ClassWriter(outDirPath, classFile);
    }

}
