package utilities.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import models.Model_BProgramVersion;
import models.Model_Tag;
import utilities.cache.Cached;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;

import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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


    public void setTags(List<String> new_tags) throws _Base_Result_Exception {
        List<String > tags = tags();

        new_tags.forEach(value -> {

            if(tags.contains(value)) {
                return;
            }

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

        this.save();
    }

    public void addTags(List<String> new_tags) throws _Base_Result_Exception {
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

        this.save();
    }

    public void removeTags(List<String> tags) throws _Base_Result_Exception {

        check_update_permission();

        List<Model_Tag> toRemove = this.tags.stream().filter(tag -> tags.contains(tag.value)).collect(Collectors.toList());
        this.tags.removeAll(toRemove);

        this.update();
    }
}
