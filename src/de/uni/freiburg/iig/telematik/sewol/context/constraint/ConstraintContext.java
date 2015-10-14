package de.uni.freiburg.iig.telematik.sewol.context.constraint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.invation.code.toval.constraint.AbstractConstraint;
import de.invation.code.toval.constraint.NumberConstraint;
import de.invation.code.toval.misc.soabase.SOABase;
import static de.invation.code.toval.misc.soabase.SOABase.createFromProperties;
import de.invation.code.toval.misc.soabase.SOABaseProperties;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.types.DataUsage;
import de.invation.code.toval.validate.CompatibilityException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.acl.ACLModel;
import de.uni.freiburg.iig.telematik.sewol.context.process.ProcessContext;
import de.uni.freiburg.iig.telematik.sewol.context.process.ProcessContextProperties;
import java.awt.Window;
import java.io.File;
import java.util.Arrays;

/**
 * This class provides context information for process execution.<br>
 * More specifically:<br>
 * <ul>
 * <li></li>
 * </ul>
 *
 * To decide which activities can be executed by which subjects, it uses an
 * access control model.
 *
 * Note: A context must be compatible with the process it is used for,<br>
 * i.e. contain all process activities.
 *
 * @author Thomas Stocker
 */
public class ConstraintContext extends ProcessContext {

        /**
         * Constraints on attributes, which are used for routing purposes.<br>
         * Example: Activity "Double Check" is executed when the credit amount
         * exceeds $50.000.<br>
         */
        protected Map<String, Set<AbstractConstraint<?>>> routingConstraints;

        protected ConstraintContextListenerSupport constraintContextListenerSupport;

	//------- Constructors ------------------------------------------------------------------
        public ConstraintContext() {
                super();
        }

        /**
         * Creates a new context using the given activity names.
         *
         * @param name Names of process activities.
         * @throws ParameterException
         */
        public ConstraintContext(String name) {
                super(name);
        }

        public ConstraintContext(ConstraintContextProperties properties) throws PropertyException {
                super(properties);
                // Set routing constraints
                routingConstraints.clear();
                for (String activity : properties.getActivitiesWithRoutingConstraints()) {
                        Set<AbstractConstraint<?>> otherRoutingConstraints = properties.getRoutingConstraints(activity);
                        for (AbstractConstraint<?> routingConstraint : otherRoutingConstraints) {
                                addRoutingConstraint(activity, routingConstraint.clone());
                        }
                }
        }

        public static ConstraintContext newInstance(SOABase context) throws Exception {
                ConstraintContext constraintContext = new ConstraintContext();
                constraintContext.takeoverValues(context, false);
                return constraintContext;
        }

        @Override
        protected void initialize() {
                super.initialize();
                constraintContextListenerSupport = new ConstraintContextListenerSupport();
                routingConstraints = new HashMap<>();
        }

        public boolean addConstraintContextListener(ConstraintContextListener listener) {
                return constraintContextListenerSupport.addListener(listener) && constraintContextListenerSupport.addListener(listener);
        }

        public boolean removeConstraintContextListener(ConstraintContextListener listener) {
                return constraintContextListenerSupport.removeListener(listener) && constraintContextListenerSupport.removeListener(listener);
        }

        @Override
        public boolean removeActivity(String activity, boolean removeFromACModel, boolean notifyListeners) {
                if (super.removeActivity(activity, removeFromACModel, notifyListeners)) {
                        removeRoutingConstraints(activity, notifyListeners);
                        return true;
                }
                return false;
        }

        @Override
        public boolean removeDataUsageFor(String activity, String attribute) throws CompatibilityException {
                if (super.removeDataUsageFor(activity, attribute)) {
                        if (!hasDataUsage(activity, attribute)) {
                                removeRoutingConstraintsOnAttribute(activity, attribute, true);
                        }
                        return true;
                }
                return false;
        }

        @Override
        public boolean removeDataUsageFor(String activity, String attribute, DataUsage dataUsage) throws CompatibilityException {
                if (super.removeDataUsageFor(activity, attribute, dataUsage)) {
                        if (!hasDataUsage(activity, attribute)) {
                                removeRoutingConstraintsOnAttribute(activity, attribute, true);
                        }
                        return true;
                }
                return false;
        }

        @Override
        public boolean removeAttribute(String attribute, boolean removeFromACModel, boolean notifyListeners) {
                if (!super.removeAttribute(attribute, removeFromACModel, false)) {
                        return false;
                }
                removeRoutingConstraintsOnAttribute(attribute, notifyListeners);
                if (notifyListeners) {
                        contextListenerSupport.notifyObjectRemoved(attribute);
                }
                return true;
        }

        private void removeRoutingConstraintsOnAttribute(String attribute) {
                removeRoutingConstraintsOnAttribute(attribute, true);
        }

        /**
         * Removes all routing constraints that relate to the given attribute
         *
         * @param attribute
         * @param notifyListeners
         */
        private void removeRoutingConstraintsOnAttribute(String attribute, boolean notifyListeners) {
                for (String activity : activities) {
                        removeRoutingConstraintsOnAttribute(activity, attribute, notifyListeners);
                }
        }

        private void removeRoutingConstraintsOnAttribute(String activity, String attribute) {
                removeRoutingConstraintsOnAttribute(activity, attribute, true);
        }

        private void removeRoutingConstraintsOnAttribute(String activity, String attribute, boolean notifyListeners) {
                if (!routingConstraints.containsKey(activity)) {
                        return;
                }

                Set<AbstractConstraint<?>> constraintsToRemove = new HashSet<>();
                for (AbstractConstraint<?> c : routingConstraints.get(activity)) {
                        if (attribute.equals(c.getElement())) {
                                constraintsToRemove.add(c);
                        }
                }
                for (AbstractConstraint<?> constraintToRemove : constraintsToRemove) {
                        removeRoutingConstraint(activity, constraintToRemove, notifyListeners);
                }
        }

        //------- Constraints -----------------------------------------------------------------------------------------------
        public final <C extends AbstractConstraint<?>> boolean addRoutingConstraint(String activity, C constraint) throws CompatibilityException {
                return addRoutingConstraint(activity, constraint, true);
        }

        public final <C extends AbstractConstraint<?>> boolean addRoutingConstraint(String activity, C constraint, boolean notifyListeners) throws CompatibilityException {
                validateActivity(activity);
                Validate.notNull(constraint);
                validateAttribute(constraint.getElement());
                if (!getAttributesFor(activity).contains(constraint.getElement())) {
                        throw new CompatibilityException("Cannot add constraint on attribute " + constraint.getElement() + " for activity " + activity + ". Activity does not use attribute.");
                }

                if (!routingConstraints.containsKey(activity)) {
                        routingConstraints.put(activity, new HashSet<>());
                }
                if (routingConstraints.get(activity).add(constraint)) {
                        if (notifyListeners) {
                                constraintContextListenerSupport.notifyConstraintAdded(activity, constraint);
                        }
                        return true;
                }
                return false;
        }

        public boolean hasRoutingConstraints(String activity, String attribute) throws CompatibilityException {
                if (!hasRoutingConstraints(activity)) {
                        return false;
                }
                validateAttribute(attribute);
                for (AbstractConstraint<?> constraint : getRoutingConstraints(activity)) {
                        if (constraint.getElement().equals(attribute)) {
                                return true;
                        }
                }
                return false;
        }

        public boolean hasRoutingConstraints(String activity) throws CompatibilityException {
                validateActivity(activity);
                return routingConstraints.containsKey(activity);
        }

        public Set<AbstractConstraint<?>> getRoutingConstraints(String activity) throws CompatibilityException {
                validateActivity(activity);
                return routingConstraints.get(activity);
        }

        public Set<String> getActivitiesWithRoutingConstraints() {
                return routingConstraints.keySet();
        }

        public <C extends AbstractConstraint<?>> void removeRoutingConstraints() throws CompatibilityException {
                removeRoutingConstraints(true);
        }

        public <C extends AbstractConstraint<?>> void removeRoutingConstraints(boolean notifyListeners) throws CompatibilityException {
                for (String activity : getActivitiesWithRoutingConstraints()) {
                        removeRoutingConstraints(activity, notifyListeners);
                }
        }

        public <C extends AbstractConstraint<?>> boolean removeRoutingConstraints(String activity) throws CompatibilityException {
                return removeRoutingConstraints(activity, true);
        }

        public <C extends AbstractConstraint<?>> boolean removeRoutingConstraints(String activity, boolean notifyListeners) throws CompatibilityException {
                if (!hasRoutingConstraints(activity)) {
                        return false;
                }
                for (AbstractConstraint<?> constraint : getRoutingConstraints(activity)) {
                        removeRoutingConstraint(activity, constraint, notifyListeners);
                }
                return true;
        }

        public <C extends AbstractConstraint<?>> boolean removeRoutingConstraint(String activity, C constraint) throws CompatibilityException {
                return removeRoutingConstraint(activity, constraint, true);
        }

        public <C extends AbstractConstraint<?>> boolean removeRoutingConstraint(String activity, C constraint, boolean notifyListeners) throws CompatibilityException {
                validateActivity(activity);
                Validate.notNull(constraint);
                validateAttribute(constraint.getElement());

                if (!routingConstraints.containsKey(activity)) {
                        return false;
                }
                if (routingConstraints.get(activity).remove(constraint)) {
                        if (notifyListeners) {
                                constraintContextListenerSupport.notifyConstraintRemoved(activity, constraint);
                        }
                        return true;
                }
                return false;
        }

        public boolean hasRoutingConstraints() {
                return !routingConstraints.isEmpty();
        }

        @Override
        protected void addStringContent(StringBuilder builder) {
                super.addStringContent(builder);

                if (hasRoutingConstraints()) {
                        builder.append('\n');
                        builder.append("routing constraints:");
                        builder.append('\n');
                        for (String activity : routingConstraints.keySet()) {
                                builder.append(activity);
                                builder.append(": ");
                                builder.append(routingConstraints.get(activity));
                                builder.append('\n');
                        }
                }
        }

        @Override
        protected Class<?> getPropertiesClass() {
                return ConstraintContextProperties.class;
        }

        @Override
        public ConstraintContextProperties getProperties() throws PropertyException {
                ConstraintContextProperties result = (ConstraintContextProperties) super.getProperties();

                for (String activity : getActivities()) {
                        Set<AbstractConstraint<?>> routingConstraintsOfActivity = getRoutingConstraints(activity);
                        if (routingConstraintsOfActivity != null && !routingConstraintsOfActivity.isEmpty()) {
                                for (AbstractConstraint<?> routingConstraint : routingConstraintsOfActivity) {
                                        result.addRoutingConstraint(activity, routingConstraint);
                                }
                        }
                }
                return result;
        }

        @Override
        public void takeoverValues(SOABase soaBase, boolean notifyListeners) throws Exception {
                super.takeoverValues(soaBase, notifyListeners);

                ConstraintContext context = (ConstraintContext) soaBase;

                //Set routing constraints
                routingConstraints.clear();
                for (String activity : context.getActivitiesWithRoutingConstraints()) {
                        Set<AbstractConstraint<?>> otherRoutingConstraints = context.getRoutingConstraints(activity);
                        for (AbstractConstraint<?> routingConstraint : otherRoutingConstraints) {
                                addRoutingConstraint(activity, routingConstraint.clone(), notifyListeners);
                        }
                }
        }

        @Override
        public int hashCode() {
                final int prime = 31;
                int result = super.hashCode();
                result = prime * result + ((routingConstraints == null) ? 0 : routingConstraints.hashCode());
                return result;
        }

        @Override
        public boolean equals(Object obj) {
                if (this == obj) {
                        return true;
                }
                if (!super.equals(obj)) {
                        return false;
                }
                if (getClass() != obj.getClass()) {
                        return false;
                }
                ConstraintContext other = (ConstraintContext) obj;
                if (routingConstraints == null) {
                        if (other.routingConstraints != null) {
                                return false;
                        }
                } else if (!routingConstraints.equals(other.routingConstraints)) {
                        return false;
                }
                return true;
        }

        public static ConstraintContext createFromFile(File file) throws Exception {
                SOABaseProperties properties = ProcessContextProperties.loadPropertiesFromFile(file);
                if (!(properties instanceof ConstraintContextProperties)) {
                        throw new Exception("Loaded properties are not compatible with constraint context");
                }
                SOABase newContext = createFromProperties(properties);
                if (!(newContext instanceof ConstraintContext)) {
                        throw new Exception("Created context of wrong type, expected \"ConstraintContext\" but was \"" + newContext.getClass().getSimpleName() + "\"");
                }
                return (ConstraintContext) newContext;
        }

        @Override
        public boolean showDialog(Window parent) throws Exception {
                return ConstraintContextDialog.showDialog(parent, this);
        }

        public static void main(String[] args) throws Exception {
                Map<String, Set<DataUsage>> usage1 = new HashMap<>();
                Set<DataUsage> modes1 = new HashSet<>(Arrays.asList(DataUsage.READ, DataUsage.WRITE));
                usage1.put("attribute1", modes1);

                Map<String, Set<DataUsage>> usage2 = new HashMap<>();
                Set<DataUsage> modes2 = new HashSet<>(Arrays.asList(DataUsage.READ, DataUsage.DELETE));
                usage2.put("attribute2", modes2);

                Set<String> activities = new HashSet<>(Arrays.asList("act1", "act2"));
                Set<String> attributes = new HashSet<>(Arrays.asList("attribute1", "attribute2"));
                Set<String> subjects = new HashSet<>(Arrays.asList("s1", "s2"));
                ConstraintContext c = new ConstraintContext("c1");
                c.setActivities(activities);
                c.addAttributes(attributes);
                c.addSubjects(subjects);
                c.setDataUsageFor("act1", usage1);
                c.setDataUsageFor("act2", usage2);
                c.addRoutingConstraint("act1", NumberConstraint.parse("attribute1 < 200"));

                ACLModel acModel = new ACLModel("acl1", c);
                acModel.setName("acmodel1");
                acModel.setActivityPermission("s1", activities);
                c.setACModel(acModel);

                c.showDialog(null);

//        System.out.println(c);
//
//        c.getProperties().store("/Users/stocker/Desktop/processContext");
//
//        ConstraintContextProperties properties = new ConstraintContextProperties();
//        properties.load("/Users/stocker/Desktop/processContext");
//        SOABase c1 = SOABase.createFromProperties(properties);
//        System.out.println(c1);
//        System.out.println(c1.equals(c));
//        System.out.println(properties.getBaseClass());
//        System.out.println(c1.getClass());
        }

}
