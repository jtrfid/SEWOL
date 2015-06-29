/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.sewol.context.constraint;

import de.invation.code.toval.debug.SimpleDebugger;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.ACModelContainer;

/**
 *
 * @author stocker
 */
public class ConstraintContextContainer extends AbstractConstraintContextContainer<ConstraintContext, ConstraintContextProperties>{

    public ConstraintContextContainer(String serializationPath) {
        super(serializationPath);
    }

    public ConstraintContextContainer(String serializationPath, SimpleDebugger debugger) {
        super(serializationPath, debugger);
    }

    public ConstraintContextContainer(String serializationPath, ACModelContainer availableACModels) {
        super(serializationPath, availableACModels);
    }

    public ConstraintContextContainer(String serializationPath, SimpleDebugger debugger, ACModelContainer availableACModels) {
        super(serializationPath, debugger, availableACModels);
    }
    
    @Override
    protected ConstraintContextProperties createNewProperties() throws Exception {
        return new ConstraintContextProperties();
    }

    @Override
    protected ConstraintContext createSOABaseFromProperties(ConstraintContextProperties properties) throws Exception {
        return new ConstraintContext(properties);
    }
    
}
