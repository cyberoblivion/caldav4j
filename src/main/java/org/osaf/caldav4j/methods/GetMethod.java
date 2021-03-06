/*
 * Copyright 2005 Open Source Applications Foundation
 * 
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

package org.osaf.caldav4j.methods;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import org.apache.commons.httpclient.Header;
import org.osaf.caldav4j.CalDAVConstants;
import org.osaf.caldav4j.exceptions.CalDAV4JException;
import org.osaf.caldav4j.exceptions.CalDAV4JProtocolException;
import org.osaf.caldav4j.util.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;


/**
 * Extends the {@link org.apache.commons.httpclient.methods.GetMethod} to provide functionality
 * to read into calendars.
 * @see org.apache.commons.httpclient.methods.GetMethod
 */
public class GetMethod extends org.apache.commons.httpclient.methods.GetMethod{
    private static final String CONTENT_TYPE_TEXT_HTML = "text/html";

	private static final String HEADER_ACCEPT = "Accept";

	private static final Logger log = LoggerFactory.getLogger(GetMethod.class);
    
    private CalendarBuilder calendarBuilder = null;

    protected GetMethod (){
        super();
        this.addRequestHeader(HEADER_ACCEPT, 
        		"text/calendar; text/html; text/xml;"); // required for bedework
        

    }

	/**
	 * @return Return the CalendarBuilder instance.
	 */
	public CalendarBuilder getCalendarBuilder() {
		return calendarBuilder;
	}

	/**
	 * Set the CalendarBuilder instance to use.
	 *
	 * @param calendarBuilder Calendar Builder object to set.
	 */
	public void setCalendarBuilder(CalendarBuilder calendarBuilder) {
		this.calendarBuilder = calendarBuilder;
	}

	/**
	 * Return the reponse as a calendar object.
	 *
	 * @return Calendar Object
	 * @throws ParserException on error parsing Calendar
	 * @throws CalDAV4JException on error in retrieving and parsing the response.
	 */
	public Calendar getResponseBodyAsCalendar()  throws
			ParserException, CalDAV4JException {
    	Calendar ret = null;
    	BufferedInputStream stream = null;
        try {
		    Header header = getResponseHeader(CalDAVConstants.HEADER_CONTENT_TYPE);
		    String contentType = (header != null) ? header.getValue() : null;
		    if (UrlUtils.isBlank(contentType) ||
		    		contentType.startsWith(CalDAVConstants.CONTENT_TYPE_CALENDAR)) {
		         stream = new BufferedInputStream(getResponseBodyAsStream());
		        ret =  calendarBuilder.build(stream);
		        return ret;		        
		    }

	        log.error("Expected content-type text/calendar. Was: " + contentType);
	        throw new CalDAV4JProtocolException("Expected content-type text/calendar. Was: " + contentType );
        } catch (IOException e) {
        	if (stream != null ) { //the server sends the response
        		if (log.isWarnEnabled()) {
        			log.warn("Server response is " + UrlUtils.parseISToString(stream));
        		}
        	}
        	throw new CalDAV4JException("Error retrieving and parsing server response at " + getPath(), e);
        }	       
    }

	/**
	 * @see org.apache.commons.httpclient.HttpMethodBase#setPath(String)
	 */
    public void setPath(String path) {
    	super.setPath(UrlUtils.removeDoubleSlashes(path));
    }
}
