import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import mess.Message;


public class Util {
	/**
	 * Tworzy czytelną reprezentację napisów dla danej tabeli dwuwymiarowej.
	 * 
	 * @param tableRows
	 *            Dwuwymiarowa lista reprezentująca tabelę danych.
	 * @return Reprezentacja napisowa podanej tabeli.
	 * @throws IllegalArgumentException
	 *             jeżeli ilości elementów w wierszach nie są równe.
	 */
	public static <T> String tableToString(final List<List<T>> tableRows) {
		return tableToString(null, tableRows);
	}

	/**
	 * Tworzy czytelną reprezentację napisów dla danej tabeli dwuwymiarowej.
	 * 
	 * @param columnNames
	 *            Nazwy kolumn.
	 * @param tableRows
	 *            Dwuwymiarowa lista reprezentująca tabelę danych.
	 * @return Reprezentacja napisowa podanej tabeli.
	 * @throws IllegalArgumentException
	 *             jeżeli ilość nazw kolumn oraz ilości elementów w wierszach
	 *             nie są równe.
	 */
	public static <T,N extends CharSequence> String tableToString(final List<N> columnNames,
			final List<List<T>> tableRows) {
		final List<Integer> columnWidths = getColumnWidths(tableRows, columnNames);
		final StringBuilder sb = new StringBuilder();
		if (columnNames != null) {
			appendTableSeparator(sb, columnWidths, 1);
			sb.append("| ");
			for (int i = 0; i < columnNames.size(); i++) {
				sb.append(alignLeft(columnNames.get(i), columnWidths.get(i)));
				if (i < columnNames.size() - 1) {
					sb.append(" | ");
				}
			}
			sb.append(" |\n");
			appendTableSeparator(sb, columnWidths, 1);
		}
		for (int i = 0; i < tableRows.size(); i++) {
			sb.append("| ");
			for (int j = 0; j < tableRows.get(i).size(); j++) {
				final String cell = String.valueOf(tableRows.get(i).get(j)).trim();
				sb.append(alignLeft(cell, columnWidths.get(j)));
				if (j < tableRows.get(i).size() - 1) {
					sb.append(" | ");
				}
			}
			sb.append(" |\n");
		}
		if (columnNames != null) {
			appendTableSeparator(sb, columnWidths, 1);
		}
		return sb.toString().trim();
	}
	private static <T,N extends CharSequence> List<Integer> getColumnWidths(
			final List<List<T>> tableRows, final List<N> columnNames) {
		for (int i = 0; i < tableRows.size() - 1; i++) {
			if (tableRows.get(i).size() != tableRows.get(i + 1).size()) {
				throw Message.util_table_rows_uneven.illegalArgument(
						i, tableRows.get(i).size(),
						i + 1, tableRows.get(i + 1).size());
			}
		}
		if ((columnNames != null) && (tableRows.size() > 0)
				&& (columnNames.size() != tableRows.get(0).size())) {
			throw Message.util_table_rows_uneven.illegalArgument(
					"columnNames", columnNames.size(),
					0, tableRows.get(0).size());
		}
		int columnCount = 0;
		if (columnNames != null) {
			columnCount = columnNames.size();
		} else if (tableRows.size() > 0) {
			columnCount = tableRows.get(0).size();
		}
		final List<Integer> columnWidths =
			new ArrayList<Integer>(Arrays.asList(new Integer[columnCount]));
		Collections.fill(columnWidths, 0);
		if (columnNames != null) {
			for (int i = 0; i < columnNames.size(); i++) {
				columnWidths.set(i, Math.max(
							columnWidths.get(i), columnNames.get(i).length()));
			}
		}
		for (int i = 0; i < tableRows.size(); i++) {
			for (int j = 0; j < tableRows.get(0).size(); j++) {
				final String cell = String.valueOf(tableRows.get(i).get(j)).trim();
				columnWidths.set(
						j, Math.max(columnWidths.get(j), cell.length()));
			}
		}
		return columnWidths;
	}
	private static StringBuilder appendTableSeparator(
			final StringBuilder sb, final List<Integer> columnWidths, final int padding) {
		sb.append("+");
		for (int i = 0; i < columnWidths.size(); i++) {
			for (int j = 0; j < columnWidths.get(i) + 2 * padding; j++) {
				sb.append("-");
			}
			sb.append("+");
		}
		return sb.append("\n");
	}

	/**
	 * Dopisuje spacje do napisu <code>s</code>, tak że napis wynikowy ma
	 * długość <code>lineLength</code>. Jeśli długość <code>s</code> jest
	 * większa niż <code>lineLength</code>, napis jest obcinany, a na końcu
	 * dopisywane są znaki <code>...</code> (wielokropek) lub napis zostaje
	 * wypełniony znakami <code>*</code>, jeśli ma postać liczbową.
	 */
	public static <N extends CharSequence>String alignLeft(final N s, final int lineLength) {
		final StringBuilder sb = new StringBuilder(s);
		while (sb.length() < lineLength) {
			sb.append(' ');
		}
		return properCut(sb.toString(), lineLength);
	}

	/**
	 * Jeśli długość <code>s</code> jest większa niż <code>lineLength</code>,
	 * napis jest obcinany, a na końcu dopisywane są znaki <code>...</code>
	 * (wielokropek) lub napis zostaje wypełniony znakami <code>*</code>, jeśli
	 * ma postać liczbową. Jeśli długość <code>s</code> jest mniejsza lub równa
	 * <code>lineLength</code>, zwracany jest napis <code>s</code>.
	 */
	public static String properCut(final String s, final int lineLength) {
		String result;
		if (s.length() <= lineLength) {
			result = s;
		} else {
			if (!isNumeric(s)) {
				if (lineLength >= 3) {
					result = s.substring(0, lineLength - 3) + "...";
				} else {
					result = "";
					for (int i = 0; i < lineLength; i++) {
						result += ".";
					}
				}
			} else {
				final StringBuilder sb = new StringBuilder();
				while (sb.length() < lineLength) {
					sb.append('*');
				}
				result = sb.toString();
			}
		}
		return result;
	}

	/**
	 * Czy podany ciąg znaków reprezentuje liczbę. Uwaga: brane są pod uwagę
	 * locale, więc dla pl_PL liczby dziesiętne muszą mieć część ułamkową
	 * oddzieloną przecinkiem, a nie kropką.
	 */
	public static boolean isNumeric(final String s) {
		final NumberFormat formatter = NumberFormat.getInstance();
		final ParsePosition pos = new ParsePosition(0);
		formatter.parse(s, pos);
		return s.length() == pos.getIndex();
	}

	/**
	 * Modyfikuje wiersze listy do zachowania jednakowej ilości rekordów(kolumn)
	 * 
	 * @param <T>
	 *            typ list
	 * @param rows
	 *            dane wejściowe
	 * @return zmodyfikowane dane wejściowe
	 */
	public static <T> List<List<T>> normalize(final List<List<T>> rows){
		return normalize(rows, null);
	}

	/**
	 * Modyfikuje wiersze listy do zachowania jednakowej ilości rekordów(kolumn)
	 * 
	 * @param <T>
	 *            typ listy
	 * @param rows
	 *            dane wejściowe
	 * @param def
	 *            wartość wstawiana
	 * @return zmodyfikowane dane wejściowe
	 */
	public static <T> List<List<T>> normalize(final List<List<T>> rows, final T def){
		int max = -1;
		for(final Collection<T> collection : rows){
			max = Math.max(max, collection.size());
		}
		for(final Collection<T> row : rows){
			if(row.size()<max){
				row.addAll(Collections.nCopies(max-row.size(), def));
			}
		}
		return rows;
	}

	/**
	 * Łączy elementy listy w jeden ciąg znaków z elementami rozdzielonymi
	 * podanym separatorem.
	 * 
	 * @param separator
	 *            Ciąg znaków oddzielający od siebie elementy listy.
	 * @return Ciąg znaków z elementami rozdzielonymi separatorem.
	 */
	public static <T> String join(final List<T> in, final String separator) {
		final StringBuilder out = new StringBuilder("");
		if ((in != null) && (in.size() > 0)) {
			for (int i = 0; i < in.size() - 1; i++) {
				out.append(in.get(i));
				out.append(separator);
			}
			out.append(in.get(in.size() - 1));
		}
		return out.toString();
	}

	/**
	 * Łączy elementy tablicy w jeden ciąg znaków z elementami rozdzielonymi
	 * podanym separatorem.
	 * 
	 * @param separator
	 *            Ciąg znaków oddzielający od siebie elementy tablicy.
	 * @return Ciąg znaków z elementami rozdzielonymi separatorem.
	 */
	public static <T> String join(final T[] in, final String separator) {
		return join(Arrays.asList(in), separator);
	}

	/**
	 * Zapisuje napis do pliku
	 * 
	 * @param string
	 *            napis do zapisu
	 * @param file
	 *            plik w którym nastąpi zapis
	 * @param encoding
	 *            kodowanie pliku wyjściowego
	 * @return status operacji
	 */
	public static boolean writeStringToFile(final String string, final File file, final String encoding)
	throws UnsupportedEncodingException {
		try {
			final PrintWriter writer = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(file), encoding));
			writer.write(string);
			writer.close();
			return true;
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Zapisuje napis do pliku
	 * 
	 * @param string
	 *            napis do zapisu
	 * @param file
	 *            plik w którym nastąpi zapis
	 * @return status operacji
	 */
	public static boolean writeStringToFile(final String string, final File file) {
		try {
			final PrintWriter writer = new PrintWriter(new FileOutputStream(file));
			writer.write(string);
			writer.close();
			return true;
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
}
