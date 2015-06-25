/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.sewol.context.process;

import de.invation.code.toval.debug.SimpleDebugger;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.parser.ACModelContainer;

/**
 *
 * @author stocker
 */
public class ProcessContextContainer extends AbstractProcessContextContainer<ProcessContext,ProcessContextProperties>{

    public ProcessContextContainer(String serializationPath, ACModelContainer availableACModels) {
        super(serializationPath, availableACModels);
    }

    public ProcessContextContainer(String serializationPath, ACModelContainer availableACModels, SimpleDebugger debugger) {
        super(serializationPath, availableACModels, debugger);
    }

    @Override
    protected ProcessContextProperties crearteNewProperties() throws Exception {
        return new ProcessContextProperties();
    }

    @Override
    protected ProcessContext createSOABaseFromProperties(ProcessContextProperties properties) throws Exception {
        return new ProcessContext(properties);
    }

}
