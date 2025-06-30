package com.janboerman.invsee.spigot.impl_1_21_7_R5;

import net.minecraft.util.ProblemReporter;

final class ThrowingProblemReporter implements ProblemReporter {

    static final ThrowingProblemReporter INSTANCE = new ThrowingProblemReporter();

    private ThrowingProblemReporter() {
    }

    @Override
    public ProblemReporter forChild(PathElement pathElement) {
        return this;
    }

    @Override
    public void report(Problem problem) {
        throw new RuntimeException(problem.description());
    }
}
