package nbody_distribuito;

public class Constants {

    // Costanti per lo scambio di messaggi che conivolgono il Master TODO
    public final static String ASSOCIATE = "associate";
    public final static String ACK_ASSOCIATE = "ackAssociate";
    public final static String DISSOCIATE = "dissociate";
    public final static String ACK_DISSOCIATE = "ackDissociate";
    public final static String CLIENT_QUEUE = "clientQueue";
    public final static String CLIENT_QUEUE_RESP = "clientQueueResp";

    public final static String DO_JOB = "doJob";

    // Costanti per lo scambio di messaggi che coinvolgono il FlagActor
    public final static String IS_SET = "isSet";
    public final static String IS_SET_RESULT = "isSetResult";
    public final static String SET_FLAG = "setFlag";
    public final static String RESET_FLAG = "resetFlag";

    // COstanti per lo scambio di messaggi che coinvolgono gli eventi per la
    // gestione dell'avvio, dello stop e della pausa della computazione
    public final static String START_EVENT = "startEvent";
    public final static String STOP_EVENT = "stopEvent";
    public final static String PAUSE_EVENT = "pauseEvent";
    public final static String RANDOMIZE_EVENT = "randomizeEvent";
    public final static String CHANGE_PARAM = "changeParamEvent";

}
