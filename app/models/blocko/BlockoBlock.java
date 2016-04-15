package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import models.person.Person;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class BlockoBlock extends Model {


/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)    public String id;
                                                               public String name;
                         @Column(columnDefinition = "TEXT")    public String general_description;
                                    @JsonIgnore @ManyToOne     public Person author;
                                    @JsonIgnore @ManyToOne     public TypeOfBlock type_of_block;

    @JsonIgnore @OneToMany(mappedBy="blocko_block", cascade = CascadeType.ALL) @OrderBy("dateOfCreate desc") public List<BlockoBlockVersion> blocko_versions = new ArrayList<>();


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

                @JsonProperty public List<String>    versions()             { List<String> l = new ArrayList<>();  for( BlockoBlockVersion m : blocko_versions)  l.add(m.id); return l; }
                @JsonProperty public String         author_id()             { return author.id;}
    @Transient  @JsonProperty public String  type_of_block_id()             { return type_of_block.id; }


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty public Boolean create_permission() {
        return BlockoBlock.find.where()
                .or(
                        com.avaje.ebean.Expr.eq("type_of_block.project.ownersOfProject.id", SecurityController.getPerson().id),
                        com.avaje.ebean.Expr.eq("author.id", SecurityController.getPerson().id)
                ).eq("id", id).findRowCount() > 0
                ||
                SecurityController.getPerson().has_permission("BlockoBlock.create");
    }

    @JsonProperty public Boolean edit_permission() {
        return BlockoBlock.find.where()
                        .or(
                                com.avaje.ebean.Expr.eq("type_of_block.project.ownersOfProject.id", SecurityController.getPerson().id),
                                com.avaje.ebean.Expr.eq("author.id", SecurityController.getPerson().id)
                        ).eq("id", id).findRowCount() > 0
                        ||
                        SecurityController.getPerson().has_permission("BlockoBlock.create");
    }

    @JsonProperty public Boolean read_permission() {
        return BlockoBlock.find.where()
                        .or(
                                com.avaje.ebean.Expr.eq("type_of_block.project.ownersOfProject.id", SecurityController.getPerson().id),
                                com.avaje.ebean.Expr.eq("author.id", SecurityController.getPerson().id)
                        ).eq("id", id).findRowCount() > 0
                        ||
                        SecurityController.getPerson().has_permission("BlockoBlock.read");
    }

    @JsonProperty public Boolean delete_permission() {
        return
                BlockoBlock.find.where()
                        .or(
                                com.avaje.ebean.Expr.eq("type_of_block.project.ownersOfProject.id", SecurityController.getPerson().id),
                                com.avaje.ebean.Expr.eq("author.id", SecurityController.getPerson().id)
                        ).eq("id", id).findRowCount() > 0
                        ||
                        SecurityController.getPerson().has_permission("BlockoBlock.delete");
    }

/* FINDER -------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,BlockoBlock> find = new Finder<>(BlockoBlock.class);

}
