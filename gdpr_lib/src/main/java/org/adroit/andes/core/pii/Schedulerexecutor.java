package org.adroit.andes.core.pii;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Schedulerexecutor {

	public static void main(String[] args) {

		ScheduledExecutorService execService

				= Executors.newScheduledThreadPool(1);

		execService.scheduleAtFixedRate(() -> {

			String html = "http://mcc-mnc.com/";
			BufferedWriter output = null;
			try {
				Document doc = Jsoup.connect(html).get();
				Elements tableElements = doc.select("table");
				File file = new File("imsi_validation_codes.csv");
				output = new BufferedWriter(new FileWriter(file));
				//Elements tableHeaderEles = tableElements.select("thead tr th");
				Elements tableRowElements = tableElements.select(":not(thead) tr");
				for (int i = 0; i < tableRowElements.size(); i++) {
					Element row = tableRowElements.get(i);
					Elements rowItems = row.select("td");
					for (int j = 0; j < rowItems.size(); j++) {
						output.write(rowItems.get(j).text());
						output.write(",");
					}
					output.write("\n");
				}
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}, 0, 2592000, TimeUnit.SECONDS);

	}

}
