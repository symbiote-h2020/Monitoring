package eu.h2020.symbiote.beans;

import com.google.gson.annotations.Expose;

import org.springframework.data.annotation.Id;

/**
 * Created by jose on 27/09/16.
 */
/**! \class NameIdBean
 * \brief NameIdBean abstract class to be used in the objects handled by the component
 **/

public abstract class NameIdBean {
    @Id
    @Expose(serialize = false, deserialize = false)
    private String internalId;

    @Expose(serialize = false, deserialize = false)
    private String id;

    private String name;

	//! Get the internal id (id within the platform) of the bean
    public String getInternalId() {
        return internalId;
    }

	//! Sets the internal id (id within the platform) of the bean
    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }


	//! Gets the id (id within SymbIoTe) of the bean
    public String getId() {
        return id;
    }

	//! Sets the id (id within SymbIoTe) of the bean
    public void setId(String id) {
        this.id = id;
    }

	//! Gets the name of the bean
    public String getName() {
        return name;
    }

	//! Sets the name of the bean
    public void setName(String name) {
        this.name = name;
    }
}
