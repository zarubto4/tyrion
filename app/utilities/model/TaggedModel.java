package utilities.model;

import controllers.BaseController;
import models.Model_Tag;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;

import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@MappedSuperclass
public abstract class TaggedModel extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(TaggedModel.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/
    @ManyToMany public List<Model_Tag> tags = new ArrayList<>();


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    public void addTags(List<String> new_tags) throws _Base_Result_Exception {
        new_tags.forEach(value -> {
            Model_Tag tag = Model_Tag.getByValue(value);

            if (tag == null) {
                tag = new Model_Tag();
                tag.value = value;

                try {
                    tag.person = BaseController.person();
                } catch (_Base_Result_Exception exception){
                    if(exception.getClass().getSimpleName().equals(Result_Error_NotFound.class.getSimpleName())){
                        logger.error("addTags::Person not found");
                    }else {
                        logger.internalServerError(exception);
                    }
                }

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
