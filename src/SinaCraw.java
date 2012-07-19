

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

public class SinaCraw {
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Options options = new Options();
		options.addOption("h", false, "Lists short help");
		options.addOption("r", false, "craw retweets");
		options.addOption("c", false, "craw comments");
		options.addOption("u", true, "username");
		options.addOption("p", true, "password");
		options.addOption("url", true, "address");
		options.addOption("id", false, "crawl user id");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		Sina sina = new Sina();
		ArrayList<String> al = new ArrayList<String>();

		if (cmd.hasOption("h")) {
			System.out.println("this is a h");
		} 
		else if ((cmd.hasOption("r") ^ cmd.hasOption("c"))
				&& cmd.getOptionValue("u") != null
				&& cmd.getOptionValue("p") != null
				&& cmd.getOptionValue("url") != null) {
			System.out.println("Starting Login ...");
			sina.login(cmd.getOptionValue("u"), cmd.getOptionValue("p"));
			String name = cmd.getOptionValue("url").replace("http://", "")
					.replace("weibo.com/", "").replace("/", "_")
					+ "_";
			String typeS = "";
			String typeL = "";
			if (cmd.hasOption("r")) {
				sina.setR();
				typeS = "r";
				typeL = "Retweeters";
//				al.addAll(sina.crawUerID(cmd.getOptionValue("url")));
//				System.out.println("Retweeters : " + al.size());
//				WriteContend(al, "r_" + name + System.currentTimeMillis()
//						/ 1000);
			}else if (cmd.hasOption("c")) {
				sina.setC();
				typeS = "c";
				typeL = "Comments";
//				al.addAll(sina.crawUerID(cmd.getOptionValue("url")));
//				System.out.println("Comments : " + al.size());
//				WriteContend(al, "c_" + name + System.currentTimeMillis()
//						/ 1000);
			}
			
			if(cmd.hasOption("id")){
				al.addAll(sina.crawUerID(cmd.getOptionValue("url")));
			}
			WriteContend(al, typeS + "_" + name + System.currentTimeMillis()
					/ 1000);
		} else
			System.out.println("Parameter wrong");

	}

	private static void WriteContend(ArrayList<String> al, String name)
			throws IOException {
		FileWriter fw = new FileWriter(name);
		BufferedWriter bw = new BufferedWriter(fw);
		for (String c : al)
			bw.write(c + "\n");
		bw.close();
	}
}
