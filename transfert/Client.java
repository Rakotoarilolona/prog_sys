import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;
import java.nio.file.Files;

public class Client 
{
    public static void main(String[] args) 
    {
        int rep=0;
        String valiny=new String();
        while(rep==0)
        {
            Scanner mode=new Scanner(System.in);
            System.out.print("transfert get/put=");
            valiny=mode.nextLine();
            
            while(!valiny.equals("get") && !valiny.equals("put"))
            {
                System.out.print("veillez ecrire get ou put\n->");
                valiny=mode.nextLine();
            }
            rep=set_mode(valiny);
        }
       
       Scanner mode2=new Scanner(System.in);
       System.out.print("nom du fichier pour l'action "+valiny+"\n->");
       if(valiny.equals("put"))
       {
            int num=0;
            while(num==0)
            {
                String filename=mode2.nextLine();
                System.out.println("envoie du fichier au serveur...");
                File f=new File(filename);
                num=sendFileToServer(f);    
            }
            
            return;
       }
       if(valiny.equals("get"))
       {
            int num=0;
            while(num==0)
            {
                String filename=mode2.nextLine();
                System.out.println("envoie de la demande...");
                num=communication(filename);
            }
            
            return;
       }
    }

    private static int sendFileToServer(File file) 
    {
        // Charger les paramètres depuis la configuration*

        String serverHost = Function_config.config("config/config.txt", "ip_serveur");
        int serverPort = Integer.parseInt(Function_config.config("config/config.txt", "PORT_PRINCIPALE_PUT"));
    
        // Vérifier si le fichier est lisible avant de commencer
        if (!file.exists() || !file.isFile() || !file.canRead()) 
        {
            System.out.print("ERREUR: Le fichier sélectionné est introuvable, n'est pas un fichier valide ou ne peut pas être lu.\n->");
            return 0;
        }
        try 
        (
            Socket socket = new Socket(serverHost, serverPort); // Connexion au serveur
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            OutputStream outputStream = socket.getOutputStream();
            FileInputStream fileInputStream = new FileInputStream(file)
        ) 
        {
            // Étape 1 : Envoyer le nom du fichier
            String fileName = file.getName();
            dataOutputStream.writeUTF(fileName);
            dataOutputStream.flush();
             // Assurez-vous que les données sont envoyées immédiatement
            System.out.println("Nom du fichier envoyé : " + fileName);
    
            // Étape 2 : Envoyer la taille du fichier (optionnelle mais utile pour le serveur)
            long fileSize = file.length();

            System.out.println("Taille du fichier envoyée : " + fileSize + " octets");
    
            // Étape 3 : Envoyer le contenu du fichier
            System.out.println("Envoi du contenu du fichier...");
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) 
            {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();
            System.out.println("Contenu du fichier envoyé avec succès.");
    
            return 1;
        } 
        catch (IOException e) 
        {
            System.out.print("Erreur lors de l'envoi du fichier : " + e.getMessage()+"\n->"); 
            return 0;
        
        }
    }
    public static Socket pair(ServerSocket s)
    {
        try
        {
            Socket client=s.accept();
            return client;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    public static byte[] get_byte()
    {
        int port=Integer.parseInt(Function_config.config("./config/config.txt","PORT_CLIENT"));
        try
        {
            ServerSocket reception=new ServerSocket(port);
            Socket serveur=pair(reception);
            InputStream in = serveur.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[10000];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) 
            {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] fileData = baos.toByteArray();

            serveur.close();
            reception.close();

            return fileData;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return null;
        }
        
    }

    public static void create_file(byte[] file,String filename)
    {
        try (FileOutputStream fos = new FileOutputStream(filename)) 
        {
            int taille=file.length;
            if(taille==0)
            {
                System.out.println("ERREUR: Le fichier reçus est vide");
            }
            else
            {
                fos.write(file);
                System.out.println("Fichier recus avec succès !");
            }
            
        }
        catch (IOException e) 
        {
            System.out.println("Erreur de reception du fichier demandé");
        }
    }
    public static int set_mode(String valiny)
    {
        String serverAddress=Function_config.config("./config/config.txt","IP_SERVEUR");
        try (Socket socket = new Socket(serverAddress, 7890)) 
        {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(valiny);
            out.close();
            socket.close();
            return 1;
        }
        catch (IOException e) 
        {
            System.out.println("ERREUR: echec de connexion");
            return 0;
        }
    }
    public static int communication(String valiny) 
    {
        int port=Integer.parseInt(Function_config.config("./config/config.txt","PORT_PRINCIPALE_RECUP"));
        String serverAddress=Function_config.config("./config/config.txt","IP_SERVEUR");
        try (Socket socket = new Socket(serverAddress, port)) 
        {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(valiny);
        }
        catch (IOException e) 
        {
            System.out.print("ERREUR: echec de connexion\n->");
            return 0;
        }
        byte[] file=get_byte();
        String filename=Function_config.config("./config/config.txt","PATH") + valiny;

        create_file(file,filename);
        return 1;
    }
}
