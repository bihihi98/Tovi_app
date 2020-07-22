package datnd;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppCrawl {

	public static void main(String[] args) {
//		final Logger logger = LogManager.getLogger(AppCrawl.class);
		File logFile = new File("/home/TOVI_App/CrawlNewVersion/logs/log.txt");
		List<AppInfo> listApp = new LinkedList<AppInfo>();
		try {
			Connection connection = connectJDBC.getSQLServerConnection();
			if (connection != null) {
				System.out.println("Connection success!");
			} else {
				System.out.println("Connection false");
			}
			Statement statement = connection.createStatement();
//			for (int i = 0; i < 200; i++) {
//				try {
			String sqlSelect = "Select * from list_app limit 0,100000";

			ResultSet rs = statement.executeQuery(sqlSelect);

			while (rs.next()) {
				String appId = rs.getString("appid");
				int id = rs.getInt("id");
				int uploadDate = rs.getInt("uploadDate");
				String version = rs.getString("version");
				AppInfo app = new AppInfo(id, appId, version, uploadDate);
				listApp.add(app);
			}
//				} catch (Exception e) {
//					logger.error(e);
//				}

			ExecutorService executor = Executors.newFixedThreadPool(50);
			for (AppInfo app : listApp) {
				titleCrawl thread = new titleCrawl(app.getId(), app.getAppId(), app.getUploadDate(), connection,
						logFile);
				executor.execute(thread);
			}
			executor.shutdown();
			listApp.clear();
			while (executor.isTerminated()) {

			}
//			}
			statement.close();
		} catch (Exception e) {
			try {
				FileWriter fw = new FileWriter(logFile);
				fw.write("Loi ---> " + e.getMessage() + "\n");
				fw.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
//			logger.error(e);
		}
	}
}