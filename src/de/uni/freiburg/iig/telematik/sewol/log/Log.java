package de.uni.freiburg.iig.telematik.sewol.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;

public class Log<E extends LogEntry> {

        LogSummary<E> summary = new LogSummary<>();
        Set<LogTrace<E>> distinctTraces = new HashSet<>();
        List<LogTrace<E>> traces = new ArrayList<>();

        public LogSummary<E> getSummary() {
                return summary;
        }

        void reinitialize() {
                summary = new LogSummary<>();
                distinctTraces = new HashSet<>();
                traces = new ArrayList<>();
        }

        public void addTraces(List<LogTrace<E>> traces) throws ParameterException {
                Validate.notNull(traces);
                for (LogTrace<E> trace : traces) {
                        addTrace(trace);
                }
        }

        public void addTrace(LogTrace<E> trace) throws ParameterException {
                Validate.notNull(trace);
                trace.setCaseNumber(traces.size() + 1);
                traces.add(trace);
                summary.addTrace(trace);
                if (!distinctTraces.add(trace)) {
                        for (LogTrace<E> storedTrace : traces) {
                                if (storedTrace.equals(trace)) {
                                        storedTrace.addSimilarInstance(trace.getCaseNumber());
                                        trace.addSimilarInstance(storedTrace.getCaseNumber());
                                }
                        }
                }
        }

        public List<LogTrace<E>> getTraces() {
                return Collections.unmodifiableList(traces);
        }
}
