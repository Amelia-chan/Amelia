package pw.mihou.amelia.logger

import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory

@Suppress("ktlint:standard:property-naming")
var logger: Logger = LoggerFactory.getLogger("Amelia") as Logger
