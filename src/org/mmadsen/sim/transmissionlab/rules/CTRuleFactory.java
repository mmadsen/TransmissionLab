package org.mmadsen.sim.transmissionlab.rules;

import org.mmadsen.sim.transmissionlab.util.SimParameterOptionsMap;
import org.mmadsen.sim.transmissionlab.interfaces.IPopulationTransformationRule;
import org.mmadsen.sim.transmissionlab.interfaces.ISimulationModel;
import org.apache.commons.logging.Log;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Feb 24, 2008
 * Time: 4:17:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class CTRuleFactory {
    private SimParameterOptionsMap paramOptionsMap = null;
    private ISimulationModel model = null;
    private Log log = null;
    private Map<RuleOptions,String> ruleClassMap = null;
    private String wfClassName = "org.mmadsen.sim.transmissionlab.rules.WrightFisherTransmissionRule";
    private String moranClassName = "org.mmadsen.sim.transmissionlab.rules.MoranProcessRandomSamplingTransmission";
    private String mutClassName = "org.mmadsen.sim.transmissionlab.rules.RandomAgentInfiniteAllelesMutation";

    public static enum RuleOptions {  WrightFisherProcess,  MoranProcess, RandomAgentInfiniteAllelesMutation };

    public static final String POP_PROCESS_PARAM = "PopulationProcessType";
    public static final String POP_PROCESS_PROPERTY = "populationProcessType";
    public static final RuleOptions DEFAULT_POP_PROCESS = RuleOptions.WrightFisherProcess;
    public static final String MUTATION_TYPE_PARAM = "MutationProcessType";
    public static final String MUTATION_TYPE_PROPERTY = "mutationProcessType";
    public static final RuleOptions DEFAULT_MUTATION_TYPE = RuleOptions.RandomAgentInfiniteAllelesMutation;



    public CTRuleFactory(ISimulationModel model) {
        this.model = model;
        this.log = this.model.getLog();
        this.ruleClassMap = new HashMap<RuleOptions,String>();
        this.paramOptionsMap = new SimParameterOptionsMap();
        this.paramOptionsMap.addParameter(POP_PROCESS_PARAM, RuleOptions.WrightFisherProcess.toString());
        this.paramOptionsMap.addParameter(POP_PROCESS_PARAM, RuleOptions.MoranProcess.toString());
        this.paramOptionsMap.addParameter(MUTATION_TYPE_PARAM, RuleOptions.RandomAgentInfiniteAllelesMutation.toString());
        this.ruleClassMap.put(RuleOptions.MoranProcess, moranClassName);
        this.ruleClassMap.put(RuleOptions.WrightFisherProcess, wfClassName);
        this.ruleClassMap.put(RuleOptions.RandomAgentInfiniteAllelesMutation, mutClassName);
    }

    public SimParameterOptionsMap getSimParameterOptionsMap() {
        return paramOptionsMap;
    }

    public IPopulationTransformationRule getRuleForParameter(String parameter) {
        String paramOption = null;
        try {
            paramOption = (String) this.model.getSimpleModelPropertyByName(parameter);
        } catch (Exception ex) {
            this.log.error("Parameter " + parameter + " does not exist in model, will try for default value...");
        }

        if(paramOption == null) {
            if (parameter.equalsIgnoreCase(POP_PROCESS_PARAM)) {
                this.log.info("Selecting " + DEFAULT_POP_PROCESS.toString() + " as population process rule");
                paramOption = DEFAULT_POP_PROCESS.toString();
            } else if(parameter.equalsIgnoreCase(MUTATION_TYPE_PARAM)) {
                this.log.info("Selecting " + DEFAULT_MUTATION_TYPE.toString() + " as mutation rule");
                paramOption = DEFAULT_MUTATION_TYPE.toString();
            } else {
                this.log.error("Unknown parameter - cannot set default");
                System.exit(1);
            }
        }

        RuleOptions option = Enum.valueOf(RuleOptions.class, paramOption);
        IPopulationTransformationRule rule = this.createRuleObject(option);
        return rule;
    }

    private IPopulationTransformationRule createRuleObject(RuleOptions option) {
        String className = this.ruleClassMap.get(option);
        Class classForRule = null;
        IPopulationTransformationRule ruleObj = null;
        try {
            classForRule = Class.forName(className);
            ruleObj = (IPopulationTransformationRule) classForRule.newInstance();
            ruleObj.setSimulationModel(this.model);
        } catch(Exception ex) {
            this.log.error("Class " + className + " fatal error: " + ex.getMessage());
            System.exit(1);
        }
        return ruleObj;
    }

}