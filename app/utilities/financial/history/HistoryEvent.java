package utilities.financial.history;

/**
 * This class contains information about financial event.
 */
public class HistoryEvent {

    /**
     *  String date when event occurred.
     */
    public String date;

    /**
     * String name of event. (e.g. Credit Upload)
     */
    public String event;

    /**
     * String reason why event happened.
     */
    public String description;

    /**
     * String id of an related invoice if there is any.
     */
    public String invoice_id;
}
