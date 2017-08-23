package utilities._AAA_printer.printNodeModels;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by zaruba on 23.08.17.
 */
public class PrintTask {

    public int printerId;
    public String title;
    public final String contentType = "pdf_base64";
    public String content;
    public String source;
    public int qty = 1;

    @JsonInclude(JsonInclude.Include.NON_NULL) public PrinterOption option;

}
