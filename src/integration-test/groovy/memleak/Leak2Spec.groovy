package memleak

import grails.test.mixin.integration.Integration
import grails.util.Holder
import grails.util.Holders
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

@Integration
public class Leak2Spec extends Specification {
  static Logger logger = LoggerFactory.getLogger(Leak1Spec.class)

  def "clean context 1"() {
    when:
    Holder holder = Holders.@servletContexts
    Map map = ReflectionTestUtils.getField(holder, "instances")
    logger.error("Number of contexts: ${map.size()}")

    then:
    1 == 1
  }

  @DirtiesContext
  def "dirty context 4"() {
    when:
    Holder holder = Holders.@servletContexts
    Map map = ReflectionTestUtils.getField(holder, "instances")
    logger.error("Number of contexts: ${map.size()}")

    then:
    1 == 1
  }

  @DirtiesContext
  def "dirty context 5"() {
    when:
    Holder holder = Holders.@servletContexts
    Map map = ReflectionTestUtils.getField(holder, "instances")
    logger.error("Number of contexts: ${map.size()}")

    then:
    1 == 1
  }
}