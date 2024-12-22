import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Serveur_secondaire_recup
{
    private int port;
    private String ip;
    private ServerSocket serv;

    public static String[] find_ip()
    {
        int nbr=Integer.parseInt(Function_config.config("./config/config.txt","ISANY"));
        String[] retour=new String[nbr];
        for(int i=1 ; i<=nbr ; i++)
        {
            retour[i-1]=Function_config.config("./config/config.txt","IP_"+i);
        }
        return retour;
    }

    public static int[] find_port()
    {
        int nbr=Integer.parseInt(Function_config.config("./config/config.txt","ISANY"));
        int[] retour=new int[nbr];
        for(int i=1 ; i<=nbr ; i++)
        {
            retour[i-1]=Integer.parseInt(Function_config.config("./config/config.txt","PORT_"+i));
        }
        return retour;
    }

    public void set_port(int port)
    {
        this.port=port;
    }

    public void set_ip(String ip)
    {
        this.ip=ip;
    }

    public Serveur_secondaire_recup(int port,String ip) throws IOException
    {
        this.set_port(port);
        this.set_ip(ip);
        this.serv=new ServerSocket(this.port,10,InetAddress.getByName(this.ip));
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

    public String get_message_principale(Socket client)
    {
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            String clientMessage = in.readLine();
            in.close();
            out.close();
            client.close();

            return clientMessage;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return "erreur de reception";
        }
    }

    public String envoie(int port,String serverAddress,int nbr)
    {
        Socket client_srv=this.pair();
        String filename=this.get_message_principale(client_srv);
        Serveur_principale.close(client_srv);
        filename=Function_config.config("./config/config.txt","PATH_RECUP_"+nbr)+filename;
        String retour=new String("erreur de transfert");
        File fichier=new File(filename);

            try
            {
                Socket socket = new Socket(serverAddress, port);
                 if(!fichier.exists())
                {
                    retour="le fichier n'existe pas"+filename;
                    Serveur_principale.close(socket);
                    return retour;
                }
                System.out.println("transfert en cours..."+filename);
                try
                {
                    FileInputStream fileInputStream = new FileInputStream(filename);
                    OutputStream outputStream = socket.getOutputStream();

                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) 
                    {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    outputStream.flush();
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
    public static void get_secondaire(int nbr)
    {
        try
        {
            String[] ip_2=Serveur_secondaire_recup.find_ip();
            int[] port_2=Serveur_secondaire_recup.find_port();

            Serveur_secondaire_recup serv_2=new Serveur_secondaire_recup(port_2[nbr-1],ip_2[nbr-1]);
            int port_p=Integer.parseInt(Function_config.config("./config/config.txt","PORT_PRINCIPALE_RECUP"));
            String ip=Function_config.config("./config/config.txt","IP_SERVEUR");
            String message2=serv_2.envoie(port_p,ip,nbr);
        }
        catch(IOException e)
        {

            e.printStackTrace();
        }
    }
    public static void main(String[] args)
    {
        if (args.length > 0) 
        {
            int nbr=Integer.parseInt(args[0]);
            Serveur_secondaire_recup.get_secondaire(nbr);
        } 
        else 
        {
            System.out.println("Aucun argument passé\nvailler mettre un numero");
        }
        
    }
}