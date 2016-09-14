/*
 * Copyright © 2016 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package scheduling.scheduling;

import java.io.WriteAbortedException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.fast.app.scheduling.rev160902.FlowSetPath;
import org.opendaylight.yang.gen.v1.urn.fast.app.scheduling.rev160902.FlowSetPathBuilder;
import org.opendaylight.yang.gen.v1.urn.fast.app.scheduling.rev160902.FlowSetPathKey;
import org.opendaylight.yang.gen.v1.urn.fast.app.scheduling.rev160902.flow.set.path.FlowSpec;
import org.opendaylight.yang.gen.v1.urn.fast.app.scheduling.rev160902.flow.set.path.Path;
import org.opendaylight.yang.gen.v1.urn.fast.app.scheduling.rev160902.flow.set.path.PathBuilder;
import org.opendaylight.yang.gen.v1.urn.fast.app.scheduling.rev160902.flow.set.path.path.LinkSpec;
import org.opendaylight.yang.gen.v1.urn.fast.app.scheduling.rev160902.flow.set.path.path.LinkSpecBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fast.api.FastDataStore;
import fast.api.FastFunction;

public class PathComputeFastFunction implements FastFunction {
	private final static Logger LOG = LoggerFactory.getLogger(PathComputeFastFunction.class);
	private FastDataStore fastDataStore = null;
	private FlowSpec flow = null;
	private static final TopologyKey topologyKey = new TopologyKey(new TopologyId("flow:1"));
	private static final List<LinkId> EMPTY_LIST = new LinkedList<LinkId>();

	@Override
	public void init(FastDataStore dataStore) {
		this.fastDataStore = dataStore;
	}

	public PathComputeFastFunction(FlowSpec flow) {
		this.flow = flow;
	}

	public class Compare implements Comparator<Map.Entry<NodeId, Integer>> {
		@Override
		public int compare(Map.Entry<NodeId, Integer> a, Map.Entry<NodeId, Integer> b) {
			return a.getValue().compareTo(b.getValue());
		}

		@Override
		public boolean equals(Object obj) {
			return (obj == this);
		}
	}

	@Override
	public void run() {
		LOG.info("run started");
		Topology topology = getTopology();
		FlowSetPath flowSetPath = computePathForFlow(topology);
		writeToDataStore(flowSetPath);
	}

	private Topology getTopology() {
		LOG.info("getTopology started");
		InstanceIdentifier<Topology> topologyIID = InstanceIdentifier.builder(NetworkTopology.class)
				.child(Topology.class, topologyKey).build();
		Topology topology = null;
		try {
			topology = fastDataStore.read(LogicalDatastoreType.OPERATIONAL, topologyIID);
		} catch (ReadFailedException e) {
			e.printStackTrace();
		}

		return topology;
	}

	// compute the path of the flow
	public FlowSetPath computePathForFlow(Topology topology) {
		if (topology == null) {
			LOG.info("topology null");
			return null;
		}
		LOG.info("start to compute the path for flow");
		List<Link> path = shortestPath(topology, flow.getSrcIp(), flow.getDstIp());
		System.out.println("Path computing result:" + path.get(0).getLinkId().toString() + ", "
				+ path.get(1).getLinkId().toString());
		FlowSetPath flowSetPath = Path2FlowSetPath(path);
		return flowSetPath;
	}

	public FlowSetPath Path2FlowSetPath(List<Link> path) {
		List<LinkSpec> linkList = new ArrayList<>();
		for (Link l : path) {
			LinkSpecBuilder lsb = new LinkSpecBuilder();
			lsb.setDestination(l.getDestination().toString());
			lsb.setLinkId(l.getLinkId().toString());
			lsb.setSource(l.getSource().toString());
			LinkSpec ls = lsb.build();
			linkList.add(ls);
		}
		PathBuilder pathBuilder = new PathBuilder();
		pathBuilder.setLinkSpec(linkList);
		Path p = pathBuilder.build();
		FlowSetPathBuilder flowSetPathBuilder = new FlowSetPathBuilder();
		flowSetPathBuilder.setFlowSpec(flow);
		flowSetPathBuilder.setPath(p);
		FlowSetPath flowSetPath = flowSetPathBuilder.build();
		return flowSetPath;
	}

	// compute the shortest path according to hop count
	public List<Link> shortestPath(Topology topology, String srcs, String dsts) {
		Node src = null;
		Node dst = null;

		if (topology == null || srcs == null || dsts == null)
			return null;

		LOG.info("shortest path started");

		List<Node> nodes = topology.getNode();
		List<Link> links = topology.getLink();

		Map<NodeId, List<LinkId>> graph = new HashMap<NodeId, List<LinkId>>();
		Map<NodeId, Node> nodeMap = new HashMap<NodeId, Node>();
		Map<LinkId, Link> linkMap = new HashMap<LinkId, Link>();

		for (Node node : nodes) {
			nodeMap.put(node.getNodeId(), node);
			// get source node
			if (node.getNodeId().getValue().equals(srcs)) {
				src = node;
			}
			// get destination node
			if (node.getNodeId().getValue().equals(dsts)) {
				dst = node;
			}
		}

		LOG.info("before get link");

		for (Link link : links) {
			linkMap.put(link.getLinkId(), link);
		}

		for (Link link : links) {
			NodeId source = link.getSource().getSourceNode();
			List<LinkId> edges = graph.getOrDefault(source, new LinkedList<LinkId>());
			edges.add(link.getLinkId());

			graph.put(source, edges);
		}

		LinkedList<Tuple> queue = new LinkedList<>();
		queue.addLast(new Tuple(src.getNodeId(), 0, null, null));
		while (!queue.isEmpty()) {
			Tuple tuple = queue.pollFirst();

			Node node = nodeMap.get(tuple.getNodeId());
			if (node.getNodeId().equals(dst.getNodeId())) {
				LinkedList<Link> path = new LinkedList<>();
				while (tuple.getPrevious() != null) {
					path.addFirst(linkMap.get(tuple.getLinkId()));

					tuple = tuple.getPrevious();
				}
				return path;
			}
			nodeMap.remove(node);
			/* remove visited nodes */

			int dist = tuple.getDistance();

			List<LinkId> edges = graph.getOrDefault(node.getNodeId(), EMPTY_LIST);
			for (LinkId linkId : edges) {
				Link link = linkMap.get(linkId);

				NodeId otherId = link.getDestination().getDestNode();
				Node other = nodeMap.get(otherId);
				if (other == null) {
					/* Already visited */
					continue;
				}

				queue.addLast(new Tuple(otherId, dist + 1, linkId, tuple));
			}
		}
		return null;
	}

	// TODO compute the shortest path according to bandwidth
	public List<Link> maxBandWidth(Topology topology, String srcs, String dsts) {

		if (topology == null || srcs == null || dsts == null)
			return null;
		List<Node> nodes = topology.getNode();
		List<Link> links = topology.getLink();

		Map<NodeId, List<LinkId>> graph = new HashMap<NodeId, List<LinkId>>();
		Map<NodeId, Node> nodeMap = new HashMap<NodeId, Node>();
		Map<LinkId, Link> linkMap = new HashMap<LinkId, Link>();

		Node src = null;
		Node dst = null;

		for (Node node : nodes) {
			nodeMap.put(node.getNodeId(), node);
			// get source node
			if (node.getNodeId().getValue().equals(srcs)) {
				src = node;
			}
			// get destination node
			if (node.getNodeId().getValue().equals(dsts)) {
				dst = node;
			}
		}

		for (Link link : links) {
			linkMap.put(link.getLinkId(), link);
		}

		for (Link link : links) {
			NodeId source = link.getSource().getSourceNode();

			List<LinkId> edges = graph.getOrDefault(source, new LinkedList<LinkId>());
			edges.add(link.getLinkId());

			graph.put(source, edges);
		}

		LinkedList<Tuple> queue = new LinkedList<>();
		queue.addLast(new Tuple(src.getNodeId(), 0, null, null));
		while (!queue.isEmpty()) {
			Tuple tuple = queue.pollFirst();

			Node node = nodeMap.get(tuple.getNodeId());
			if (node.getNodeId().equals(dst.getNodeId())) {
				LinkedList<Link> path = new LinkedList<>();
				while (tuple.getPrevious() != null) {
					path.addFirst(linkMap.get(tuple.getLinkId()));

					tuple = tuple.getPrevious();
				}
				return path;
			}
			nodeMap.remove(node);
			/* remove visited nodes */

			int dist = tuple.getDistance();

			List<LinkId> edges = graph.getOrDefault(node.getNodeId(), EMPTY_LIST);
			for (LinkId linkId : edges) {
				Link link = linkMap.get(linkId);

				NodeId otherId = link.getDestination().getDestNode();
				Node other = nodeMap.get(otherId);
				if (other == null) {
					/* Already visited */
					continue;
				}

				queue.addLast(new Tuple(otherId, dist + 1, linkId, tuple));
			}
		}
		return null;
	}

	private void writeToDataStore(FlowSetPath flowSetPath) {
		InstanceIdentifier<FlowSetPath> flowSetPathIID = InstanceIdentifier.builder(FlowSetPath.class,
				new FlowSetPathKey(flowSetPath.getId())).build();
		try {
			fastDataStore.put(LogicalDatastoreType.OPERATIONAL, flowSetPathIID, flowSetPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class Tuple {
		private NodeId nodeId = null;
		private int dist = Integer.MAX_VALUE;
		private LinkId linkId = null;
		private Tuple previous = null;

		Tuple(NodeId nodeId, int dist, LinkId linkId, Tuple previous) {
			this.nodeId = nodeId;
			this.dist = dist;
			this.linkId = linkId;
			this.previous = previous;
		}

		public NodeId getNodeId() {
			return nodeId;
		}

		public int getDistance() {
			return dist;
		}

		public LinkId getLinkId() {
			return linkId;
		}

		public Tuple getPrevious() {
			return previous;
		}
	}

}
