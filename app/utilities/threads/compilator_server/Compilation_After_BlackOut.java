package utilities.threads.compilator_server;

import models.Model_CProgramVersion;
import models.Model_CompilationServer;
import utilities.enums.CompilationStatus;
import utilities.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Compilation_After_BlackOut {

    /**
     * Třída Singleton určená pro zpětné kompilování kodu, když vypadne spojení se všemi kompilačními servery.
     * Je zřízena tak, aby dokázala podchytit stavy kdy se spojí všechny kompilační servery a je nutné kompilace mezi
     * servery vhodně přidělovat.
     */

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger terminal_logger = new Logger(Compilation_After_BlackOut.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    private static Compilation_After_BlackOut instance = null;
    private static List<Thread> threads = new ArrayList<>();

    protected Compilation_After_BlackOut() {
        // Exists only to defeat instantiation.
    }

    public static Compilation_After_BlackOut getInstance() {
        if (instance == null) instance = new Compilation_After_BlackOut();
        return instance;
    }



    public void start(Model_CompilationServer server) {

        terminal_logger.debug("Compilation_After_BlackOut:: start:: creating new 2 threads for compilations");
        Compilation_Thread thread_1 = new Compilation_Thread(server.id + "_1");
        Compilation_Thread thread_2 = new Compilation_Thread(server.id + "_2");

        threads.add(thread_1);
        threads.add(thread_2);

        terminal_logger.trace("Compilation_After_BlackOut:: start:: starting new 2 threads");
        thread_1.start();
        thread_2.start();
    }

    // Vlákno které hledá záznamy nezkompilovaných programů a dodělá je se zpožděním.
    // Vyhledá vždy jedno! aby když se připojí více kompilačních serverů aby nekompilovaly tentýž program
    private class Compilation_Thread extends Thread{

            public String thread_name;

            public Compilation_Thread(String name) {
                this.thread_name = name;
            }

            @Override
            public void run() {
                try {

                    // Náhodný sleeping před startem - aby se vlákna časove rozhodila
                    sleep(2000 + ThreadLocalRandom.current().nextInt(50, 5000));

                    while (true) {

                        Model_CProgramVersion version = Model_CProgramVersion.find.query().where().eq("c_compilation.status", CompilationStatus.SERVER_OFFLINE.name()).order().desc("created").setMaxRows(1).findOne();
                        if (version == null) {
                            break;
                        }
                        terminal_logger.debug("Compilation_After_BlackOut:: run:: " + thread_name + " starting compilation");
                        // version.compilation.status = CompilationStatus.IN_PROGRESS;
                        // version.compilation.update();

                        // Výsledek se kterým se dále nic neděje
                        // TODO version.compile_program_procedure();
                    }
                } catch (Exception e) {
                    terminal_logger.internalServerError(e);
                }
            }
    }


}