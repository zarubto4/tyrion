package utilities.swagger.swagger_diff_tools.servise_class;

import java.util.ArrayList;
import java.util.List;

public class Swagger_Diff {

    public String new_Version;
    public String old_Version;

    // Skupiny
    public List<String> add_groups = new ArrayList<>();
    public List<String> removed_groups = new ArrayList<>();


    // Paths
    public List<News> paths_new = new ArrayList<>();
    public List<Remws> paths_removes = new ArrayList<>();
    public List<Diffs> paths_diffs = new ArrayList<>();


    // Objekty
    public List<News> object_new = new ArrayList<>();
    public List<Remws> object_removes = new ArrayList<>();
    public List<Diffs> object_diffs = new ArrayList<>();







}

