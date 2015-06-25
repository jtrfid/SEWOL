/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.sewol.context.constraint;

import de.invation.code.toval.debug.SimpleDebugger;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.parser.ACModelContainer;

/**
 *
 * @author stocker
 */
public class ConstraintContextContainer extends AbstractConstraintContextContainer<ConstraintContext, ConstraintContextProperties>{

    public ConstraintContextContainer(String serializationPath, ACModelContainer availableACModels) {
        super(serializationPath, availableACModels);
    }

    public ConstraintContextContainer(String serializationPath, ACModelContainer availableACModels, SimpleDebugger debugger) {
        super(serializationPath, availableACModels, debugger);
    }
    
    @Override
    protected ConstraintContextProperties crearteNewProperties() throws Exception {
        return new ConstraintContextProperties();
    }

    @Override
    protected ConstraintContext createSOABaseFromProperties(ConstraintContextProperties properties) throws Exception {
        return new ConstraintContext(properties);
    }
    
}
