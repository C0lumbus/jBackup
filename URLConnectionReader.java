import java.net.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by mac_user on 21.06.14.
 */
public class URLConnectionReader {
    URL sypexURL;
    public String output;
    public ArrayList files;


    public URLConnectionReader(String backupURL) throws Exception {
        sypexURL = new URL(backupURL);

        URLConnection sypexConn = sypexURL.openConnection();

        StringBuilder builder = new StringBuilder();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        sypexConn.getInputStream()));
        String inputLine;

        files = new ArrayList();

        while ((inputLine = in.readLine()) != null) {
            builder.append(inputLine);

            files.add(inputLine.toString());
        }

        output = builder.toString();


        in.close();
    }
}
