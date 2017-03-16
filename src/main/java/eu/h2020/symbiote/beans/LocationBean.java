package eu.h2020.symbiote.beans;

/**
 * Created by jose on 26/09/16.
 */
/**! \class LocationBean
 * \brief LocationBean class used to specify the location of a \a ResourceBean
 **/
public class LocationBean {

    private String name;
    private String description;
    private Double longitude;
    private Double latitude;
    private Double altitude;

	//! Get the name of the location
    public String getName() {
        return name;
    }

	//! Set the name of the location
    public void setName(String name) {
        this.name = name;
    }

	//! Get the description of the location
    public String getDescription() {
        return description;
    }

	//! Set the description of the location
    public void setDescription(String description) {
        this.description = description;
    }

	//! Get the longitude of the location
    public Double getLongitude() {
        return longitude;
    }

	//! Set the longitude of the location
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

	//! Get the latitude of the location
    public Double getLatitude() {
        return latitude;
    }

	//! Set the latitude of the location
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

	//! Get the altitude of the location
    public Double getAltitude() {
        return altitude;
    }

	//! Set the altitude of the location
    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }
}
