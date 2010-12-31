import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mess.Message;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public enum OptionHelper {
	help("print this message"),
	sheet("sheet", "s", true, null, ','),
	column("column", "c", true, null, ','),
	row("row", "r", true, null, ','),
	;
	private String description = null;
	private String longOpt = null;
	private boolean hasArg = false;
	private String argName = null;
	private char withValueSeparator = 0;
	private OptionHelper(String description){
		this.description = description;
		
	}
	private OptionHelper(String description, String longOpt, boolean hasArg, String argName, char withValueSeparator){
		this(description);
		this.longOpt = longOpt;
		this.hasArg = hasArg;
		this.argName = argName;
		this.withValueSeparator = withValueSeparator;
	}
	public String getDescription() {
		return description;
	}
	public String getLongOpt() {
		return longOpt;
	}
	public boolean hasArg(){
		return hasArg;
	}
	public String getArgName() {
		return argName;
	}
	public int withValueSeparator() {
		return withValueSeparator;
	}
	private static Options options = new Options();
	private static CommandLine line = null;
	
	private static void addOption(OptionHelper oh){
		if(oh.getLongOpt()!=null){
			OptionBuilder.withLongOpt( oh.getLongOpt() );
		}
        OptionBuilder.withDescription( Message.get(oh.name(), oh.getDescription()) );
        OptionBuilder.hasArg(oh.hasArg());
        OptionBuilder.withArgName(oh.getArgName());
        if(oh.withValueSeparator()>0){
        	OptionBuilder.withValueSeparator(',');
        }
		options.addOption(OptionBuilder.create(oh.name()));
	}
	@SuppressWarnings("unchecked")
	public static List<String> getArgList(){
		return line.getArgList();
	}
	public static Options getOptions(){
		if(options == null || options.getOptions().size()==0){
			for(OptionHelper oh : values()){
				addOption(oh);
			}
		}
		return options;
	}
	public Option getOption(){
		return options.getOption(name());
	}
	
	public static boolean hasOptions(){
		return line.iterator().hasNext();
	}
	public boolean hasOption(){
		return line.hasOption(name());
	}
	public String getOptionValue(){
		return line.getOptionValue(name());
	}
	public String getOptionValue(String defaultValue){
		return line.getOptionValue(name(), null);
	}
	public String[] getOptionValues(){
		String[] values = line.getOptionValues(name());
		if(values != null && withValueSeparator()>0){
			List<String> tmp = new ArrayList<String>();
			for(String value : values){
				String[] sValues = value.split(String.valueOf(withValueSeparator));
				for(String sValue : sValues){
					if(!tmp.contains(sValue.trim())){
						tmp.add(sValue.trim());
					}
				}
			}
			values = tmp.toArray(values);
		}
		return values; 
	}
	public List<Integer> getValues(){
		List<Integer> sheets = new ArrayList<Integer>();
		String[] vs = getOptionValues();
		if(vs != null){
			for(String v : vs){
				try{
					sheets.add(Integer.parseInt(v));
				}catch(NumberFormatException nfe){
					return Collections.emptyList();
				}
			}
		}
		return sheets;
	}
	public static void parse(String[] args){
		// create the parser
	    CommandLineParser parser = new GnuParser();
		try {
			// parse the command line arguments
	        line = parser.parse( getOptions(), args );
		}catch( ParseException exp ) {
			//oops, something went wrong
			System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
			System.exit(-1);
		}
	}
	public static void printHelp(String cmdLineSyntax){
		if(help.hasOption()){
			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( cmdLineSyntax , getOptions() );
			System.exit(0);
		}
	}
}
