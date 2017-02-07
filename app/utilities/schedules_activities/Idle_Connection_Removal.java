package utilities.schedules_activities;


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Date;

public class Idle_Connection_Removal implements Job {

    public Idle_Connection_Removal(){ /** do nothing */ }

    Connection connection;
    Statement statement;

    // Logger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public void execute(JobExecutionContext context) throws JobExecutionException {
        if(!remove_idle_connection_thread.isAlive()) remove_idle_connection_thread.start();
    }

    Thread remove_idle_connection_thread = new Thread() {

        @Override
        public void run() {

            try {
                connection = play.db.DB.getConnection();

                statement = connection.createStatement();

                statement.execute("WITH inactive_connections AS ( SELECT pid, rank() over (partition by client_addr order by backend_start ASC) as rank FROM pg_stat_activity WHERE pid <> pg_backend_pid( ) AND application_name !~ '(?:psql)|(?:pgAdmin.+)' AND datname = current_database() AND usename = current_user AND state in ('idle', 'idle in transaction', 'idle in transaction (aborted)', 'disabled') AND current_timestamp - state_change > interval '5 minutes' ) SELECT pg_terminate_backend(pid) FROM inactive_connections WHERE rank > 1");
                statement.close();

                connection.close();

                logger.info("Idle database connections removed on " + new Date());
            } catch (Exception e) {
                logger.error("Database connection probably lost!");
            }
        }
    };
}
