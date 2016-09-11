/*
 * Copyright © 2016 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package scheduling.api;

public class FlowSpec {
	
	private String srcIp = null;
	private String dstIp = null;
	private String srcPort = null;
	private String dstPort = null;
	private String protocol = null;

	FlowSpec(String srcIp, String dstIp, String srcPort, String dstPort, String protocol) {
		this.srcIp = srcIp;
		this.dstIp = dstIp;
		this.srcPort = srcPort;
		this.dstPort = dstPort;
		this.protocol = protocol;
	}

	public String toString() {
		String string = new String();
		string = srcIp + "," + dstIp + "," + srcPort + "," + dstPort + "," + protocol;
		return string;
	}

	public String getSrcIp() {
		return srcIp;
	}

	public String getDstIp() {
		return dstIp;
	}

	public String getSrcPort() {
		return srcPort;
	}

	public String getDstPort() {
		return dstPort;
	}

	public String getProtocol() {
		return protocol;
	}
}
