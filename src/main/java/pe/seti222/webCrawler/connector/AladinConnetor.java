package pe.seti222.webCrawler.connector;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import pe.seti222.webCrawler.domain.OrderInfo;

public class AladinConnetor {
	final private static String LoginUrl = "http://www.aladin.co.kr/login/wlogin.aspx?returnurl=/";
	final private static String OrderUrl = "http://www.aladin.co.kr/account/wmaininfo.aspx?pType=OrdersHistoryList";
	final private static String OrderDetailUrl = "https://www.aladin.co.kr/account/wordersinfo.aspx";
	final private static String OrderDateClass = "td_date myacc_td03";
	
	final private static String PriceStr = "총 주문 금액";
	final private static String SalePriceStr = "실 결제 금액";
	
	String password;
	String userId;
	CloseableHttpClient httpclient;
	BasicCookieStore cookieStore;
	public AladinConnetor(String userId, String password){
		this.userId = userId;
		this.password = password;
		cookieStore = new BasicCookieStore();
        httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();
	}
	
	public List<OrderInfo> getOrderList() throws ClientProtocolException, IOException, URISyntaxException{
		List<OrderInfo> orderList  = null;

        //getSiteCookie();
		
        siteLogin();
        
        orderList = getSiteOrderList(); 
		httpclient.close();
		return orderList;
	}

	private List<OrderInfo> getSiteOrderList() throws IOException,
			ClientProtocolException {
		List<OrderInfo> orderList;
		HttpGet orderUrl = new HttpGet(OrderUrl);

        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

            public String handleResponse(
                    final HttpResponse response) throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }

        };
        try {
            String responseBody = httpclient.execute(orderUrl, responseHandler);
            orderList = parseOrder(responseBody);
            for(int i=0;i<orderList.size();i++)
            	this.setOrderPrice(orderList.get(i));
        } finally {
        	//response3.close();
        }
		return orderList;
	}

	private void siteLogin() throws URISyntaxException, IOException,
			ClientProtocolException {
		HttpUriRequest login = RequestBuilder.post()
                .setUri(new URI(LoginUrl))
                .addParameter("Email", this.userId)
                .addParameter("Password", this.password)
                .addParameter("Action","1")
                .addParameter("fbAppId","1")
                .addParameter("x","63")
                .addParameter("y","24")
                .build();
        setHeader(login);
        CloseableHttpResponse response2 = httpclient.execute(login);
        try {
            HttpEntity entity = response2.getEntity();
            int status = response2.getStatusLine().getStatusCode();

            System.out.println("Login form get: " + response2.getStatusLine());
            EntityUtils.consume(entity);
            /*Header [] h1 = response2.getAllHeaders();
            for(int i=0;i<h1.length;i++){
            	System.out.println("Header ["+i+"] name="+h1[i].getName()+"\t  value="+h1[i].getValue());
            }
            System.out.println("Post logon cookies:");
            List<Cookie> cookies = cookieStore.getCookies();
            if (cookies.isEmpty()) {
                System.out.println("None");
            } else {
                for (int i = 0; i < cookies.size(); i++) {
                    System.out.println("- " + cookies.get(i).toString());
                }
            }
            if (status >= 200 && status < 300) System.out.println(EntityUtils.toString(entity));            //response2.
            */
        } finally {
            response2.close();
        }
	}

	@SuppressWarnings("unused")
	private void getSiteCookie() throws IOException, ClientProtocolException {
		HttpGet httpget = new HttpGet(LoginUrl);

        CloseableHttpResponse response1 = httpclient.execute(httpget);
        try {
            HttpEntity entity = response1.getEntity();

            System.out.println("Login form get: " + response1.getStatusLine());
            EntityUtils.consume(entity);
            Header [] h1 = response1.getAllHeaders();
            for(int i=0;i<h1.length;i++){
            	System.out.println("Header ["+i+"] name="+h1[i].getName()+"\t  value="+h1[i].getValue());
            }
            System.out.println("Initial set of cookies:");
            List<Cookie> cookies = cookieStore.getCookies();
            if (cookies.isEmpty()) {
                System.out.println("None");
            } else {
                for (int i = 0; i < cookies.size(); i++) {
                    System.out.println("- " + cookies.get(i).toString());
                }
            }
        } finally {
            response1.close();
        }
	}

	private void setHeader(HttpUriRequest header) {
		header.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		header.addHeader("Accept-Language", "ko-KR,ko;q=0.8,en-US;q=0.6,en;q=0.4");
		header.addHeader("Accept-Encoding", "gzip, deflate, sdch");
		header.addHeader("Connection", "keep-alive");
		header.addHeader("Cache-Control","max-age=0");
		header.addHeader("Host","www.aladin.co.kr");
		header.addHeader("Content-Type", "application/x-www-form-urlencoded");
		header.addHeader("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36");
	}
	
	private List<OrderInfo> parseOrder(String body){
		List<OrderInfo> orderList  = new ArrayList<OrderInfo>();
		String result = body.substring(body.indexOf("class=\"account_table\""),body.indexOf("</form"));
		
		int idx = result.indexOf(OrderDateClass);
		int endIdx = 0;
		if(idx>0){
			while( idx >0 ) {
				OrderInfo order = new OrderInfo();
				endIdx = result.indexOf("</td>", idx);
				String dateStr = result.substring(idx+OrderDateClass.length()+2,endIdx);
				order.setDateStr(dateStr);
				
				idx = result.indexOf(OrderDetailUrl,idx);
				endIdx = result.indexOf("'>",idx);
						
				String linkStr = result.substring(idx+OrderDetailUrl.length(),endIdx);;
				order.setOrderLink(linkStr);
				orderList.add(order);
				
				idx = result.indexOf(OrderDateClass,idx);
			}
		}
		return orderList;
	}
	private void setOrderPrice(OrderInfo info) throws ClientProtocolException, IOException{
		HttpGet orderDetail = new HttpGet(OrderDetailUrl+info.getOrderLink());
		
		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

            public String handleResponse(
                    final HttpResponse response) throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }

        };
        
        String str = httpclient.execute(orderDetail, responseHandler);
        int idx = str.indexOf(PriceStr);
        int endIdx = 0;
        idx = str.indexOf("<strong>",idx);
        endIdx = str.indexOf("</strong>",idx);
        info.setPrice(str.substring(idx+8,endIdx).replace(",", ""));
        
        
        idx = str.indexOf(SalePriceStr);
        endIdx = 0;
        idx = str.indexOf("<span",idx);
        idx = str.indexOf(">",idx);
        endIdx = str.indexOf("</span>",idx);
        info.setSalePrice(Integer.parseInt(str.substring(idx+1,endIdx).replace(",", "")));
       
	}
}
