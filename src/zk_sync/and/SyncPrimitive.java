package zk_sync.and;

/******************************************************************************
	Baseado no código publicado em:
		Programming with ZooKeeper - A basic tutorial
		http://zookeeper.apache.org/doc/r3.1.2/zookeeperTutorial.html
		Escrito por: Flavio Junqueira (Ver: http://zookeeper.apache.org/doc/r3.1.2/zookeeperProgrammers.html)
	
	E com alterações propostas em:
		Programming with ZooKeeper - A quick tutorial
		https://cwiki.apache.org/confluence/display/ZOOKEEPER/Tutorial
		Por: Christian Spann
	
	Modificações:
		Anderson Coelho Weller
		Nov/2013
******************************************************************************/

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

public class SyncPrimitive implements Watcher {

	static ZooKeeper zk = null;
	static final Object mutex = new Object();
	
	String root;

	SyncPrimitive(String address) {
		if(zk == null){
			try {
				System.out.println("Starting ZK:");
				zk = new ZooKeeper(address, 3000, this);
				System.out.println("Finished starting ZK: " + zk);
			} catch (IOException e) {
				System.out.println(e.toString());
				zk = null;
			}
		}
	}

	public void process(WatchedEvent event) {
		synchronized (mutex) {
			mutex.notify();
		}
	}

	/**
	 * Barrier
	 */
	static public class Barrier extends SyncPrimitive {
		int size;
		String name;

		/**
		 * Barrier constructor
		 *
		 * @param address
		 * @param root
		 * @param size
		 */
		Barrier(String address, String root, int size) {
			super(address);
			this.root = root;
			this.size = size;

			// Create barrier node
			if (zk != null) {
				try {
					Stat s = zk.exists(root, false);
					if (s == null) {
						zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE,
								CreateMode.PERSISTENT);
					}
				} catch (KeeperException e) {
					System.out
							.println("Keeper exception when instantiating queue: "
									+ e.toString());
				} catch (InterruptedException e) {
					System.out.println("Interrupted exception");
				}
			}

			// My node name
			try {
				name = new String(InetAddress.getLocalHost().getCanonicalHostName().toString());
			} catch (UnknownHostException e) {
				System.out.println(e.toString());
			}

		}

		/**
		 * Join barrier
		 *
		 * @return
		 * @throws KeeperException
		 * @throws InterruptedException
		 */

		boolean enter() throws KeeperException, InterruptedException{
			zk.create(root + "/" + name, new byte[0], Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL_SEQUENTIAL);
			while (true) {
				synchronized (mutex) {
					List<String> list = zk.getChildren(root, true);
					if (list.size() < size) {
						mutex.wait();
					} else {
						return true;
					}
				}
			}
		}

		/**
		 * Wait until all reach barrier
		 *
		 * @return
		 * @throws KeeperException
		 * @throws InterruptedException
		 */

		boolean leave() throws KeeperException, InterruptedException{
			zk.delete(root + "/" + name, 0);
			while (true) {
				synchronized (mutex) {
					List<String> list = zk.getChildren(root, true);
						if (list.size() > 0) {
							System.out.println("*** Waiting barrier");
							mutex.wait();
						} else {
							System.out.println("*** Leave barrier");
							return true;
						}
					}
				}
		}
	}

	/**
	 * Producer-Consumer queue
	 */
	static public class Queue extends SyncPrimitive {

		/**
		 * Constructor of producer-consumer queue
		 *
		 * @param address
		 * @param name
		 */
		Queue(String address, String name) {
			super(address);
			this.root = name;
			// Create ZK node name
			if (zk != null) {
				try {
					Stat s = zk.exists(root, false);
					if (s == null) {
						zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE,
								CreateMode.PERSISTENT);
					}
				} catch (KeeperException e) {
					System.out
							.println("Keeper exception when instantiating queue: "
									+ e.toString());
				} catch (InterruptedException e) {
					System.out.println("Interrupted exception");
				}
			}
		}

		/**
		 * Add element to the queue.
		 *
		 * @param i
		 * @return
		 */
		boolean produce(int i) throws KeeperException, InterruptedException{
			ByteBuffer b = ByteBuffer.allocate(4);
			byte[] value;
			
			// Add child with value i
			b.putInt(i);
			value = b.array();
			zk.create(root + "/element", value, Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT_SEQUENTIAL);
						
			return true;
		}


		/**
		 * Remove first element from the queue.
		 *
		 * @return
		 * @throws KeeperException
		 * @throws InterruptedException
		 */
		int consume() throws KeeperException, InterruptedException{
			int retvalue = -1;
			Stat stat = null;
			
			// Get the first element available
			while (true) {
				synchronized (mutex) {
					List<String> list = zk.getChildren(root, true);
					if (list.isEmpty()) {
						System.out.println("Going to wait");
						mutex.wait();
					} else {
						String sMin = list.get(0);
						System.out.println("Lendo item: (" + sMin + ")");
						for(String s : list){
							// Utiliza a comparação Lexicográfica pois nome
							// é armazenado neste formato: element0000000011
							if (s.compareTo(sMin) < 0) sMin = s;
						}
						// Deleta o menor item (Se item já foi excluído é gerada uma exceção)
						System.out.println("Temporary value: " + root + "/" + sMin);
						byte[] b = zk.getData(root + "/" + sMin, false, stat);
						zk.delete(root + "/" + sMin, 0);
						
						ByteBuffer buffer = ByteBuffer.wrap(b);
						retvalue = buffer.getInt();
						
						return retvalue;
					}
				}
			}
		}
	}

	public static void main(String args[]) {
		if (args[0].equals("qTest"))
			queueTest(args);
		else if (args[0].equals("bTest"))
			barrierTest(args);
		else {
			System.out.println("---------------------------------------------------------------------------");
			System.out.println("Parametros de entrada:");
			System.out.println("[0]: qTest (para executar 'queueTest'), bTest (para executar 'barrierTest')");
			System.out.println("[1]: <host>:<port>,");
			System.out.println("[2]: <size> (Numero de itens da barreira; ou Total Produzir-Consumir)");
			System.out.println("[3]: p [para Producer]; c [para Consumer] (Somente para qTest)");
			System.out.println("---------------------------------------------------------------------------");
		}
	}

	public static void queueTest(String args[]) {
		Queue q = new Queue(args[1], "/app1");
		
		System.out.println("Input: " + args[1]);
		int i;
		Integer max = new Integer(args[2]);
		
		if (args[3].equals("p")) {
			System.out.println("Producer (Max = " + max + ")");
			for (i = 0; i < max; i++)
				try{
					q.produce(i);
					System.out.println("Produced Item: " + (i));
				} catch (KeeperException e){
				} catch (InterruptedException e){
				}
		} else {
			System.out.println("Consumer (Max = " + max + ")");
			for (i = 0; i < max; i++) {
				try{
					int r = q.consume();
					System.out.println("Item: " + r);
				} catch (KeeperException e){
					i--; // Retorna um item caso o Consumidor perca uma disputa
				} catch (InterruptedException e){
				}
			}
		}
	}

	public static void barrierTest(String args[]) {
		Barrier b = new Barrier(args[1], "/b1", new Integer(args[2]));
		try{
			boolean flag = b.enter();
			System.out.println("*** Entered barrier: " + args[2]);
			// Apresenta mensagem de que vai iniciar
			int tempoMax = 5;
			for (int tempo = 0; tempo < tempoMax; tempo++) {
				try {
					System.out.println("*** Start in "+ (tempoMax - tempo) +" seconds.");
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			if(!flag) System.out.println("Error when entering the barrier");
		} catch (KeeperException e){
		} catch (InterruptedException e){
		}
		
		// Generate random integer
		Random rand = new Random();
		int r = rand.nextInt(100);
		// Loop for rand iterations
		for (int i = 0; i < r; i++) {
			try {
				Thread.sleep(100);
				System.out.println("Do something: " + i + "-" + r);
			} catch (InterruptedException e) {
			}
		}
		try{
			b.leave();
		} catch (KeeperException e){
		} catch (InterruptedException e){
		}
		System.out.println("*** Left barrier");
	}
}
