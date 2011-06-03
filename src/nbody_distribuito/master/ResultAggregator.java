package nbody_distribuito.master;

import java.util.List;

import nbody_distribuito.BodiesMap;
import nbody_distribuito.Body;

public class ResultAggregator {

    private BodiesMap bm;

    private static final int dimension = 2;
    // private float[][] acceleration;

    private int numBodies;
    private float deltaTime;
    private float deltaTimeSqr;

    /**
     * 
     * @param numBodies
     *            : numero totale di corpi dell'intera simulazione
     */
    public ResultAggregator(int numBodies, float deltaTime) {
	this.numBodies = numBodies;
	this.bm = new BodiesMap(numBodies);
	// this.acceleration = new float[numBodies][dimension];

	this.deltaTime = deltaTime;
	this.deltaTimeSqr = deltaTime * deltaTime;
    }

    /**
     * Questo metodo inizia a precalcolare parte dell'equazione del moto che
     * porterà al risultato finale con i dati dello step di computazione
     * precedente
     * */
    public void initialize(BodiesMap oldMap) {

	// Per tutti i corpi preleva dalla mappa la velocità e la posizione
	// dello step precedente
	for (int i = 0; i < numBodies; i++) {
	    Body b = oldMap.getBody(i);
	    float[] oldVel = b.getVelocity();
	    float[] oldPos = b.getPosition();

	    float[] position = new float[dimension];
	    float[] velocity = new float[dimension];
	    for (int j = 0; j < dimension; j++) {

		position[j] = oldVel[j] * deltaTime + oldPos[j];
		velocity[j] = oldVel[j]; // v0
		// acceleration[i][j] = 0;
	    }
	    b.setPosition(position);
	    b.setVelocity(velocity);
	    bm.addBody(b);
	}
    }

    /**
     * Ricevute le accelerazioni parziali calcolate dai worker, il metodo esegue
     * l'ultima fase della computazione. Per fare ciò utilizza delle variabili
     * in stile accumulatore.
     * */
    public void aggregate(JobResult res) {
	List<ClientResponse> resultList = res.getResultList();
	for (int i = 0; i < resultList.size(); i++) {
	    ClientResponse cr = resultList.get(i);
	    int id = cr.getBodyId();
	    float[] partialAcceleration = cr.getPartialAccelation();

	    Body b = bm.getBody(id);

	    float[] position = b.getPosition();
	    float[] velocity = b.getVelocity();

	    for (int j = 0; j < dimension; j++) {
		// a = SUM(ai);
		// acceleration[id][j] += partialAcceleration[j];

		// v = a * t
		velocity[j] += partialAcceleration[j] * deltaTime;
		position[j] += 0.5 * partialAcceleration[j] * deltaTimeSqr;
	    }
	    b.setVelocity(velocity);
	    b.setPosition(position);
	}
    }

    public BodiesMap getResultMap() {
	return bm;
    }
}
