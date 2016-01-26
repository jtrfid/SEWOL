/*
 * Copyright (c) 2016, IIG Telematics, Uni Freiburg
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted (subject to the limitations in the disclaimer
 * below) provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of IIG Telematics, Uni Freiburg nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY
 * THIS LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BELIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.uni.freiburg.iig.telematik.sewol.writer.view;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import de.invation.code.toval.parser.ParserException;
import de.uni.freiburg.iig.telematik.sewol.log.DULogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.Log;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;
import de.uni.freiburg.iig.telematik.sewol.log.LogView;
import de.uni.freiburg.iig.telematik.sewol.log.filter.ContainsFilter;
import de.uni.freiburg.iig.telematik.sewol.log.filter.MaxEventsFilter;
import de.uni.freiburg.iig.telematik.sewol.log.filter.MinEventsFilter;
import de.uni.freiburg.iig.telematik.sewol.log.filter.TimeFilter;
import de.uni.freiburg.iig.telematik.sewol.parser.LogParser;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Adrian Lange <lange@iig.uni-freiburg.de>
 */
public class LogViewSerializer {

        private static final XStream xstream;

        static {
                xstream = new XStream(new DomDriver());
                xstream.alias("view", LogView.class);
                xstream.alias("logentry", LogEntry.class);
                xstream.alias("dulogentry", DULogEntry.class);
                xstream.alias("contains", ContainsFilter.class);
                xstream.alias("min", MinEventsFilter.class);
                xstream.alias("max", MaxEventsFilter.class);
                xstream.alias("time", TimeFilter.class);
                xstream.omitField(LogView.class, "allTraces");
                xstream.omitField(LogView.class, "uptodate");
                xstream.omitField(Log.class, "summary");
                xstream.omitField(Log.class, "traces");
                xstream.omitField(Log.class, "distinctTraces");
        }

        public static void write(LogView logView, String path) throws IOException {
                String xml = xstream.toXML(logView);
                try (BufferedWriter out = new BufferedWriter(new FileWriter(path))) {
                        out.write(xml);
                }
        }

        public static void main(String[] args) throws IOException, ParserException {
                String logPath = "/home/alange/P2P-var1.mxml";
                List<List<LogTrace<LogEntry>>> logs = LogParser.parse(logPath);
                Log<LogEntry> log = new Log<>();
                if (logs.size() > 0) {
                        log.addTraces(logs.get(0));
                }
                LogView view = new LogView("view1");
                view.addFilter(new TimeFilter(new Date(1419202800000L), new Date(1454281199000L), false));
                view.addFilter(new ContainsFilter(ContainsFilter.ContainsFilterParameter.ACTIVITY, "PO released", true));
                view.addFilter(new MinEventsFilter(1));
                view.addFilter(new MaxEventsFilter(10000));
                view.addTraces(log.getTraces());
                write(view, "/home/alange/view1.xml");
        }
}
