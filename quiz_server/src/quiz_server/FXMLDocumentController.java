/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quiz_server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

/**
 *
 * @author pawel
 */
public class FXMLDocumentController implements Initializable {
    
    final BlockingQueue<String> q = new LinkedBlockingDeque<>(); //inicjalizacja kolejki
    Queue<String> pytania=new LinkedList<>();          //inicjalizacja
    public String[] pytanie=new String[2];
    int nrPytania=0;
    
    @FXML
    private TextArea txt_area;
    
    @FXML
    public void ChangeTxt(String lineOut)
        {
        txt_area.setText(txt_area.getText()+"\n"+lineOut); 
        }
    
    public int pobierzPytania() throws FileNotFoundException, IOException{
        int i = 0; //ilość pytań w pliku
        String path="pytania.txt";
        File file=new File(path);
        String line = "";
        if (file.exists()) 
            {
            BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF8"));
            line=br.readLine().trim();  //zczytanie 1 elementu z pliku
            while(line!=null && line.length()>0)
                {
                pytania.add(line);
                i++;
                line=br.readLine();
                //line=line.trim();
                }
            }
    return i;    
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        pytania.add("W jakim języku zostałem napisany?:java");
        pytania.add("W jakim roku zostałem napisany?:2019");
        pytania.add("Powtórz UTP:UTP");                         
                                                         
        String host = "127.0.0.1"; // nazwa hosta
        int port = 50000; // numer portu 
        
        Thread thread1 = new Thread(()->{
            try
                {
                ServerSocket ServerSocket = new ServerSocket(port); // Utworzenie gniazda  
                
                while(true)
                    {
                    Socket socket=ServerSocket.accept();
                    InputStream sockIn = socket.getInputStream(); 
                    BufferedReader in = new BufferedReader(new InputStreamReader(sockIn));
                    
                    String line = in.readLine();
                    if(line!=null)
                        {
                        q.put(line);
                        }
                    }
                }
            catch (IOException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        Thread thread2=new Thread(()->{
            Boolean show=false;
            int iloscPytan=0;
            try {
                if((iloscPytan=pobierzPytania())>0)
                    {
                    pytania.remove();
                    pytania.remove();
                    pytania.remove();
                    }
                else
                    {
                    ChangeTxt("Brak pytań w pliku!\nWczytuję standardowe pytania!\n");
                    }
            } catch (IOException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
            while(true)
                {
                if(show==false)             //wyświetlenie pytania
                    {       
                    pytanie=pytania.poll().split(":");
                    int i=0;
                    ChangeTxt("Pytanie nr "+(nrPytania + 1)+":\n{"+pytanie[0]);
                    show=true;
                    }
                
                if(q.isEmpty()!=true)               //sprawdzenie kolejki
                    {
                    String[] nazwaiOdpowiedz={"",""};
                    try {
                        nazwaiOdpowiedz=q.take().split(":");
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                if(nrPytania<iloscPytan && nazwaiOdpowiedz[1].equals(pytanie[1]))
                    {
                    ChangeTxt(nazwaiOdpowiedz[0]+" odpowiedział poprawnie :) \n");
                    nrPytania++;
                    show=false;
                    
                    while(q.size()!=0) q.remove();      //czyszczenie po podaniu dobrej odpowiedzi
                    
                    if(nrPytania>=iloscPytan)   
                        {
                        ChangeTxt("Koniec pytań, dziękuję za grę :)");
                        show=true;
                        }      
                    }
                else if(nrPytania>=iloscPytan) while(q.size()!=0) q.remove();     //czyszczenie po ostatnim pytaniu
                else ChangeTxt(nazwaiOdpowiedz[0]+" odpowiedział błędnie :/ \n");
                }
            }
        });
        
        thread1.start();
        thread2.start();
    }
}
