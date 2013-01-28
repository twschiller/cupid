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
package edu.washington.cs.cupid.usage.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.Charset;
import java.util.List;

import org.eclipse.core.runtime.Platform;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import edu.washington.cs.cupid.usage.events.CupidEvent;
import edu.washington.cs.cupid.usage.events.SessionLog;
import edu.washington.cs.cupid.usage.events.SystemData;
import edu.washington.cs.cupid.usage.preferences.PreferenceConstants;

/**
 * Cupid plug-in data collector.
 * @author Todd Schiller
 */
public class CupidDataCollector {

	private Gson gson; 
	
	private File logDirectory;
	private File logFile;
	
	private boolean init = false;
	
	private	SystemData system;
	private List<CupidEvent> sessionLog;
	
	/**
	 * Construct the Cupid plug-in data collector.
	 */
	public CupidDataCollector(){
		gson = new Gson();
		logDirectory = Activator.getDefault().getStateLocation().toFile();	
	}
		
	/**
	 * Start the data collector; creates a new session file in the Eclipse user data location labeled with
	 * the time the session began.
	 * @throws Exception if initialization fails
	 */
	public synchronized void start() throws Exception {
		if (init) {
			throw new IllegalStateException("The Cupid data collector is already running");
		}
		
		long timestamp = System.currentTimeMillis();
		logFile = new File(logDirectory, "cupid-usage." + timestamp + ".json");	
		system = create(timestamp);
		sessionLog = Lists.newLinkedList();
		init = true;
	
		Activator.getDefault().logInformation("Cupid data collection started");
	}
	
	/**
	 * Stops the data collector, closing the session information.
	 * @throws Exception if stopping the data collector fails
	 */
	public synchronized void stop() throws Exception {
		try {
			if (init) {
				writeSession();
			}
		} finally {
			system = null;
			sessionLog = null;
			init = false;
		}
	}
	
	private synchronized void writeSession() throws IOException {
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(logFile), Charset.forName("UTF-8")));
		gson.toJson(new SessionLog(system, sessionLog), SessionLog.class, writer);
		writer.close();
		Activator.getDefault().logInformation("Wrote Cupid session log: " + logFile.getAbsolutePath());
	}
	
	public synchronized void deleteLocalData(){
		for (File file : logDirectory.listFiles()){
			if (file.getName().endsWith(".json")){
				file.delete();
			}
		}
		if (init){
			sessionLog.clear();
		}
	}
	
	public synchronized String getAllJson(String indent) throws IOException {
		List<SessionLog> logs = Lists.newArrayList();
		
		for (File file : logDirectory.listFiles()){
			if (file.getName().endsWith(".json")){
				JsonReader reader = new JsonReader(new FileReader(file));
				logs.add((SessionLog) gson.fromJson(reader, SessionLog.class));
				reader.close();			
			}
		}

		if (init){
			logs.add(new SessionLog(system, sessionLog));
		}
		
		StringWriter result = new StringWriter();
		JsonWriter writer = new JsonWriter(result);
		if (indent != null){
			writer.setIndent(indent);	
		}
		gson.toJson(logs, new TypeToken<List<SessionLog>>(){}.getType(), writer);
		writer.close();
		return result.toString();
	}
	
	
	/**
	 * Record an event in the Cupid session log. Does nothing if the data collector is not running.
	 * @param event the event to log
	 */
	public synchronized void record(final CupidEvent event) {
		if (init) {
			sessionLog.add(event);
		}
	}
	
	private static SystemData create(long timestamp){
		RuntimeMXBean RuntimemxBean = ManagementFactory.getRuntimeMXBean();
		
		return new SystemData(
				Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_UUID),
				timestamp,
				Platform.getNL(),
				Platform.getOS(),
				Platform.getOSArch(),
				Platform.getWS(),
				RuntimemxBean.getVmName(),
				RuntimemxBean.getVmVendor(),
				RuntimemxBean.getVmVersion());
	}
	
}
