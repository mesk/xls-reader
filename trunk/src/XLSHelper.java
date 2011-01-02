import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.List;

import mess.Message;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XLSHelper {
	
	private static Workbook getWorkbook(final PushbackInputStream input) throws IOException{
		Workbook wb = null;
		if(POIFSFileSystem.hasPOIFSHeader(input)){
			wb = new HSSFWorkbook(input);
		}else if(POIXMLDocument.hasOOXMLHeader(input)){
			wb = new XSSFWorkbook(input);
		}else{
			System.out.println(Message.unknown_file);
		}
		return wb;
	}
	
	public static List<List<Object>> read(final String fileName) throws IOException{
		return read(fileName, null);
	}
	
	public static List<List<Object>> read(final String fileName, final Object valueForNull) throws IOException{
		return read(new FileInputStream(fileName), valueForNull);
	}
	
	public static List<List<Object>> read(final InputStream input) throws IOException{
		return read(input, null);
	}

	/**
	 * Wczytuje strumień jako xls, xlsx
	 * 
	 * @param input
	 *            strumień plików xls, xlsx
	 * @param valueForNull
	 *            wartość dla komórek pustych
	 * @return lista list wartości, które prezentuje zawartość komórek
	 * @throws IOException
	 */
	public static List<List<Object>> read(final InputStream input, final Object valueForNull) throws IOException{
		final List<List<Object>> data = new ArrayList<List<Object>>();
		final Workbook wb1 = getWorkbook(new PushbackInputStream(input, 1024));
		if(wb1!=null){
			Sheet sheet;
			final int sheetCount = wb1.getNumberOfSheets();
			final List<Integer> sheets = OptionHelper.sheet.getValues();
			final List<Integer> columns = OptionHelper.column.getValues();
			final List<Integer> rows = OptionHelper.row.getValues();
			sheet:for(int s = 0; s < sheetCount; s++){
				if((sheets.size()>0) && !sheets.contains(s)){
					continue sheet;
				}
				sheet = wb1.getSheetAt(s);
				final int firstRow = sheet.getFirstRowNum();
				row:for(int row_nr = firstRow;row_nr<sheet.getLastRowNum(); row_nr++){
					if((rows.size()>0) && !rows.contains(row_nr)){
						continue row;
					}
					final Row row = sheet.getRow(row_nr);
					if(row!=null){
						final List<Object> rowl = new ArrayList<Object>();
						boolean add = false;
						column:for(int cell_nr = row.getFirstCellNum(); cell_nr < row.getLastCellNum(); cell_nr++){
							if((columns.size()>0) && !columns.contains(cell_nr)){
								continue column;
							}
							final Cell cell = row.getCell(cell_nr);
							Object value = valueForNull;
							if(cell!=null){
								final CellStyle style = cell.getCellStyle();
								if(style.getHidden()){
									continue;
								}
								if(cell.getCellType() == Cell.CELL_TYPE_STRING){
									value = (cell.getStringCellValue());
								}else if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
									value = (cell.getNumericCellValue());
									if(style.getDataFormatString().contains("%")){
										value = (Double)value*100;
									}
								}else if(cell.getCellType() == Cell.CELL_TYPE_BOOLEAN){
									value = (cell.getBooleanCellValue());
								}
							}
							if(value != null){
								add = true;
							}
							rowl.add(value);
						}
						if(add){
							data.add(rowl);
						}
					}
				}
			}
		}
		return data;
	}
}
