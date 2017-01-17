package utilities.independent_threads;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.Controller_WebSocket;
import models.compiler.Model_CompilationServer;
import models.compiler.Model_VersionObject;
import utilities.enums.Compile_Status;

import java.util.ArrayList;
import java.util.List;

public class Compilation_After_BlackOut {

    /**
     * Třída Singleton určená pro zpětné kompilování kodu, když vypadne spojení se všemi kompilačními servery.
     * Je zřízena tak, aby dokázala podchytit stavy kdy se spojí všechny kompilační servery a je nutné kompilace mezi
     * servery vhodně přidělovat.
     */

    // Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

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

        logger.debug("Compilation_After_BlackOut creating new 2 threads for compilations");
        Compilation_Thread thread_1 = new Compilation_Thread(server.server_name + "_1");
        Compilation_Thread thread_2 = new Compilation_Thread(server.server_name + "_2");

        threads.add(thread_1);
        threads.add(thread_2);

        logger.debug("Compilation_After_BlackOut starting new 2 threads");
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
                    while (true) {

                        if(Controller_WebSocket.compiler_cloud_servers.isEmpty()) break;

                        Model_VersionObject version_object = Model_VersionObject.find.where().eq("c_compilation.status", Compile_Status.server_was_offline.name()).order().desc("date_of_create").setMaxRows(1).findUnique();
                        if(version_object == null){
                            break;
                        }
                        logger.debug("Compilation_Thread:: " + thread_name + " starting compilation");
                        version_object.c_compilation.status = Compile_Status.compilation_in_progress;
                        version_object.c_compilation.update();

                        // Výsledek se kterým se dále nic neděje
                        JsonNode jsonNode = version_object.compile_program_procedure();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
    }


}