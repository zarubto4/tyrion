package utilities.webSocket;

import controllers.WebSocketController;

public class ClientThreadChecker  extends Thread {

    //###################################################
    private  int periodReconnectionTime;
    private String identificator;
    private String serverAddress;
    private boolean reconnection = true;


    public ClientThreadChecker setIDentificator( String identificator){
        this.identificator = identificator;
        return this;
    }

    public ClientThreadChecker setServerAddress( String serverAddress){
        this.serverAddress = serverAddress;
        return this;
    }

    public ClientThreadChecker setReconnection(boolean reconnection){
        this.reconnection = reconnection;
        return this;
    }

    public ClientThreadChecker setPeriodReconnectionTime(int periodReconnectionTime){
        this.periodReconnectionTime = periodReconnectionTime;
        return this;
    }

    public ClientThreadChecker connectToServer(){
        try {
            this.start();

        }catch (Exception e){
            e.printStackTrace();
        }

        return this;
    }

    private void connect() throws Exception {
        WebSocketController.connectToServer(identificator,serverAddress, this);
    }


    //# Hlavní vlákno - po základní obsluze poslouchá, popřípadě zavádí nová volání na server
    @Override
    public void run() {
        while (true) {
            try {
                connect();
                System.out.println("Připojení k serveru: " + identificator +" proběhlo v pořádku a teď hlídací vlákno čeká věčný spánek dokud se to neposere \n");
                sleep(300000000);


            } catch ( Exception e){}
            finally {
                try {
                    System.out.println("Tak se připojení k " +identificator +" posralo a připojení vyzkouším za cca randomem pokřivených " + (periodReconnectionTime/1000) + " sekund \n");
                    sleep(periodReconnectionTime);
                } catch (InterruptedException e) {}
            }
            if(!reconnection) break;
        }
    }

}
