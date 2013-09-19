/*******************************************************************************
 * Copyright (c) 2013 Todd Schiller.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Todd Schiller - initial API, implementation, and documentation
 ******************************************************************************/
package edu.washington.cs.cupid.usage.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import edu.washington.cs.cupid.usage.server.data.CupidEvent;
import edu.washington.cs.cupid.usage.server.data.CupidSession;
import edu.washington.cs.cupid.usage.server.data.CupidUser;
import edu.washington.cs.cupid.usage.server.data.SystemData;
import edu.washington.cs.cupid.usage.server.json.JsonCupidEvent;
import edu.washington.cs.cupid.usage.server.json.JsonCupidSession;

@SuppressWarnings("serial")
public class CupidUsageDataServlet extends HttpServlet {
	
	private static final Logger log = Logger.getLogger(CupidUsageDataServlet.class.getName());
	
	private Gson gson = new Gson();
	
	private void writeSession(final JsonCupidSession raw){
		if (raw == null) throw new IllegalArgumentException("Received null session data from client");
		
		log.info("Received data for uuid " + raw.uuid);
		
		EntityManager em = EMFService.get().createEntityManager();
		if (em == null) throw new RuntimeException("Error loading entity manager");
		
		try{
			EntityTransaction tx = em.getTransaction();
			try {
				tx.begin();
				
				CupidUser user = em.find(CupidUser.class, raw.uuid);

				if (user == null){
					user = new CupidUser(raw.uuid);
				}
				SystemData system = new SystemData(
						raw.system.locale, raw.system.os, raw.system.osArch, raw.system.ws,
						raw.system.vmName, raw.system.vmVendor, raw.system.vmVersion,
						raw.system.bundles);

				CupidSession session = new CupidSession(user, system);
				for (JsonCupidEvent rawEvent : raw.events){
					session.addEvent(new CupidEvent(
							user, session,
							rawEvent.what, rawEvent.kind, rawEvent.data,
							rawEvent.bundleId, rawEvent.bundleVersion, rawEvent.when));
				}

				user.addSession(session);
				em.persist(user);
				
				tx.commit();
				
				log.info("Recorded data for uuid " + raw.uuid);
			} catch (RuntimeException ex){
				log.log(Level.WARNING, "Error recording data for uuid " + raw.uuid, ex);
				throw ex;
			} finally {
				if (tx.isActive()){
					tx.rollback();
				}
			}
		} finally {
			em.close();
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			BufferedReader reader = req.getReader();
			JsonCupidSession raw = gson.fromJson(reader, JsonCupidSession.class);
			writeSession(raw);
			resp.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception ex) {
			log.log(Level.WARNING, "Error recording Cupid session log", ex);
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed Cupid session log: " + ex.getLocalizedMessage());
		}
	}
}
