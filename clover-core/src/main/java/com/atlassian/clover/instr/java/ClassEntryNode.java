package com.atlassian.clover.instr.java;

import com.atlassian.clover.CloverNames;
import com.atlassian.clover.context.ContextSet;
import com.atlassian.clover.instr.tests.naming.JUnitParameterizedTestExtractor;
import com.atlassian.clover.instr.tests.naming.SpockFeatureNameExtractor;
import com.atlassian.clover.registry.entities.FullClassInfo;
import com.atlassian.clover.registry.FixedSourceRegion;
import com.atlassian.clover.registry.entities.Modifiers;

import java.util.List;
import java.util.Map;

public class ClassEntryNode extends Emitter {
    private String className;
    private String pkgname;
    private String superclass;
    private boolean isTopLevel;
    private boolean isInterface;
    private boolean isEnum;
    private boolean isAnnotation;
    private Map<String, List<String>> tags;
    private Modifiers mods;
    private boolean outerDetectTests;
    private CloverToken recorderInsertPoint;
    private RecorderInstrEmitter recorderInstrEmitter;

    public ClassEntryNode(Map<String, List<String>> tags, Modifiers mods,
                          String className, String pkgname, String superclass,
                          ContextSet context, int line, int col, boolean isTopLevel,
                          boolean isInterface, boolean isEnum, boolean isAnnotation) {
        super(context, line, col);
        this.tags = tags;
        this.mods = mods;
        this.className = className;
        this.pkgname = pkgname;
        this.superclass = superclass;
        this.isTopLevel = isTopLevel;
        this.isInterface = isInterface;
        this.isEnum = isEnum;
        this.isAnnotation = isAnnotation;
    }

    @Override
    public void init(InstrumentationState state) {
        outerDetectTests = state.isDetectTests();

        boolean testClass = state.getTestDetector() != null &&
                state.getTestDetector().isTypeMatch(state, new JavaTypeContext(tags, mods, pkgname, className, superclass));
        state.setDetectTests(testClass);
        // using 'testClass &&' as user could have custom test detector
        state.setSpockTestClass(testClass && SpockFeatureNameExtractor.isClassWithSpecAnnotations(mods));
        state.setParameterizedJUnitTestClass(testClass && JUnitParameterizedTestExtractor.isParameterizedClass(mods));

        FullClassInfo clazz = (FullClassInfo) state.getSession().enterClass(className,
                new FixedSourceRegion(getLine(), getColumn()), mods,
                isInterface, isEnum, isAnnotation);

        if (isTopLevel) {
            String recorderPrefix =
                CloverNames.recorderPrefixFor(
                    state.getFileInfo().getDataIndex(),
                    clazz.getDataIndex());

            if (state.getCfg().isClassInstrStrategy() || isEnum) {
                recorderPrefix += "." + CloverNames.RECORDER_FIELD_NAME;
            }
            state.setRecorderPrefix(recorderPrefix);
        }
    }

    public CloverToken getRecorderInsertPoint() {
        return recorderInsertPoint;
    }

    public void setRecorderInsertPoint(CloverToken recorderInsertPoint) {
        this.recorderInsertPoint = recorderInsertPoint;
    }

    public boolean isOuterDetectTests() {
        return outerDetectTests;
    }

    public void setRecorderInstrEmitter(RecorderInstrEmitter emitter) {
        this.recorderInstrEmitter = emitter;
    }

    public RecorderInstrEmitter getRecorderInstrEmitter() {
        return recorderInstrEmitter;
    }
}
