package de.uni.freiburg.iig.telematik.sewol.accesscontrol.properties;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import de.invation.code.toval.misc.ArrayUtils;
import de.invation.code.toval.misc.StringUtils;
import de.invation.code.toval.properties.AbstractProperties;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.types.DataUsage;
import de.invation.code.toval.validate.Validate;

public class ACModelProperties extends AbstractProperties {

    public ACModelProperties() {
        super();
    }

	//------- Property setting -------------------------------------------------------------
    private void setProperty(ACModelProperty property, Object value) {
        props.setProperty(property.toString(), value.toString());
    }

    private String getProperty(ACModelProperty property) {
        return props.getProperty(property.toString());
    }

	//-- Name
    public void setName(String name) {
        Validate.notNull(name);
        Validate.notEmpty(name);
        setProperty(ACModelProperty.NAME, name);
    }

    public String getName() throws PropertyException {
        String propertyValue = getProperty(ACModelProperty.NAME);
        if (propertyValue == null) {
            throw new PropertyException(ACModelProperty.NAME, propertyValue);
        }
        return propertyValue;
    }

	//-- Model type
    protected void setType(ACModelType type) {
        Validate.notNull(type);
        setProperty(ACModelProperty.MODEL_TYPE, type.toString());
    }

    public ACModelType getType() throws PropertyException {
        String propertyValue = getProperty(ACModelProperty.MODEL_TYPE);
        if (propertyValue == null) {
            throw new PropertyException(ACModelProperty.MODEL_TYPE, propertyValue);
        }

        ACModelType result = null;
        try {
            result = ACModelType.parse(propertyValue);
        } catch (Exception e) {
            throw new PropertyException(ACModelProperty.MODEL_TYPE, propertyValue, "Invalid value for model type.");
        }

        return result;
    }

	//-- Subject descriptor
    public void setSubjectDescriptor(String descriptor) {
        Validate.notNull(descriptor);
        Validate.notEmpty(descriptor);
        setProperty(ACModelProperty.SUBJECT_DESCRIPTOR, descriptor);
    }

    public String getSubjectDescriptor() throws PropertyException {
        String propertyValue = getProperty(ACModelProperty.SUBJECT_DESCRIPTOR);
        if (propertyValue == null) {
            throw new PropertyException(ACModelProperty.SUBJECT_DESCRIPTOR, propertyValue);
        }
        return propertyValue;
    }

	//-- Context name
    public void setContextName(String contextName) {
        Validate.notNull(contextName);
        Validate.notEmpty(contextName);
        setProperty(ACModelProperty.CONTEXT_NAME, contextName);
    }

    public String getContextName() throws PropertyException {
        String propertyValue = getProperty(ACModelProperty.CONTEXT_NAME);
        if (propertyValue == null) {
            throw new PropertyException(ACModelProperty.CONTEXT_NAME, propertyValue);
        }
        return propertyValue;
    }

	//-- Valid usage modes
    public void setValidUsageModes(Collection<DataUsage> validUsageModes) throws PropertyException {
        Validate.notNull(validUsageModes);
        if (validUsageModes.isEmpty()) {
            return;
        }
        Validate.noNullElements(validUsageModes);
        Set<DataUsage> usageSet = new HashSet<DataUsage>(validUsageModes);
        setProperty(ACModelProperty.VALID_USAGE_MODES, ArrayUtils.toString(encapsulateValues(usageSet)));
    }

    public Set<DataUsage> getValidUsageModes() throws PropertyException {
        String propertyValue = getProperty(ACModelProperty.VALID_USAGE_MODES);
        if (propertyValue == null) {
            throw new PropertyException(ACModelProperty.VALID_USAGE_MODES, propertyValue);
        }
        StringTokenizer tokens = StringUtils.splitArrayString(propertyValue, String.valueOf(ArrayUtils.VALUE_SEPARATION));
        Set<DataUsage> result = new HashSet<DataUsage>();
        while (tokens.hasMoreTokens()) {
            String nextToken = tokens.nextToken();
            if (nextToken.length() < 3) {
                throw new PropertyException(ACModelProperty.VALID_USAGE_MODES, propertyValue);
            }
            nextToken = nextToken.substring(1, nextToken.length() - 1);
            DataUsage nextUsage = null;
            try {
                nextUsage = DataUsage.parse(nextToken);
            } catch (Exception e) {
                throw new PropertyException(ACModelProperty.VALID_USAGE_MODES, nextToken);
            }
            result.add(nextUsage);
        }
        return result;
    }

}
