package models.overflow;

import com.avaje.ebean.Model;
import models.Person;

import javax.persistence.*;

@Entity
public class LinkedPost extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String linkId;

    @ManyToOne public Person author;
    @ManyToOne public Post answer;  // Objekt který je odpovědí
    @ManyToOne public Post question; // Objekt na který je navěšuje již odpovězená (podobná) otázka

    public LinkedPost(){}
    public static Finder<String,LinkedPost> find = new Finder<>(LinkedPost.class);



}
