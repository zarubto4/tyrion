package utilities.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.Model_Tag;
import utilities.logger.Logger;
import utilities.swagger.output.Swagger_Short_Reference;

import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static controllers._BaseController.person;

@MappedSuperclass
public abstract class TaggedModel extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(TaggedModel.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToMany public List<Model_Tag> tags = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    // @JsonIgnore @Transient @Cached public List<String> cache_tags = new ArrayList<>();

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/


    @JsonProperty @Transient
    public List<String > tags() {
        try {

            List<String> tagsList = new ArrayList<>();

            for (Model_Tag tag : tags) {
                tagsList.add(tag.value);
            }

            return tagsList;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    public void setTags(List<String> new_tags) {

        List<String > tags = tags();

        boolean change = false;
        for (String value : new_tags) {
            if (tags.contains(value)) {
                continue;
            }

            change = true;

            Model_Tag tag = Model_Tag.getByValue(value);

            if (tag == null) {
                tag = new Model_Tag();
                tag.value = value;
                tag.save();
            }

            if (!this.tags.contains(tag)) {
                this.tags.add(tag);
            }
        }

        // This object can be new!
        if(this.id != null) {
            if (change) {
                // TODO - Lexa - Tady je to trochu nedomyšlené, protože update tagu nejde skrze kontrolu controlleru ani modelu
                // checkUpdate(person(), this);
                this.update();
            }
        }
    }

    public void addTags(List<String> new_tags) {
        new_tags.forEach(value -> {
            Model_Tag tag = Model_Tag.getByValue(value);

            if (tag == null) {
                tag = new Model_Tag();
                tag.value = value;
                tag.save();
            }

            if (!this.tags.contains(tag)) {
                this.tags.add(tag);
            }
        });

        this.update();
    }

    @JsonIgnore
    public Swagger_Short_Reference ref() {
        return new Swagger_Short_Reference(id, name, description, this.tags());
    }

    public void removeTags(List<String> tags) {

        List<Model_Tag> toRemove = this.tags.stream().filter(tag -> tags.contains(tag.value)).collect(Collectors.toList());
        this.tags.removeAll(toRemove);

        this.update();
    }
}
