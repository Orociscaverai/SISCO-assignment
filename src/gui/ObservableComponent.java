package gui;

import java.util.ArrayList;

import nbody.ControllerAgent;
import nbody.event.Event;

/**
 * Base class for representing passive components generating events.
 * 
 * @author aricci
 * 
 */
public class ObservableComponent {

    private ArrayList<ControllerAgent> observers;

    public ObservableComponent() {
	observers = new ArrayList<ControllerAgent>();
    }

    /**
     * Register an agent to perceive events generated by this component.
     * 
     * @param agent
     */
    public void register(ControllerAgent agent) {
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
	    for (ControllerAgent ag : observers) {
		ag.notifyEvent(ev);
	    }
	}
    }

}
