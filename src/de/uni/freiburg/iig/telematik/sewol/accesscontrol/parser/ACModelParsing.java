/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.sewol.accesscontrol.parser;

import de.invation.code.toval.debug.SimpleDebugger;
import de.invation.code.toval.file.FileUtils;
import de.invation.code.toval.graphic.dialog.MessageDialog;
import de.invation.code.toval.misc.soabase.SOABase;
import de.invation.code.toval.misc.wd.ProjectComponentException;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.AbstractACModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.acl.ACLModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.properties.ACLModelProperties;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.properties.ACMValidationException;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.properties.ACModelProperties;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.properties.ACModelProperty;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.properties.ACModelType;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.properties.RBACModelProperties;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.RBACModel;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author stocker
 */
public class ACModelParsing {

//    public static <C extends SOABase> void loadACModelsFromDirectory(String directory, Map<String,C> availableContexts) throws ProjectComponentException{
//        loadACModelsFromDirectory(directory, availableContexts, null);
//    }
//    
//    public static <C extends SOABase> Set<AbstractACModel> loadACModelsFromDirectory(String directory, Map<String,C> availableContexts, SimpleDebugger debugger) throws ProjectComponentException{
//        Validate.notNull(availableContexts);
//        Validate.noNullElements(availableContexts.values());
//        if(debugger != null) debugger.message("Searching for access control models:");
//        List<String> acFiles = null;
//        try {
//            acFiles = FileUtils.getFileNamesInDirectory(directory, true);
//        } catch (IOException e) {
//            throw new ProjectComponentException("Cannot access access control model directory.", e);
//        }
//        Set<AbstractACModel> result = new HashSet<>();
//        for (String acFile : acFiles) {
//            if(debugger != null) debugger.message("Loading access control model: " + acFile.substring(acFile.lastIndexOf('/') + 1) + "...   ");
//            try {
//                AbstractACModel parsedModel = ACModelParsing.loadACModel(acFile, availableContexts);
//                result.add(parsedModel);
//                if(debugger != null) debugger.message("Done.");
//            } catch (Exception e) {
//                if(debugger != null) debugger.message("Error: " + e.getMessage());
//                throw new ProjectComponentException("Cannot parse access control model.", e);
//            }
//        }
//        if(debugger != null) debugger.newLine();
//        return result;
//    }
    public static <C extends SOABase> AbstractACModel loadACModel(String acFile, Map<String, C> availableContexts) throws PropertyException, IOException {
        ACModelProperties testProperties = new ACModelProperties();
        try {
            testProperties.load(acFile);
        } catch (IOException e) {
            throw new IOException("Cannot load properties file: " + acFile + ".");
        }

        String contextName = testProperties.getContextName();
        if (contextName == null) {
            throw new PropertyException(ACModelProperty.CONTEXT_NAME, null, "Cannot extract context name from AC model properties");
        }
        if (!availableContexts.containsKey(contextName)) {
            throw new PropertyException(ACModelProperty.CONTEXT_NAME, contextName, "No context with adequate name available.");
        }

        // Check ACModel type
        AbstractACModel newModel = null;
        if (testProperties.getType().equals(ACModelType.ACL)) {
            ACLModelProperties aclProperties = new ACLModelProperties();
            aclProperties.load(acFile);
            newModel = new ACLModel(aclProperties, availableContexts.get(contextName));
        } else {
            RBACModelProperties rbacProperties = new RBACModelProperties();
            rbacProperties.load(acFile);
            newModel = new RBACModel(rbacProperties, availableContexts.get(contextName));
        }
        try {
            newModel.checkValidity();
        } catch (ACMValidationException e) {
            throw new ParameterException(e.getMessage());
        }
        return newModel;

    }

}
