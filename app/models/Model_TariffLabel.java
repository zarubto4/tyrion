package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import utilities.logger.Class_Logger;

import javax.persistence.*;
import java.util.UUID;

@Entity
@ApiModel(value = "TariffLabel", description = "Model of TariffLabel")
@Table(name="TariffLabel")
public class Model_TariffLabel extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_TariffLabel.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                                    @Id  public String id;
                                                @JsonIgnore @ManyToOne   public Model_Tariff tariff;

                                                                         public String label;
                                                                         public String description;
                                                                         public String icon;
                                                @JsonIgnore              public Integer order_position;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save(){

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString().substring(0,8);
            if (find.byId(this.id) == null) break;
        }
        if(tariff != null) {
            order_position = find.where().eq("tariff.id", tariff.id).findRowCount() + 1;
        }
        super.save();
    }

    @JsonIgnore @Override
    public void delete(){

        int pointer = 1;

        if(tariff !=null) {
            for (Model_TariffLabel label : tariff.labels) {

                if (!label.id.equals(this.id)) {
                    label.order_position = pointer++;
                    label.update();
                }
            }
        }
        super.delete();
    }

    @JsonIgnore @Transient
    public void up(){

        if(tariff != null) {
            tariff.labels.get(order_position - 2).order_position = this.order_position;
            tariff.labels.get(order_position - 2).update();
        }

        this.order_position -= 1;
        this.update();
    }

    @JsonIgnore @Transient
    public void down(){

        if(tariff != null) {
            tariff.labels.get(order_position).order_position =  tariff.labels.get(order_position).order_position-1;
            tariff.labels.get(order_position).update();
        }

        this.order_position += 1;
        this.update();

    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/


/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_TariffLabel get_byId(String id) {

        terminal_logger.warn("CACHE is not implemented - TODO");
        return find.byId(id);

    }


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_TariffLabel> find = new Finder<>(Model_TariffLabel.class);


}