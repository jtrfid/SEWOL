/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.sewol.context.process;

import de.invation.code.toval.debug.SimpleDebugger;
import de.invation.code.toval.misc.soabase.AbstractSOABaseContainer;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.AbstractACModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.parser.ACModelContainer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author stocker
 */
public abstract class AbstractProcessContextContainer<C extends ProcessContext, P extends ProcessContextProperties> extends AbstractSOABaseContainer<C,P> {
    
    private static final String PROCESS_CONTEXT_DESCRIPTOR = "Process Context";

    private ACModelContainer availableACModels = null;
    
    protected AbstractProcessContextContainer(String serializationPath, ACModelContainer availableACModels) {
        this(serializationPath, availableACModels, null);
    }

    protected AbstractProcessContextContainer(String serializationPath, ACModelContainer availableACModels, SimpleDebugger debugger) {
        super(serializationPath, debugger);
        Validate.notNull(availableACModels);
        this.availableACModels = availableACModels;
    }

    @Override
    public String getComponentDescriptor() {
        return PROCESS_CONTEXT_DESCRIPTOR;
    }
    
    /**
     * Checks if there are contexts whose access control model equals the given
     * model.
     *
     * @param acModelName The access control model.
     * @return <code>true</code> if there is at least one such context;<br>
     * <code>fasle</code> otherwise.
     */
    public boolean containsContextsWithACModel(AbstractACModel acModel) {
        for (ProcessContext context : getComponents()) {
            if (context.getACModel().equals(acModel)) {
                return true;
            }
        }
        return false;
    }

    public Set<String> getNamesOfContextsWithACModel(AbstractACModel acModel) {
        Set<String> result = new HashSet<String>();
        for (ProcessContext context : getComponents()) {
            if (context.getACModel().equals(acModel)) {
                result.add(context.getName());
            }
        }
        return result;
    }

    @Override
    protected void loadCustomContent(C processContext, P properties) throws Exception {
        if(!availableACModels.containsComponent(properties.getACModelName()))
            throw new Exception( "No AC-model with adequate name available.");
        AbstractACModel acModel = (AbstractACModel) availableACModels.getComponent(properties.getACModelName());
        if (acModel == null) {
            throw new Exception("Referred AC-model could not be loaded: " + properties.getACModelName());
        }
        processContext.setACModel(acModel);
        for (String activity : properties.getActivitiesWithDataUsage()) {
            processContext.setDataUsageFor(activity, properties.getDataUsageFor(activity));
        }
    }
}
