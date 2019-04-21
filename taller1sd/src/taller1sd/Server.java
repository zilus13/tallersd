/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taller1sd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;


/**
 *
 * @author franciscogomezlopez
 */
public class Server {
    
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    
    private HashMap<String, String> participant;
    private boolean finishGame = false;
    private boolean tengopapa = false;
    private String name = "";
    
    
    
    public void start(int port, String name) throws IOException {
        
        System.out.println("Im listening ... on " + port + " I'm " + name);
        
        serverSocket = new ServerSocket(port);
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String greeting = in.readLine();
            
        if(greeting.startsWith("regparticipante")) {
            
            String partipante = greeting.substring("regparticipante".length(), 15);
            String puerto = greeting.substring("regparticipante".length()+4,4);
            String ip = greeting.substring("regparticipante".length()+8);
            System.out.println("momo");
            // agregar participante
            this.participant.put(partipante, ip+":"+puerto);
            
            // paseo por cada uno de los elementos de los participantes para enviar la lista
            for (Map.Entry<String, String> entry : participant.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                if(!key.equals(this.name)){
                    
                    ClientMessage sendmessage = new ClientMessage();
                    sendmessage.startConnection(value.substring(4), new Integer(value.substring(0,4)));
                    sendmessage.sendMessage(this.serializarLista());
                
                }
                
            }
            
            out.println("agregadoparticipante");
            
        } else if(greeting.startsWith("recibepapa")) {
            
            this.tengopapa = true;
            
            out.println("tienes la papa" + this.name);
            // agregar participante
            
            if(this.finishGame && this.tengopapa) {
                
                System.out.println("perdiste:" + this.name);
                System.exit(0);
            } 
            
            try {
                
                TimeUnit.SECONDS.sleep(5);
                
                boolean foun = false;
                for (Map.Entry<String, String> entry : participant.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    
                    if(key.equals(this.name)) {
                        foun = true;
                    }
                    
                    if(foun) {
                    // siguiente
                    ClientMessage sendmessage = new ClientMessage();
                    sendmessage.startConnection(value.substring(4), new Integer(value.substring(0,4)));
                    sendmessage.sendMessage("recibepapa");
                    }
                    
                }
                
            } catch (InterruptedException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } else if(greeting.startsWith("sequemo")) {
            
            this.finishGame = true;
            
            // agregar participante
            if(this.finishGame && this.tengopapa) {
                
                System.out.println("perdiste:" + this.name);
                System.exit(0);
            } 
            
        }  else if(greeting.startsWith("actualizalista")) {
            
            String lista = greeting.substring("actualizalista".length());
            
            String[] listatmp = lista.split(",");
            this.participant = new HashMap<String, String>();
            for (String tmp : listatmp) {
                String[] finaltmp = tmp.split("#");
                this.participant.put(finaltmp[0], finaltmp[1]);
            }
            
        }
        
        else {
            System.out.println("Mensaje no reconocido");
            out.println("mensaje corrupto vete de aqui");
        }

        this.name = name;
        
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
        
        this.start(port, name);
        
    }
    
    
    private String serializarLista() {
        
        String finallista = "";
        
        for (Map.Entry<String, String> entry : participant.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            finallista += key+"#"+value+",";
            
        }
        
        return finallista; 
    }
 
    public void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }
    
}
