
import java.util.concurrent.ArrayBlockingQueue;

import gui.NBodyView;
import nbody.BodiesMap;
import nbody.EventHandler;
import nbody.FrameRateUpdater;
import nbody.Master;
import nbody.StateMonitor;
import nbody.StateVariables;

public class Main {

    public static void main(String[] args) {

	NBodyView view = new NBodyView(600, 600);
	StateVariables var = new StateVariables();

	StateMonitor state = new StateMonitor();
	EventHandler c = new EventHandler(view,state,var);
	ArrayBlockingQueue<BodiesMap> mapQueue = new ArrayBlockingQueue<BodiesMap>(50, true);
	c.start();
	Master m = new Master(state,var,mapQueue);
	m.start();
	FrameRateUpdater u = new FrameRateUpdater(state,mapQueue);
	u.addListener(view);
	u.start();	
    }

}
