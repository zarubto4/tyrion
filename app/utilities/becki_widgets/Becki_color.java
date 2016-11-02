package utilities.becki_widgets;

public class Becki_color {

    private String color_code;
    public Becki_color(String color_code) { this.color_code = color_code;}
    public String getColor(){return  color_code;}


    public final static Becki_color byzance_blue     = new Becki_color("blue");
    public final static Becki_color byzance_pink     = new Becki_color("#EC008B");
    public final static Becki_color byzance_grey_1   = new Becki_color("grey-salt");
    public final static Becki_color byzance_grey_2   = new Becki_color("grey-mint");
    public final static Becki_color byzance_grey_3   = new Becki_color("grey-gallery");
    public final static Becki_color byzance_green    = new Becki_color("green-meadow");

    public final static Becki_color white    = new Becki_color("wweert");

    public final static Becki_color byzance_red    = new Becki_color("red-thunderbird");



}
