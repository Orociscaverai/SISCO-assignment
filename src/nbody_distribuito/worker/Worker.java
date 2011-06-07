package nbody_distribuito.worker;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import nbody_distribuito.Constants;
import nbody_distribuito.master.ClientData;
import nbody_distribuito.master.ClientResponse;
import nbody_distribuito.master.Job;
import pcd.actors.Actor;
import pcd.actors.Message;
import pcd.actors.Port;
import pcd.actors.filters.MsgFilter;
import pcd.actors.filters.MsgFilterImpl;

public class Worker extends Actor {

    private Port masterPort, computePort;
    private ExecutorService ex;
    private ExecutorCompletionService<ClientResponse> compServ;

    public Worker(String actorName, String serverName, String serverAddress, MsgFilter filter) {
	super(actorName);

	// TODO inserire il metodo per ottenere l'IP
	this.setLocalPort(new Port(actorName, "192.168.100.101"));
	masterPort = new Port(serverName, serverAddress);
    }

    private boolean associate() {
	Message m;
	// invio la richiesta per associarmi al Master
	try {
	    m = new Message(Constants.ASSOCIATE, this.getLocalPort());
	    log("Ho preparato il seguente messaggio: " + m.toString());
	    log("Lo invio a: " + masterPort.toString());
	    send(masterPort, m);

	    log("Invio messaggio: " + m);
	} catch (UnknownHostException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	// ricevo il messaggio di risposta
	m = receive();
	// il server mi manda la porta associata al compute Actor
	if (!m.getType().equalsIgnoreCase(Constants.ACK_ASSOCIATE)) {
	    log("Il master non ha apprezzato la mia associazione");
	    log("Il master ha risposto con: " + m.toString());
	    return false;
	}
	computePort = (Port) m.getArg(0);
	log("nuova porta = " + computePort.getActorName() + " " + computePort.getHostName());
	log("messaggio ricevuto, associaizione avvenuta " + m.toString());
	return true;
    }

    private void initPool() {
	int poolSize = Runtime.getRuntime().availableProcessors() * 3;
	ex = Executors.newFixedThreadPool(poolSize);
	this.compServ = new ExecutorCompletionService<ClientResponse>(ex);
    }

    private boolean init() {
	if (!associate()) {
	    return false;
	}
	initPool();
	return true;

    }

    private void shutdownAndReset() throws InterruptedException {
	ex.shutdownNow();
	ex.awaitTermination(2, TimeUnit.MINUTES);

	int debug = 0;
	while (compServ.poll() != null) {
	    debug++;
	}
	log("Numero di risultati già terminati: " + debug);

	initPool();
    }

    private List<ClientResponse> doCompute(Job job, float deltaTime, float softFactor) {
	int numBodies = job.getNumBodies();
	InteractionMatrix interactionMatrix = new InteractionMatrix(numBodies);
	int numTask = job.getNumTask();
	for (int i = 0; i < numTask; i++) {
	    ClientData[] nextData = job.getDataOfNextInteraction();
	    compServ.submit(new ComputeMutualAcceleration(nextData[0], nextData[1],
		    interactionMatrix, softFactor), null);
	    numTask++;
	}
	log("numTask = " + numTask);
	for (int n = 0; n < numTask; n++) {
	    try {
		compServ.take();
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	for (int i = 0; i < numBodies; i++) {
	    ClientData c = job.getData(i);
	    // TODO decidere dove scrivere i risultati, per ora non fa nulla..
	    compServ.submit(new ComputeNewPosition(c, deltaTime, interactionMatrix));
	}
	List<ClientResponse> response = new ArrayList<ClientResponse>(numBodies);
	for (int i = 0; i < numBodies; i++) {
	    try {
		ClientResponse cr = compServ.take().get();
		response.add(cr);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    } catch (ExecutionException e) {
		e.printStackTrace();
	    }
	}

	return response;
    }

    @Override
    public void run() {
	if (init()) {

	    Message m = receive();
	    if (m.getType().equals(Constants.DO_JOB)) {

		Job j = (Job) m.getArg(0);
		float deltaTime = (Float) m.getArg(1);
		float softFactor = (Float) m.getArg(2);
		log("finito step 1");
		List<ClientResponse> jr = doCompute(j, deltaTime, softFactor);
		log("sending JOB_RESULT to " + computePort.getActorName() + " "
			+ computePort.getHostName());
		try {
		    send(computePort, new Message(Constants.JOB_RESULT, jr));
		} catch (UnknownHostException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }

	}

	log("Termino spontaneamente l'esecuzione");
    }

    private boolean isStopped() {

	Port stopFlag = new Port("stopFlag");

	try {
	    send(stopFlag, new Message(Constants.IS_SET, getLocalPort()));
	} catch (UnknownHostException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	Message m = receive(new MsgFilterImpl(Constants.IS_SET_RESULT, 1));
	Boolean b = (Boolean) m.getArg(0);
	return b.booleanValue();
    }

    private void log(String msg) {
	System.out.println(getActorName() + ": " + msg);
    }

}