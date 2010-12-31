import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import mess.Message;

public class XLS2TXT {

	public static void main(String[] args) throws IOException {
		String encoding = System.getProperty("enc");
		if (args.length == 0) {
			System.err.println(Message.usage);
		} else {
			boolean one = false;

			args: for (String arg : args) {
				File f = new File(arg);
				if (!f.exists()) {
					f = new File(Util.join(args, " "));
				}
				System.err.println(Message.read.format(arg));
				FileInputStream in = new FileInputStream(f);
				List<List<Object>> data = XLSHelper.read(in, "");
				if (data != null && data.size() > 0) {
					f = new File(f.getAbsolutePath().replaceAll("xls[x]??",
							"txt"));
					System.err.println(Message.save.format(f.getAbsolutePath()));
					String string = Util.tableToString(Util
							.normalize(data, ""));
					if (encoding != null) {
						Util.writeStringToFile(string, f, encoding);
					} else {
						Util.writeStringToFile(string, f);
					}
				}
				if (one) {
					break args;
				}
			}
		}
	}

}
