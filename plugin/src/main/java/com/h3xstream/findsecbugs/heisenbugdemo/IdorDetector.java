package com.h3xstream.findsecbugs.heisenbugdemo;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.ba.AnalysisContext;
import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.CFGBuilderException;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.ba.Location;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.Instruction;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class IdorDetector implements Detector {

    private static final List<String> REQUEST_MAPPING_ANNOTATION_TYPES = Arrays.asList(
            "Lorg/springframework/web/bind/annotation/GetMapping;", //
            "Lorg/springframework/web/bind/annotation/PostMapping;", //
            "Lorg/springframework/web/bind/annotation/PutMapping;", //
            "Lorg/springframework/web/bind/annotation/DeleteMapping;", //
            "Lorg/springframework/web/bind/annotation/RequestMapping;", //
            "Lorg/springframework/web/bind/annotation/PatchMapping;");

    private static final String CAN_ACCESS_METHOD_NAME = "canAccess";

    private final BugReporter bugReporter;

    public IdorDetector(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }


    @Override
    public void visitClassContext(ClassContext classContext) {
        List<Method> endpoints = findEndpoints(classContext);

        for (Method m : endpoints) {
            try {
                checkCanAccessCalled(classContext, m);
            } catch (CFGBuilderException e) {
                AnalysisContext.logError("Cannot analyze method", e);
            }
        }
    }

    @Override
    public void report() {

    }

    private List<Method> findEndpoints(ClassContext classContext) {
        List<Method> endpoints = new LinkedList<>();

        JavaClass javaClass = classContext.getJavaClass();

        for (Method m : javaClass.getMethods()) {

            for (AnnotationEntry ae : m.getAnnotationEntries()) {
                if (REQUEST_MAPPING_ANNOTATION_TYPES.contains(ae.getAnnotationType())) {
                    endpoints.add(m);
                }
            }
        }

        return endpoints;
    }

    private void checkCanAccessCalled(ClassContext classContext, Method m) throws CFGBuilderException {
        ConstantPoolGen cpg = classContext.getConstantPoolGen();
        String className = classContext.getJavaClass().getClassName();

        CFG cfg = classContext.getCFG(m);

        boolean success = false;

        for (Iterator<Location> i = cfg.locationIterator(); i.hasNext(); ) {
            Location loc = i.next();
            Instruction inst = loc.getHandle().getInstruction();

            if (inst instanceof INVOKESPECIAL) {
                INVOKESPECIAL invoke = (INVOKESPECIAL) inst;

                if (CAN_ACCESS_METHOD_NAME.equals(invoke.getMethodName(cpg)) &&
                        className.equals(invoke.getClassName(cpg))) {
                    success = true;
                }
            }
        }

        if (!success) {
            JavaClass clz = classContext.getJavaClass();
            bugReporter.reportBug(new BugInstance(this, "IDOR", Priorities.NORMAL_PRIORITY) //
                    .addClass(clz)
                    .addMethod(clz,m));
        }
    }
}
