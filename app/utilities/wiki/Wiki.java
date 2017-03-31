package utilities.wiki;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Wiki {

    public static List<Element> getWikiFiles(){

        File[] files = new File(System.getProperty("user.dir") + "/conf/markdown_documentation").listFiles();


        List<Element> tree = new ArrayList<>();


        for (File file : files) {

            if(!file.isDirectory() && !file.getPath().contains(".markdown")) continue;

            Element element = new Element();
            element.file_name = file.getName();
            element.file_path = file.getName();
            element.file = file;

            tree.add(element);

            if(file.isDirectory()){
                element.find_all_elements();
            }
        }

        return tree;
    }


    public static class Element {

        public String file_name;
        public String file_path;
        public File file;
        public List<Element> children = new ArrayList<>();

        public void find_all_elements(){

            File[] files = new File( file.getPath() ).listFiles();

            for (File file : files) {

                Element element = new Element();
                element.file_name = file.getName();
                element.file_path = file_path + "/" + file.getName();
                element.file = file;

                children.add(element);

                if(file.isDirectory()){
                    element.find_all_elements();
                }
            }

        }
    }


}
