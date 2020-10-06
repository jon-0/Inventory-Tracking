package model;

import java.security.InvalidParameterException;

public class InHouse extends Part {
    private int machineId;
    
    public InHouse(int id, String name, double price, int stock, int min, int max, int machineId) throws NullPointerException, InvalidParameterException {
        super(id, name, price, stock, min, max);
        this.machineId = machineId;
    }

    public int getMachineId() { return machineId; }

    public void setMachineId(int machineId) {
        int oldMachineId = this.machineId;
        this.machineId = machineId;
        propertyChangeSupport.firePropertyChange(ModelHelper.PROP_MACHINEID, oldMachineId, machineId);
    }
}
