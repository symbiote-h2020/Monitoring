package eu.h2020.symbiote.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jose on 26/09/16.
 */
/**! \class ResourceBean
 * \brief ResourceBean class that extends from the \a NameIdBean  and that represents a resource within the system
 **/
public class ResourceBean extends NameIdBean{

    private String owner;
    private String description;
    private LocationBean location;
    private List<String> observedProperties = new ArrayList<String>();
    private String resourceURL;

	//! Get the owner of a resource
    public String getOwner() {
        return owner;
    }

	//! Set the owner of a resource
    public void setOwner(String owner) {
        this.owner = owner;
    }

	//! Get the description of a resource
    public String getDescription() {
        return description;
    }

	//! Set the description of a resource
    public void setDescription(String description) {
        this.description = description;
    }

	//! Get the location of a resource
    public LocationBean getLocation() {
        return location;
    }

	//! Set the location of a resource
    public void setLocation(LocationBean location) {
        this.location = location;
    }

	//! Get the list of observed properties by a resource
    public List<String> getObservedProperties() {
        return observedProperties;
    }

	//! Set the list of observed properties by a resource
    public void setObservedProperties(List<String> observedProperties) {
        this.observedProperties = observedProperties;
    }

	//! Get the url form a resource
    public String getResourceURL() {
        return resourceURL;
    }

	//! Set the url form a resource
    public void setResourceURL(String resourceURL) {
        this.resourceURL = resourceURL;
    }
}
