package org.mmadsen.sim.transmissionlab.util;

import java.util.*;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Feb 23, 2008
 * Time: 3:03:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimParameterOptionsMap {
    private Map<String,List<String>> parameterMap = null;
    Enum e = null;

    public SimParameterOptionsMap() {
        this.parameterMap = new HashMap<String,List<String>>();
    }

    /*public void addParameterAsEnum(String paramName, Enum e) {
        List<String> paramOptionList = new ArrayList<String>();
        Collection optionCollection = null;
        Class clazz = null;
        Class enumClass = e.getClass();
        try {
            Method m = clazz.getDeclaredMethod("values",new Class[0]);
            optionCollection = (Collection) m.invoke(e,new Object[0]);
        } catch( Exception ex ) {
            System.out.println("Exception getting values() from enum - might not be an Enum?");
            System.exit(1);
        }
        for(Iterator it = optionCollection.iterator(); it.hasNext(); ) {
            String option = ((enumClass)it.next()).getString();
            paramOptionList.add(option);
        }
    }*/

    public void addParameter(String paramName, String option) {
        List<String> paramOptionList = null;
        if (!this.parameterMap.containsKey(paramName)) {
            paramOptionList = new ArrayList<String>();
        } else {
            paramOptionList = this.parameterMap.get(paramName);
        }
        paramOptionList.add(option);
        this.parameterMap.put(paramName, paramOptionList);
    }

    public Set<String> getParameterNames() {
        return this.parameterMap.keySet();
    }

    public List<String> getOptionsForParam(String paramName) {
        assert(this.parameterMap.containsKey(paramName));
        return this.parameterMap.get(paramName);
    }
    
}
