package com.janboerman.invsee.spigot.impl_1_21_7_R5;

import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;

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
        if (problem instanceof TagValueInput.UnexpectedNonNumberProblem unexpectedNonNumberProblem) {
            throw new WrongNbtTypeException(unexpectedNonNumberProblem);
        } else {
            throw new RuntimeException(problem.description());
        }
    }
}

final class WrongNbtTypeException extends RuntimeException {
    final TagValueInput.UnexpectedNonNumberProblem problem;

    WrongNbtTypeException(TagValueInput.UnexpectedNonNumberProblem problem) {
        this.problem = problem;
    }
}