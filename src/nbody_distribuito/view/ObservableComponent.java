package nbody_distribuito.view;

import java.util.ArrayList;

import nbody_distribuito.controller.ObserverInterface;
import nbody_distribuito.event.Event;

/**
 * Base class for representing passive components generating events.
 * 
 * @author aricci
 * 
 */
public class ObservableComponent {

    private ArrayList<ObserverInterface> observers;

    public ObservableComponent() {
	observers = new ArrayList<ObserverInterface>();
    }

    /**
     * Register an agent to perceive events generated by this component.
     * 
     * @param agent
     */
    public void register(ObserverInterface agent) {
	synchronized (observers) {
	    observers.add(agent);
	}
    }

    /**
     * Generate an event
     * 
     * @param ev
     */
    protected void notifyEvent(Event ev) {
	synchronized (observers) {
	    for (ObserverInterface ag : observers) {
		ag.notifyEvent(ev);
	    }
	}
    }

}
