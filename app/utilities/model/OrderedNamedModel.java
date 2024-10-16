package utilities.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Finder;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.UUID;

@MappedSuperclass
public abstract class OrderedNamedModel extends NamedModel {
    @ApiModelProperty(required = true) public Integer order_position;

    private final Finder<UUID, ? extends OrderedNamedModel>  finder;

    public OrderedNamedModel(Finder<UUID, ? extends OrderedNamedModel>  finder) {
        this.finder = finder;
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {
        // usually order is not set first time instance is created, in this case, add the item at the end
        if(order_position == null) {
            order_position = finder.query().findCount() + 1;
        }

        super.save();
    }

    @JsonIgnore @Override
    public boolean delete() {
        int order = 1;
        for (OrderedNamedModel item : finder.query().where().eq("deleted", false).orderBy("order_position").findList()) {
            if (!item.id.equals(this.id)) {
                item.order_position = order++;
                item.update();
            }
        }

        return super.delete();
    }

/* ORDER ---------------------------------------------------------------------------------------------------------------*/


    @JsonIgnore
    @Transient
    public void up() {

        OrderedNamedModel up = finder.query().where().eq("order_position", (order_position-1) ).findOne();
        if (up == null) return;

        up.order_position += 1;
        up.update();

        this.order_position -= 1;
        this.update();
    }

    @JsonIgnore @Transient
    public void down() {

        OrderedNamedModel down = finder.query().where().eq("order_position", (order_position+1) ).findOne();
        if (down == null) return;

        down.order_position -= 1;
        down.update();

        this.order_position += 1;
        this.update();

    }
}
