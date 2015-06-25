package de.uni.freiburg.iig.telematik.sewol.context.constraint;

import de.invation.code.toval.constraint.AbstractConstraint;

import de.invation.code.toval.event.AbstractListenerSupport;

public class ConstraintContextListenerSupport extends AbstractListenerSupport<ConstraintContextListener> {
    
    public void notifyConstraintAdded(String activity, AbstractConstraint<?> constraint) {
        for (ConstraintContextListener listener : listeners) {
            listener.constraintAdded(activity, constraint);
        }
    }

    public void notifyConstraintRemoved(String activity, AbstractConstraint<?> constraint) {
        for (ConstraintContextListener listener : listeners) {
            listener.constraintRemoved(activity, constraint);
        }
    }

}
