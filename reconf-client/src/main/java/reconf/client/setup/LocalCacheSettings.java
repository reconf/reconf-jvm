/*
 *    Copyright 1996-2014 UOL Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package reconf.client.setup;

import java.io.*;
import org.apache.commons.lang.builder.*;


public class LocalCacheSettings {

    private int maxLogFileSize = 10;
    private boolean compressed;
    private File backupLocation;

    public int getMaxLogFileSize() {
        return maxLogFileSize;
    }
    public void setMaxLogFileSize(int maxLogFileSize) {
        this.maxLogFileSize = maxLogFileSize;
    }

    public boolean isCompressed() {
        return compressed;
    }
    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    public File getBackupLocation() {
        return backupLocation;
    }
    public void setBackupLocation(File backupLocation) {
        this.backupLocation = backupLocation;
    }

    @Override
    public String toString() {
        ToStringBuilder result = new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE)
        .append("location", getBackupLocation())
        .append("compressed", isCompressed())
        .append("max-log-file-size-mb", getMaxLogFileSize());

        return result.toString();
    }
}
