/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package honhimw.jackson.dataformat.hyper.schema.style;

import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFColor;

interface ColorBinder<T, C extends Color> {

    void bind(T t, C c);

    C color(Workbook workbook, byte[] rgb);

    interface XSSFColorBinder<T> extends ColorBinder<T, XSSFColor> {
        @Override
        void bind(T t, XSSFColor c);

        default XSSFColor color(final Workbook workbook, final byte[] rgb) {
            final XSSFColor color = (XSSFColor) workbook.getCreationHelper().createExtendedColor();
            color.setRGB(rgb);
            return color;
        }
    }

    interface HSSFColorBinder<T> extends ColorBinder<T, HSSFColor> {
        @Override
        default void bind(final T t, final HSSFColor c) {
            if (c != null) {
                accept(t, c.getIndex());
            }
        }

        void accept(T t, short c);

        default HSSFColor color(final Workbook workbook, final byte[] rgb) {
            final HSSFPalette palette = ((HSSFWorkbook) workbook).getCustomPalette();
            final HSSFColor color = palette.findColor(rgb[0], rgb[1], rgb[2]);
            if (color == null) {
                return palette.findSimilarColor(rgb[0], rgb[1], rgb[2]);
            }
            return color;
        }
    }
}
