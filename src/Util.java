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
	 * Tworzy czyteln± reprezentacjê napisow± dla danej tabeli dwuwymiarowej.
	 *
	 * @param tableRows   Dwuwymiarowa lista reprezentuj±ca tabelê danych.
	 * @return            Reprezentacja napisowa podanej tabeli.
	 * @throws IllegalArgumentException je¿eli ilo¶ci elementów w wierszach
	 *                    nie s± równe.
	 */
	public static <T> String tableToString(List<List<T>> tableRows) {
		return tableToString(null, tableRows);
	}
	/**
	 * Tworzy czyteln± reprezentacjê napisow± dla danej tabeli dwuwymiarowej.
	 *
	 * @param columnNames Nazwy kolumn.
	 * @param tableRows   Dwuwymiarowa lista reprezentuj±ca tabelê danych.
	 * @return            Reprezentacja napisowa podanej tabeli.
	 * @throws IllegalArgumentException je¿eli ilo¶æ nazw kolumn oraz ilo¶ci
	 *                    elementów w wierszach nie s± równe.
	 */
	public static <T,N extends CharSequence> String tableToString(List<N> columnNames,
			List<List<T>> tableRows) {
		List<Integer> columnWidths = getColumnWidths(tableRows, columnNames);
		StringBuilder sb = new StringBuilder();
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
				String cell = String.valueOf(tableRows.get(i).get(j)).trim();
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
			List<List<T>> tableRows, List<N> columnNames) {
		for (int i = 0; i < tableRows.size() - 1; i++) {
			if (tableRows.get(i).size() != tableRows.get(i + 1).size()) {
				throw Message.util_table_rows_uneven.illegalArgument(
						i, tableRows.get(i).size(),
						i + 1, tableRows.get(i + 1).size());
			}
		}
		if (columnNames != null && tableRows.size() > 0
				&& columnNames.size() != tableRows.get(0).size()) {
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
		List<Integer> columnWidths =
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
				String cell = String.valueOf(tableRows.get(i).get(j)).trim();
				columnWidths.set(
						j, Math.max(columnWidths.get(j), cell.length()));
			}
		}
		return columnWidths;
	}
	private static StringBuilder appendTableSeparator(
			StringBuilder sb, List<Integer> columnWidths, int padding) {
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
	 * Dopisuje spacje do napisu <code>s</code>, tak ¿e napis wynikowy ma
	 * d³ugo¶æ <code>lineLength</code>.
	 * Je¶li d³ugo¶æ <code>s</code> jest wiêksza ni¿ <code>lineLength</code>,
	 * napis jest obcinany, a na koñcu dopisywane s± znaki <code>...</code>
	 * (wielokropek) lub napis zostaje wype³niony znakami <code>*</code>,
	 * je¶li ma postaæ liczbow±.
	 */
	public static <N extends CharSequence>String alignLeft(N s, int lineLength) {
		StringBuilder sb = new StringBuilder(s);
		while (sb.length() < lineLength) {
			sb.append(' ');
		}
		return properCut(sb.toString(), lineLength);
	}
	/**
	 * Je¶li d³ugo¶æ <code>s</code> jest wiêksza ni¿ <code>lineLength</code>,
	 * napis jest obcinany, a na koñcu dopisywane s± znaki <code>...</code>
	 * (wielokropek) lub napis zostaje wype³niony znakami <code>*</code>,
	 * je¶li ma postaæ liczbow±.
	 * Je¶li d³ugo¶æ <code>s</code> jest mniejsza lub równa
	 * <code>lineLength</code>, zwracany jest napis <code>s</code>.
	 */
	public static String properCut(String s, int lineLength) {
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
				StringBuilder sb = new StringBuilder();
				while (sb.length() < lineLength) {
					sb.append('*');
				}
				result = sb.toString();
			}
		}
		return result;
	}
	/**
	 * Czy podany ci±g znaków reprezentuje liczbê.
	 * Uwaga: brane s± pod uwagê locale, wiêc dla pl_PL liczby dziesiêtne
	 * musz± mieæ czê¶æ u³amkow± oddzielon± przecinkiem, a nie kropk±.
	 */
	public static boolean isNumeric(String s) {
		NumberFormat formatter = NumberFormat.getInstance();
		ParsePosition pos = new ParsePosition(0);
		formatter.parse(s, pos);
		return s.length() == pos.getIndex();
	}
	/**
	 * Modyfikuje wiersze listy do zachowania jednakowej ilo¶ci rekordów(kolumn)
	 * @param <T> typ list
	 * @param rows dane wej¶ciowe
	 * @return zmodyfikowane dane wej¶ciowe
	 */
	public static <T> List<List<T>> normalize(List<List<T>> rows){
		return normalize(rows, null);
	}
	
	/**
	 * Modyfikuje wiersze listy do zachowania jednakowej ilo¶ci rekordów(kolumn)
	 * @param <T> typ listy
	 * @param rows dane wej¶ciowe
	 * @param def warto¶æ wstawiana
	 * @return zmodyfikowane dane wej¶ciowe
	 */
	public static <T> List<List<T>> normalize(List<List<T>> rows, final T def){
		int max = -1;
		for(Collection<T> collection : rows){
			max = Math.max(max, collection.size());
		}
		for(Collection<T> row : rows){
			if(row.size()<max){
				row.addAll(Collections.nCopies(max-row.size(), def));
			}
		}
		return rows;
	}
	/**
	 * £±czy elementy listy w jeden ci±g znaków z elementami
	 * rozdzielonymi podanym separatorem.
	 *
	 * @param  separator Ci±g znaków oddzielaj±cy od siebie elementy listy.
	 * @return           Ci±g znaków z elementami rozdzielonymi separatorem.
	 */
	public static <T> String join(List<T> in, String separator) {
		StringBuilder out = new StringBuilder("");
		if (in != null && in.size() > 0) {
			for (int i = 0; i < in.size() - 1; i++) {
				out.append(in.get(i));
				out.append(separator);
			}
			out.append(in.get(in.size() - 1));
		}
		return out.toString();
	}
	/**
	 * £±czy elementy tablicy w jeden ci±g znaków z elementami
	 * rozdzielonymi podanym separatorem.
	 *
	 * @param  separator Ci±g znaków oddzielaj±cy od siebie elementy tablicy.
	 * @return           Ci±g znaków z elementami rozdzielonymi separatorem.
	 */
	public static <T> String join(T[] in, String separator) {
		return join(Arrays.asList(in), separator);
	}
	/**
	 * Zapisuje napis do pliku
	 * @param string napis do zapisu
	 * @param file plik w którym nast±pi zapis
	 * @param encoding kodowanie pliku wyj¶ciowego
	 * @return status operacji
	 */
	public static boolean writeStringToFile(String string, File file, String encoding)
	throws UnsupportedEncodingException {
		try {
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(file), encoding));
			writer.write(string);
			writer.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * Zapisuje napis do pliku
	 * @param string napis do zapisu
	 * @param file plik w którym nast±pi zapis
	 * @return status operacji
	 */
	public static boolean writeStringToFile(String string, File file) {
		try {
			PrintWriter writer = new PrintWriter(new FileOutputStream(file));
			writer.write(string);
			writer.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
}
