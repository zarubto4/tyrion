package utilities.lablel_printer_service.printNodeModels;

import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import javax.validation.Valid;

/**
 * Created by zaruba on 23.08.17.
 */
public class Printer extends _Swagger_Abstract_Default {

    public Printer() {}

    public int id;
    public String name;
    public String description;
    private Capabilities capabilities;
    private String defaults;
    public String state;

    @Valid public Computer computer;

}
