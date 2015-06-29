/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.sewol.context.process;

import de.invation.code.toval.debug.SimpleDebugger;
import de.invation.code.toval.misc.soabase.AbstractSOABaseContainer;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.AbstractACModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.ACModelContainer;
import java.util.HashMap;
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
    
    private Map<String,String> requiredACModelLinks = new HashMap<>();
    
    protected AbstractProcessContextContainer(String serializationPath) {
        super(serializationPath);
    }
    
    protected AbstractProcessContextContainer(String serializationPath, SimpleDebugger debugger) {
        super(serializationPath, debugger);
    }
    
    protected AbstractProcessContextContainer(String serializationPath, ACModelContainer availableACModels) {
        this(serializationPath, null, availableACModels);
    }

    protected AbstractProcessContextContainer(String serializationPath, SimpleDebugger debugger, ACModelContainer availableACModels) {
        this(serializationPath, debugger);
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
        for (String activity : properties.getActivitiesWithDataUsage()) {
            processContext.setDataUsageFor(activity, properties.getDataUsageFor(activity));
        }
        
        String acModelName = null;
        try{
            acModelName = properties.getACModelName();
        } catch(PropertyException e){
            // Cannot extract AC-model name property
            // -> Abort here, since AC-model is optionel in process context
            return;
        } catch(Exception e){
            // Exception while extracting AC-model name
            throw e;
        }
        
        if(availableACModels == null){
            // Remember acModel name for later linking
            requiredACModelLinks.put(processContext.getName(), acModelName);
        } else {
            linkACModel(processContext, availableACModels, acModelName);
        }
    }
    
    private void linkACModel(C processContext, ACModelContainer availableACModels, String acModelName) throws Exception{
        if(!availableACModels.containsComponent(acModelName))
            throw new Exception( "No AC-model with adequate name available.");
        AbstractACModel acModel = (AbstractACModel) availableACModels.getComponent(acModelName);
        if (acModel == null) {
            throw new Exception("Referred AC-model could not be loaded: " + acModelName);
        }
        processContext.setACModel(acModel);
    }
    
    public void linkACModels(ACModelContainer availableACModels, boolean removeContextsWithoutValidLink) throws Exception{
        for(String contextName: requiredACModelLinks.keySet()){
            linkACModel(getComponent(contextName), availableACModels, requiredACModelLinks.get(contextName));
        }
    }
}
