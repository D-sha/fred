package freenet.node;

import freenet.support.Logger;

/**
 * Track average round-trip time for each peer node, get a geometric mean.
 */
public class NodePinger implements Runnable {

	private double meanPing = 0;
	
	NodePinger(Node n) {
		this.node = n;
		Thread t = new Thread(this, "Node pinger");
		t.setDaemon(true);
		t.start();
	}
	
	final Node node;
	
	public void run() {
		while(true) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// Ignore
			}
			recalculateMean(node.peers.connectedPeers);
		}
	}

	/** Recalculate the mean ping time */
	void recalculateMean(PeerNode[] peers) {
		int peerCount = 0;
		double total = 1.0;
		for(int i=0;i<peers.length;i++) {
			PeerNode peer = peers[i];
			if(!peer.isConnected()) continue;
			peerCount++;
			double avgPingTime = peer.averagePingTime();
			Logger.minor(this, "Peer: "+peer.getPeer()+", avgPingTime: "+avgPingTime);
			total *= avgPingTime;
		}
		if(peerCount > 0) {
			total = Math.pow(total, 1.0 / peerCount);
			meanPing = total;
			Logger.minor(this, "Mean ping: "+meanPing+"ms");
		}
	}

	public double averagePingTime() {
		return meanPing;
	}
}
