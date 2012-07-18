import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;

import org.apache.http.message.BasicNameValuePair;

import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Sina {

	private DefaultHttpClient client;
	private Document doc;
	private String commentadd = "http://weibo.com/aj/comment/big?";
	private String retweetadd = "http://weibo.com/aj/mblog/info/big?";
	private static String urlbase;

	public void setR() {
		urlbase = retweetadd;
	}

	public void setC() {
		urlbase = commentadd;
	}

	public ArrayList<String> crawContent(String url) throws Exception {
		String json = null;
		String html = null;
		ArrayList<String> al = new ArrayList<String>();
		ArrayList<String> rst = new ArrayList<String>();

		String weiboid = extractWeiboID(url);
		al = getAimUrls(weiboid);
		for (String sunurl : al) {
			json = getJson(urlbase + sunurl);
			html = extractHtmlFromJson(json);
			rst.addAll(extractContents(html));
		}
		return rst;
	}

	public ArrayList<String> crawUerID(String url) throws Exception {
		String json = null;
		String html = null;
		ArrayList<String> al = new ArrayList<String>();
		ArrayList<String> rst = new ArrayList<String>();

		String weiboid = extractWeiboID(url);
		al = getAimUrls(weiboid);
		for (String sunurl : al) {
			json = getJson(urlbase + sunurl);
			html = extractHtmlFromJson(json);
			rst.addAll(extractUserIDs(html));
		}
		return rst;
	}

	private String extractWeiboID(String url) {
		String content = getPageByUrl(url);
		Pattern p = Pattern.compile("mid=\\\\\"(.*?)\\\\\"");
		Matcher m = p.matcher(content);
		if (m.find()) {
			return m.group(1);
		}
		return null;
	}

	private ArrayList<String> getAimUrls(String weiboid) throws JSONException,
			IOException {
		String json = getJson(urlbase + "id=" + weiboid);
		String html = extractHtmlFromJson(json);
		doc = Jsoup.parse(html);
		ArrayList<String> tem = extractMultPages(html);
		if (tem.size() == 0)
			tem.add("id=" + weiboid);
		return tem;
	}

	private ArrayList<String> extractMultPages(String html) {
		ArrayList<String> nextPageAdd = new ArrayList<String>();
		Elements emt = doc.getElementsByClass("W_pages");
		for (Element e : emt) {
			Elements link = e.select("a[action-data]");
			int num = link.size();
			String tail = link.get(num - 1).attr("action-data");
			Pattern pattern = Pattern.compile("(.+?)page=(.*)");
			Matcher m = pattern.matcher(tail);
			String pt1 = null;
			int maxpg = 0;
			if (m.find()) {
				pt1 = m.group(1);
				maxpg = Integer.parseInt(m.group(2));
			}
			for (int i = 0; i <= maxpg; i++) {
				nextPageAdd.add(pt1 + "page=" + i);
			}
		}
		return nextPageAdd;
	}

	private ArrayList<String> extractUserIDs(String html) {
		ArrayList<String> contents = new ArrayList<String>();
		doc = Jsoup.parse(html, "UTF-8");
		Elements emt = doc.getElementsByClass("comment_list");
		for (Element e : emt) {
			contents.add(e.select("dt > a").attr("href").replaceAll("/", ""));
		}
		return contents;
	}

	private ArrayList<String> extractContents(String html) {
		ArrayList<String> contents = new ArrayList<String>();
		doc = Jsoup.parse(html);
		Elements emt = doc.getElementsByClass("comment_list");
		for (Element e : emt) {
			contents.add(e.text().substring(0, e.text().lastIndexOf("(")));
		}
		return contents;
	}

	private String extractHtmlFromJson(String content) throws JSONException,
			IOException {
		JSONObject jsonObj = new JSONObject(content);
		String html = jsonObj.getJSONObject("data").getString("html");
		FileWriter fw = new FileWriter("tt");
		fw.write(html);
		fw.close();
		return html;
	}

	private String getJson(String url) {
		String content = getPageByUrl(url);
		return content;
	}

	@SuppressWarnings("deprecation")
	public boolean login(String username, String password) {
		client = new DefaultHttpClient();
		client.getParams().setParameter("http.protocol.cookie-policy",
				CookiePolicy.BROWSER_COMPATIBILITY);
		client.getParams().setParameter(
				HttpConnectionParams.CONNECTION_TIMEOUT, 5000);
		try {
			HttpPost post = new HttpPost(
					"http://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.3.19)");

			String data = getServerTime();

			String nonce = makeNonce(6);

			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("entry", "weibo"));
			nvps.add(new BasicNameValuePair("gateway", "1"));
			nvps.add(new BasicNameValuePair("from", ""));
			nvps.add(new BasicNameValuePair("savestate", "7"));
			nvps.add(new BasicNameValuePair("useticket", "1"));
			nvps.add(new BasicNameValuePair("ssosimplelogin", "1"));
			nvps.add(new BasicNameValuePair("su", encodeAccount(username)));
			nvps.add(new BasicNameValuePair("service", "miniblog"));
			nvps.add(new BasicNameValuePair("servertime", data));
			nvps.add(new BasicNameValuePair("nonce", nonce));
			nvps.add(new BasicNameValuePair("pwencode", "wsse"));
			nvps.add(new BasicNameValuePair("sp", new SinaSSOEncoder().encode(
					password, data, nonce)));

			nvps.add(new BasicNameValuePair(
					"url",
					"http://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack"));
			nvps.add(new BasicNameValuePair("returntype", "META"));
			nvps.add(new BasicNameValuePair("encoding", "UTF-8"));
			nvps.add(new BasicNameValuePair("vsnval", ""));

			post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

			HttpResponse response = client.execute(post);
			String entity = EntityUtils.toString(response.getEntity());

			String url = entity.substring(
					entity.indexOf("http://weibo.com/ajaxlogin.php?"),
					entity.indexOf("code=0") + 6);

			// 获取到实际url进行连接
			HttpGet getMethod = new HttpGet(url);

			response = client.execute(getMethod);
			entity = EntityUtils.toString(response.getEntity());
			entity = entity.substring(entity.indexOf("userdomain") + 13,
					entity.lastIndexOf("\""));

			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}

	private String getPageByUrl(String url) {
		HttpGet getMethod = new HttpGet(url);
		HttpResponse response;
		try {
			response = client.execute(getMethod);
			String entity = EntityUtils.toString(response.getEntity());
			return entity;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	private static String encodeAccount(String account) {
		String userName = "";
		try {
			userName = Base64.encodeBase64String(URLEncoder.encode(account,
					"UTF-8").getBytes());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userName;
	}

	private static String makeNonce(int len) {
		String x = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		String str = "";
		for (int i = 0; i < len; i++) {
			str += x.charAt((int) (Math.ceil(Math.random() * 1000000) % x
					.length()));
		}
		return str;
	}

	private static String getServerTime() {
		long servertime = new Date().getTime() / 1000;
		return String.valueOf(servertime);
	}
}