package utilities.independent_threads;
import utilities.logger.Class_Logger;


public class Check_Online_Status_after_user_login extends Thread {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Check_Online_Status_after_user_login.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    private String person_id = null;

    // Umožněno kontrolovat COmpilator i Homer server
    public Check_Online_Status_after_user_login(String person_id){
        this.person_id = person_id;
    }


    @Override
    public void run(){


        try {

            // TODO doplnit to co lze uživatelovi zpracovat dopředu http://youtrack.byzance.cz/youtrack/issue/TYRION-485

        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }
    }
}
