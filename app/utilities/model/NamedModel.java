package utilities.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class NamedModel extends BaseModel {

    public String name;
    @Column(columnDefinition = "TEXT") public String description;
}
