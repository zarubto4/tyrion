package utilities._AAA_printer.printNodeModels;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.Valid;

/**
 * Created by zaruba on 23.08.17.
 */

public class Computer {

    public Computer(){}

    @JsonIgnore public int id;
    public String name = null;
    public String inet = null;
    public String inet6 = null;
    public String hostname = null;
    public String version = null;
    public String jre = null;
    public String createTimestamp = null;
    public String state = null;


}
