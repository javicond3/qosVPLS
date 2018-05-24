package org.foo.app;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleEvent;
import org.onosproject.net.flow.FlowRuleListener;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.DefaultFlowRule;

import static org.onosproject.net.flow.FlowRuleEvent.Type.*;

import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;

import com.google.common.collect.HashMultimap;





import org.onlab.packet.Ethernet;
import org.onlab.packet.IPv4;
import org.onlab.packet.MacAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleEvent;
import org.onosproject.net.flow.FlowRuleListener;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.*;
import org.onosproject.net.flow.criteria.Criterion.Type;
import org.onosproject.net.flow.criteria.EthCriterion;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketPriority;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.onosproject.net.meter.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.*;

import static org.onosproject.net.flow.FlowRuleEvent.Type.RULE_REMOVED;
import static org.onosproject.net.flow.criteria.Criterion.Type.ETH_SRC;
import static org.onosproject.net.flow.criteria.Criterion.Type.VLAN_VID;

/**
 * Skeletal ONOS application component.
 */
@Component(immediate = true)
public class QosVplsComponent {

    private ApplicationId appId;
    private ApplicationId appIntentId;
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;
    
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    private final FlowRuleListener flowListener = new InternalFlowListener();

    @Activate
    protected void activate() {
        log.info("Started");
        // bug del test por eso tengo que poner el if pq al pasar el test me lo
        // detecta null
        if (flowRuleService != null && coreService != null) {
            flowRuleService.addListener(flowListener);
            appId = coreService.registerApplication("org.foo.app");
            appIntentId = coreService.getAppId("org.onosproject.net.intent");
        }
    }

    @Deactivate
    protected void deactivate() {
        log.info("Stopped");
        if (flowRuleService != null) {
            QosOldFlows qosOldFlows = QosOldFlows.getInstance();
            qosOldFlows.clear();
            flowRuleService.removeFlowRulesById(appId);
            flowRuleService.removeListener(flowListener);
        }
    }

    private class InternalFlowListener implements FlowRuleListener {
        @Override
        public void event(FlowRuleEvent event) {
            FlowRule flowRule = event.subject();
            Criterion criterioPort = flowRule.selector().getCriterion(
                    Criterion.Type.IN_PORT);
            Criterion criterioVlan = flowRule.selector().getCriterion(
                    Criterion.Type.VLAN_VID);
            Criterion e1 = flowRule.selector().getCriterion(
                    Criterion.Type.ETH_DST_MASKED);
            Criterion e2 = flowRule.selector().getCriterion(
                    Criterion.Type.ETH_DST);
            if ((flowRule.appId() == appIntentId.id() && event.type() == RULE_ADDED)
                    || (flowRule.appId() == appId.id() && event.type() == RULE_REMOVED)) {
                String device = flowRule.deviceId().toString();
                String portMeter = "";
                String vlan = "-1";
                if (criterioPort != null) {
                    portMeter = criterioPort.toString().split(":")[1]; // pq devuelve IN_PORT:1                                                                                                                  
                }
                if (criterioVlan != null) {
                    vlan = criterioVlan.toString().split(":")[1]; // pq devuelve  VLAN_VID:1                                                                    
                }
                QosApliedMeters qosApliedMeters = QosApliedMeters.getInstance();
                QosOldFlows qosOldFlows = QosOldFlows.getInstance();
                if (qosApliedMeters.getMapaMeters().containsKey(device)) {
                    QosMeter newMeter = new QosMeter("noIdMeter", portMeter,
                            vlan);
                    if (qosApliedMeters.getMapaMeters().get(device).contains(
                            newMeter)) {
                        if (event.type() == RULE_ADDED) {
                            QosMeter meterGuardado = qosApliedMeters.getMapaMeters()
                                    .get(device).get(
                                            qosApliedMeters.getMapaMeters().get(
                                                    device).indexOf(newMeter));
                            FlowRule oldFlowRule = qosOldFlows
                                    .isOldFlow(flowRule, "" + device + ""
                                            + meterGuardado.toString());
                            MeterId meterId = null;
                            meterId = MeterId.meterId(Long
                                    .parseLong(meterGuardado.getId()));
                            TrafficTreatment newTreatment = DefaultTrafficTreatment
                                    .builder()
                                    .addTreatment(flowRule.treatment())
                                    .meter(meterId).build();
                            FlowRule newFlow = DefaultFlowRule.builder()
                                    .withSelector(flowRule.selector())
                                    .withTreatment(newTreatment)
                                    .forDevice(flowRule.deviceId())
                                    .makePermanent().fromApp(appId)
                                    .withPriority(flowRule.priority() + 50)
                                    .build();
                            if (oldFlowRule != null
                                    && (oldFlowRule
                                            .treatment()
                                            .toString()
                                            .equals(newFlow.treatment()
                                                    .toString()) == false)) {
                                flowRuleService.removeFlowRules(oldFlowRule);
                            }
                            // obtengo el meter guardado que hace referencia la nueva entrada                            
                            qosOldFlows.addFlow(newFlow, "" + device + ""
                                    + meterGuardado.toString());

                            flowRuleService.applyFlowRules(newFlow);
                        }
                        if (event.type() == RULE_REMOVED) {
                            QosMeter meterGuardado = qosApliedMeters.getMapaMeters()
                                    .get(device).get(
                                            qosApliedMeters.getMapaMeters().get(
                                                    device).indexOf(newMeter));
                            FlowRule actualFlowRule = qosOldFlows
                                    .isOldFlow(flowRule, "" + device + ""
                                            + meterGuardado.toString());
                            qosOldFlows.addFlow(actualFlowRule, "" + device
                                    + "" + meterGuardado.toString());
                            flowRuleService.applyFlowRules(actualFlowRule);
                        }
                    }
                }
            }
        }
    }
}
