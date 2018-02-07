package utilities.model;

import controllers.BaseController;
import models.Model_Tag;

import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import java.util.ArrayList;
import java.util.List;

@MappedSuperclass
public abstract class TaggedModel extends NamedModel {

    @ManyToMany
    public List<Model_Tag> tags = new ArrayList<>();

    public void addTags(List<String> tags) {
        tags.forEach(value -> {
            Model_Tag tag = Model_Tag.getByValue(value);
            if (tag == null) {
                tag = new Model_Tag();
                tag.value = value;
                tag.person = BaseController.person();
                tag.save();
            }

            this.tags.add(tag);
        });

        this.save();
    }

    public void removeTags(List<String> tags) {

    }
}
