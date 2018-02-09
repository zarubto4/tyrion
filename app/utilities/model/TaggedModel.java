package utilities.model;

import controllers.BaseController;
import models.Model_Tag;

import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

            if (!this.tags.contains(tag)) {
                this.tags.add(tag);
            }
        });

        this.save();
    }

    public void removeTags(List<String> tags) {

        List<Model_Tag> toRemove = this.tags.stream().filter(tag -> tags.contains(tag.value)).collect(Collectors.toList());
        this.tags.removeAll(toRemove);

        this.save();
    }
}
