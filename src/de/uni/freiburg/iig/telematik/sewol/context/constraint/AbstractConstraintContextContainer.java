/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.sewol.context.constraint;

import de.invation.code.toval.constraint.AbstractConstraint;
import de.invation.code.toval.debug.SimpleDebugger;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.ACModelContainer;
import de.uni.freiburg.iig.telematik.sewol.context.process.AbstractProcessContextContainer;

/**
 *
 * @author stocker
 * @param <C>
 * @param <P>
 */
public abstract class AbstractConstraintContextContainer<C extends ConstraintContext, P extends ConstraintContextProperties> extends AbstractProcessContextContainer<C, P> {

        private static final String CONSTRAINT_CONTEXT_DESCRIPTOR = "Constraint Context";

        public AbstractConstraintContextContainer(String serializationPath) {
                super(serializationPath);
        }

        public AbstractConstraintContextContainer(String serializationPath, SimpleDebugger debugger) {
                super(serializationPath, debugger);
        }

        public AbstractConstraintContextContainer(String serializationPath, ACModelContainer availableACModels) {
                super(serializationPath, availableACModels);
        }

        public AbstractConstraintContextContainer(String serializationPath, SimpleDebugger debugger, ACModelContainer availableACModels) {
                super(serializationPath, debugger, availableACModels);
        }

        @Override
        public String getComponentDescriptor() {
                return CONSTRAINT_CONTEXT_DESCRIPTOR;
        }

        @Override
        protected void loadCustomContent(C constraintContext, P properties) throws Exception {
                super.loadCustomContent(constraintContext, properties);
                for (String activity : properties.getActivitiesWithRoutingConstraints()) {
                        for (AbstractConstraint<?> constraint : properties.getRoutingConstraints(activity)) {
                                constraintContext.addRoutingConstraint(activity, constraint);
                        }
                }
        }

}
