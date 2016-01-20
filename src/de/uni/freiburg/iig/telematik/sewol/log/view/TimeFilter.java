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
package de.uni.freiburg.iig.telematik.sewol.log.view;

import de.invation.code.toval.misc.Filterable;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;
import java.util.Date;

/**
 * Filter for log traces to restrict the allowed time interval for a trace.
 *
 * @author Adrian Lange <lange@iig.uni-freiburg.de>
 * @param <E>
 */
public class TimeFilter<E extends LogEntry> implements Filterable<LogTrace<E>> {

        private Date startDate = null;
        private Date endDate = null;

        public TimeFilter() {
        }

        public TimeFilter(Date startDate, Date endDate) {
                setStartDate(startDate);
                setEndDate(endDate);
        }

        /**
         * Returns the start date.
         *
         * @return
         */
        public final Date getStartDate() {
                return startDate;
        }

        /**
         * Returns the end date.
         *
         * @return
         */
        public final Date getEndDate() {
                return endDate;
        }

        /**
         * Sets the start date.
         *
         * @param startDate
         */
        public final void setStartDate(Date startDate) {
                if (startDate != null && endDate != null && endDate.before(startDate)) {
                        throw new ParameterException("The start date must be before the end date of the filter.");
                }
                this.startDate = startDate;
        }

        /**
         * Sets the end date.
         *
         * @param endDate
         */
        public final void setEndDate(Date endDate) {
                if (endDate != null && startDate != null && endDate.before(startDate)) {
                        throw new ParameterException("The start date must be before the end date of the filter.");
                }
                this.endDate = endDate;
        }

        @Override
        public boolean accept(LogTrace<E> trace) {
                for (E entry : trace.getEntries()) {
                        if (startDate != null && entry.getTimestamp().before(startDate)) {
                                return false;
                        }
                        if (endDate != null && entry.getTimestamp().after(endDate)) {
                                return false;
                        }
                }
                return true;
        }

        /**
         * Returns the type of the time filter.
         *
         * @return
         */
        public TimeFrameFilterType getType() {
                if (startDate == null && endDate == null) {
                        return TimeFrameFilterType.INOPERATIVE;
                } else if (startDate != null && endDate == null) {
                        return TimeFrameFilterType.MIN_DATE;
                } else if (startDate == null && endDate != null) {
                        return TimeFrameFilterType.MAX_DATE;
                } else {
                        return TimeFrameFilterType.TIMEFRAME;
                }
        }

        public enum TimeFrameFilterType {

                INOPERATIVE, MIN_DATE, MAX_DATE, TIMEFRAME
        }
}