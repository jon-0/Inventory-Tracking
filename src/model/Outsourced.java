package model;

import java.security.InvalidParameterException;

public class Outsourced extends Part {
    private String companyName;
    

    public Outsourced(int id, String name, double price, int stock, int min, int max, String companyName) throws NullPointerException, InvalidParameterException {
        super(id, name, price, stock, min, max);
        if (companyName == null)
            throw new NullPointerException("Company name cannot be null.");
        if ((companyName = companyName.trim()).length() == 0)
            throw new InvalidParameterException("Company name cannot be empty.");
        this.companyName = companyName;
    }

    public String getCompanyName() { return companyName; }

    public void setCompanyName(String companyName) {
        if (companyName == null)
            throw new NullPointerException("Company name cannot be null.");
        if ((companyName = companyName.trim()).length() == 0)
            throw new InvalidParameterException("Company name cannot be empty.");
        String oldCompanyName = this.companyName;
        if (companyName.equals(oldCompanyName))
            return;
        this.companyName = companyName;
        propertyChangeSupport.firePropertyChange(ModelHelper.PROP_COMPANYNAME, oldCompanyName, companyName);
    }
}
