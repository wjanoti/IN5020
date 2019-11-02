package example.gossip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;


/**
 * @author Lucas Provensi
 * 
 * Basic Shuffling protocol template
 * 
 * The basic shuffling algorithm, introduced by Stavrou et al in the paper: 
 * "A Lightweight, Robust P2P System to Handle Flash Crowds", is a simple 
 * peer-to-peer communication model. It forms an overlay and keeps it 
 * connected by means of an epidemic algorithm. The protocol is extremely 
 * simple: each peer knows a small, continuously changing set of other peers, 
 * called its neighbors, and occasionally contacts a random one to exchange 
 * some of their neighbors.
 * 
 * This class is a template with instructions of how to implement the shuffling
 * algorithm in PeerSim.
 * Should make use of the classes Entry and GossipMessage:
 *    Entry - Is an entry in the cache, contains a reference to a neighbor node
 *  		  and a reference to the last node this entry was sent to.
 *    GossipMessage - The message used by the protocol. It can be a shuffle
 *    		  request, reply or reject message. It contains the originating
 *    		  node and the shuffle list.
 *
 */
public class BasicShuffle  implements Linkable, EDProtocol, CDProtocol{
	
	private static final String PAR_CACHE = "cacheSize";
	private static final String PAR_L = "shuffleLength";
	private static final String PAR_TRANSPORT = "transport";

	private final int tid;

	// The list of neighbors known by this node, or the cache.
	private List<Entry> cache;
	
	// The maximum size of the cache;
	private final int size;
	
	// The maximum length of the shuffle exchange;
	private final int l;

	// field indicating if this node is waiting for a response
    private boolean waitingForResponse;

    private Entry neighbour;

    /**
	 * Constructor that initializes the relevant simulation parameters and
	 * other class variables.
	 * 
	 * @param n simulation parameters
	 */
	public BasicShuffle(String n)
	{
		this.size = Configuration.getInt(n + "." + PAR_CACHE);
		this.l = Configuration.getInt(n + "." + PAR_L);
		this.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);

		cache = new ArrayList<Entry>(size);
	}

	/* START YOUR IMPLEMENTATION FROM HERE
	 * 
	 * The simulator engine calls the method nextCycle once every cycle 
	 * (specified in time units in the simulation script) for all the nodes.
	 * 
	 * You can assume that a node initiates a shuffling operation every cycle.
	 * 
	 * @see peersim.cdsim.CDProtocol#nextCycle(peersim.core.Node, int)
	 */
	@Override
	public void nextCycle(Node node, int protocolID) {
		// Implement the shuffling protocol using the following steps (or
		// you can design a similar algorithm):
		// Let's name this node as P
        BasicShuffle P = (BasicShuffle) node.getProtocol(protocolID);
        if (P == null) {
            System.out.println("wrong protocol");
            return;
        }
		// 1. If P is waiting for a response from a shuffling operation initiated in a previous cycle, return;
        if (P.waitingForResponse) {
            return;
        }
		// 2. If P's cache is empty, return;
        if (P.cache.isEmpty()) {
            return;
        }
		// 3. Select a random neighbor (named Q) from P's cache to initiate the shuffling;
		//	  - You should use the simulator's common random source to produce a random number: CommonState.r.nextInt(cache.size())

        int neighbourIndex = CommonState.r.nextInt(P.cache.size());
        Entry Q = P.cache.get(neighbourIndex);
        neighbour = Q;

		// 4. If P's cache is full, remove Q from the cache;
        if (P.cache.size() == this.size) {
            P.cache.remove(neighbourIndex);
        }
		// 5. Select a subset of other l - 1 random neighbors from P's cache;
		//	  - l is the length of the shuffle exchange
		//    - Do not add Q to this subset
        List<Entry> subset = P.GetSubsetExcluding(Q.getNode(), l - 1);
		// 6. Add P to the subset;
        subset.add(new Entry(node));
		// 7. Send a shuffle request to Q containing the subset;
		//	  - Keep track of the nodes sent to Q
		//	  - Example code for sending a message:
		//
		// GossipMessage message = new GossipMessage(node, subset);
		// message.setType(MessageType.SHUFFLE_REQUEST);
		// Transport tr = (Transport) node.getProtocol(tid);
		// tr.send(node, Q.getNode(), message, protocolID);
         GossipMessage message = new GossipMessage(node, subset);
         message.setType(MessageType.SHUFFLE_REQUEST);
         Transport tr = (Transport) node.getProtocol(tid);
         tr.send(node, Q.getNode(), message, protocolID);
         subset.forEach(entry -> {entry.setSentTo(Q.getNode());});
		// 8. From this point on P is waiting for Q's response and will not initiate a new shuffle operation;
        P.waitingForResponse = true;
		// The response from Q will be handled by the method processEvent.
		
	}

    /**
     * Returns a subset of the current cache, excluding the entryx.
     * The subset will contain count entries.
     * If neighbourIndex is -1, no neighbour will be excluded from the draw
     * @param entryToExclude
     * @param count
     * @return
     */
    private List<Entry> GetSubsetExcluding(Node entryToExclude, int count) {
        List<Entry> copy = new ArrayList<>(this.cache);

        Collections.shuffle(copy, CommonState.r);

        return copy.stream()
                .filter(entry -> entry.getNode().getID() != entryToExclude.getID())
                .limit(count)
                .collect(Collectors.toList());
    }

    /* The simulator engine calls the method processEvent at the specific time unit that an event occurs in the simulation.
	 * It is not called periodically as the nextCycle method.
	 * 
	 * You should implement the handling of the messages received by this node in this method.
	 * 
	 * @see peersim.edsim.EDProtocol#processEvent(peersim.core.Node, int, java.lang.Object)
	 */
	@Override
	public void processEvent(Node node, int pid, Object event) {
		// Let's name this node as Q;
		// Q receives a message from P;
		//	  - Cast the event object to a message:
		GossipMessage message = (GossipMessage) event;

		// P is the node that sent the message
		BasicShuffle P = (BasicShuffle) message.getNode().getProtocol(pid);
		BasicShuffle Q = (BasicShuffle) node.getProtocol(pid);

		switch (message.getType()) {
		// If the message is a shuffle request:
		case SHUFFLE_REQUEST:
		    //	  1. If Q is waiting for a response from a shuffling initiated in a previous cycle, send back to P a message rejecting the shuffle request;
            if (Q.waitingForResponse) {
                GossipMessage outboundMessage = new GossipMessage(node, new ArrayList<>());
                outboundMessage.setType(MessageType.SHUFFLE_REJECTED);
                Transport tr = (Transport) node.getProtocol(tid);
                tr.send(node, message.getNode(), outboundMessage, pid);
                return;
            }
            //	  2. Q selects a random subset of size l of its own neighbors;
            List<Entry> subset = Q.GetSubsetExcluding(message.getNode(), l);
            //	  3. Q reply P's shuffle request by sending back its own subset;
            GossipMessage outboundMessage = new GossipMessage(node, subset);
            outboundMessage.setType(MessageType.SHUFFLE_REPLY);
            Transport tr = (Transport) node.getProtocol(tid);
            tr.send(node, message.getNode(), outboundMessage, pid);
            subset.forEach(entry -> {entry.setSentTo(message.getNode());});
            //	  4. Q updates its cache to include the neighbors sent by P:
            //		 - No neighbor appears twice in the cache
            //		 - Use empty cache slots to add the new entries
            //		 - If the cache is full, you can replace entries among the ones sent to P with the new ones
            Q.mergeShuffleList(message.getShuffleList(), message.getNode());
			break;
		
		// If the message is a shuffle reply:
		case SHUFFLE_REPLY:
		//	  1. In this case Q initiated a shuffle with P and is receiving a response containing a subset of P's neighbors
            List<Entry> shuffleList = message.getShuffleList();
		//	  2. Q updates its cache to include the neighbors sent by P:
            Q.mergeShuffleList(shuffleList, message.getNode());
		//	  3. Q is no longer waiting for a shuffle reply;
            Q.waitingForResponse = false;
			break;
		
		// If the message is a shuffle rejection:
		case SHUFFLE_REJECTED:
		//	  1. If P was originally removed from Q's cache, add it again to the cache.
            if (!Q.contains(Q.neighbour.getNode())) {
                Q.addNeighbor(Q.neighbour.getNode());
            }
		//	  2. Q is no longer waiting for a shuffle reply;
            Q.waitingForResponse = false;
			break;
			
		default:
			break;
		}
		
	}

    private void mergeShuffleList(List<Entry> shuffleList, Node sendingNode) {
	    List<Entry> removalCandidates = this.cache.stream()
                .filter(e -> e.getSentTo() != null && e.getSentTo().getID() == sendingNode.getID())
                .collect(Collectors.toList());
	    int removalIndex = 0;
        for (Entry entry :
                shuffleList) {
            if (this.contains(entry.getNode()))
            {
                continue;
            }
            if (this.cache.size() < size) {
                this.addNeighbor(entry.getNode());
            }
            else {
                if (removalIndex < removalCandidates.size()) {
                    this.cache.remove(removalCandidates.get(removalIndex));
                    this.addNeighbor(entry.getNode());
                    removalIndex++;
                }
                else {
                    return;
                }
            }
        }
        //		 - No neighbor appears twice in the cache
        //		 - Use empty cache slots to add new entries
        //		 - If the cache is full, you can replace entries among the ones originally sent to P with the new ones
    }

    /* The following methods are used only by the simulator and don't need to be changed */
	
	@Override
	public int degree() {
		return cache.size();
	}

	@Override
	public Node getNeighbor(int i) {
		return cache.get(i).getNode();
	}

	@Override
	public boolean addNeighbor(Node neighbour) {
		if (contains(neighbour))
			return false;

		if (cache.size() >= size)
			return false;

		Entry entry = new Entry(neighbour);
		cache.add(entry);

		return true;
	}

	@Override
	public boolean contains(Node neighbor) {
		return cache.contains(new Entry(neighbor));
	}

	public Object clone()
	{
		BasicShuffle gossip = null;
		try { 
			gossip = (BasicShuffle) super.clone(); 
		} catch( CloneNotSupportedException e ) {
			
		} 
		gossip.cache = new ArrayList<Entry>();

		return gossip;
	}

	@Override
	public void onKill() {
		// TODO Auto-generated method stub		
	}

	@Override
	public void pack() {
		// TODO Auto-generated method stub	
	}
}
