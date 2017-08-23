package utilities.independent_threads.compilator_server;

import controllers.Controller_WebSocket;
import models.Model_CompilationServer;
import models.Model_VersionObject;
import utilities.enums.Enum_Compile_status;
import utilities.logger.Class_Logger;

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

    private static final Class_Logger terminal_logger = new Class_Logger(Compilation_After_BlackOut.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    private static Compilation_After_BlackOut instance = null;
    private static List<Thread> threads = new ArrayList<>();

    protected Compilation_After_BlackOut() {
        // Exists only to defeat instantiation.
    }

    public static Compilation_After_BlackOut getInstance() {
        if(instance == null) instance = new Compilation_After_BlackOut();
        return instance;
    }



    public void start(Model_CompilationServer server){

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

            public Compilation_Thread(String name){
                this.thread_name = name;
            }

            @Override
            public void run() {
                try {

                    // Náhodný sleeping před startem - aby se vlákna časove rozhodila
                    sleep(2000 + ThreadLocalRandom.current().nextInt(50, 5000));

                    while (true) {



                        if(Controller_WebSocket.compiler_cloud_servers.isEmpty()){
                            terminal_logger.warn("Compilation_After_BlackOut:: run:: server is offline again");
                            break;
                        }

                        Model_VersionObject version_object = Model_VersionObject.find.where().eq("c_compilation.status", Enum_Compile_status.server_was_offline.name()).order().desc("date_of_create").setMaxRows(1).findUnique();
                        if(version_object == null){
                            break;
                        }
                        terminal_logger.debug("Compilation_After_BlackOut:: run:: " + thread_name + " starting compilation");
                        version_object.c_compilation.status = Enum_Compile_status.compilation_in_progress;
                        version_object.c_compilation.update();

                        // Výsledek se kterým se dále nic neděje
                        version_object.compile_program_procedure();
                    }
                }catch(Exception e){
                    terminal_logger.internalServerError("run:", e);
                }
            }
    }


}