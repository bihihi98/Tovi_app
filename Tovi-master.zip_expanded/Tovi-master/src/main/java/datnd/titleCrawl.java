package datnd;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class titleCrawl implements Runnable {
//	private final Logger logger = LogManager.getLogger(titleCrawl.class);
	private File logFile;
	private Connection con;
	private readFile readFile = new readFile();
	private String appId;
	private int uploadDate;
	private int id;
	private String version;
	private String fileUrl;
	private Document doc = null;
	private String html = null;
	private String url;
	private FileWriter fileWriter;
	private FileWriter fw;

	public titleCrawl(int id, String appId, int uploadDate, Connection con, File file) {
		super();
		this.id = id;
		this.appId = appId;
		this.uploadDate = uploadDate;
		this.con = con;
		this.logFile = file;
		this.fileUrl = "/home/TOVI_App/CrawlNewVersion/html/" + this.id + ".html";
		this.url = "https://play.google.com/store/apps/details?id=" + this.appId + "&hl=us";
	}

	public void run() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		String datenow = dtf.format(now);
		try {
			FileWriter fw = new FileWriter(logFile, true);
			try {
				doc = Jsoup.connect(url).get();
				html = doc.html();
				try {
					fileWriter = new FileWriter(fileUrl);
					fileWriter.write(html);
				} catch (Exception e) {
					fw.write("Loi ghi file ---> " + e.getMessage() + "\n");
//				logger.error("Loi ghi file ---> ", e);
				}
				readFile.readFile1(fileUrl, fw);
				if (uploadDate < readFile.getDate()) {
					version = readFile.getVersion();
					System.out.println("co ban moi: " + version);
//					Date d = new Date((long) readFile.getDate() * 1000);
//					DateFormat f = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.mmm' '");
//					System.out.println("Ngay cap nhat: " + f.format(d) + "\n");
				}
				try {
					String sqlInsert = "Insert into newversion (appid, upload_date, version, upload_date_new, status, created_at)"
							+ "value(?, ?, ?, ?, ?, ?)";
					PreparedStatement st = con.prepareStatement(sqlInsert);
					st.setString(1, url);
					st.setDate(2, new Date((long) uploadDate * 1000));
					st.setString(3, version);
					st.setDate(4, new Date((long) readFile.getDate() * 1000));
					st.setInt(5, 1);
					st.setString(6, datenow);
					st.executeUpdate();
					st.close();
				} catch (Exception e) {
					fw.write("Loi insert DB ---> " + e.getMessage() + "\n");
//				logger.error("Loi insert DB ---> ", e);
				}
				fw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			try {
				fw.write("Loi app da bi xoa ---> " + e.getMessage() + "\n");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
//			logger.error("App bi xoa khoi CHPlay ---> ", e);
		}
	}
}
