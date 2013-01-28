package edu.washington.cs.cupid.usage.server.data;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class CupidUser implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String uuid;
	
	public CupidUser(String uuid) {
		this.uuid = uuid;
	}

	public String getUUID() {
		return uuid;
	}
}
