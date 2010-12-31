package mess;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Dostarczaj±ca obs³ugê komunikatów niezale¿nych od jêzyka
 */
public enum Message {
	usage,
	usage_reader,
	read,
	save,
	util_table_rows_uneven,
	unknown_file,
	;

	private static ResourceBundle rb;
	private static final MessageFormat mf = new MessageFormat("");
	private static final Locale locale = Locale.getDefault();
	private static final String MISSING = "missing param: ";
	static{
		refresh();
	}
	/**
	 * Pobiera tre¶æ komunikatu dla którego kluczem jest <code>name</code>
	 * @param name klucz komunikatu
	 * @return tre¶æ komunikatu lub missing param je¿eli brak tre¶ci
	 */
	public static String get(Enum<?> en, String defMessage){
		return get(en.name(), defMessage);
	}
	/**
	 * Pobiera tre¶æ komunikatu dla którego kluczem jest <code>name</code>
	 * @param name klucz komunikatu
	 * @return tre¶æ komunikatu lub missing param je¿eli brak tre¶ci
	 */
	public static String get(String name, String defMessage){
		name = name.replaceAll("_", ".");
		if(!locale.equals(Locale.getDefault())){
			refresh();
		}
		if(rb!=null && rb.containsKey(name)){
			return rb.getString(name);
		}
		return (defMessage!=null)?defMessage:MISSING + name;
	}
	
	/**
	 * Zaczytuje tre¶ci komunikatów z pliku message.properties
	 */
	private static void refresh(){
		try{
			rb = ResourceBundle.getBundle(System.getProperty("message", "message"));
		}catch(Exception e){e.printStackTrace();}
	}
	boolean exists(){
		return rb != null && rb.containsKey(name().replaceAll("_", "."));
	}
	@Override
	public String toString() {
		return get(this, null);
	}
	/**
	 * Formatuje tre¶æ komunikatu wed³ug warto¶ci parametrów
	 * @param params parametry dla kumunikatu
	 * @return sformatowana tre¶æ komunikatu lub missing param je¿eli brak tre¶ci
	 * @see java.text.MessageFormat#format(String, Object...)
	 */
	public String format(Object... params){
		mf.applyPattern(toString());
		return mf.format(params);
	}
	/**
	 * Tworzy instancjê wyj±tku IllegalArgumentException z informacj±
	 * równ± sformatowanej tre¶ci komunikatu wed³ug warto¶ci parametrów.
	 *
	 * @param params Parametry dla komunikatu.
	 * @see #format(Object...)
	 */
	public IllegalArgumentException illegalArgument(Object... params) {
		return new IllegalArgumentException(format(params));
	}
}
