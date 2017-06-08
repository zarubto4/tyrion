package utilities.financial.history;

import play.data.validation.Constraints;

import java.util.ArrayList;
import java.util.List;

/**
 * History class serves as a container for financial history events, that happened on the product.
 */
public class History {

    /**
     * Last spending of credit
     */
    public Long last_spending;

    /**
     * List of HistoryEvents
     */
    @Constraints.Required
    public List<HistoryEvent> history = new ArrayList<>();
}
