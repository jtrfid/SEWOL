/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.sewol.context.process;

import de.invation.code.toval.debug.SimpleDebugger;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.ACModelContainer;

/**
 *
 * @author stocker
 */
public class ProcessContextContainer extends AbstractProcessContextContainer<ProcessContext,ProcessContextProperties>{

    private static final String PROCESS_CONTEXT_EXTENSION = "context";

	public ProcessContextContainer(String serializationPath) {
        super(serializationPath);
    }

    public ProcessContextContainer(String serializationPath, SimpleDebugger debugger) {
        super(serializationPath, debugger);
    }

    public ProcessContextContainer(String serializationPath, ACModelContainer availableACModels) {
        super(serializationPath, availableACModels);
    }

    public ProcessContextContainer(String serializationPath, SimpleDebugger debugger, ACModelContainer availableACModels) {
        super(serializationPath, debugger, availableACModels);
    }

    @Override
    protected ProcessContextProperties createNewProperties() throws Exception {
        return new ProcessContextProperties();
    }

    @Override
    protected ProcessContext createSOABaseFromProperties(ProcessContextProperties properties) throws Exception {
        return new ProcessContext(properties);
    }

	@Override
	protected String getFileEndingForComponent(ProcessContext component) {
	            return PROCESS_CONTEXT_EXTENSION;
	   
	}

}
