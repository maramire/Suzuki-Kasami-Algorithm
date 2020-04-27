import java.net.MalformedURLException;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.rmi.*;
import java.lang.*;
import java.io.*;
import java.io.IOException;


public class Process extends java.rmi.server.UnicastRemoteObject implements ProcessInterface {

    private int pid;
    private int[] RN;
    private int N;
    private int capacidad;
    private int velocidad;
    private boolean bearer;
    private boolean requesting;
    private Token token;
    private boolean isRegistryCreated;
    private int delay;
    private String state;
    private String file;
    private boolean isFinished;
    private Registry registro;
    private int resources;

    // Process constructor
    public Process(int pid, int N, int capacidad, int velocidad, boolean bearer, int delay, String file, int resources) throws RemoteException{
        this.pid = pid;
        this.N = N;
        this.RN = new int[N];
        Arrays.fill(this.RN, -1);
        this.capacidad = capacidad;
        this.velocidad = velocidad;
        this.bearer = bearer;
        this.requesting = false;
        this.token = null;
        this.isRegistryCreated = false;
        this.delay = delay;
        this.state = "OCIOSO";
        this.file = file;
        this.isFinished = false;
        this.registro = null;
        this.resources = resources;
    }

    // tomar token recibido
    public synchronized void takeToken(Token token) throws RemoteException {
        this.bearer = true;
        this.token = token;
        // despierta a mi compadre
        notifyAll();
        //System.out.println("Token recibido");
    }

    public boolean supreme(){
        if (this.registro!=null){
            return true;
        }else {return false;}
    }

    // manejar peticion de token
    public synchronized void request(int id, int seq) throws RemoteException {
        this.RN[id] = Math.max(this.RN[id], seq);
        if (this.bearer && !requesting && (this.RN[id] == this.token.getLN()[id] + 1)){
            this.bearer = false;
            //enviar token a id
            try {
                ProcessInterface process =  (ProcessInterface) LocateRegistry.getRegistry().lookup("Process"+Integer.toString(id));
                // una vez obtenido el proceso, enviar token
                process.takeToken(this.token);
                this.token = null;
            }catch (NotBoundException nbe){
                System.err.println("error, objeto no encontrado");
            }
        }
    }

    // esperar por token
    public synchronized void waitToken() throws InterruptedException {
        this.state = "ESPERANDO";
        System.out.println("ESTADO: ESPERANDO TOKEN");
        synchronized (this){
            while (!this.bearer){
                // mi compadre es paciente
                try {
                    wait();
                }catch (InterruptedException ie){}

            }
        }
    }

    //kill algorithm
    public synchronized void kill() throws RemoteException, InterruptedException {
        System.out.println("Adiós");
        System.exit(0);
    }

    // verifica que el archivo haya quedado vacio
    public void fileIsEmpty (String filename) throws  IOException, InterruptedException{
        BufferedReader br = new BufferedReader(new FileReader(filename));
        File file = new File(filename);
        if (br.readLine() == null && file.length() == 0)
        {
            //System.out.println("File empty");
            this.isFinished = true;
            for (int i = 0; i < this.N; i++) {
                if (i!=this.pid){
                    try {
                        ProcessInterface process = (ProcessInterface) LocateRegistry.getRegistry().lookup("Process"+Integer.toString(i));
                        if (!process.supreme()){
                            process.kill();
                        }

                    } catch (NotBoundException nbe) {
                        System.err.println("error, objeto no encontrado");
                    } catch (ConnectException ce){System.exit(0);}
                }
            }
        }
        else
        {
            //System.out.println("Quedan recursos");
        }
    }

    // entrar a la seccion critica
    public void enterSC() throws IOException, InterruptedException{
        this.state = "SC";
        //System.out.println("Entré a la SC");
        String ANSI_RESET = "\u001B[0m";
        String ANSI_BLUE = "\u001B[34m";
        String ANSI_RED = "\u001B[31m";
        String ANSI_GREEN = "\u001B[32m";
        String ANSI_YELLOW = "\u001B[33m";

        int characterCount = 0;
        try (InputStream input = new FileInputStream(this.file);
             Reader rea = new InputStreamReader(input)) {

            int d;
            // file until a null is returned
            while ((d = rea.read()) != -1) {
                characterCount++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        try (InputStream in = new FileInputStream(this.file);
             Reader reader = new InputStreamReader(in)) {

            BufferedWriter writer = new BufferedWriter(new FileWriter("new.txt"));

            int c;
            int howMany = 0;

            while ((c = reader.read()) != -1) {
                if (howMany<this.capacidad){

                    double percentage = 100*(((double)characterCount-howMany)/this.resources);

                    if (percentage<=100 && percentage>=75){
                        System.out.println(ANSI_BLUE+"Extraccion: "+(char)c + ANSI_RESET);
                    }
                    else if (percentage<75 && percentage>=50){
                        System.out.println(ANSI_GREEN+"Extraccion: "+(char)c + ANSI_RESET);
                    }
                    else if (percentage<50 && percentage>=25){
                        System.out.println(ANSI_YELLOW+"Extraccion: "+(char)c + ANSI_RESET);
                    }else{
                        System.out.println(ANSI_RED+"Extraccion: "+(char)c + ANSI_RESET);
                    }
                    howMany++;
                    try {
                        Thread.sleep(1000);
                    }catch (InterruptedException ie){}

                }else{
                    writer.write((char) c);
                }
            }
            writer.close();

            File real =new File(this.file);
            real.delete();
            File tf = new File("new.txt");
            tf.renameTo(real);

            // check if file is empty
            fileIsEmpty(this.file);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // si se quiere acceder a la SC
    public void attemptSC() throws RemoteException, IOException, InterruptedException {
        this.requesting = true;
        // si no tengo el token
        if (!this.bearer) {
            //System.out.println("no soy portador :o");
            // actualizo RN
            this.RN[this.pid] = this.RN[this.pid] + 1;
            // broadcast request, excepto yo
            for (int i = 0; i < this.N; i++) {
                if (i != this.pid) {
                    try {
                        ProcessInterface process = (ProcessInterface) LocateRegistry.getRegistry().lookup("Process"+Integer.toString(i));
                        // una vez obtenido el proceso hacer el request
                        process.request(this.pid, RN[this.pid]);
                        //System.out.println("Request a proceso "+ i);
                    } catch (NotBoundException nbe) {
                        System.err.println("error, objeto no encontrado");
                    }

                }
            }
            // esperar antes que el token sea mio
            waitToken();
        }

        // finalmente, entrar a la sc
        enterSC();

        System.out.println("ESTADO: OCIOSO");

        // cuando termina el trabajo, actualizar LN.
        this.token.updateLN(this.pid, RN[this.pid]);

        for (int i = 0; i < this.N ; i++) {
            if (i!=this.pid){
                // si hay peticiones pendientes
                if (!this.token.queueContains(i) && (this.RN[i] == this.token.getLN()[i] + 1)){
                   //append i to queue
                    this.token.addToQueue(i);
                }
            }

        }
        if (!this.token.queueIsEmpty()){
            this.bearer = false;
            int idProcess = this.token.popQueue();
            //enviar token a id
            try {
                ProcessInterface newProcess =  (ProcessInterface) LocateRegistry.getRegistry().lookup("Process"+Integer.toString(idProcess));
                // una vez obtenido el proceso, enviar token
                newProcess.takeToken(this.token);
                this.token = null;
            }catch (NotBoundException nbe){
                System.err.println("error, objeto no encontrado");
            }catch (ConnectException ce){System.exit(0);}
        }
        this.requesting = false;

        // enfriamiento
        try {
            Thread.sleep(500*(this.capacidad));
        }catch (InterruptedException ie){
            ie.printStackTrace();
        }

        if(this.isFinished){
            System.exit(0);
        }


    }

    public static void main(String[] args) throws RemoteException, InterruptedException, IOException {
        // Ejecucion proceso
        int delay = Integer.parseInt(args[4]);

        int nro_procesos =  Integer.parseInt(args[0]);
        String file = args[1];

        // cantidad de recursos-----------------
        int resources = 0;
        try (InputStream in = new FileInputStream(file);
             Reader reader = new InputStreamReader(in)) {
            int d;
            while ((d = reader.read()) != -1) {
                resources++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //-------------------------------
        System.out.println("El archivo tiene "+resources+" caracteres.");
        int capacidad = Integer.parseInt(args[2]);
        int velocidad = Integer.parseInt(args[3]);
        boolean bearer = Boolean.parseBoolean(args[5]);
        int pid = Integer.parseInt(args[6]);

        // crear proceso
        Process process = new Process(pid, nro_procesos, capacidad, velocidad, bearer, delay, file,resources);
        System.out.println("ESTADO: OCIOSO");
        //delay
        //System.out.println("En delay");
        Thread.sleep(process.delay);

        // si es bearer, crear registro y token
        if (process.bearer){
            //System.out.println("Soy el proceso inicial con token");
            // crear token
            Token token = new Token(process.N);
            process.token = token;
            try {
                // create and bind

                Registry registry = LocateRegistry.createRegistry(1099);
                process.isRegistryCreated = true;
                registry.rebind("Process" + process.pid, process);
                process.registro = registry;
            } catch (RemoteException e) {
                System.err.println("Error en registro");
                e.printStackTrace();
            }

        }else{
            while(!process.isRegistryCreated) {
                try {
                    // System.out.println("Intentando conectar a RMIRegistry...");
                    // locate and bind
                    Registry registry = LocateRegistry.getRegistry(1099);
                    registry.rebind("Process" + process.pid, process);
                    process.isRegistryCreated = true;
                } catch (RemoteException e) {
                    //System.err.println("Registry aun no ha sido creado");
                }
                Thread.sleep(500);
            }

        }

        boolean eachProcessArrive = false;

        while (!eachProcessArrive){
            for (int i = 0; i < process.N; i++) {
                try {
                    ProcessInterface testing = (ProcessInterface) LocateRegistry.getRegistry().lookup("Process"+Integer.toString(i));
                    eachProcessArrive = true;
                } catch (NotBoundException nbe) {
                    //System.err.println("error, objeto no encontrado");
                    eachProcessArrive = false;
                }
            }
        }

        // se intenta ingresar a sección crítica por primera vez
        while (!process.isFinished){
            //System.out.println("Voy a intentar ingresar a la SC");
            process.attemptSC();
        }

        System.exit(0);

    }


}
