import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import mess.Message;

public class XLSReader {

	public static void main(String[] args) throws IOException {
		
		OptionHelper.parse(args);
		OptionHelper.printHelp("java " + XLSReader.class.getName()
				+ " [OPTION]... FILES");
		
		boolean one = false;
		args: for (String arg : OptionHelper.getArgList()) {
			File f = new File(arg);
			if (!f.exists()) {
				f = new File(Util.join(args, " "));
			}
			System.err.println(Message.read.format(arg));
			FileInputStream in = new FileInputStream(f);
			List<List<Object>> data = XLSHelper.read(in, "");
			if (data != null) {
				System.out
						.println(Util.tableToString(Util.normalize(data, "")));
			}
			if (one) {
				break args;
			}
		}
	}

}
