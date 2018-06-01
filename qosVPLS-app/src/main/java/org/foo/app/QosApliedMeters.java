package org.foo.app;

import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
 *Clase que almacena los mecanismos de QoS aplicados
**/
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
	/**
     * Getter de mapameters.
	 *
     * @return mapa con los mecanismos de QoS aplicados
     */
	public Map<String, ArrayList<QosMeter>> getMapaMeters(){
		return this.mapaMeters;
	}

	/**
     * Actualiza mapaMeters con un nuevo Meter.
     * 
     * @param conmutador el switch en el que se aplica el meter
     * @param meterId el identificador del meter
     * @param meterPort el puerto donde aplicar el meter
     * @param meterVlan la vlan sobre la que aplica el meter
     * @return true si lo añade, false si ya existía
     */
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

	/**
     * Elimina un meter de mapaMeters.
     * 
     * @param conmutador el switch en el que se aplica el meter
     * @param meterId el identificador del meter
     * @param meterPort el puerto donde aplica el meter
     * @param meterVlan la vlan sobre la que aplica el meter
     */
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
	/**
     * Muestra todos los mecanismos de QoS aplicados.
     */
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
	/**
     * Elimina todos los mecanismos de QoS borrando el contenido de mapaMeters.
     */
	public void clear(){
		this.mapaMeters.clear();
	}

}
