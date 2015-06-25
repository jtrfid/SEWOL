/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.sewol.accesscontrol.parser;

import de.invation.code.toval.debug.SimpleDebugger;
import de.invation.code.toval.misc.soabase.AbstractSOABaseContainer;
import de.invation.code.toval.misc.soabase.SOABase;
import de.invation.code.toval.misc.soabase.SOABaseProperties;
import de.invation.code.toval.misc.wd.AbstractComponentContainer;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.AbstractACModel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author stocker
 * @param <C>
 */
public class ACModelContainer<C extends SOABase, P extends SOABaseProperties> extends AbstractComponentContainer<AbstractACModel> {

    private static final String DESCRIPTOR = "AC-Model";
    private AbstractSOABaseContainer<C,P> availableContexts;
    private SimpleDebugger debugger = null;

    public ACModelContainer(String serializationPath, AbstractSOABaseContainer<C,P> availableContexts) {
        this(serializationPath, availableContexts, null);
    }

    public ACModelContainer(String serializationPath, AbstractSOABaseContainer<C,P> availableContexts, SimpleDebugger debugger) {
        super(serializationPath, debugger);
        Validate.notNull(availableContexts);
        this.availableContexts = availableContexts;
    }

    @Override
    protected void serializeComponent(AbstractACModel component, String serializationPath, String fileName) throws Exception {
        Validate.notNull(component);
        component.getProperties().store(serializationPath + fileName);
    }

    @Override
    protected AbstractACModel loadComponentFromFile(String file) throws Exception {
        return ACModelParsing.loadACModel(file, availableContexts.getComponentMap());
    }

    @Override
    public Set<String> getAcceptedFileEndings() {
        return new HashSet<String>(Arrays.asList(""));
    }

    @Override
    public String getComponentDescriptor() {
        return DESCRIPTOR;
    }

}
