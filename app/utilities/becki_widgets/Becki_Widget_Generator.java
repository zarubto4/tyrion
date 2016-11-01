package utilities.becki_widgets;

import views.html.becki_widgets.A_Type_Widget;
import views.html.becki_widgets.B_Type_Widget;
import views.html.becki_widgets.C_Type_Widget;

import javax.naming.SizeLimitExceededException;


public class Becki_Widget_Generator extends play.mvc.Controller {

    /**
     * ICONY:
     * http://fontawesome.io/icons/
     */


    // Vrchní 4 widgety: http://keenthemes.com/preview/metronic/theme/admin_4/dashboard_3.html
    public static String create_A_Type_Widget(String main_text, String sub_text, Integer number, Becki_Color color, String icon_code) throws SizeLimitExceededException {

        if(main_text.length() > 24 ) throw new SizeLimitExceededException("Maximální délka hlavního textu smí být jen 24 znaků");
        return A_Type_Widget.render(main_text, sub_text, number, color.getColor(), icon_code).body();

    }

    // Vrchní 4 widgety:  http://keenthemes.com/preview/metronic/theme/admin_4/
    public static String create_B_Type_Widget(String main_text, String sub_text, Integer main_number, String symbol_after_main_number, Integer percentage_number, Becki_Color color, String icon_code) throws SizeLimitExceededException {

        if(main_text.length() > 24 ) throw new SizeLimitExceededException("Maximální délka hlavního textu smí být jen 24 znaků");
        if(symbol_after_main_number.length() > 5 ) throw new SizeLimitExceededException("Maximální délka symbolu za číslem smí být jen 5 znaky");
        if(percentage_number >= 0 && percentage_number <= 100 ) throw new SizeLimitExceededException("Procenta musí být mezi 0 a 100 včětně");

        return B_Type_Widget.render(main_text, sub_text, main_number, symbol_after_main_number, percentage_number, color.getColor(), icon_code).body();
    }

    // Vrchní 4 widgety:  http://keenthemes.com/preview/metronic/theme/admin_4/dashboard_2.html
    public static String create_C_Type_Widget(String main_text_Number, String sub_text, Becki_Color color, String icon_code) throws SizeLimitExceededException {

        if(main_text_Number.length() > 20 ) throw new SizeLimitExceededException("Maximální délka hlavního textu smí být jen 24 znaků");
        if(sub_text.length() > 10 ) throw new SizeLimitExceededException("Maximální délka symbolu za číslem smí být jen 5 znaky");

        return C_Type_Widget.render(main_text_Number, sub_text, color.getColor(), icon_code).body();
    }


}
