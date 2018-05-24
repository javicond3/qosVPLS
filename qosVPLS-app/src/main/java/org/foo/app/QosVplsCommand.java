package org.foo.app;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.onosproject.net.ElementId;
import org.onosproject.net.DeviceId;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleEvent;
import org.onosproject.net.flow.FlowRuleListener;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;

import static org.onosproject.net.flow.FlowRuleEvent.Type.*;

import com.google.common.collect.HashMultimap;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
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
import static org.onosproject.net.flow.FlowRuleEvent.Type.*;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Argument;
import org.foo.app.QosApliedMeters;
import org.onosproject.cli.AbstractShellCommand;

import java.io.*;
import java.util.*;

/**
 * Sample Apache Karaf CLI command
 */
@Command(scope = "onos", name = "qosVpls", description = "Commands to add qos to Vpls")
public class QosVplsCommand extends AbstractShellCommand {

	protected CoreService coreService;
	protected FlowRuleService flowRuleService;

	@Argument(index = 0, name = "command", description = "Command name (add-qos|"
			+ "delete-qos|show|clean)", required = true, multiValued = false)
	String command = null;

	@Argument(index = 1, name = "deviceId", description = "The id of the device where apply the Meter", required = false, multiValued = false)
	String switchName = null;

	@Argument(index = 2, name = "idMeter", description = "The id of the Meter", required = false, multiValued = false)
	String idMeter = null;

	@Argument(index = 3, name = "portMeter", description = "The port where applies the Meter", required = false, multiValued = false)
	String portMeter = null;

	@Argument(index = 4, name = "vlanMeter", description = "The vlan who applies the Meter", required = false, multiValued = false)
	String vlanMeter = null;

	@Override
	protected void execute() {
		QosVplsCommandEnum enumCommand = QosVplsCommandEnum
				.enumFromString(command);
		QosApliedMeters qosApliedMeters = QosApliedMeters.getInstance();
		if (enumCommand != null) {
			switch (enumCommand) {
			case ADD_QOS:
				add_qos(switchName, idMeter, portMeter, vlanMeter);
				break;
			case DELETE_QOS:
				delete_qos(switchName, idMeter, portMeter, vlanMeter);
				break;
			case SHOW:
				qosApliedMeters.show();
				break;
			case CLEAN:
				clear();
				print("Qos cleaned");
				break;
			default:
				print("Command not found");
			}
		} else {
			print("Command not found");
		}
	}

	private void add_qos(String switchName, String idMeter, String portMeter,
			String vlanMeter) {
		QosApliedMeters qosApliedMeters = QosApliedMeters.getInstance();
		QosOldFlows qosOldFlows = QosOldFlows.getInstance();
		if (coreService == null) {
			coreService = get(CoreService.class);
		}
		if (flowRuleService == null) {
			flowRuleService = get(FlowRuleService.class);
		}
		
		if (switchName == null) {
			print("Introduce deviceId");
		} else {
			if (idMeter == null) {
				print("Introduce port");
			} else {
				if (portMeter == null) {
					print("Introduce meter");
				} else {
					if (vlanMeter == null) {
						vlanMeter = "-1";
					}
					DeviceId deviceIdentificador = null;
					deviceIdentificador = deviceIdentificador
							.deviceId(switchName);
					MeterId meterId = null;
					meterId = MeterId.meterId(Long.parseLong(idMeter));
					ApplicationId appId = coreService.getAppId("org.foo.app");
					ApplicationId appIntentId = coreService
							.getAppId("org.onosproject.net.intent");
					QosMeter meterToAdd = new QosMeter(idMeter, portMeter,
							vlanMeter);
					// compureba primero si existe ese meter, sino existe lo
					// mete y añade flows, sino da problemas
					if (qosApliedMeters.addElement(switchName, idMeter,
							portMeter, vlanMeter) == true) {
						for (FlowEntry flowEntry : flowRuleService
								.getFlowEntries(deviceIdentificador)) {
							Criterion criterioPort = flowEntry.selector()
									.getCriterion(Criterion.Type.IN_PORT);
							Criterion criterioVlan = flowEntry.selector()
									.getCriterion(Criterion.Type.VLAN_VID);
							String port_in = " ";
							String vlan = "-1";
							if (criterioPort != null) {
								port_in = criterioPort.toString().split(":")[1];
							}
							if (criterioVlan != null) {
								vlan = criterioVlan.toString().split(":")[1];
							}
							if (flowEntry.appId() == appIntentId.id()
									&& port_in.equals(portMeter)
									&& vlan.equals(vlanMeter)) {
								TrafficTreatment newTreatment = DefaultTrafficTreatment
										.builder()
										.addTreatment(flowEntry.treatment())
										.meter(meterId).build();
								FlowRule newFlow = DefaultFlowRule
										.builder()
										.withSelector(flowEntry.selector())
										.withTreatment(newTreatment)
										.forDevice(flowEntry.deviceId())
										.makePermanent()
										.fromApp(appId)
										.withPriority(flowEntry.priority() + 50)
										.build();
								flowRuleService.applyFlowRules(newFlow);
								qosOldFlows.addFlow(newFlow, "" + switchName
										+ "" + meterToAdd.toString());
							}
						}
					}
				}
			}
		}
	}

	private void delete_qos(String switchName, String idMeter,
			String portMeter, String vlanMeter) {
		QosApliedMeters qosApliedMeters = QosApliedMeters.getInstance();
		QosOldFlows qosOldFlows = QosOldFlows.getInstance();
		if (coreService == null) {
			coreService = get(CoreService.class);
		}
		if (flowRuleService == null) {
			flowRuleService = get(FlowRuleService.class);
		}
		if (switchName == null) {
			print("Introduce deviceId");
		} else {
			if (idMeter == null) {
				print("Introduce port");
			} else {
				if (portMeter == null) {
					print("Introduce meter");
				} else {
					if (vlanMeter == null) {
						vlanMeter = "-1";
					}
					QosMeter meterToDelete = new QosMeter(idMeter, portMeter,
							vlanMeter);
					ArrayList<FlowRule> flowRulesToDelete = qosOldFlows.getMapaOldFlows().get("" + switchName + ""
									+ meterToDelete.toString());
					// Borro la entrada del meter borrar ningun flow pq si se
					// borra uno y llega la señal de REMOVED a un meter
					// guardado lo va a volver a crear al verlo en el mapa
					qosApliedMeters.delElement(switchName, idMeter, portMeter,
							vlanMeter);
					if (flowRulesToDelete != null) {
						for (FlowRule flowRuleToDelete : flowRulesToDelete) {
							flowRuleService.removeFlowRules(flowRuleToDelete);
						}
						// borro todos los oldflows
						qosOldFlows.getMapaOldFlows().remove("" + switchName + ""
								+ meterToDelete.toString());
					}
					// Borro todpos los flows del mapa de flows.
				}
			}
		}
	}

	private void clear() {
		QosApliedMeters qosApliedMeters = QosApliedMeters.getInstance();
		QosOldFlows qosOldFlows = QosOldFlows.getInstance();
		if (coreService == null) {
			coreService = get(CoreService.class);
		}
		if (flowRuleService == null) {
			flowRuleService = get(FlowRuleService.class);
		}
		qosOldFlows.clear();
		
		qosApliedMeters.clear();
		ApplicationId appId = coreService.getAppId("org.foo.app");
		flowRuleService.removeFlowRulesById(appId);
	}
}
