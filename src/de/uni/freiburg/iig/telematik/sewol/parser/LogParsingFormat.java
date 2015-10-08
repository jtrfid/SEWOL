package de.uni.freiburg.iig.telematik.sewol.parser;

import de.invation.code.toval.file.FileFormat;
import de.uni.freiburg.iig.telematik.sewol.format.LogFF_MXML;
import de.uni.freiburg.iig.telematik.sewol.format.LogFF_Petrify;
import de.uni.freiburg.iig.telematik.sewol.format.LogFF_Plain;
import de.uni.freiburg.iig.telematik.sewol.format.LogFF_XES;

public enum LogParsingFormat {

        XES(new LogFF_XES()),
        MXML(new LogFF_MXML()),
        PETRIFY(new LogFF_Petrify()),
        PLAIN_TAB(new LogFF_Plain()),
        PLAIN_SPACE(new LogFF_Plain());

        public final FileFormat fileFormat;

        private LogParsingFormat(FileFormat fileFormat) {
                this.fileFormat = fileFormat;
        }
}
