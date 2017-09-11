package utilities.lablel_printer_service.printNodeModels;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by zaruba on 23.08.17.
 */
public class PrinterOption {
  
    public PrinterOption() {}

    @JsonInclude(JsonInclude.Include.NON_NULL)  public String bin;
    @JsonInclude(JsonInclude.Include.NON_NULL) public Boolean collate;
    @JsonInclude(JsonInclude.Include.NON_NULL) public int copies = 1;
    @JsonInclude(JsonInclude.Include.NON_NULL) public String dpi;
    @JsonInclude(JsonInclude.Include.NON_NULL) public String duplex;
    @JsonInclude(JsonInclude.Include.NON_NULL) public Boolean fitToPage;
    @JsonInclude(JsonInclude.Include.NON_NULL) public String media;
    @JsonInclude(JsonInclude.Include.NON_NULL) public int nup = 1;
    @JsonInclude(JsonInclude.Include.NON_NULL) public String pages;
    @JsonInclude(JsonInclude.Include.NON_NULL) public String paper;
    @JsonInclude(JsonInclude.Include.NON_NULL) public int rotate = 0;
    
}
