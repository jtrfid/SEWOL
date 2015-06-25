package de.uni.freiburg.iig.telematik.sewol.context.constraint;

import de.invation.code.toval.constraint.AbstractConstraint;

import de.invation.code.toval.misc.soabase.SOABaseListener;

public interface ConstraintContextListener extends SOABaseListener {

    public void constraintAdded(String activity, AbstractConstraint<?> constraint);

    public void constraintRemoved(String activity, AbstractConstraint<?> constraint);

}
