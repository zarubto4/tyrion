package utilities.threads;

import utilities.logger.Logger;

import java.util.UUID;


public class Check_Online_Status_after_user_login extends Thread {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger terminal_logger = new Logger(Check_Online_Status_after_user_login.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    private UUID person_id;

    // Umožněno kontrolovat COmpilator i Homer server
    public Check_Online_Status_after_user_login(UUID person_id) {
        this.person_id = person_id;
    }


    @Override
    public void run() {


        try {

            // TODO doplnit to co lze uživatelovi zpracovat dopředu http://youtrack.byzance.cz/youtrack/issue/TYRION-485

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }
}
