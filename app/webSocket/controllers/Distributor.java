package webSocket.controllers;


import play.mvc.WebSocket;

/**
 * Created by zaruba on 26.08.15.
 */
public class Distributor {

    private static Distributor ourInstance = new Distributor();

    /* SINGLETON ---------------------------------------------------------------------------------------------- */
    public static Distributor getInstance() {
        return ourInstance;
    }
    private Distributor() {}


    /* METHODS ---------------------------------------------------------------------------------------------- */
    public static void turingMachine(WebSocket.Out<String> out, final String packet) {
        try {

            System.out.println("turingMachine: " + packet);



        } catch (Exception e) {
            e.printStackTrace();
            out.write("Nesprávná verze packetu, není to JSON pro Turing Machine! Atd..");
        }

    }


}
