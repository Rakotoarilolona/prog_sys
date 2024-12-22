import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur_principale 
{
    private int port;
    private ServerSocket serv;

    public static String get_action()
    {
        int port_p=7890;
        Serveur_principale s=new Serveur_principale(port_p);
        Socket client=s.pair();
        String message=s.get_message_client(client);
        Serveur_principale.close(client);
        s.close_server();
        return message;
    }
    public static void main(String[] args)
    {
        String act=get_action();
        action(act);
    }
    public static int[] divise(int nbr,int taille)
    {
        int[] retour=new int[taille];
        int rep=0;
        for(int i=0 ; i<taille-1 ; i++)
        {
            retour[i]=(int)(nbr/taille);
            rep=rep+retour[i];
        }
        retour[taille-1]=nbr-rep;
        return retour;
    }
    public static void action(String act) 
    {
        if (act.equals("put")) 
        {
            int port =Integer.parseInt(Function_config.config("config/config.txt","PORT_PRINCIPALE_PUT"));
            try (ServerSocket serverSocket = new ServerSocket(port)) 
            {
                System.out.println("Serveur principal en attente de connexion...");

                while (true) 
                {
                    try (
                            Socket clientSocket = serverSocket.accept();
                            DataInputStream dataa = new DataInputStream(clientSocket.getInputStream())
                        ) 
                        {

                            String filename = dataa.readUTF();
                            File outputfile = new File(filename);

                            System.out.println("Client connecté. Réception du fichier... " + filename);
                            try
                            {
                                InputStream inputStream = clientSocket.getInputStream();
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                byte[] buffer = new byte[4096]; // Taille du tampon
                                int bytesRead;

                                while ((bytesRead = inputStream.read(buffer)) != -1) 
                                {
                                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                                }
                                byte[] end=byteArrayOutputStream.toByteArray();
                                divideAndSend(filename,end, Integer.parseInt(Function_config.config("config/config.txt","isany")));
                            }
                            catch (IOException e) 
                            {
                                System.out.println("Erreur lors de la réception : " + e.getMessage());
                            }
                            //  Serveur.get(7000, (Function_config.config("config/config.txt","ip_serveur")), Integer.parseInt(Function_config.config("config/config.txt","PORT_SECOND")), 3);
                            clientSocket.close();
                        }
                        catch (IOException e) 
                        {
                            e.printStackTrace();
                        }

                }
            
            } 
            catch (IOException e) 
            {
                System.err.println("Erreur lors de l'envoi au serveur secondaire : " + e.getMessage());
                e.printStackTrace();
            }
        }
        if(act.equals("get"))
        {
             Serveur_principale.get();
            // Appel de la méthode get() de Serveur
        }
    }  
    

    private static void divideAndSend(String file,byte[] longueur, int numberOfParts) throws IOException 
    {
        System.out.println("Taille du fichier : " + longueur.length + " caractères.");
        int[] repartition=divise(longueur.length,numberOfParts);
        // Découper le contenu en parties et écrire dans des fichiers séparés´
        int start=0;
        int evolution=0;
        for (int i = 0; i < numberOfParts; i++) 
        {
            evolution=evolution+repartition[i];
            byte[] sec=new byte[repartition[i]];
            int rep=0;
            for(int j=start ; j<evolution ; j++)
            {
                sec[rep]=longueur[j];
                rep=rep+1;
            }
            String partFileName = "received_part" + (i + 1) + file;
            System.out.println("Partie " + (i + 1) + " créée avec " + repartition[i] + " caractères.");

            // Envoyer la partie au serveur secondaire
            int num=i+1;
            sendToSecondaryServer(partFileName,sec,(Function_config.config("config/config.txt","IP_"+num)), Integer.parseInt(Function_config.config("config/config.txt","PORT_PUT_"+num)));
            start=start+repartition[i];
        }
    }
   
    private static void sendToSecondaryServer(String filename,byte[] content, String serverHost, int serverPort) 
    {
        System.out.println("Envoi de " + filename + " au serveur secondaire " + serverPort + "...");
        try
        {/*
            
*/
            Socket socket = new Socket(serverHost, serverPort);
            OutputStream out = socket.getOutputStream();
            out.write(content);
            out.flush();
            out.close();

            Socket socket2 = new Socket(serverHost, serverPort);
            PrintWriter outw = new PrintWriter(socket2.getOutputStream(), true);
            outw.println(filename);
            outw.flush();
            outw.close();
            System.out.println("Partie envoyée au serveur secondaire " + serverPort + ".");
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }

        
    }
    public void set_port(int port)
    {
        this.port=port;
    }

    public Serveur_principale(int port)
    {
        this.set_port(port);
        try
        {
            this.serv=new ServerSocket(this.port);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        
    }

    public void close_server()
    {
        try
        {
            this.serv.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    public Socket pair()
    {
        try
        {
            Socket client=this.serv.accept();
            return client;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public String get_message_client(Socket client)
    {
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            String clientMessage = in.readLine();
            //in.close();
            //out.close();
            //client.close();

            return clientMessage;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return "erreur de reception";
        }
        
    }

    public int send_message(int partie,int port,String serverAddress,String filename)
    {
        String retour="/received_part"+partie+filename;
        try
        {
            Socket socket = new Socket(serverAddress, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            out.println(retour);
            return 1;
        }
        catch (IOException e) 
        {
            return 0;
        }
    }

    public byte[] receiveFile(Socket socket) 
    {
        try (InputStream inputStream = socket.getInputStream()) 
        {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096]; // Taille du tampon
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) 
            {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            return byteArrayOutputStream.toByteArray();

        } 
        catch (IOException e) 
        {
            System.err.println("Erreur lors de la réception du fichier : " + e.getMessage());
            return new byte[0]; // Retourne un tableau vide en cas d'erreur
        }
    }

    public byte[] end(byte[][] fichier)
    {
        int taille=0;
        for(int i=0 ;i<fichier.length ; i++)
        {
            taille=taille+fichier[i].length;
        }
        byte[] file=new byte[taille];
        int rep=0;
        for(int i=0 ; i<fichier.length ; i++)
        {
            for(int j=0 ; j<fichier[i].length ; j++)
            {
                file[rep]=fichier[i][j];
                rep=rep+1;
            }
        }
        return file;
    }

    public void create_file(byte[] file,String filename)
    {
        try (FileOutputStream fos = new FileOutputStream(filename)) 
        {
            fos.write(file);
            System.out.println("Fichier créé avec succès !");
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    public String envoie(byte[] buffer)
    {
        int port=Integer.parseInt(Function_config.config("./config/config.txt","PORT_CLIENT"));
        String serverAddress=Function_config.config("./config/config.txt","IP_CLIENT");
        String retour=new String("Fichier non envoye!!");
            try
            {
                Socket socket = new Socket(serverAddress, port);
                
                System.out.println("transfert final en cours...");
                try
                {
                    OutputStream out = socket.getOutputStream();
                    out.write(buffer);
                    out.flush();
                    retour="Fichier envoyé";
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    Serveur_principale.close(socket);
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            return retour;
    }

    public static void close(Socket s)
    {
        try
        {
            s.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    public static Socket set_socket(String ip,int port)
    {
        try
        {
            Socket socket_srv=new Socket("localhost",port);
            return socket_srv;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static void get()
    {

        String[] ip_2=Serveur_secondaire_recup.find_ip();
        int[] port_2=Serveur_secondaire_recup.find_port();

        int port_p=Integer.parseInt(Function_config.config("./config/config.txt","PORT_PRINCIPALE_RECUP"));
        String ip=Function_config.config("./config/config.txt","IP_SERVEUR");

        int nbr=Integer.parseInt(Function_config.config("./config/config.txt","ISANY"));
        
        Serveur_principale s=new Serveur_principale(port_p);
        Socket client=s.pair();
        String message=s.get_message_client(client);
        Serveur_principale.close(client);
        byte[][] file=new byte[nbr][];
        
        String message2=new String();
        for(int i=0 ; i<nbr ; i++)
        {
                //Serveur_secondaire_recup serv_2=new Serveur_secondaire_recup(port_2[i],ip_2[i]);       
                int j=s.send_message(i+1,port_2[i],ip_2[i],message);
                if(j==1)
                {
                    Socket end_reception=s.pair();
                    file[i]=s.receiveFile(end_reception);
                    
                    int num=i+1;
                    System.out.println("reception sur le serveur secondaire "+num+"\nport="+port_2[i]+"\nip="+ip_2[i]);

                }
                else
                {
                    file[i]=new byte[0];
                    int num=i+1;
                    System.out.println("le serveur secondaire "+num+" est ferme\nport="+port_2[i]+"\nip="+ip_2[i]);
                }
             //message2=serv_2.envoie(port_p,ip,i+1);
                    
        }
        byte[] rep=s.end(file);
        String message3=s.envoie(rep);
        System.out.println(message3);
    }
}

// Classe Serveur indépendante
