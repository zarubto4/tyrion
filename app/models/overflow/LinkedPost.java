package models.overflow;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import models.persons.Person;

import javax.persistence.*;

@Entity
public class LinkedPost extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String linkId;

    @JsonIgnore  @ManyToOne public Person author;
                 @ManyToOne public Post answer;  // Objekt který je odpovědí
    @JsonIgnore  @ManyToOne public Post question; // Objekt na který je navěšuje již odpovězená (podobná) otázka

    public static Finder<String,LinkedPost> find = new Finder<>(LinkedPost.class);



}
