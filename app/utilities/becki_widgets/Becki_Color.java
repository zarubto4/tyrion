package utilities.becki_widgets;

public class Becki_Color {

    private String color_code;
    public Becki_Color(String color_code) { this.color_code = color_code;}
    public String getColor(){return  color_code;}


    public final static Becki_Color byzance_blue     = new Becki_Color("blue");
    public final static Becki_Color byzance_pink     = new Becki_Color("#EC008B");
    public final static Becki_Color byzance_grey_1   = new Becki_Color("grey-salt");
    public final static Becki_Color byzance_grey_2   = new Becki_Color("grey-mint");
    public final static Becki_Color byzance_grey_3   = new Becki_Color("grey-gallery");
    public final static Becki_Color byzance_green    = new Becki_Color("green-meadow");

    public final static Becki_Color white    = new Becki_Color("wweert");

    public final static Becki_Color byzance_red    = new Becki_Color("red-thunderbird");



}
