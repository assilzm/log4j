/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

// Contributors:   Mathias Bogaert

package org.apache.log4j.xml;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.helpers.DateLayout;
import org.apache.log4j.helpers.Transform;

/**
   The output of the XMLLayout consists of a series of log4j:event
   elements as defined in the <a
   href="doc-files/log4j.dtd">log4j.dtd</a>. It does not output a
   complete well-formed XML file. The output is designed to be
   included as an <em>external entity</em> in a separate file to form
   a correct XML file.

   <p>For example, if <code>abc</code> is the name of the file where
   the XMLLayout ouput goes, then a well-formed XML file would be:

   <pre>
   &lt;?xml version="1.0" ?&gt;

   &lt;!DOCTYPE log4j:eventSet SYSTEM "log4j.dtd" [&lt;!ENTITY data SYSTEM "abc"&gt;]&gt;

   &lt;log4j:eventSet version="1.2" xmlns:log4j="http://jakarta.apache.org/log4j/"&gt;
       &nbsp;&nbsp;&data;
   &lt;/log4j:eventSet&gt;
   </pre>

   <p>This approach enforces the independence of the XMLLayout and the
   appender where it is embedded. 

   <p>The <code>version</code> attribute helps components to correctly
   intrepret output generated by XMLLayout. The value of this
   attribute should be "1.1" for output generated by log4j versions
   prior to log4j 1.2 (final release) and "1.2" for relase 1.2 and
   later.

   @author Ceki  G&uuml;lc&uuml;
   @since 0.9.0 */
public class XMLLayout extends Layout {

  private  final int DEFAULT_SIZE = 256;
  private final int UPPER_LIMIT = 2048;

  private StringBuffer buf = new StringBuffer(DEFAULT_SIZE);
  private boolean locationInfo = false;

 
  /**
     The <b>LocationInfo</b> option takes a boolean value. By
     default, it is set to false which means there will be no location
     information output by this layout. If the the option is set to
     true, then the file name and line number of the statement
     at the origin of the log statement will be output. 

     <p>If you are embedding this layout within an {@link
     org.apache.log4j.net.SMTPAppender} then make sure to set the
     <b>LocationInfo</b> option of that appender as well.
   */
  public
  void setLocationInfo(boolean flag) {
    locationInfo = flag;
  }
  
  /**
     Returns the current value of the <b>LocationInfo</b> option.
   */
  public
  boolean getLocationInfo() {
    return locationInfo;
  }
  
  public
  void activateOptions() {
  }


  /**
     Formats a {@link LoggingEvent} in conformance with the log4j.dtd.  */
  public
  String format(LoggingEvent event) {

    // Reset working buffer. If the buffer is too large, then we need a new
    // one in order to avoid the penalty of creating a large array.
    if(buf.capacity() > UPPER_LIMIT) {
      buf = new StringBuffer(DEFAULT_SIZE);
    } else {
      buf.setLength(0);
    }
    
    // We yield to the \r\n heresy.

    buf.append("<log4j:event logger=\"");
    buf.append(event.categoryName);
    buf.append("\" timestamp=\"");
    buf.append(event.timeStamp);
    buf.append("\" level=\"");
    buf.append(event.level);
    buf.append("\" thread=\"");
    buf.append(event.getThreadName());
    buf.append("\">\r\n");


       buf.append("<log4j:message><![CDATA[");
       buf.append(event.getRenderedMessage());
       buf.append("]]></log4j:message>\r\n");       

       String ndc = event.getNDC();
       if(ndc != null) {
	 buf.append("<log4j:NDC><![CDATA[");
	 buf.append(ndc);
	 buf.append("]]></log4j:NDC>\r\n");       
       }

       String[] s = event.getThrowableStrRep();
       if(s != null) {
	 buf.append("<log4j:throwable><![CDATA[");
	 for(int i = 0; i < s.length; i++) {
	   buf.append(s[i]);
           buf.append("\r\n");
	 }
	 buf.append("]]></log4j:throwable>\r\n");
       }

       if(locationInfo) { 
	 LocationInfo locationInfo = event.getLocationInformation();	
	 buf.append("<log4j:locationInfo class=\"");
	 buf.append(locationInfo.getClassName());
	 buf.append("\" method=\"");
	 buf.append(Transform.escapeTags(locationInfo.getMethodName()));
	 buf.append("\" file=\"");
	 buf.append(locationInfo.getFileName());
	 buf.append("\" line=\"");
	 buf.append(locationInfo.getLineNumber());
	 buf.append("\"/>\r\n");
       }

    buf.append("</log4j:event>\r\n\r\n");

    return buf.toString();
  }
  
  /**
     The XMLLayout prints and does not ignore exceptions. Hence the
     return value <code>false</code>.
  */
  public
  boolean ignoresThrowable() {
    return false;
  }
}
