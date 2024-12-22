import java.io.BufferedReader;
import java.io.FileReader;

public class Function_config {
    public static String config(String cheminversconfig, String valeurraisina) {
        try {
            String result = "";
            FileReader fil = new FileReader(cheminversconfig);
            try (BufferedReader br = new BufferedReader(fil)) {
                String line; //exemple PORT :444
                while ((line=br.readLine())!=null) {
                    String[] val= line.split(" ");
                    if (val[0].equalsIgnoreCase(valeurraisina)) {
                        result=val[val.length-1];
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
