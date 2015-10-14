/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.sewol.accesscontrol;

import de.invation.code.toval.debug.SimpleDebugger;
import de.invation.code.toval.misc.soabase.AbstractSOABaseContainer;
import de.invation.code.toval.misc.soabase.SOABase;
import de.invation.code.toval.misc.soabase.SOABaseProperties;
import de.invation.code.toval.misc.wd.AbstractComponentContainer;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.parser.ACModelParsing;

/**
 *
 * @author stocker
 * @param <C>
 * @param <P>
 */
public class ACModelContainer<C extends SOABase, P extends SOABaseProperties> extends AbstractComponentContainer<AbstractACModel> {

    public static final String ACMODEL_DESCRIPTOR = "AC-Model";
    public static final boolean DEFAULT_VALIDATE_PARSED_ACMODELS = false;
    private AbstractSOABaseContainer<C,P> availableContexts;
    private boolean validateParsedACModels = DEFAULT_VALIDATE_PARSED_ACMODELS;

    public ACModelContainer(String serializationPath, AbstractSOABaseContainer<C,P> availableContexts) {
        this(serializationPath, availableContexts, null);
    }

    public ACModelContainer(String serializationPath, AbstractSOABaseContainer<C,P> availableContexts, SimpleDebugger debugger) {
        super(serializationPath, debugger);
        Validate.notNull(availableContexts);
        this.availableContexts = availableContexts;
    }
    
    public void setValidateParsedACModels(boolean validateParsedACModels){
        this.validateParsedACModels = validateParsedACModels;
    }

    @Override
    protected void serializeComponent(AbstractACModel component, String serializationPath, String fileName) throws Exception {
        Validate.notNull(component);
        component.getProperties().store(serializationPath + fileName);
    }

    @Override
    protected AbstractACModel loadComponentFromFile(String file) throws Exception {
        return ACModelParsing.loadACModel(file, availableContexts.getComponentMap(), validateParsedACModels);
    }

    @Override
    public String getComponentDescriptor() {
        return ACMODEL_DESCRIPTOR;
    }

}
