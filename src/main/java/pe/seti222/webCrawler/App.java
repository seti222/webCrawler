package pe.seti222.webCrawler;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;

import pe.seti222.webCrawler.connector.AladinConnetor;

/**
 * Hello world!
 *
 */
public class App 
{
	static String userId = "";
	static String password = "";
    public static void main( String[] args )
    {
        System.out.println( "Aladin Crawler start" );
        
        AladinConnetor con = new AladinConnetor(userId, password);
        try {
			System.out.println(con.getOrderList());
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
}
