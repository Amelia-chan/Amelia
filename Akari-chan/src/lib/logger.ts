import winston, { format } from 'winston'

export default winston.createLogger({
    format: format.combine(format.json(), format.timestamp()),
    transports: new winston.transports.Console()
})