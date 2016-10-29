package org.jfaster.mango.util.logging;

import org.junit.Test;

/**
 * @author ash
 */
public class Log4J2LoggerTest {

  @Test
  public void testMsg() throws Exception {
    Log4J2LoggerFactory f = new Log4J2LoggerFactory();
    InternalLogger logger = f.newInstance("org");
    logger.debug("ok");
  }

}
