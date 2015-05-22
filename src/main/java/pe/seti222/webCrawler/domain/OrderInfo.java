package pe.seti222.webCrawler.domain;

public class OrderInfo {
	String dateStr;
	String orderLink;
	int price =0;
	int salePrice   =0;
	public String getDateStr() {
		return dateStr;
	}
	public void setDateStr(String dateStr) {
		this.dateStr = dateStr;
	}
	public String getOrderLink() {
		return orderLink;
	}
	public void setOrderLink(String orderLink) {
		this.orderLink = orderLink;
	}
	
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public void setPrice(String s) {
		try {
			this.price = Integer.parseInt(s);
		}catch(Exception e){
			this.price =0;
		}
	}
	public int getSalePrice() {
		return salePrice;
	}
	public void setSalePrice(int salePrice) {
		this.salePrice = salePrice;
	}
	@Override
	public String toString() {
		return "OrderInfo [dateStr=" + dateStr + ", orderLink=" + orderLink
				+ ", price=" + price + ", salePrice=" + salePrice + "]\n";
	}
	
	
	
}
