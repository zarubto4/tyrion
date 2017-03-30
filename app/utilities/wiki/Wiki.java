package utilities.wiki;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Wiki {

    public static List<Element> getWikiFiles(){



        List<String> fileNames = new ArrayList<>();



        File[] files = new File(System.getProperty("user.dir") + "/conf/markdown_documentation").listFiles();

        if (files != null) {
            for (File file : files) {
                fileNames.add(file.getName().substring(0, file.getName().lastIndexOf('.')));
            }
        }

        List<Element> tree = new ArrayList<>();

        for (String fileName : fileNames) {

            String[] steps = fileName.split("_");

            List<String> steps_list = new ArrayList<>();
            for (String step : steps) steps_list.add(step);

            tree = makeTree(tree, steps_list, fileName);
        }

        return tree;
    }

    public static class Element {
        public String text;
        public String value;
        public boolean leaf;
        public List<Element> children = new ArrayList<>();
    }

    private static List<Element> makeTree(List<Element> localTree, List<String> steps, String fileName){

        Element temp = new Element();
        temp.text = steps.get(0);
        temp.leaf = false;


        Element existing = localTree.stream()
                .filter(current -> temp.text.equals(current.text) && current.children != null)
                .findAny()
                .orElse(null);

        if (steps.size() == 1){

            temp.leaf = true;
            temp.value = fileName;
            temp.children = null;
            localTree.add(temp);

        } else {

            steps.remove(temp.text);

            if(existing != null){

                int index = localTree.indexOf(existing);

                existing.children = makeTree(existing.children, steps, fileName);

                localTree.set(index, existing);
            } else {

                temp.children = makeTree(temp.children, steps, fileName);
                localTree.add(temp);
            }
        }

        return localTree;
    }
}
