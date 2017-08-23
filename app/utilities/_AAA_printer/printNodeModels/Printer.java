package utilities._AAA_printer.printNodeModels;

import com.printnode.api.*;

import javax.validation.Valid;

/**
 * Created by zaruba on 23.08.17.
 */
public class Printer {

    public Printer(){}

    private int id;
    private String name;
    private String description;
    private Capabilities capabilities;
    private String defaults;
    private String createTimestamp;
    private String state;

    @Valid public Computer computer;

}
