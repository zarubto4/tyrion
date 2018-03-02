package utilities.lablel_printer_service.printNodeModels;

import java.util.HashMap;

/**
 * Created by zaruba on 23.08.17.
 */
public class Capabilities {

    public Capabilities() {}
    
    public String[] bins;
    public boolean collate;
    public int copies;
    public boolean color;
    public String[] dpis;
    public boolean duplex;
    public int[][] extent = new int[2][];
    public String[] medias;
    public int[] nup;
    public HashMap<String, int[]> papers;
    public boolean supports_custom_paper_size;

}
