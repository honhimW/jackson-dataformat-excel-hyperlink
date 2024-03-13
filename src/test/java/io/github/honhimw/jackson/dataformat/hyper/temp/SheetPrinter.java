package io.github.honhimw.jackson.dataformat.hyper.temp;

import io.github.honhimw.jackson.dataformat.hyper.deser.CellValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author hon_him
 * @since 2023-05-10
 */

public class SheetPrinter {

    public static void print(Workbook workbook) {
        Iterator<Sheet> sheetIterator = workbook.sheetIterator();
        sheetIterator.forEachRemaining(rows -> {
            System.out.println("# " + rows.getSheetName() + " #");
            print(workbook, rows.getSheetName());
            System.out.println("--------------------------------------------");
        });
    }

    public static void print(Workbook workbook, int sheetAt) {
        print(workbook.getSheetAt(sheetAt));
    }

    public static void print(Workbook workbook, String sheetName) {
        print(workbook.getSheet(sheetName));
    }

    public static void print(Sheet sheet) {
        Iterator<Row> rowIterator = sheet.rowIterator();
        Map<Integer, Integer> widths = new HashMap<>();
        List<List<String>> table = new ArrayList<>();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            List<String> line = new ArrayList<>();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                int columnIndex = cell.getColumnIndex();
                CellValue cellValue = new CellValue(cell);
                Object value = cellValue.getValue();
                String str = String.valueOf(value);
                widths.compute(columnIndex, (key, old) -> {
                    old = Objects.isNull(old) ? 0 : old;
                    return Math.max(old, utf8length(str));
                });
                line.add(str);
            }
            table.add(line);
        }
        for (final List<String> line : table) {
            System.out.print("|");
            for (int i = 0; i < line.size(); i++) {
                System.out.print(" ");
                String content = line.get(i);
                System.out.print(content);
                Integer width = widths.get(i);
                for (int j = 0; j < width - utf8length(content); j++) {
                    System.out.print(" ");
                }
                System.out.print(" |");
            }
            System.out.println();
        }
    }

    private static int utf8length(String str) {
        int length = 0;

        if (StringUtils.isNotBlank(str)) {
            byte[] bytes  = str.getBytes(StandardCharsets.UTF_8);
            for (int i = 0; i < bytes.length; i++) {
                byte _byte = bytes[i];
                int unsignedInt = Byte.toUnsignedInt(_byte);

                if (unsignedInt <= 0x7F) {
                    i += 1;
                }

                if (unsignedInt >= 0xC0 && unsignedInt <= 0xDF) {
                    i += 2;
                }

                if (unsignedInt >= 0xE0 && unsignedInt <= 0xEF) {
                    i += 3;
                }

                if (unsignedInt >= 0xF0 && unsignedInt <= 0xF7) {
                    i += 4;
                }

                if (unsignedInt >= 0xF8 && unsignedInt <= 0xFB) {
                    i += 5;
                }

                if (unsignedInt >= 0xFC && unsignedInt <= 0xFD) {
                    i += 6;
                }

                if (unsignedInt >= 0x7F && unsignedInt <= 0xC0) {
                    i += 1;
                    continue;
                }

                length += 1;
            }
        }
        return length;
    }

}
