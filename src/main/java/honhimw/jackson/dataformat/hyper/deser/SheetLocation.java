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

package honhimw.jackson.dataformat.hyper.deser;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.io.ContentReference;
import lombok.EqualsAndHashCode;
import org.apache.poi.ss.util.CellReference;

@EqualsAndHashCode(callSuper = true)
public final class SheetLocation extends JsonLocation {

    public static final String UNKNOWN = "UNKNOWN";
    private final int _row;
    private final int _column;

    public SheetLocation(final ContentReference contentRef, final int row, final int column) {
        super(contentRef, -1, -1, -1);
        _row = row;
        _column = column;
    }

    public int getRow() {
        return _row;
    }

    public int getColumn() {
        return _column;
    }

    @Override
    public String sourceDescription() {
        final Object content = _contentReference.getRawContent();
        if (content instanceof SheetInput) {
            return content.toString();
        }
        return super.sourceDescription();
    }

    @Override
    public StringBuilder appendOffsetDescription(final StringBuilder sb) {
        sb.append("row: ");
        if (_row >= 0) {
            sb.append(_row);
        } else {
            sb.append(UNKNOWN);
        }
        sb.append(", column: ");
        if (_column >= 0) {
            sb.append(_column);
        } else {
            sb.append(UNKNOWN);
        }
        sb.append(", address: ");
        if (_row >= 0 && _column >= 0) {
            sb.append(CellReference.convertNumToColString(_column));
            sb.append(_row + 1);
        } else {
            sb.append(UNKNOWN);
        }
        return sb;
    }
}
