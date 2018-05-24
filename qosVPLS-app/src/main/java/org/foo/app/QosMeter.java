package org.foo.app;

public class QosMeter {

	public String id;
	public String port;
	public String vlan;
	
	public QosMeter(String id, String port, String vlan){
		this.id = id;
		this.port = port;
		this.vlan = vlan;
	}
	
	public String getId(){
		return this.id;
	}
	
	public String getPort(){
		return this.port;
	}
	
	public String getVlan(){
		return this.vlan;
	}
	
	@Override
	public boolean equals(Object other){
	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof QosMeter)){
	    	return false;
	    }
	    QosMeter otherMyClass = (QosMeter)other;
	    if (this.port.equals(otherMyClass.getPort()) && this.vlan.equals(otherMyClass.getVlan())){
	    	return true;
	    }
	    return false;
	}

	@Override
    public String toString(){
      return " Meter ID"+this.id+" PortId: "+ this.port + " VlanId: "+ this.vlan;
             
    }
}