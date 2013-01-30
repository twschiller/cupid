package edu.washington.cs.cupid.usage.server.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;


@Entity
public class CupidUser implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String uuid;
	
	@OneToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL, mappedBy="user")
	private List<CupidSession> sessions;
	
	public CupidUser(String uuid) {
		this.uuid = uuid;
		sessions = new ArrayList<CupidSession>();
	}

	public String getUUID() {
		return uuid;
	}
	
	public List<CupidSession> getSessions(){
		return sessions;
	}
	
	public void addSession(CupidSession session){
		if (sessions == null){
			sessions = new ArrayList<CupidSession>();
		}
		
		sessions.add(session);
	}
}
