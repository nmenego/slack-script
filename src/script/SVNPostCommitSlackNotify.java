package script;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import org.json.simple.JSONObject;

/**
 * Simple script that notifies Slack when a commit is made. This program is
 * meant to be called as an SVN post-commit script. Disclaimer: wrote this in
 * 10mins, please ignore code style. :D
 * 
 * @author nicomartin.enego
 *
 */
public class SVNPostCommitSlackNotify {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		if (args == null || args.length != 8) {
			System.err.println(
					"ERROR! req param: path, rev, author, message, subdomain, token, proxy, port");
			System.exit(-1);

		}

		// TODO add checking of required parameters and other input-related
		// processing
		String path = args[0];
		String rev = args[1];
		String author = args[2];
		String message = args[3];
		String subdomain = args[4];
		String token = args[5];
		String proxyUrl = args[6];
		String port = args[7];

		String url = String.format(
				"https://%s/services/hooks/subversion?token=%s", subdomain,
				token);

		// proxy: remove this if no proxy needed.
		Proxy proxy = null;
		if (!proxyUrl.isEmpty() && !port.isEmpty()) {
			proxy = new Proxy(Proxy.Type.HTTP,
					new InetSocketAddress(proxyUrl, Integer.parseInt(port)));
		}
		System.out.println("request to -> " + url);

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection(proxy);

		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("charset", "utf-8");

		// create message
		StringBuilder strB = new StringBuilder();
		strB.append(
				String.format("> A commit has been made by *%s*.\n", author));
		strB.append(String.format("> Revision: %s\n", rev));
		strB.append(String.format("> SVN: %s\n", path));
		strB.append(String.format("> Commit message: %s\n", message));

		JSONObject payload = new JSONObject();
		payload.put("text", strB.toString());
		payload.put("mrkdwn", true);

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(payload.toJSONString());
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + payload);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		System.out.println(response.toString());
	}

}
