package org.foo.app;

import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class QosApliedMeters {
	private Map<String, ArrayList<QosMeter>> mapaMeters;
	
	public static QosApliedMeters instance;

	private QosApliedMeters() {
		this.mapaMeters = new HashMap<String, ArrayList<QosMeter>>();
	}

	public static QosApliedMeters getInstance() {
		if (null == instance)
			instance = new QosApliedMeters();
		return instance;
	}
	
	public Map<String, ArrayList<QosMeter>> getMapaMeters(){
		return this.mapaMeters;
	}

	// devuleve true si lo añade, false si ya existia
	public Boolean addElement(String conmutador, String meterId,
			String meterPort, String meterVlan) {
		if (!this.mapaMeters.containsKey(conmutador)) {
			this.mapaMeters.put(conmutador, new ArrayList<QosMeter>());
		}
		QosMeter nuevoMeter = new QosMeter(meterId, meterPort, meterVlan);
		if (!this.mapaMeters.get(conmutador).contains(nuevoMeter)) {
			this.mapaMeters.get(conmutador).add(nuevoMeter);
			System.out.println("New qos in " + conmutador + " port "
					+ meterPort + " vlan " + meterVlan);
			return true;
		} else {
			System.out.println("Qos already exists");
			return false;
		}

	}

	public void delElement(String conmutador, String meterId,
			String meterPort, String meterVlan) {
		if (!this.mapaMeters.containsKey(conmutador)) {
			System.out.println("Qos doesn't exist");
			return;
		}

		QosMeter nuevoMeter = new QosMeter(meterId, meterPort, meterVlan);
		if (this.mapaMeters.get(conmutador).remove(nuevoMeter) == true) {
			System.out.println("Deleted qos in " + conmutador + " port "
					+ meterPort + " vlan " + meterVlan);
		} else {
			System.out.println("Qos doesn't exist");
		}
		if (this.mapaMeters.get(conmutador).isEmpty()) {
			this.mapaMeters.remove(conmutador);
		}

	}

	public void show() {
		if (this.mapaMeters.isEmpty()) {
			System.out.println("No qos");
		}
		for (Entry<String, ArrayList<QosMeter>> entry : this.mapaMeters.entrySet()) {
			String conmutador = entry.getKey();
			String meterId;
			String meterPort;
			String meterVlan;
			System.out.println("Qos on switch " + conmutador);
			ArrayList<QosMeter> meters = entry.getValue();
			for (QosMeter meter : meters) {
				meterId = meter.getId();
				meterVlan = meter.getVlan();
				meterPort = meter.getPort();
				System.out.println("     meter: " + meterId + " port: "
						+ meterPort + " vlan: " + meterVlan);
			}
		}
	}
	
	public void clear(){
		this.mapaMeters.clear();
	}

}
