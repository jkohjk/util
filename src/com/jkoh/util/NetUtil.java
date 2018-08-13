package com.jkoh.util;

import java.net.InetAddress;
import java.util.*;

public class NetUtil {
	public static String getHostname() {
		String hostname;
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			Map<String, String> env = System.getenv();
			if(env.containsKey("HOSTNAME")) {
				hostname = env.get("HOSTNAME");
			} else if(env.containsKey("COMPUTERNAME")) {
				hostname = env.get("COMPUTERNAME");
			} else {
				hostname = UUID.randomUUID().toString();
			}
		}
		return hostname;
	}
}