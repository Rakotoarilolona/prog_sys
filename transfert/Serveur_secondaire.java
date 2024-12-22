import java.io.*;
import java.net.*;
import java.net.Socket;

public class Serveur_secondaire 
{
    public static String get_message(int nbr2,ServerSocket reception)
    {
        int port=Integer.parseInt(Function_config.config("./config/config.txt","PORT_PUT_"+nbr2));
        try
        {
            Socket client=reception.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            String clientMessage = in.readLine();
            client.close();
            return clientMessage;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return "erreur de reception";
        }
    }
    public static byte[] get_byte(int nbr2,ServerSocket reception)
    {
        int port=Integer.parseInt(Function_config.config("./config/config.txt","PORT_PUT_"+nbr2));
        
        try 
        {
            Socket socket=reception.accept();
            InputStream inputStream = socket.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096]; // Taille du tampon
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) 
            {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            socket.close();
            return byteArrayOutputStream.toByteArray();

        } 
        catch (IOException e) 
        {
            System.err.println("Erreur lors de la réception du fichier : " + e.getMessage());
            return new byte[0]; // Retourne un tableau vide en cas d'erreur
        }
    }
    public static void create_file(byte[] file,String filename)
    {

        try (FileOutputStream fos = new FileOutputStream(filename)) 
        {
            int taille=file.length;
            fos.write(file);
        }
        catch (IOException e) 
        {
            System.out.println("Erreur de reception du fichier demandé");
        }
    }

    public static void main(String[] args) 
    {
        if (args.length < 1) 
        {
            System.err.println("Usage : java Serveur_secondaire <port>");
            return;
        }

        int nbr2 = Integer.parseInt(args[0]);
        int port=Integer.parseInt(Function_config.config("./config/config.txt","PORT_PUT_"+nbr2));
        String chemin=Function_config.config("./config/config.txt","PATH_RECUP_"+nbr2);
        try
        {
            ServerSocket reception=new ServerSocket(port);
            String filename=new String("test");
            
            byte[] end=get_byte(nbr2,reception);
            filename=get_message(nbr2,reception);
            filename=chemin+"/"+filename;
            create_file(end,filename);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
                
    }
}
