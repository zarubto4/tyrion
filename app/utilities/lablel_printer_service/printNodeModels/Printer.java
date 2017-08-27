package utilities.lablel_printer_service.printNodeModels;

import javax.validation.Valid;

/**
 * Created by zaruba on 23.08.17.
 */
public class Printer {

    public Printer(){}

    public int id;
    public String name;
    public String description;
    private Capabilities capabilities;
    private String defaults;
    private String createTimestamp;
    public String state;

    @Valid public Computer computer;

}
