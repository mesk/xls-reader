package mess;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Dostarczająca obsługę komunikatów niezależnych od języka
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
	 * Pobiera treść komunikatu dla którego kluczem jest <code>name</code>
	 * 
	 * @param name
	 *            klucz komunikatu
	 * @return treść komunikatu lub missing param jeżeli brak treści
	 */
	public static String get(final Enum<?> en, final String defMessage){
		return get(en.name(), defMessage);
	}

	/**
	 * Pobiera treść komunikatu dla którego kluczem jest <code>name</code>
	 * 
	 * @param name
	 *            klucz komunikatu
	 * @return treść komunikatu lub missing param jeżeli brak treści
	 */
	public static String get(String name, final String defMessage){
		name = name.replaceAll("_", ".");
		if(!locale.equals(Locale.getDefault())){
			refresh();
		}
		if((rb!=null) && rb.containsKey(name)){
			return rb.getString(name);
		}
		return (defMessage!=null)?defMessage:MISSING + name;
	}

	/**
	 * Zaczytuje treści komunikatów z pliku message.properties
	 */
	private static void refresh(){
		try{
			rb = ResourceBundle.getBundle(System.getProperty("message", "message"));
		}catch(final Exception e){e.printStackTrace();}
	}
	boolean exists(){
		return (rb != null) && rb.containsKey(name().replaceAll("_", "."));
	}
	@Override
	public String toString() {
		return get(this, null);
	}

	/**
	 * Formatuje treść komunikatu według wartości parametrów
	 * 
	 * @param params
	 *            parametry dla kumunikatu
	 * @return sformatowana treść komunikatu lub missing param jeżeli brak
	 *         treści
	 * @see java.text.MessageFormat#format(String, Object...)
	 */
	public String format(final Object... params){
		mf.applyPattern(toString());
		return mf.format(params);
	}

	/**
	 * Tworzy instancję wyjątku IllegalArgumentException z informację równą
	 * sformatowanej treści komunikatu według wartości parametrów.
	 * 
	 * @param params
	 *            Parametry dla komunikatu.
	 * @see #format(Object...)
	 */
	public IllegalArgumentException illegalArgument(final Object... params) {
		return new IllegalArgumentException(format(params));
	}
}
