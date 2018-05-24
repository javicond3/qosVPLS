package org.foo.app;

import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleEvent;
import org.onosproject.net.flow.FlowRuleListener;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.*;
import org.onosproject.net.flow.criteria.Criterion.Type;
import org.onosproject.net.flow.criteria.EthCriterion;

import static org.onosproject.net.flow.criteria.Criterion.Type.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class QosOldFlows {
	
	private Map<String, ArrayList<FlowRule>> mapaOldFlows; 
	
	public static QosOldFlows instance;

	private QosOldFlows() {
		this.mapaOldFlows = new HashMap<String, ArrayList<FlowRule>>();
	}

	public static QosOldFlows getInstance() {
		if (null == instance)
			instance = new QosOldFlows();
		return instance;
	}
	
	public Map<String, ArrayList<FlowRule>> getMapaOldFlows(){
		return this.mapaOldFlows;
	}
	
	
	public FlowRule isOldFlow(FlowRule flowRuleNew, String meter) {
		Criterion criterioDestNew = flowRuleNew.selector().getCriterion(
				Criterion.Type.ETH_DST_MASKED);
		if (criterioDestNew == null) {
			criterioDestNew = flowRuleNew.selector().getCriterion(
					Criterion.Type.ETH_DST);
		}
		if (!this.mapaOldFlows.containsKey(meter)) {
			this.mapaOldFlows.put(meter, new ArrayList<FlowRule>());
		}
		ArrayList<FlowRule> oldFlows = this.mapaOldFlows.get(meter);
		int contador = 0;
		for (FlowRule oldFlowRule : oldFlows) {
			Criterion criterioDestOld = oldFlowRule.selector().getCriterion(
					Criterion.Type.ETH_DST_MASKED);
			if (criterioDestOld == null) {
				criterioDestOld = oldFlowRule.selector().getCriterion(
						Criterion.Type.ETH_DST);
			}
			if (criterioDestOld.toString().equals(criterioDestNew.toString())) {
				this.mapaOldFlows.get(meter).remove(contador);
				return oldFlowRule;
			}
			contador++;
		}
		return null;
	}

	public void addFlow(FlowRule flowRuleNew, String meter) {
		if (!this.mapaOldFlows.containsKey(meter)) {
			this.mapaOldFlows.put(meter, new ArrayList<FlowRule>());
		}
		this.mapaOldFlows.get(meter).add(flowRuleNew);
	}
	
	public void clear(){
		this.mapaOldFlows.clear();
	}

}
