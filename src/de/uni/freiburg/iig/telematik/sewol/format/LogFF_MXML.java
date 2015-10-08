package de.uni.freiburg.iig.telematik.sewol.format;

import java.nio.charset.Charset;

import de.invation.code.toval.file.FileFormat;

public class LogFF_MXML extends FileFormat {

        @Override
        public String getFileExtension() {
                return "mxml";
        }

        @Override
        public String getName() {
                return "MXML";
        }

        @Override
        public boolean supportsCharset(Charset charset) {
                return true;
        }
}
