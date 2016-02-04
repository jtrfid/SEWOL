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
package de.uni.freiburg.iig.telematik.sewol.log;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import de.invation.code.toval.parser.ParserException;
import de.uni.freiburg.iig.telematik.sewol.log.filter.AbstractLogFilter;
import de.uni.freiburg.iig.telematik.sewol.log.filter.ContainsFilter;
import de.uni.freiburg.iig.telematik.sewol.log.filter.MaxEventsFilter;
import de.uni.freiburg.iig.telematik.sewol.log.filter.MinEventsFilter;
import de.uni.freiburg.iig.telematik.sewol.log.filter.TimeFilter;
import de.uni.freiburg.iig.telematik.sewol.parser.LogParser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class to serialize and parse {@link LogView}s.
 *
 * @author Adrian Lange <lange@iig.uni-freiburg.de>
 */
public class LogViewSerialization {

        private static final XStream xstream;

        static {
                xstream = new XStream(new DomDriver());
                // class alias
                xstream.alias("view", LogView.class);
                xstream.alias("logentry", LogEntry.class);
                xstream.alias("dulogentry", DULogEntry.class);
                xstream.alias("contains", ContainsFilter.class);
                xstream.alias("min", MinEventsFilter.class);
                xstream.alias("max", MaxEventsFilter.class);
                xstream.alias("time", TimeFilter.class);
                // field alias
                xstream.aliasField("start", TimeFilter.class, "startDate");
                xstream.aliasField("end", TimeFilter.class, "endDate");
                // attribute alias
                xstream.useAttributeFor(LogView.class, "name");
                xstream.useAttributeFor(LogView.class, "parentLogName");
                xstream.aliasField("parent", LogView.class, "parentLogName");
                xstream.useAttributeFor(AbstractLogFilter.class, "invert");
                xstream.useAttributeFor(AbstractLogFilter.class, "changed");
                xstream.useAttributeFor(ContainsFilter.class, "parameter");
                xstream.useAttributeFor(ContainsFilter.class, "value");
                xstream.useAttributeFor(MaxEventsFilter.class, "max");
                xstream.useAttributeFor(MinEventsFilter.class, "min");
                xstream.useAttributeFor(TimeFilter.class, "startDate");
                xstream.aliasField("start", TimeFilter.class, "startDate");
                xstream.useAttributeFor(TimeFilter.class, "endDate");
                xstream.aliasField("end", TimeFilter.class, "endDate");
                // omit fields
                xstream.omitField(LogView.class, "allTraces");
                xstream.omitField(LogView.class, "uptodate");
                xstream.omitField(Log.class, "summary");
                xstream.omitField(Log.class, "traces");
                xstream.omitField(Log.class, "distinctTraces");
                // implicit collection
                xstream.addImplicitCollection(LogView.class, "filters");
                xstream.addImplicitCollection(AbstractLogFilter.class, "obs");
        }

        /**
         * Parses a {@link LogView} under a given path and using the given
         * {@link Log}.
         *
         * @param path Path to the log view.
         * @return The parsed log view.
         * @throws IOException If the log view can't be read under the given
         * path.
         */
        public static LogView parse(String path) throws IOException {
                return parse(new File(path));
        }

        /**
         * Parses a {@link LogView} under a given path and using the given
         * {@link Log}.
         *
         * @param path Path to the log view.
         * @param log Log for the view.
         * @return The parsed log view.
         * @throws IOException If the log view can't be read under the given
         * path.
         */
        public static LogView parse(String path, Log log) throws IOException {
                return parse(new File(path), log);
        }

        /**
         * Parses a {@link LogView} file and using the given {@link Log}.
         *
         * @param file Log view file.
         * @return The parsed log view.
         * @throws IOException If the log view can't be read under the given
         * path.
         */
        public static LogView parse(File file) throws IOException {
                LogView view = (LogView) xstream.fromXML(file);
                view.reinitialize();
                return view;
        }

        /**
         * Parses a {@link LogView} file and using the given {@link Log}.
         *
         * @param file Log view file.
         * @param log Log for the view.
         * @return The parsed log view.
         * @throws IOException If the log view can't be read under the given
         * path.
         */
        public static LogView parse(File file, Log log) throws IOException {
                LogView view = (LogView) xstream.fromXML(file);
                view.reinitialize();
                view.addTraces(log.getTraces());
                return view;
        }

        /**
         * Serializes the log view under the given path.
         *
         * @param logView Log view to serialize.
         * @param path Target path of the serialized log view.
         * @throws IOException If the log view can't be written under the given
         * path.
         */
        public static void write(LogView logView, String path) throws IOException {
                String xml = xstream.toXML(logView);
                try (BufferedWriter out = new BufferedWriter(new FileWriter(path))) {
                        out.write(xml);
                }
        }

        public static void main(String[] args) throws IOException, ParserException {
                // serializer
                String logPath = "/home/alange/P2P-var1.mxml";
                List<List<LogTrace<LogEntry>>> logs = LogParser.parse(logPath);
                Log<LogEntry> log = new Log<>();
                if (logs.size() > 0) {
                        log.addTraces(logs.get(0));
                }
                LogView view = new LogView("view1");
                view.addFilter(new TimeFilter(new Date(1419202800000L), new Date(1454281199000L), false));
                view.addFilter(new ContainsFilter(ContainsFilter.ContainsFilterParameter.ACTIVITY, "P5 released", true));
                view.addFilter(new MinEventsFilter(1));
                view.addFilter(new MaxEventsFilter(10000));
                view.addTraces(log.getTraces());
                write(view, "/home/alange/view1.view");

                // parser
                view = parse("/home/alange/view1.view", log);
                Iterator iter = view.getFilters().iterator();
                while (iter.hasNext()) {
                        System.out.println(iter.next());
                }
        }
}
