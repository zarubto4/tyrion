package utilities.financial.history;

import play.data.validation.Constraints;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * History class serves as a container for financial history events, that happened on the product.
 */
public class History {

    public History(){
        HistoryEvent event = new HistoryEvent();
        event.event = "New History";
        event.description = "History is created, probably new product.";
        event.date = new Date().toString();

        history.add(event);
    }

    /**
     * Last spending of credit
     */
    public Long last_spending = 0L;

    /**
     * Last spending of credit
     */
    public Long average_spending = 0L;

    /**
     * Last spending of credit
     */
    public Long mean_coefficient = 1L;

    /**
     * List of HistoryEvents
     */
    @Constraints.Required
    public List<HistoryEvent> history = new ArrayList<>();
}
