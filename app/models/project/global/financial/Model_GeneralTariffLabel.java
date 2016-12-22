package models.project.global.financial;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;

import javax.persistence.*;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of GeneralTariffLabel",
        value = "GeneralTariffLabel")
public class Model_GeneralTariffLabel extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                         @JsonIgnore @Id public String id;

                                                @JsonIgnore @ManyToOne   public Model_GeneralTariff general_tariff;
                                                @JsonIgnore @ManyToOne   public Model_GeneralTariffExtensions extensions;

                                                                         public String label;
                                                                         public String description;
                                                                         public String icon;
                                                @JsonIgnore              public Integer order_position;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save(){

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_GeneralTariffLabel.find.byId(this.id) == null) break;
        }
        if(general_tariff != null) {
            order_position = Model_GeneralTariffLabel.find.where().eq("general_tariff.id", general_tariff.id).findRowCount() + 1;
        }else {
            order_position = Model_GeneralTariffLabel.find.where().eq("extensions.id", extensions.id).findRowCount() + 1;
        }
        super.save();
    }

    @JsonIgnore @Override
    public void delete(){

        int pointer = 1;

        if(general_tariff!=null) {
            for (Model_GeneralTariffLabel label : general_tariff.labels) {

                if (!label.id.equals(this.id)) {
                    label.order_position = pointer++;
                    label.update();
                }
            }
        }else {
            for (Model_GeneralTariffLabel label : extensions.labels) {

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

        if(general_tariff != null) {
            general_tariff.labels.get(order_position - 2).order_position = this.order_position;
            general_tariff.labels.get(order_position - 2).update();
        }else {
            extensions.labels.get(order_position - 2).order_position = this.order_position;
            extensions.labels.get(order_position - 2).update();
        }

        this.order_position -= 1;
        this.update();
    }

    @JsonIgnore @Transient
    public void down(){

        if(general_tariff != null) {
            general_tariff.labels.get(order_position).order_position =  general_tariff.labels.get(order_position).order_position-1;
            general_tariff.labels.get(order_position).update();
        }else {
            extensions.labels.get(order_position).order_position =  extensions.labels.get(order_position).order_position-1;
            extensions.labels.get(order_position).update();
        }

        this.order_position += 1;
        this.update();

    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_GeneralTariffLabel> find = new Finder<>(Model_GeneralTariffLabel.class);


}