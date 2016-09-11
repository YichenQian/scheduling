/*
 * Copyright © 2016 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package scheduling.scheduling;

import fast.api.FastDataStore;
import fast.api.FastFunction;
import scheduling.api.FlowSpec;
import scpsolver.constraints.LinearSmallerThanEqualsConstraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LinearProgram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RSAFastFunction implements FastFunction {
	private final static Logger LOG = LoggerFactory.getLogger(RSAFastFunction.class);
	private FastDataStore fastDataStore = null;
	private List<FlowSpec> flowList = null;
	private Map<Link, LinkSim> linkMap = new HashMap<>();
	private Map<String, List<Link>> flowPathMap = new HashMap<>();
	private Map<LinkSim, List<String>> linkPathMap = new HashMap<>();
	private Map<String, List<LinkSim>> pathLinkMap = new HashMap<>();
	private static final TopologyKey topologyKey = new TopologyKey(new TopologyId("flow:1"));

	public RSAFastFunction(List<FlowSpec> flowList) {
		this.flowList = flowList;
	}

	private class LinkSim {
		public String id;
		public String src;
		public String dst;
		public int hopcount;
		public int bandwidth;

		public LinkSim(String id, String src, String dst, int hopcount, int bandwidth) {
			this.id = id;
			this.src = src;
			this.dst = dst;
			this.hopcount = hopcount;
			this.bandwidth = bandwidth;
		}
	}

	@Override
	public void init(FastDataStore dataStore) {
		this.fastDataStore = dataStore;
	}

	// private Topology getTopology() {
	// LOG.info("getTopology started");
	// InstanceIdentifier<Topology> topologyIID =
	// InstanceIdentifier.builder(NetworkTopology.class)
	// .child(Topology.class, topologyKey).build();
	// Topology topology = null;
	// try {
	// topology = fastDataStore.read(LogicalDatastoreType.OPERATIONAL,
	// topologyIID);
	// NodeId nodeid = new NodeId("123");
	// Link link = topology.getLink().get(nodeid);
	// Metric metric = link.
	// } catch (ReadFailedException e) {
	// e.printStackTrace();
	// }
	//
	// return topology;
	// }

	public void RSA() {
		getFlowSetPath();
		preprocessing();
		aggregationOnly();
		Set<LinkSim> minimal = mecs();
		
		// writeToDatastore(minimal);
	}

	private void getFlowSetPath() {

	}

	private void preprocessing() {

		// construct linkPathMap
		for (FlowSpec flow : flowList) {
			List<LinkSim> list = new ArrayList<>();
			for (Link l : flowPathMap.get(flow)) {
				list.add(new LinkSim(l.getLinkId().toString(), l.getSource().toString(), l.getDestination().toString(),
						1, 1));
			}
			pathLinkMap.put(flow.toString(), list);
		}

		for (String pathID : pathLinkMap.keySet()) {
			List<LinkSim> path = pathLinkMap.get(pathID);
			for (LinkSim link : path) {
				List<String> _paths = null;
				if (!linkPathMap.containsKey(link)) {
					_paths = new ArrayList<>();
					_paths.add(pathID);
				} else {
					_paths = linkPathMap.get(link);
					_paths.add(pathID);
				}
				linkPathMap.put(link, _paths);
			}
		}
	}

	private void aggregate(LinkSim useful, LinkSim redundant) {
		// hop count
		useful.hopcount += redundant.hopcount;
		// bandwidth
		useful.bandwidth = useful.bandwidth < redundant.bandwidth ? useful.bandwidth : redundant.bandwidth;
		// other attribute(delay, loss rate)
	}

	// can be optimized，convert to integer，sort
	private void aggregationOnly() {
		Set<LinkSim> redundant = new HashSet<>();
		LinkSim[] keys = linkPathMap.keySet().toArray(new LinkSim[linkPathMap.size()]);

		for (int i = 0; i < keys.length; i++) {
			LinkSim link1 = keys[i];
			if (redundant.contains(link1))
				continue;
			for (int j = i + 1; j < keys.length; j++) {

				LinkSim link2 = keys[j];
				if (redundant.contains(link2))
					continue;
				// no need bandwidth
				if (linkPathMap.get(link1).equals(linkPathMap.get(link2))) {
					redundant.add(link2);
					aggregate(link1, link2);
				}
			}
		}

		// delete redundant links in pathLinkMap
		for (String pathID : pathLinkMap.keySet()) {
			List<LinkSim> links = pathLinkMap.get(pathID);
			links.removeAll(redundant);
		}

		// delete redundant links in linkPathMap
		for (LinkSim link : redundant) {
			linkPathMap.remove(link);
		}
	}

	private Set<LinkSim> mecs() {
		Set<LinkSim> minimal = new HashSet<>();
		Set<LinkSim> constraints = new HashSet<>();
		constraints.addAll(linkPathMap.keySet());
		Set<LinkSim> toAggregate = new HashSet<>();
		for (LinkSim link : linkPathMap.keySet()) {
			int y = solveLP(constraints, link);
			if (link.bandwidth < y) {
				minimal.add(link);
			} else {
				toAggregate.add(link);
			}
		}
		if (minimal.size() > 0) {
			for (LinkSim link : toAggregate) {
				LinkSim useful = minimal.iterator().next();
				aggregate(useful, link);
			}
		}
		return minimal;
	}

	private int solveLP(Set<LinkSim> constraints, LinkSim minus) {

		// set objective coef
		double[] objective = new double[pathLinkMap.keySet().size()];
		int i = 0;
		for (List<LinkSim> path : pathLinkMap.values()) {
			if (path.contains(minus)) {
				objective[i] = 1;
			} else {
				objective[i] = 0;
			}
			i++;
		}

		LinearProgram lp = new LinearProgram(objective);
		lp.setMinProblem(false);
		double[] upperBound = new double[pathLinkMap.keySet().size()];
		for (i = 0; i < upperBound.length; i++) {
			upperBound[i] = Double.MAX_VALUE;
		}
		lp.setUpperbound(upperBound);
		LinearProgramSolver solver = SolverFactory.newDefault();

		int constraintNum = 0;
		for (LinkSim link : constraints) {
			if (link == minus)
				continue;
			double[] variable = new double[pathLinkMap.keySet().size()];
			i = 0;
			for (String pathID : pathLinkMap.keySet()) {
				if (linkPathMap.get(link).contains(pathID)) {
					variable[i] = 1;
				} else {
					variable[i] = 0;
				}
				i++;
			}
			lp.addConstraint(
					new LinearSmallerThanEqualsConstraint(variable, link.bandwidth, "c" + constraintNum++));

		}
		double[] sol = solver.solve(lp);
		int result = 0;
		for (double bw : sol)
			result += bw;
		if (result == 0)
			return Integer.MAX_VALUE;
		return result;
	}

	// private void writeToDatastore(Set<LinkSim> minimal) {
	// InstanceIdentifier<Graph> iid =
	// InstanceIdentifier.builder(Graph.class).build();
	// fastDataStore.delete(LogicalDatastoreType.CONFIGURATION, iid);
	// for (LinkSim link : minimal) {
	// InstanceIdentifier<Link> linkIID =
	// InstanceIdentifier.builder(Graph.class)
	// .child(Link.class, new LinkKey(link.id)).build();
	//
	// LinkBuilder linkBuilder = new LinkBuilder();
	// linkBuilder.setLinkId(link.id);
	// linkBuilder.setKey(new LinkKey(link.id));
	// linkBuilder.setSource(link.src);
	// linkBuilder.setDestination(link.dst);
	//
	// List<Metric> metrics = new ArrayList<>();
	// MetricBuilder bandwidthBuilder = new MetricBuilder();
	// bandwidthBuilder.setMetricName("bandwidth");
	// bandwidthBuilder.setKey(new MetricKey("bandwidth"));
	// bandwidthBuilder.setMetricValue(BigInteger.valueOf(link.bandwidth));
	// metrics.add(bandwidthBuilder.build());
	//
	// MetricBuilder hopcountBuilder = new MetricBuilder();
	// hopcountBuilder.setMetricName("hopcount");
	// hopcountBuilder.setKey(new MetricKey("hopcount"));
	// hopcountBuilder.setMetricValue(BigInteger.valueOf(link.hopcount));
	// metrics.add(hopcountBuilder.build());
	// linkBuilder.setMetric(metrics);
	// fastDataStore.put(LogicalDatastoreType.CONFIGURATION, linkIID,
	// linkBuilder.build(), true);
	// }
	// }

	@Override
	public void run() {
		RSA();
	}

}
