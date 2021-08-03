package com.rav.bhaj.drools.entity;

import javax.persistence.Entity;

@Entity
public class SessionInfoDetails {

	@javax.persistence.Id
	private Long id;

	private long sessionId;

	private String session;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getSessionId() {
		return sessionId;
	}

	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}

}
